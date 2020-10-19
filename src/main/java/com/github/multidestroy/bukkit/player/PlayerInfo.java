package com.github.multidestroy.bukkit.player;

import com.github.multidestroy.bukkit.Config;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

public class PlayerInfo {
    private String email;
    private String hashedPassword;
    private String lastSuccessfulIp;
    private boolean loginStatus;
    private boolean registered;
    private boolean loginSession;
    private final Map<String, Short[]> blockadeCounter;

    public PlayerInfo() {
        this.email = null;
        this.hashedPassword = null;
        this.lastSuccessfulIp = null;
        this.loginStatus = false;
        this.registered = false;
        this.loginSession = false;
        this.blockadeCounter = new HashMap<>();
    }

    public PlayerInfo(String email, String hashedPassword, String lastSuccessfulIp, boolean loginStatus, boolean registered, boolean loginSession) {
        this.email = email;
        this.hashedPassword = hashedPassword;
        this.lastSuccessfulIp = lastSuccessfulIp;
        this.loginStatus = loginStatus;
        this.registered = registered;
        this.loginSession = loginSession;
        blockadeCounter = new HashMap<>();
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setHashedPassword(String hashedPassword) {
        this.hashedPassword = hashedPassword;
    }

    public void setLastSuccessfulIp(InetAddress address) {
        this.lastSuccessfulIp = address.getHostAddress();
    }

    public void setLastSuccessfulIp(String address) {
        this.lastSuccessfulIp = address;
    }

    public void setLoginStatus(boolean loginStatus) {
        this.loginStatus = loginStatus;
    }

    public void setRegisterStatus(boolean registered) {
        this.registered = registered;
    }

    public void setLoginSession(boolean loginSession) {
        this.loginSession = loginSession;
    }

    public String getEmail() {
        return email;
    }

    public String getLastSuccessfulIp() {
        return lastSuccessfulIp;
    }

    public String getHashedPassword() {
        return hashedPassword;
    }

    public boolean isRegistered() {
        return registered;
    }

    public boolean isLoggedIn() {
        return loginStatus;
    }

    @Deprecated
    public boolean isLoginSession() {
        return loginSession;
    }

    /**
     * @return TRUE - if counter[0] is equal to 0, FALSE if it's bigger
     */

    public boolean lowerBlockadeCounter(Config config, InetAddress ipAddress) {
        String address = ipAddress.getHostAddress();
        Short[] counter;
        if ((counter = blockadeCounter.get(address)) == null)
            counter = setUpBlockadeCounter(config, address);

        short left = (short) (counter[0] - 1);
        if (left > 0) {
            counter[0] = left;
            return false;
        } else {
            counter[0] = 0;
            return true;
        }
    }

    public boolean canNotifyAboutSoonBlockade(Config config, InetAddress ipAddress) {
        String address = ipAddress.getHostAddress();
        Short[] counter = blockadeCounter.get(address);
        if (counter == null)
            counter = setUpBlockadeCounter(config, address);
        return counter[0] <= counter[1];
    }

    public void resetBlockadeCounter() {
        blockadeCounter.clear();
    }

    private Short[] setUpBlockadeCounter(Config config, String ipAddress) {
        short randomNumber;
        Short[] counter;
        int same_ip_blockade_low = config.get().getInt("settings.login_attempt.ip_blockade.attempts.same_ip.low");
        int different_ip_blockade_low = config.get().getInt("settings.login_attempt.ip_blockade.attempts.different_ip.low");


        if (ipAddress.equals(lastSuccessfulIp))
            blockadeCounter.put(ipAddress, counter = new Short[]{randomNumber = config.getRandomNumberSameIp(), //counter
                    (short) (randomNumber - same_ip_blockade_low + 1)}); //Notification should start one attempt before low limit
            //Lower limit, when counter reaches that number, notification about allow is sent to player
        else
            blockadeCounter.put(ipAddress, counter = new Short[]{randomNumber = config.getRandomNumberDifferentIp(), //counter
                    (short) (randomNumber - different_ip_blockade_low + 1)}); //Notification should start one attempt before low limit
        //Lower limit, when counter reaches that number, notification about allow is sent to player
        return counter;
    }

}