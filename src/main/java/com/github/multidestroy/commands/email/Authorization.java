package com.github.multidestroy.commands.email;

import com.github.multidestroy.EmailSender;
import com.github.multidestroy.Messages;
import com.github.multidestroy.system.ThreadSystem;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public interface Authorization {

    default Runnable sendEmailAuthorization(Player player, String email, ThreadSystem threadSystem, EmailSender emailSender) {
        return () -> {
            if(threadSystem.tryLock(player.getName())) {
                try {
                    if (emailSender.sendEmail())
                        player.sendMessage(Messages.getColoredString("EMAIL.AUTHORIZATION"));
                    else
                        player.sendMessage(Messages.getColoredString("ERROR"));
                } finally {
                    threadSystem.unlock(player.getName());
                }
            }
            else
                player.sendMessage(Messages.getColoredString("COMMAND.THREAD.LOCK"));

        };
    }
}
