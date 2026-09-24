package ma.youcode.lineperm.service;

import ma.youcode.lineperm.dao.LogDao;

import java.sql.SQLException;
import java.util.Map;
import java.util.OptionalInt;

public class StatsService {
    private final LogDao logDao;

    public StatsService() {
        this(new LogDao());
    }

    public StatsService(LogDao logDao) {
        this.logDao = logDao;
    }

    public long totalActions() throws SQLException {
        return logDao.countTotalActions();
    }

    public long refusedAccesses() throws SQLException {
        return logDao.countRefusedAccesses();
    }

    public long distinctUsers() throws SQLException {
        return logDao.countDistinctUsers();
    }

    public Map<Integer, Long> actionsPerUser() throws SQLException {
        return logDao.countActionsPerUser();
    }

    public Map<Integer, Long> topThreeConsultedFiles() throws SQLException {
        return logDao.findTopThreeConsultedFiles();
    }

    public long refusedAccessesForUser(int userId) throws SQLException {
        return logDao.countRefusedAccessesForUser(userId);
    }

    public OptionalInt mostActiveUser() throws SQLException {
        return logDao.findMostActiveUser();
    }

    public Map<String, Long> actionsByType() throws SQLException {
        return logDao.countActionsByType();
    }
}
