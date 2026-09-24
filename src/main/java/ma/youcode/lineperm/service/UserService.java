package ma.youcode.lineperm.service;

import ma.youcode.lineperm.dao.UserDao;
import ma.youcode.lineperm.model.User;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.SQLException;
import java.util.Optional;

public class UserService {
    private final UserDao userDao;

    public UserService() {
        this(new UserDao());
    }

    public UserService(UserDao userDao) {
        this.userDao = userDao;
    }

    public User signup(String login, String password) throws SQLException {
        validateCredentials(login, password);

        if (userDao.findByLogin(login).isPresent()) {
            throw new IllegalArgumentException("Ce login est déjà pris.");
        }

        String hash = BCrypt.hashpw(password, BCrypt.gensalt());
        return userDao.save(new User(login, hash));
    }

    public User login(String login, String password) throws SQLException {
        if (login == null || password == null) {
            return null;
        }

        Optional<User> user = userDao.findByLogin(login);
        if (!user.isPresent()) {
            return null;
        }

        return BCrypt.checkpw(password, user.get().getPasswordHash())
                ? user.get()
                : null;
    }

    public boolean userExists(String login) throws SQLException {
        return login != null && userDao.findByLogin(login).isPresent();
    }

    public Optional<User> findById(int id) throws SQLException {
        return userDao.findById(id);
    }

    private void validateCredentials(String login, String password) {
        if (login == null || login.trim().isEmpty()) {
            throw new IllegalArgumentException("Le login ne peut pas être vide.");
        }
        if (login.contains(" ") || login.contains(":")) {
            throw new IllegalArgumentException("Le login ne peut pas contenir d'espace ni de ':'.");
        }
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("Le mot de passe ne peut pas être vide.");
        }
    }
}
