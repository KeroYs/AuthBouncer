package com.github.multidestroy.bukkit.player;

public enum PlayerGlobalRank {
    PLAYER((short) 10),
    YOUTUBER((short) 20),
    TWITCHER((short) 30),
    HELPER((short) 40),
    MODERATOR((short) 50),
    ADMIN((short) 60),
    HEADADMIN((short) 70),
    OWNER((short) 80);

    private final short id;

    PlayerGlobalRank(short id) {
        this.id = id;
    }

    public short getRankId() {
        return id;
    }
}
