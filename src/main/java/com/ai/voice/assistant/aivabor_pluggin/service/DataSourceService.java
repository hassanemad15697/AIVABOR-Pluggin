package com.ai.voice.assistant.aivabor_pluggin.service;


import com.ai.voice.assistant.aivabor_pluggin.model.DatabaseDialect;
import com.ai.voice.assistant.aivabor_pluggin.request.ProcessRequest;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.sql.*;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Service for managing the DataSource and processing tables.
 */
@Service
@Slf4j
public class DataSourceService {

    @Getter
    private DataSource dataSource;

    @Getter
    private DatabaseDialect dialect;

    /**
     * Initializes the DataSource based on the provided request.
     */
    public void initializeDataSource(ProcessRequest request) throws Exception {
        createDataSource(request);
        testConnection();
        detectDialect();
    }

    /**
     * Creates DataSource based on the request.
     */
    private void createDataSource(ProcessRequest request) throws Exception {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(request.getUrl());
        config.setUsername(request.getUsername());
        config.setPassword(request.getPassword());
        config.setDriverClassName(getDriverClassNameFromUrl(request.getUrl()));

        this.dataSource = new HikariDataSource(config);
    }

    /**
     * Tests the database connection.
     */
    private void testConnection() throws Exception {
        try (Connection conn = dataSource.getConnection()) {
            if (conn == null || conn.isClosed()) {
                throw new SQLException("Failed to establish connection.");
            }
            log.info("Database connection test successful.");
        }
    }

    /**
     * Detects the database dialect.
     */
    private void detectDialect() throws Exception {
        try (Connection conn = dataSource.getConnection()) {
            String databaseProductName = conn.getMetaData().getDatabaseProductName();
            this.dialect = mapDatabaseProductNameToDialect(databaseProductName);
            log.info("Detected database dialect: {}", this.dialect);
        }
    }

    /**
     * Maps JDBC URL to driver class name.
     */
    private String getDriverClassNameFromUrl(String url) throws Exception {
        if (url.startsWith("jdbc:mysql:")) {
            return "com.mysql.cj.jdbc.Driver";
        } else if (url.startsWith("jdbc:postgresql:")) {
            return "org.postgresql.Driver";
        } else if (url.startsWith("jdbc:oracle:")) {
            return "oracle.jdbc.driver.OracleDriver";
        } else if (url.startsWith("jdbc:sqlserver:")) {
            return "com.microsoft.sqlserver.jdbc.SQLServerDriver";
        } else {
            throw new SQLException("Unsupported JDBC URL: " + url);
        }
    }

    /**
     * Maps database product name to DatabaseDialect enum.
     */
    private DatabaseDialect mapDatabaseProductNameToDialect(String productName) throws Exception {
        if (productName.equalsIgnoreCase("MySQL")) {
            return DatabaseDialect.MYSQL;
        } else if (productName.equalsIgnoreCase("PostgreSQL")) {
            return DatabaseDialect.POSTGRESQL;
        } else if (productName.equalsIgnoreCase("Oracle")) {
            return DatabaseDialect.ORACLE;
        } else if (productName.equalsIgnoreCase("Microsoft SQL Server")) {
            return DatabaseDialect.SQLSERVER;
        } else {
            throw new SQLException("Unsupported database product: " + productName);
        }
    }
}