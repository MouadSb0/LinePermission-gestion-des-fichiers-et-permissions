package ma.youcode.lineperm.service;

import ma.youcode.lineperm.dao.FichierDao;
import ma.youcode.lineperm.model.FichierProtege;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class FileService {
    private final FichierDao fichierDao;

    public FileService() {
        this(new FichierDao());
    }

    public FileService(FichierDao fichierDao) {
        this.fichierDao = fichierDao;
    }

    public FichierProtege createFile(
            String nom,
            String chemin,
            int ownerId,
            String rights
    ) throws SQLException {
        validateFile(nom, chemin, ownerId);
        return fichierDao.save(new FichierProtege(nom, chemin, ownerId, rights));
    }

    public Optional<FichierProtege> findById(int fileId) throws SQLException {
        return fichierDao.findById(fileId);
    }

    public List<FichierProtege> findAll() throws SQLException {
        return fichierDao.findAll();
    }

    public List<FichierProtege> findByOwner(int ownerId) throws SQLException {
        return fichierDao.findByOwnerId(ownerId);
    }

    public void updateFile(FichierProtege file) throws SQLException {
        validateFile(file.getNom(), file.getChemin(), file.getProprietaireId());
        fichierDao.update(file);
    }

    public void updateRights(int fileId, String rights) throws SQLException {
        if (rights == null || rights.trim().isEmpty()) {
            throw new IllegalArgumentException("Les droits ne peuvent pas être vides.");
        }
        fichierDao.updateRights(fileId, rights);
    }

    public void deleteFile(int fileId) throws SQLException {
        fichierDao.delete(fileId);
    }

    private void validateFile(String name, String path, int ownerId) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Le nom du fichier ne peut pas être vide.");
        }
        if (path == null || path.trim().isEmpty()) {
            throw new IllegalArgumentException("Le chemin du fichier ne peut pas être vide.");
        }
        if (ownerId <= 0) {
            throw new IllegalArgumentException("Le propriétaire doit avoir un identifiant valide.");
        }
    }
}
