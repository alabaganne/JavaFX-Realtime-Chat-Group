package app.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Configuration manager for loading application properties from application.properties file
 */
public class Config {
    private static final Logger logger = LoggerFactory.getLogger(Config.class);
    private static final Properties properties = new Properties();
    private static boolean loaded = false;

    static {
        loadProperties();
    }

    /**
     * Loads properties from application.properties file
     */
    private static void loadProperties() {
        if (loaded) {
            return;
        }

        try (InputStream input = Config.class.getClassLoader().getResourceAsStream("application.properties")) {
            if (input == null) {
                logger.error("Unable to find application.properties");
                throw new RuntimeException("Configuration file not found");
            }
            properties.load(input);
            loaded = true;
            logger.info("Configuration loaded successfully");
        } catch (IOException e) {
            logger.error("Error loading configuration", e);
            throw new RuntimeException("Failed to load configuration", e);
        }
    }

    /**
     * Gets a property value by key
     * @param key Property key
     * @return Property value
     */
    public static String get(String key) {
        return properties.getProperty(key);
    }

    /**
     * Gets a property value with a default fallback
     * @param key Property key
     * @param defaultValue Default value if key not found
     * @return Property value or default
     */
    public static String get(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }

    /**
     * Gets an integer property value
     * @param key Property key
     * @param defaultValue Default value if key not found or invalid
     * @return Integer property value
     */
    public static int getInt(String key, int defaultValue) {
        String value = properties.getProperty(key);
        if (value != null) {
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException e) {
                logger.warn("Invalid integer value for key {}: {}", key, value);
            }
        }
        return defaultValue;
    }

    // Database configuration getters
    public static String getDbUrl() {
        return get("db.url");
    }

    public static String getDbUsername() {
        return get("db.username");
    }

    public static String getDbPassword() {
        return get("db.password");
    }

    public static String getDbDriver() {
        return get("db.driver");
    }

    // Server configuration getters
    public static String getServerHost() {
        return get("server.host", "localhost");
    }

    public static int getServerPort() {
        return getInt("server.port", 1234);
    }

    // Connection pool getters
    public static int getMaxPoolSize() {
        return getInt("db.pool.maximumPoolSize", 10);
    }

    public static int getMinIdle() {
        return getInt("db.pool.minimumIdle", 2);
    }

    public static int getConnectionTimeout() {
        return getInt("db.pool.connectionTimeout", 30000);
    }
}
