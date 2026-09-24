package ma.youcode.lineperm.dao;

import ma.youcode.lineperm.model.AccessLog;
import ma.youcode.lineperm.model.FichierProtege;
import ma.youcode.lineperm.model.User;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.OptionalInt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestMethodOrder(OrderAnnotation.class)
class DaoIntegrationTest {
    private static Connection connection;
    private static UserDao userDao;
    private static FichierDao fichierDao;
    private static LogDao logDao;

    @BeforeAll
    static void createInMemoryDatabase() throws Exception {
        System.setProperty("DB_URL", "jdbc:sqlite::memory:");
        Class.forName("org.sqlite.JDBC");
        connection = ma.youcode.lineperm.database.DBConnection
                .getInstance().getConnection();
        executeSchema();
        userDao = new UserDao();
        fichierDao = new FichierDao();
        logDao = new LogDao();
    }

    @BeforeEach
    void cleanDatabase() throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate("DROP TRIGGER IF EXISTS prevent_logs_update");
            statement.executeUpdate("DROP TRIGGER IF EXISTS prevent_logs_delete");
            statement.executeUpdate("DELETE FROM logs");
            statement.executeUpdate("DELETE FROM fichiers");
            statement.executeUpdate("DELETE FROM users");
        }
        try {
            executeSchema();
        } catch (IOException e) {
            throw new SQLException("Unable to recreate test schema triggers.", e);
        }
    }

    @AfterAll
    static void closeDatabase() throws SQLException {
        if (connection != null) {
            connection.close();
        }
        System.clearProperty("DB_URL");
    }

    @Test
    @Order(1)
    void userCrudAndLoginLookupWork() throws SQLException {
        User saved = userDao.save(new User("alice", "hash"));

        assertTrue(saved.getId() > 0);
        assertEquals("alice", userDao.findByLogin("alice").get().getLogin());
        assertEquals(saved.getId(), userDao.findById(saved.getId()).get().getId());
        assertEquals(1, userDao.findAll().size());

        saved.setPasswordHash("new-hash");
        userDao.update(saved);
        assertEquals("new-hash",
                userDao.findById(saved.getId()).get().getPasswordHash());

        userDao.delete(saved.getId());
        assertTrue(userDao.findAll().isEmpty());
    }

    @Test
    @Order(2)
    void fileCrudOwnerLookupAndRightsUpdateWork() throws SQLException {
        User owner = userDao.save(new User("owner", "hash"));
        FichierProtege saved = fichierDao.save(
                new FichierProtege("report.txt", "/tmp/report.txt", owner.getId(), "r")
        );

        assertEquals(saved.getId(), fichierDao.findById(saved.getId()).get().getId());
        assertEquals(1, fichierDao.findByOwnerId(owner.getId()).size());
        assertEquals(1, fichierDao.findAll().size());

        fichierDao.updateRights(saved.getId(), "rw");
        assertEquals("rw", fichierDao.findById(saved.getId()).get().getRights());

        fichierDao.delete(saved.getId());
        assertTrue(fichierDao.findAll().isEmpty());
    }

    @Test
    @Order(3)
    void auditAggregatesUseDatabaseQueries() throws SQLException {
        User first = userDao.save(new User("first", "hash"));
        User second = userDao.save(new User("second", "hash"));
        FichierProtege mostConsulted = fichierDao.save(
                new FichierProtege("one", "/one", first.getId(), "r"));
        FichierProtege secondMost = fichierDao.save(
                new FichierProtege("two", "/two", first.getId(), "r"));
        FichierProtege third = fichierDao.save(
                new FichierProtege("three", "/three", second.getId(), "r"));
        FichierProtege fourth = fichierDao.save(
                new FichierProtege("four", "/four", second.getId(), "r"));
        LocalDateTime time = LocalDateTime.of(2026, 9, 24, 12, 0);

        saveLog(first, mostConsulted, "READ", "ACCEPTE", time);
        saveLog(first, mostConsulted, "READ", "REFUSE", time.plusMinutes(1));
        saveLog(second, mostConsulted, "WRITE", "ACCEPTE", time.plusMinutes(2));
        saveLog(second, secondMost, "READ", "REFUSE", time.plusMinutes(3));
        saveLog(second, third, "READ", "ACCEPTE", time.plusMinutes(4));
        saveLog(first, fourth, "DELETE", "REFUSE", time.plusMinutes(5));

        assertEquals(6, logDao.countTotalActions());
        assertEquals(3, logDao.countRefusedAccesses());
        assertEquals(2, logDao.countDistinctUsers());
        assertEquals(Map.of(first.getId(), 3L, second.getId(), 3L),
                logDao.countActionsPerUser());
        assertEquals(Map.of(mostConsulted.getId(), 3L, secondMost.getId(), 1L,
                        third.getId(), 1L),
                logDao.findTopThreeConsultedFiles());
        assertEquals(2, logDao.countRefusedAccessesForUser(first.getId()));
        OptionalInt active = logDao.findMostActiveUser();
        assertTrue(active.isPresent());
        assertEquals(first.getId(), active.getAsInt());
        assertEquals(Map.of("DELETE", 1L, "READ", 4L, "WRITE", 1L),
                logDao.countActionsByType());
    }

    @Test
    @Order(4)
    void logsHaveNoUpdateOrDeleteMethods() {
        assertFalse(hasDeclaredMethod("update"));
        assertFalse(hasDeclaredMethod("delete"));
    }

    @Test
    @Order(5)
    void duplicateLoginViolatesUniqueConstraint() throws SQLException {
        userDao.save(new User("duplicate", "hash"));
        SQLException exception = assertThrows(SQLException.class,
                () -> userDao.save(new User("duplicate", "other")));
        assertTrue(exception.getMessage().toLowerCase().contains("unique"));
    }

    @Test
    @Order(6)
    void deletingOwnerWithFilesIsRejected() throws SQLException {
        User owner = userDao.save(new User("owner", "hash"));
        fichierDao.save(new FichierProtege("file", "/file", owner.getId(), "r"));

        assertThrows(SQLException.class, () -> userDao.delete(owner.getId()));
    }

    @Test
    @Order(7)
    void logWithUnknownUserIsRejected() {
        AccessLog log = new AccessLog(
                999, null, "READ", "ACCEPTE",
                LocalDateTime.of(2026, 9, 24, 12, 0), null);

        assertThrows(SQLException.class, () -> logDao.save(log));
    }

    private static void saveLog(
            User user,
            FichierProtege file,
            String action,
            String result,
            LocalDateTime occurredAt
    ) throws SQLException {
        logDao.save(new AccessLog(
                user.getId(), file.getId(), action, result, occurredAt, null));
    }

    private static boolean hasDeclaredMethod(String name) {
        for (java.lang.reflect.Method method : LogDao.class.getDeclaredMethods()) {
            if (method.getName().equals(name)) {
                return true;
            }
        }
        return false;
    }

    private static void executeSchema() throws IOException, SQLException {
        Path schema = Paths.get("src", "main", "resources", "schema.sql");
        String source = new String(Files.readAllBytes(schema), StandardCharsets.UTF_8);
        StringBuilder statement = new StringBuilder();
        boolean inTrigger = false;

        for (String line : source.split("\\R")) {
            statement.append(line).append('\n');
            if (line.trim().toUpperCase().startsWith("CREATE TRIGGER")) {
                inTrigger = true;
            }
            if (line.trim().equals("END;")) {
                inTrigger = false;
            }
            if (line.trim().endsWith(";") && !inTrigger) {
                String sql = statement.toString().trim();
                if (!sql.isEmpty() && !sql.startsWith("PRAGMA")) {
                    try (Statement jdbcStatement = connection.createStatement()) {
                        jdbcStatement.execute(sql);
                    }
                } else if (sql.startsWith("PRAGMA")) {
                    try (Statement jdbcStatement = connection.createStatement()) {
                        jdbcStatement.execute(sql);
                    }
                }
                statement.setLength(0);
            }
        }
        assertNotNull(connection);
    }
}
