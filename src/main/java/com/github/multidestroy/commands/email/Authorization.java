package com.github.multidestroy.commands.email;

import com.github.multidestroy.EmailSender;
import com.github.multidestroy.system.ThreadSystem;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public interface Authorization {

    default Runnable sendEmailAuthorization(Player player, String email, ThreadSystem threadSystem, EmailSender emailSender) {
        return () -> {
            if(threadSystem.tryLock(player.getName())) {
                try {
                    if (emailSender.sendEmail())
                        player.sendMessage(ChatColor.GREEN + "Check out your e-mail to authorize e-mail changing operation!");
                    else
                        player.sendMessage(ChatColor.RED + "E-mail could not be changed! Try a few minutes later.");
                } finally {
                    threadSystem.unlock(player.getName());
                }
            }
            else
                player.sendMessage(ChatColor.RED + "Wait until previous command is done!");

        };
    }
}
