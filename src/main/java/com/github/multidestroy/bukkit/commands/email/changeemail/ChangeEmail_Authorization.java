package com.github.multidestroy.bukkit.commands.email.changeemail;

import com.github.multidestroy.bukkit.Config;
import com.github.multidestroy.bukkit.EmailSender;
import com.github.multidestroy.bukkit.commands.email.Authorization;
import com.github.multidestroy.bukkit.database.Database;
import com.github.multidestroy.bukkit.system.PluginSystem;
import com.github.multidestroy.bukkit.system.ThreadSystem;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class ChangeEmail_Authorization extends ChangeEmail implements Authorization {

    private final EmailSender emailSender;

    public ChangeEmail_Authorization(PluginSystem system, Database database, Config config, EmailSender emailSender, ThreadSystem threadSystem, JavaPlugin plugin) {
        super(system, database, config, threadSystem, plugin);
        this.emailSender = emailSender;
    }

    @Override
    protected Runnable finalizeCommand(Player player, String email) {
        return sendEmailAuthorization(player, email, threadSystem, emailSender);
    }

}
