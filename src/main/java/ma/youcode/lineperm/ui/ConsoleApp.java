package ma.youcode.lineperm.ui;

import ma.youcode.lineperm.model.User;
import ma.youcode.lineperm.service.StatsService;
import ma.youcode.lineperm.service.UserService;

import java.sql.SQLException;
import java.util.Map;
import java.util.Scanner;

public class ConsoleApp {
    private final UserService userService = new UserService();
    private final StatsService statsService = new StatsService();
    private User currentUser = null;
    private final Scanner scanner = new Scanner(System.in);

    public void start() {
        System.out.println("Bienvenue dans LinePermission !");
        while (true) {
            if (!scanner.hasNextLine()) {
                System.out.println("\nFin de la saisie.");
                return;
            }
            String prompt = buildPrompt();
            System.out.print(prompt);
            String line = scanner.nextLine().trim();

            if (line.isEmpty()) {
                continue; // ligne vide → on ignore
            }

            String[] parts = line.split(" ", 2);
            String command = parts[0].toLowerCase();

            switch (command) {
                case "signup":
                    handleSignup(parts);
                    break;
                case "login":
                    handleLogin(parts);
                    break;
                case "logout":
                    handleLogout();
                    break;
                case "stats":
                    handleStats(parts);
                    break;
                case "exit":
                    System.out.println("Au revoir !");
                    return;
                default:
                    // Commande inconnue (ou commande de la partie 2)
                    System.out.println("Commande inconnue.");
                    break;
            }
        }
    }

    private String buildPrompt() {
        if (currentUser == null) {
            return "linperm> ";
        } else {
            return currentUser.getLogin() + "@linperm> ";
        }
    }

    // --- Gestionnaires de commandes ---

    private void handleSignup(String[] parts) {
        if (currentUser != null) {
            System.out.println("Vous êtes déjà connecté. Déconnectez-vous d'abord.");
            return;
        }
        if (parts.length < 2) {
            System.out.println("Usage: signup <login> <mot_de_passe>");
            return;
        }
        // On récupère le login et le mot de passe
        String[] args = parts[1].split(" ", 2);
        if (args.length < 2) {
            System.out.println("Usage: signup <login> <mot_de_passe>");
            return;
        }
        String login = args[0].trim();
        String password = args[1].trim();

        try {
            userService.signup(login, password);
            System.out.println("Compte créé avec succès !");
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
        } catch (SQLException e) {
            printDatabaseError(e);
        }
    }

    private void handleLogin(String[] parts) {
        if (currentUser != null) {
            System.out.println("Vous êtes déjà connecté. Déconnectez-vous d'abord.");
            return;
        }
        if (parts.length < 2) {
            System.out.println("Usage: login <login> <mot_de_passe>");
            return;
        }
        String[] args = parts[1].split(" ", 2);
        if (args.length < 2) {
            System.out.println("Usage: login <login> <mot_de_passe>");
            return;
        }
        String login = args[0].trim();
        String password = args[1].trim();

        try {
            User user = userService.login(login, password);
            if (user == null) {
                System.out.println("Login ou mot de passe incorrect.");
            } else {
                currentUser = user;
                System.out.println("Bienvenue " + login + " !");
            }
        } catch (SQLException e) {
            printDatabaseError(e);
        }
    }

    private void handleLogout() {
        if (currentUser == null) {
            System.out.println("Personne n'est connecté.");
            return;
        }
        System.out.println("Au revoir " + currentUser.getLogin() + " !");
        currentUser = null;
    }

    private void handleStats(String[] parts) {
        if (currentUser == null) {
            System.out.println("Connectez-vous pour consulter les statistiques.");
            return;
        }
        if (parts.length < 2 || parts[1].trim().isEmpty()) {
            printStatsUsage();
            return;
        }

        String[] statisticParts = parts[1].trim().toLowerCase().split("\\s+");
        String statistic = statisticParts[0];
        try {
            switch (statistic) {
                case "total":
                    System.out.println("Actions totales : " + statsService.totalActions());
                    break;
                case "refused":
                    System.out.println("Accès refusés : " + statsService.refusedAccesses());
                    break;
                case "users":
                    System.out.println("Utilisateurs distincts : " + statsService.distinctUsers());
                    break;
                case "per-user":
                    printIntegerStatistics("Actions par utilisateur",
                            statsService.actionsPerUser());
                    break;
                case "top-files":
                    printIntegerStatistics("Top 3 des fichiers consultés",
                            statsService.topThreeConsultedFiles());
                    break;
                case "most-active":
                    printMostActiveUser();
                    break;
                case "by-action":
                    printActionStatistics();
                    break;
                case "refused-user":
                    handleRefusedUserStatistic(statisticParts);
                    break;
                default:
                    printStatsUsage();
                    break;
            }
        } catch (NumberFormatException e) {
            System.out.println("L'identifiant utilisateur doit être un nombre entier.");
        } catch (SQLException e) {
            printDatabaseError(e);
        }
    }

    private void handleRefusedUserStatistic(String[] arguments) throws SQLException {
        if (arguments.length != 2) {
            System.out.println("Usage: stats refused-user <id_utilisateur>");
            return;
        }
        int userId = Integer.parseInt(arguments[1]);
        long count = statsService.refusedAccessesForUser(userId);
        System.out.println("Accès refusés pour l'utilisateur " + userId + " : " + count);
    }

    private void printMostActiveUser() throws SQLException {
        java.util.OptionalInt mostActiveUser = statsService.mostActiveUser();
        if (mostActiveUser.isPresent()) {
            System.out.println("Utilisateur le plus actif : "
                    + mostActiveUser.getAsInt());
        } else {
            System.out.println("Aucune activité enregistrée.");
        }
    }

    private void printActionStatistics() throws SQLException {
        Map<String, Long> statistics = statsService.actionsByType();
        if (statistics.isEmpty()) {
            System.out.println("Aucune action enregistrée.");
            return;
        }
        System.out.println("Répartition des actions :");
        statistics.forEach((action, count) ->
                System.out.println("- " + action + " : " + count));
    }

    private void printIntegerStatistics(String title, Map<Integer, Long> statistics) {
        System.out.println(title + " :");
        if (statistics.isEmpty()) {
            System.out.println("Aucune donnée.");
            return;
        }
        statistics.forEach((id, count) ->
                System.out.println("- " + id + " : " + count));
    }

    private void printStatsUsage() {
        System.out.println("Usage: stats <total|refused|users|per-user|top-files|"
                + "most-active|by-action|refused-user>");
        System.out.println("Pour refused-user, utilisez: stats refused-user <id_utilisateur>");
    }

    private void printDatabaseError(SQLException exception) {
        if (isConstraintViolation(exception)) {
            System.out.println("Opération refusée : les données entrent en conflit "
                    + "avec une contrainte existante (login ou référence déjà utilisé).");
        } else if (isConnectionError(exception)) {
            System.out.println("Base de données inaccessible. Vérifiez la connexion "
                    + "et réessayez.");
        } else {
            System.out.println("Une erreur de base de données est survenue. "
                    + "Veuillez réessayer.");
        }
    }

    private boolean isConstraintViolation(SQLException exception) {
        String state = exception.getSQLState();
        return state != null && state.startsWith("23")
                || exception.getMessage() != null
                && exception.getMessage().toLowerCase().contains("constraint");
    }

    private boolean isConnectionError(SQLException exception) {
        String state = exception.getSQLState();
        return state != null && state.startsWith("08")
                || exception.getMessage() != null
                && exception.getMessage().toLowerCase().contains("connect");
    }
}