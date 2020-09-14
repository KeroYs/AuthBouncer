package com.github.multidestroy.system;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.concurrent.locks.ReentrantLock;

public class ThreadSystem {

    //private JavaPlugin plugin;
    private HashMap<String, ReentrantLock> threadLocksMap;
    //[0] - represents whether player is executing any command
    //[1] - if player should be kicked from the server after executing command (unless is not logged in)

    public ThreadSystem() {
        threadLocksMap = new HashMap<>();
    }

    public void lock(String playerName) {
        threadLocksMap.computeIfAbsent(playerName, k -> new ReentrantLock()).lock();
    }

    public void unlock(String playerName) {
        threadLocksMap.computeIfAbsent(playerName, k -> new ReentrantLock()).unlock();
    }
    public boolean tryLock(String playerName) {
        return threadLocksMap.computeIfAbsent(playerName, k -> new ReentrantLock()).tryLock();
    }

   /* private void kickPlayer(String playerName) {
        //TODO:
        Bukkit.getScheduler().runTask(plugin, new Runnable() {
            @Override
            public void run() {
                Bukkit.getPlayer(playerName).kickPlayer("wasd");
            }
        });
    }*/

}
