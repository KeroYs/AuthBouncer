package com.github.multidestroy;

import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.HashMap;

public class Authorization {

    //String represents nickname in lower case
    //Boolean represents actual player's login status - if player has not been authorized, he cannot move between servers and use bungee commands
    private final HashMap<String, Boolean> playersAuthorizationMap;

    Authorization() {
        playersAuthorizationMap = new HashMap<>();
    }

    void authorizePlayer(ProxiedPlayer player) {
        playersAuthorizationMap.put(player.getName().toLowerCase(), true);
    }

    public void deAuthorizePlayer(ProxiedPlayer player) {
        playersAuthorizationMap.put(player.getName().toLowerCase(), false);
    }

    public boolean isPlayerAuthorized(ProxiedPlayer player) {
        return playersAuthorizationMap.getOrDefault(player.getName().toLowerCase(), false);
    }

}
