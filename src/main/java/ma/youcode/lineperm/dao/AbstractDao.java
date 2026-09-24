package ma.youcode.lineperm.dao;

import ma.youcode.lineperm.database.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Shared JDBC support for concrete DAOs.
 *
 * @param <T> entity type managed by the DAO
 */
public abstract class AbstractDao<T> implements Dao<T> {
    protected final Connection connection;

    protected AbstractDao() {
        this.connection = DBConnection.getInstance().getConnection();
    }

    /**
     * Executes a query and delegates result-set mapping to the concrete DAO.
     */
    protected <R> R executeQuery(
            String sql,
            ResultSetMapper<R> mapper,
            Object... parameters
    ) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            bindParameters(statement, parameters);
            try (ResultSet resultSet = statement.executeQuery()) {
                return mapper.map(resultSet);
            }
        }
    }

    /**
     * Executes an INSERT, UPDATE, or DELETE statement.
     */
    protected int executeUpdate(String sql, Object... parameters) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            bindParameters(statement, parameters);
            return statement.executeUpdate();
        }
    }

    protected void bindParameters(PreparedStatement statement, Object... parameters)
            throws SQLException {
        for (int index = 0; index < parameters.length; index++) {
            statement.setObject(index + 1, parameters[index]);
        }
    }

    protected void requireAffectedRow(int affectedRows, String operation, int id) {
        if (affectedRows == 0) {
            throw new IllegalArgumentException(
                    "Cannot " + operation + " entity with id " + id + ": entity not found."
            );
        }
    }

    @FunctionalInterface
    protected interface ResultSetMapper<R> {
        R map(ResultSet resultSet) throws SQLException;
    }
}
