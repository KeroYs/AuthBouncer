package com.github.multidestroy.database;

import com.github.multidestroy.configs.Config;
import com.github.multidestroy.player.PlayerGlobalRank;
import com.github.multidestroy.player.PlayerInfo;
import com.github.multidestroy.player.PlayerActivityStatus;
import org.bukkit.entity.Player;

import java.net.InetAddress;
import java.time.Instant;
import java.util.Map;
import java.sql.*;

public class Database {

    private DataSource dataSource;

    public Database(Config config) {
        Map<String, String> databaseInfo = config.getDataBaseInfo();
        Connection conn = null;
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        try {
            dataSource = new DataSource(databaseInfo);
            conn = dataSource.getConnection();

            //Create tables if they do not exist
            createPlayersTable(conn);
            createActivityHistoryTable(conn);
            createIpBlockadesTable(conn);

        } catch (SQLException e) {
            e.printStackTrace();
        }
        closeConn(conn);
    }

    public boolean savePlayer(String playerName, String hashedPassword, PlayerGlobalRank rank, Instant created) {
        Connection conn = null;
        short rankId = rank.getRankId();
        long creationTimeAsEpoch = created.toEpochMilli();
        boolean queryResult;
        String query = "INSERT INTO players VALUES(DEFAULT, ?, ?, ?, ?, NULL)";

        try {
            conn = dataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(query);
            ps.setString(1, playerName);
            ps.setString(2, hashedPassword);
            ps.setShort(3, rankId);
            ps.setLong(4, creationTimeAsEpoch);

            ps.execute();
            queryResult = true;
        } catch (SQLException e) {
            e.printStackTrace();
            queryResult = false;
        } finally {
            closeConn(conn);
        }
        return queryResult;
    }

    public void saveLoginAttempt(Player player, PlayerActivityStatus status, Instant time) {
        Connection conn = null;
        long timeAsEpoch = time.toEpochMilli();
        String playerName = player.getName();
        String ipAddress = player.getAddress().getHostName();
        short statusId = (short) status.getId();
        String queryLoginHistory = "INSERT INTO activity_history VALUES(DEFAULT, (SELECT id FROM players WHERE LOWER(nick)=?), ?, ?, ?)";

        try {
            //save into login_history table
            conn = dataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(queryLoginHistory);
            ps.setString(1, playerName.toLowerCase());
            ps.setLong(2, timeAsEpoch);
            ps.setShort(3, statusId);
            ps.setString(4, ipAddress);

            ps.execute();

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeConn(conn);
        }
    }

    /**
     * @return TRUE - if password was updated successfully, otherwise FALSE
     */

    public boolean changePassword(String playerName, String hashedPassword) {
        boolean returnValue;
        String query = "UPDATE players SET password=? WHERE LOWER(nick)=?";
        Connection conn = null;
        try {
            conn = dataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(query);
            ps.setString(1, hashedPassword);
            ps.setString(2, playerName.toLowerCase());
            ps.execute();
            returnValue = true;
        } catch(SQLException e) {
            e.printStackTrace();
            returnValue = false;
        } finally {
            closeConn(conn);
        }
        return returnValue;
    }

    /**
     * @return -1 - if e-mail could not be updated in the database,
     * 0 - if e-mail was already assigned to different account,
     * 1 - if e-mail was successfully updated
     */

    public int changeEmail(String playerName, String email) {
        int returnValue;
        String updateQuery = "UPDATE players SET email=? WHERE LOWER(nick)=?";
        String assignedQuery = "SELECT * FROM players WHERE email=?";
        Connection conn = null;
        try {
            conn = dataSource.getConnection();
            //Check whether e-mail is already assigned to another account
            PreparedStatement isAssigned = conn.prepareStatement(assignedQuery);
            isAssigned.setString(1, email);
            if(!isAssigned.executeQuery().next()) {
                //Save e-mail
                PreparedStatement ps = conn.prepareStatement(updateQuery);
                ps.setString(1, email);
                ps.setString(2, playerName.toLowerCase());
                ps.execute();
                returnValue = 1;
            } else
                returnValue = 0;
        } catch(SQLException e) {
            e.printStackTrace();
            returnValue = -1;
        } finally {
            closeConn(conn);
        }
        return returnValue;
    }

    /**
     * @return PlayerInfo object with basic information about player. NULL if couldn't get information about player.
     */

    public PlayerInfo getRegisteredPlayer(String playerName) {
        Connection conn = null;
        PlayerInfo playerInfo = null;
        try {
            conn = dataSource.getConnection();
            String query = "SELECT p.password, p.email, a.ip_address FROM players" +
                    " AS p INNER JOIN activity_history AS a ON a.player=p.id" +
                    " WHERE LOWER(p.nick)=? AND (a.status=? OR a.status=?)" +
                    " ORDER BY a.id DESC LIMIT 1;";
            PreparedStatement ps = conn.prepareStatement(query);
            ps.setString(1, playerName.toLowerCase());
            ps.setInt(2, PlayerActivityStatus.SUCCESSFUL_LOGIN.getId());
            ps.setInt(3, PlayerActivityStatus.REGISTRATION.getId());

            ResultSet rs = ps.executeQuery();
            if(rs.next()) {
                String hashedPassword = rs.getString(1);
                String email = rs.getString(2);
                String ip_address = rs.getString(3);
                playerInfo = new PlayerInfo(email, hashedPassword, ip_address, false, true, false);
            }
            else
                playerInfo = new PlayerInfo();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeConn(conn);
        }
        return playerInfo;
    }

    public boolean lockIpAddressOnAccount(InetAddress address, String playerName, Instant time) {
        String query = "INSERT INTO ip_blockades VALUES(DEFAULT, (SELECT id FROM players WHERE LOWER(nick)=?), ?, ?, FALSE)";
        Connection conn = null;
        boolean returnValue;
        try {
            conn = dataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(query);
            ps.setString(1, playerName.toLowerCase());
            ps.setLong(2, time.toEpochMilli());
            ps.setString(3, address.getHostAddress());
            ps.execute();
            returnValue = true;
        } catch (SQLException e) {
            e.printStackTrace();
            returnValue = false;
        } finally {
            closeConn(conn);
        }
        return returnValue;
    }

    private PlayerInfo shellPlayerInfo(ResultSet rs, String playerName) {
        PlayerInfo playerInfo = new PlayerInfo();
        try {
            String hashedPassword = rs.getString(3);
            String email = rs.getString(6);
            //String ipAddress = getLastSuccessfulIp(conn, playerName);

            playerInfo.setHashedPassword(hashedPassword);
            playerInfo.setEmail(email);
            playerInfo.setRegisterStatus(true);
            //playerInfo.setLastSuccessfulIp(ipAddress);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return playerInfo;
    }

    private void createPlayersTable(Connection conn) throws SQLException {
        String query = "CREATE TABLE IF NOT EXISTS players (" +
                "id SERIAL PRIMARY KEY NOT NULL," + //Primary key
                "nick varchar(17) NOT NULL UNIQUE," + //Nickname of the player
                "password varchar(255) NOT NULL," + //Hashed password to the account
                "rank SMALLINT NOT NULL," + //Global rank of the player
                "created BIGINT NOT NULL," + //Time when player registered (Epoch time in milliseconds)
                "email varchar(50) UNIQUE)"; //E-mail assigned to this account
        conn.createStatement().execute(query);
    }

    private void createActivityHistoryTable(Connection conn) throws SQLException {
        String query = "CREATE TABLE IF NOT EXISTS activity_history (" +
                "id SERIAL PRIMARY KEY NOT NULL," + //Primary key
                "player INT NOT NULL," + //Id of the player from 'players' table
                "time BIGINT NOT NULL," + //Time when action was done on the account
                "status SMALLINT NOT NULL," + //Type of activity that was performed
                "ip_address VARCHAR(30) NOT NULL)"; //Ip address which from activity on the account was done
        conn.createStatement().execute(query);
    }

    private void createIpBlockadesTable(Connection conn) throws SQLException {
        String query = "CREATE TABLE IF NOT EXISTS ip_blockades (" +
                "id SERIAL PRIMARY KEY NOT NULL," + //Primary key
                "player INT NOT NULL," + //Id of the player from 'players' table on whose account ip was blocked
                "time BIGINT NOT NULL," + //Epoch time in milliseconds when ip was blocked
                "ip_address VARCHAR(30) NOT NULL," + //Ip address of locked account
                "to_check BOOLEAN NOT NULL DEFAULT FALSE)"; //Represents if the player decided if this blockade should be checked by administrator
        conn.createStatement().execute(query);
    }

    private void createRanksTable(Connection conn) {
        //TODO
    }

    private void closeConn(Connection conn) {
        if(conn != null) {
            try {
                conn.close();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }
}
