package com.github.multidestroy.bukkit;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.filter.AbstractFilter;
import org.apache.logging.log4j.message.Message;
import org.bukkit.plugin.java.JavaPlugin;

class CommandsFilter extends AbstractFilter {

    private final JavaPlugin plugin;
    private boolean working;

    CommandsFilter(JavaPlugin plugin) {
        this.plugin = plugin;
        Logger logger = (Logger) LogManager.getRootLogger();
        logger.addFilter(this);
        working = true;
    }

    public void stopFilter() {
        working = false;
    }

    @Override
    public Result filter(LogEvent event) {
        return working && event != null ? isLoggable(event.getMessage().getFormattedMessage()) : Result.NEUTRAL;
    }

    @Override
    public Result filter(Logger logger, Level level, Marker marker, Message msg, Throwable t) {
        if (working)
            return isLoggable(msg.getFormattedMessage());
        return Result.NEUTRAL;
    }

    @Override
    public Result filter(Logger logger, Level level, Marker marker, String msg, Object... params) {
        if (working)
            return isLoggable(msg);
        return Result.NEUTRAL;
    }

    @Override
    public Result filter(Logger logger, Level level, Marker marker, Object msg, Throwable t) {
        return working && msg != null ? isLoggable(msg.toString()) : Result.NEUTRAL;
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