package com.github.multidestroy.commands.email.setemail;

import com.github.multidestroy.Config;
import com.github.multidestroy.EmailSender;
import com.github.multidestroy.commands.email.Authorization;
import com.github.multidestroy.database.Database;
import com.github.multidestroy.system.PluginSystem;
import com.github.multidestroy.system.ThreadSystem;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class SetEmail_Authorization extends SetEmail implements Authorization {

    private final EmailSender emailSender;

    public SetEmail_Authorization(PluginSystem system, Database database, Config config, EmailSender emailSender, ThreadSystem threadSystem, JavaPlugin plugin) {
        super(system, database, config, threadSystem, plugin);
        this.emailSender = emailSender;
    }

    @Override
    protected Runnable finalizeCommand(Player player, String email) {
        return sendEmailAuthorization(player, email, threadSystem, emailSender);
    }

}
