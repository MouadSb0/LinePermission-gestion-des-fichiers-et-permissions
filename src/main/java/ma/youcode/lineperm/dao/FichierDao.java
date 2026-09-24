package ma.youcode.lineperm.dao;

import ma.youcode.lineperm.model.FichierProtege;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class FichierDao extends AbstractDao<FichierProtege> {
    @Override
    public FichierProtege save(FichierProtege fichier) throws SQLException {
        String sql = "INSERT INTO fichiers "
                + "(nom, chemin, proprietaire_id, droits) VALUES (?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(
                sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, fichier.getNom());
            statement.setString(2, fichier.getChemin());
            statement.setInt(3, fichier.getProprietaireId());
            statement.setString(4, fichier.getRights());
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (!keys.next()) {
                    throw new SQLException("Database did not return the generated file ID.");
                }
                fichier.setId(keys.getInt(1));
            }
        }
        return fichier;
    }

    @Override
    public Optional<FichierProtege> findById(int id) throws SQLException {
        return executeQuery(
                selectSql() + " WHERE id = ?",
                resultSet -> resultSet.next()
                        ? Optional.of(mapFile(resultSet))
                        : Optional.empty(),
                id
        );
    }

    @Override
    public List<FichierProtege> findAll() throws SQLException {
        return findWithQuery(selectSql() + " ORDER BY id");
    }

    public List<FichierProtege> findByOwnerId(int ownerId) throws SQLException {
        return findWithQuery(selectSql()
                + " WHERE proprietaire_id = ? ORDER BY id", ownerId);
    }

    public void updateRights(int fileId, String rights) throws SQLException {
        int affectedRows = executeUpdate(
                "UPDATE fichiers SET droits = ? WHERE id = ?",
                rights, fileId
        );
        requireAffectedRow(affectedRows, "update rights for", fileId);
    }

    @Override
    public void update(FichierProtege fichier) throws SQLException {
        int affectedRows = executeUpdate(
                "UPDATE fichiers SET nom = ?, chemin = ?, proprietaire_id = ?, droits = ? "
                        + "WHERE id = ?",
                fichier.getNom(), fichier.getChemin(), fichier.getProprietaireId(),
                fichier.getRights(), fichier.getId()
        );
        requireAffectedRow(affectedRows, "update", fichier.getId());
    }

    @Override
    public void delete(int id) throws SQLException {
        int affectedRows = executeUpdate("DELETE FROM fichiers WHERE id = ?", id);
        requireAffectedRow(affectedRows, "delete", id);
    }

    private String selectSql() {
        return "SELECT id, nom, chemin, proprietaire_id, droits FROM fichiers";
    }

    private List<FichierProtege> findWithQuery(String sql, Object... parameters)
            throws SQLException {
        return executeQuery(
                sql,
                resultSet -> {
                    List<FichierProtege> files = new ArrayList<>();
                    while (resultSet.next()) {
                        files.add(mapFile(resultSet));
                    }
                    return files;
                },
                parameters
        );
    }

    private FichierProtege mapFile(ResultSet resultSet) throws SQLException {
        return new FichierProtege(
                resultSet.getInt("id"),
                resultSet.getString("nom"),
                resultSet.getString("chemin"),
                resultSet.getInt("proprietaire_id"),
                resultSet.getString("droits")
        );
    }
}
