package ma.youcode.lineperm;

import ma.youcode.lineperm.ui.ConsoleApp;

import java.sql.SQLException;

public class Main {
    public static void main(String[] args) {
        try {
            ConsoleApp app = new ConsoleApp();
            app.start();
        } catch (IllegalStateException e) {
            if (e.getCause() instanceof SQLException) {
                System.out.println("Base de données inaccessible. Vérifiez la connexion "
                        + "et la configuration JDBC.");
            } else {
                System.out.println("Impossible de démarrer l'application.");
            }
        }
    }
}