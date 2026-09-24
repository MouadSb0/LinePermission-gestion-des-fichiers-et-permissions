package ma.youcode.lineperm.dao;

import ma.youcode.lineperm.model.User;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UserDao extends AbstractDao<User> {
    @Override
    public User save(User user) throws SQLException {
        String sql = "INSERT INTO users (login, password_hash) VALUES (?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(
                sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, user.getLogin());
            statement.setString(2, user.getPasswordHash());
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (!keys.next()) {
                    throw new SQLException("Database did not return the generated user ID.");
                }
                user.setId(keys.getInt(1));
            }
        }
        return user;
    }

    @Override
    public Optional<User> findById(int id) throws SQLException {
        return executeQuery(
                "SELECT id, login, password_hash FROM users WHERE id = ?",
                resultSet -> resultSet.next()
                        ? Optional.of(mapUser(resultSet))
                        : Optional.empty(),
                id
        );
    }

    public Optional<User> findByLogin(String login) throws SQLException {
        return executeQuery(
                "SELECT id, login, password_hash FROM users WHERE login = ?",
                resultSet -> resultSet.next()
                        ? Optional.of(mapUser(resultSet))
                        : Optional.empty(),
                login
        );
    }

    @Override
    public List<User> findAll() throws SQLException {
        return executeQuery(
                "SELECT id, login, password_hash FROM users ORDER BY id",
                resultSet -> {
                    List<User> users = new ArrayList<>();
                    while (resultSet.next()) {
                        users.add(mapUser(resultSet));
                    }
                    return users;
                }
        );
    }

    @Override
    public void update(User user) throws SQLException {
        int affectedRows = executeUpdate(
                "UPDATE users SET login = ?, password_hash = ? WHERE id = ?",
                user.getLogin(), user.getPasswordHash(), user.getId()
        );
        requireAffectedRow(affectedRows, "update", user.getId());
    }

    @Override
    public void delete(int id) throws SQLException {
        int affectedRows = executeUpdate("DELETE FROM users WHERE id = ?", id);
        requireAffectedRow(affectedRows, "delete", id);
    }

    private User mapUser(ResultSet resultSet) throws SQLException {
        return new User(
                resultSet.getInt("id"),
                resultSet.getString("login"),
                resultSet.getString("password_hash")
        );
    }
}
