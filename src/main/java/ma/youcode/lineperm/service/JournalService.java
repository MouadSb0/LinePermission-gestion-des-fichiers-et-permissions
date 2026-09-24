package ma.youcode.lineperm.service;

import ma.youcode.lineperm.dao.LogDao;
import ma.youcode.lineperm.model.AccessLog;

import java.sql.SQLException;
import java.time.LocalDateTime;

public class JournalService {
    private final LogDao logDao;

    public JournalService() {
        this(new LogDao());
    }

    public JournalService(LogDao logDao) {
        this.logDao = logDao;
    }

    public AccessLog record(
            int userId,
            Integer fileId,
            String action,
            String result,
            String details
    ) throws SQLException {
        if (userId <= 0) {
            throw new IllegalArgumentException("L'utilisateur doit avoir un identifiant valide.");
        }
        if (action == null || action.trim().isEmpty()) {
            throw new IllegalArgumentException("L'action ne peut pas être vide.");
        }
        if (!"ACCEPTE".equals(result) && !"REFUSE".equals(result)) {
            throw new IllegalArgumentException("Le résultat doit être ACCEPTE ou REFUSE.");
        }

        AccessLog log = new AccessLog(
                userId,
                fileId,
                action,
                result,
                LocalDateTime.now(),
                details
        );
        return logDao.save(log);
    }
}
