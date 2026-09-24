package ma.youcode.lineperm.dao;

import ma.youcode.lineperm.model.AccessLog;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;

public class LogDao extends AbstractDao<AccessLog> {
    @Override
    public AccessLog save(AccessLog log) throws SQLException {
        String sql = "INSERT INTO logs "
                + "(user_id, fichier_id, action, resultat, occurred_at, details) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(
                sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setInt(1, log.getUserId());
            if (log.getFichierId() == null) {
                statement.setNull(2, java.sql.Types.INTEGER);
            } else {
                statement.setInt(2, log.getFichierId());
            }
            statement.setString(3, log.getAction());
            statement.setString(4, log.getResult());
            statement.setTimestamp(5, Timestamp.valueOf(log.getOccurredAt()));
            statement.setString(6, log.getDetails());
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (!keys.next()) {
                    throw new SQLException("Database did not return the generated log ID.");
                }
                return new AccessLog(
                        keys.getInt(1),
                        log.getUserId(),
                        log.getFichierId(),
                        log.getAction(),
                        log.getResult(),
                        log.getOccurredAt(),
                        log.getDetails()
                );
            }
        }
    }

    @Override
    public Optional<AccessLog> findById(int id) throws SQLException {
        return executeQuery(
                selectSql() + " WHERE id = ?",
                resultSet -> resultSet.next()
                        ? Optional.of(mapLog(resultSet))
                        : Optional.empty(),
                id
        );
    }

    @Override
    public List<AccessLog> findAll() throws SQLException {
        return executeQuery(
                selectSql() + " ORDER BY occurred_at, id",
                resultSet -> {
                    List<AccessLog> logs = new ArrayList<>();
                    while (resultSet.next()) {
                        logs.add(mapLog(resultSet));
                    }
                    return logs;
                }
        );
    }

    public long countTotalActions() throws SQLException {
        return count("SELECT COUNT(*) FROM logs");
    }

    public long countRefusedAccesses() throws SQLException {
        return count("SELECT COUNT(*) FROM logs WHERE resultat = 'REFUSE'");
    }

    public long countDistinctUsers() throws SQLException {
        return count("SELECT COUNT(DISTINCT user_id) FROM logs");
    }

    public Map<Integer, Long> countActionsPerUser() throws SQLException {
        return countByInteger(
                "SELECT user_id, COUNT(*) FROM logs GROUP BY user_id ORDER BY user_id"
        );
    }

    public Map<Integer, Long> findTopThreeConsultedFiles() throws SQLException {
        return countByInteger(
                "SELECT fichier_id, COUNT(*) FROM logs "
                        + "WHERE fichier_id IS NOT NULL "
                        + "GROUP BY fichier_id ORDER BY COUNT(*) DESC, fichier_id LIMIT 3"
        );
    }

    public long countRefusedAccessesForUser(int userId) throws SQLException {
        return count(
                "SELECT COUNT(*) FROM logs WHERE user_id = ? AND resultat = 'REFUSE'",
                userId
        );
    }

    public OptionalInt findMostActiveUser() throws SQLException {
        return executeQuery(
                "SELECT user_id FROM logs GROUP BY user_id "
                        + "ORDER BY COUNT(*) DESC, user_id LIMIT 1",
                resultSet -> resultSet.next()
                        ? OptionalInt.of(resultSet.getInt("user_id"))
                        : OptionalInt.empty()
        );
    }

    public Map<String, Long> countActionsByType() throws SQLException {
        return executeQuery(
                "SELECT action, COUNT(*) FROM logs "
                        + "GROUP BY action ORDER BY action",
                resultSet -> {
                    Map<String, Long> counts = new LinkedHashMap<>();
                    while (resultSet.next()) {
                        counts.put(resultSet.getString("action"), resultSet.getLong(2));
                    }
                    return counts;
                }
        );
    }

    private long count(String sql, Object... parameters) throws SQLException {
        return executeQuery(
                sql,
                resultSet -> {
                    if (!resultSet.next()) {
                        throw new SQLException("Count query returned no result.");
                    }
                    return resultSet.getLong(1);
                },
                parameters
        );
    }

    private Map<Integer, Long> countByInteger(String sql) throws SQLException {
        return executeQuery(
                sql,
                resultSet -> {
                    Map<Integer, Long> counts = new LinkedHashMap<>();
                    while (resultSet.next()) {
                        counts.put(resultSet.getInt(1), resultSet.getLong(2));
                    }
                    return counts;
                }
        );
    }

    private String selectSql() {
        return "SELECT id, user_id, fichier_id, action, resultat, occurred_at, details "
                + "FROM logs";
    }

    private AccessLog mapLog(ResultSet resultSet) throws SQLException {
        int fileId = resultSet.getInt("fichier_id");
        Integer nullableFileId = resultSet.wasNull() ? null : fileId;
        Timestamp timestamp = resultSet.getTimestamp("occurred_at");
        LocalDateTime occurredAt = timestamp.toLocalDateTime();
        return new AccessLog(
                resultSet.getInt("id"),
                resultSet.getInt("user_id"),
                nullableFileId,
                resultSet.getString("action"),
                resultSet.getString("resultat"),
                occurredAt,
                resultSet.getString("details")
        );
    }
}
