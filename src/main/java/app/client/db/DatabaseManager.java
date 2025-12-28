package app.client.db;

import app.config.Config;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Database connection manager using HikariCP connection pooling
 * This provides efficient, thread-safe database connection management
 */
public class DatabaseManager {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseManager.class);
    private static HikariDataSource dataSource;

    static {
        initializeConnectionPool();
    }

    /**
     * Initializes the HikariCP connection pool with configuration from application.properties
     */
    private static void initializeConnectionPool() {
        try {
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(Config.getDbUrl());
            config.setUsername(Config.getDbUsername());
            config.setPassword(Config.getDbPassword());
            config.setDriverClassName(Config.getDbDriver());

            // Connection pool settings
            config.setMaximumPoolSize(Config.getMaxPoolSize());
            config.setMinimumIdle(Config.getMinIdle());
            config.setConnectionTimeout(Config.getConnectionTimeout());

            // Performance and reliability settings
            config.setAutoCommit(true);
            config.setConnectionTestQuery("SELECT 1");
            config.setPoolName("ChatAppHikariPool");

            // Additional optimizations
            config.addDataSourceProperty("cachePrepStmts", "true");
            config.addDataSourceProperty("prepStmtCacheSize", "250");
            config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

            dataSource = new HikariDataSource(config);
            logger.info("Database connection pool initialized successfully");
        } catch (Exception e) {
            logger.error("Failed to initialize database connection pool", e);
            throw new RuntimeException("Database initialization failed", e);
        }
    }

    /**
     * Gets a connection from the connection pool
     * @return Database connection
     * @throws SQLException if connection cannot be obtained
     */
    public static Connection getConnection() throws SQLException {
        if (dataSource == null || dataSource.isClosed()) {
            throw new SQLException("Connection pool is not initialized or has been closed");
        }
        return dataSource.getConnection();
    }

    /**
     * Closes the connection pool and releases all resources
     * Call this method when shutting down the application
     */
    public static void shutdown() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            logger.info("Database connection pool closed successfully");
        }
    }

    /**
     * Checks if the connection pool is initialized and running
     * @return true if pool is running, false otherwise
     */
    public static boolean isInitialized() {
        return dataSource != null && !dataSource.isClosed();
    }
}
