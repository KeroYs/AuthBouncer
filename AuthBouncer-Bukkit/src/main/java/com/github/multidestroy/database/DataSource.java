package com.github.multidestroy.database;

import com.github.multidestroy.Config;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;

class DataSource {

    private HikariDataSource dataSource;

    DataSource(Config config) throws Exception {
        dataSource = null;
        HikariConfig hikariConfig = new HikariConfig();
        String host = config.get().getString("database.host");
        String name = config.get().getString("database.name");
        String username = config.get().getString("database.username");
        String password = config.get().getString("database.password");

        hikariConfig.setJdbcUrl("jdbc:postgresql://" + host + "/" + name);
        hikariConfig.setUsername(username);
        hikariConfig.setPassword(password);
        hikariConfig.addDataSourceProperty("cachePrepStmts", "true");
        hikariConfig.addDataSourceProperty("prepStmtCacheSize", "250");
        hikariConfig.addDataSourceProperty("prepStmtCacheSqlLimit", "1024");
        try {
            dataSource = new HikariDataSource(hikariConfig);
        } catch (Exception e) {
            throw new Exception(e);
        }
    }

    Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public void close() {
        if (dataSource != null)
            dataSource.close();
    }
}