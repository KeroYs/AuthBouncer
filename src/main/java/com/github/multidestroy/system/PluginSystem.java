package com.github.multidestroy.system;

import com.github.multidestroy.PasswordHasher;
import com.github.multidestroy.Utils;
import com.github.multidestroy.configs.Settings;
import com.github.multidestroy.player.PlayerInfo;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class PluginSystem {

    private Map<String, PlayerInfo> system;
    private PasswordHasher passwordHasher;

    //General Email Regex (RFC 5322 Official Standard)
    private static Pattern emailPattern = Pattern.compile("(?:[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*|\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\\[(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|[a-z0-9-]*[a-z0-9]:(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)\\])");


    public PluginSystem(PasswordHasher passwordHasher) {
        this.system = new HashMap<>();
        this.passwordHasher = passwordHasher;
    }

    public void saveNewPlayer(String playerName, InetAddress address) {
        playerName = playerName.toLowerCase(); //save player's nickname always in lowe case
        system.put(playerName, new PlayerInfo("", "", address.getHostAddress(), false, false, false));
    }

    public void saveNewPlayer(String playerName, PlayerInfo playerInfo) {
        //save player's nickname always in lowe case
        system.put(playerName.toLowerCase(), playerInfo);
    }

    public void setPlayerRegisterStatus(String playerName, boolean status, String hashedPassword) {
        PlayerInfo playerInfo = system.get(playerName.toLowerCase());
        playerInfo.setRegisterStatus(status);
        playerInfo.setHashedPassword(hashedPassword);
    }

    public boolean isPlayerInSystem(String playerName) {
        return system.get(playerName.toLowerCase()) != null;
    }

    public boolean isLoginSession(String playerName, InetAddress address) {
        PlayerInfo playerInfo = system.get(playerName.toLowerCase());
        if(playerInfo.isLoginSession())
            if(playerInfo.getLastSuccessfulIp() != null)
                return playerInfo.getLastSuccessfulIp().equals(address.getHostAddress());
        return false;
    }

    public boolean isPlayerRegistered(String playerName) {
        return system.get(playerName.toLowerCase()).isRegistered();
    }

    public boolean isPlayerLoggedIn(String playerName) {
        return system.get(playerName.toLowerCase()).isLoggedIn();
    }

    public boolean isEmailAssigned(String playerName) {
        return system.get(playerName.toLowerCase()).getEmail() != null;
    }

    public boolean isPasswordCorrect(String playerName, String password) {
        return passwordHasher.checkPasswordCorrectness(password, system.get(playerName.toLowerCase()).getHashedPassword());
    }

    public boolean isPasswordCorrect(PlayerInfo playerInfo, String password) {
        return passwordHasher.checkPasswordCorrectness(password, playerInfo.getHashedPassword());
    }

    public boolean isEmailCorrect(String playerName, String email){
        return system.get(playerName.toLowerCase()).getEmail().equals(email);
    }

    public String emailHint(String playerName) {
        String email = system.get(playerName.toLowerCase()).getEmail();
        int indexOfDomainStart = email.indexOf("@");
        if(Utils.isEmailNameLongerThan7chars(email))
            //if e-mail name is longer than 7 characters, show first and last char as hint
            return email.replaceFirst("\\b(\\w)\\S*?(\\S@)(\\S)\\S*(\\.\\S*)\\b",
                    "$1****$2$3****$4");
        //otherwise show only the first character
        return system.get(playerName).getEmail().replaceFirst("\\b(\\w)\\S*?(@)(\\S)\\S*(\\.\\S*)\\b",
                "$1****$2$3****$4");
    }

    public PlayerInfo getPlayerInfo(String playerName) {
        return system.get(playerName.toLowerCase());
    }

    //Static methods

    public static boolean isPasswordPossible(String password) {
        return password.length() >= 5;
    }

    public static boolean isEmailPossible(String email) {
        return emailPattern.matcher(email).matches();
    }

}
