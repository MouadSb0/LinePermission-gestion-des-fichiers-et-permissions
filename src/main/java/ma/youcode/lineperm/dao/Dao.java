package ma.youcode.lineperm.dao;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Generic persistence contract for application entities.
 *
 * @param <T> entity type managed by the DAO
 */
public interface Dao<T> {
    T save(T entity) throws SQLException;

    Optional<T> findById(int id) throws SQLException;

    List<T> findAll() throws SQLException;

    void update(T entity) throws SQLException;

    void delete(int id) throws SQLException;
}
