package com.github.multidestroy;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.filter.AbstractFilter;
import org.apache.logging.log4j.message.Message;
import org.bukkit.plugin.java.JavaPlugin;

public class CommandsFilter extends AbstractFilter {

    private JavaPlugin plugin;

    CommandsFilter(JavaPlugin plugin) {
        this.plugin = plugin;
        Logger logger = (Logger) LogManager.getRootLogger();
        logger.addFilter(this);
    }

    @Override
    public Result filter(LogEvent event) {
        return event == null ? Result.NEUTRAL : isLoggable(event.getMessage().getFormattedMessage());
    }

    @Override
    public Result filter(Logger logger, Level level, Marker marker, Message msg, Throwable t) {
        return isLoggable(msg.getFormattedMessage());
    }

    @Override
    public Result filter(Logger logger, Level level, Marker marker, String msg, Object... params) {
        return isLoggable(msg);
    }

    @Override
    public Result filter(Logger logger, Level level, Marker marker, Object msg, Throwable t) {
        return msg == null ? Result.NEUTRAL : isLoggable(msg.toString());
    }

    private Result isLoggable(String message) {
        if (message != null) {
            if (message.contains("issued server command:")) {
                if (Utils.containsCommand(message, "login", plugin) ||
                    Utils.containsCommand(message, "register", plugin) ||
                    Utils.containsCommand(message, "changepassword", plugin) ||
                    Utils.containsCommand(message, "setemail", plugin) ||
                    Utils.containsCommand(message, "changeemail", plugin)) {
                    return Result.DENY;
                }
            }
        }
        return Result.NEUTRAL;
    }

}