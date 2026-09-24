package ma.youcode.lineperm.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Provides the application's single shared JDBC connection.
 *
 * The connection settings can be overridden with the DB_URL, DB_USER, and
 * DB_PASSWORD environment variables. The defaults target a local SQLite
 * database, which keeps the console application easy to run locally.
 */
public final class DBConnection {
    private static final String DEFAULT_URL = "jdbc:sqlite:lineperm.db";

    private final Connection connection;

    private DBConnection() throws SQLException {
        String url = getSetting("DB_URL", DEFAULT_URL);
        String user = getSetting("DB_USER", "");
        String password = getSetting("DB_PASSWORD", "");

        if (user.isEmpty() && password.isEmpty()) {
            connection = DriverManager.getConnection(url);
        } else {
            connection = DriverManager.getConnection(url, user, password);
        }

        if (url.startsWith("jdbc:sqlite:")) {
            try (Statement statement = connection.createStatement()) {
                statement.execute("PRAGMA foreign_keys = ON");
            }
        }
    }

    private static String getSetting(String name, String defaultValue) {
        String value = System.getenv(name);
        return value == null || value.trim().isEmpty() ? defaultValue : value;
    }

    private static class Holder {
        private static final DBConnection INSTANCE = createInstance();

        private static DBConnection createInstance() {
            try {
                return new DBConnection();
            } catch (SQLException e) {
                throw new IllegalStateException("Unable to connect to the database.", e);
            }
        }
    }

    public static DBConnection getInstance() {
        return Holder.INSTANCE;
    }

    public Connection getConnection() {
        return connection;
    }

    public void close() throws SQLException {
        if (!connection.isClosed()) {
            connection.close();
        }
    }
}
