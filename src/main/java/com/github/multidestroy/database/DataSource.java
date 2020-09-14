package com.github.multidestroy.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

public class DataSource {

    private static HikariDataSource dataSource;

    public DataSource(Map<String, String> databaseInfo) {
        HikariConfig hikariConfig = new HikariConfig();

        hikariConfig.setJdbcUrl("jdbc:postgresql://" + databaseInfo.get("host") + "/" + databaseInfo.get("name"));
        hikariConfig.setUsername(databaseInfo.get("username"));
        hikariConfig.setPassword(databaseInfo.get("password"));
        hikariConfig.addDataSourceProperty( "cachePrepStmts" , "true" );
        hikariConfig.addDataSourceProperty( "prepStmtCacheSize" , "250" );
        hikariConfig.addDataSourceProperty( "prepStmtCacheSqlLimit" , "1024" );

        dataSource = new HikariDataSource(hikariConfig);
    }

    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }
}