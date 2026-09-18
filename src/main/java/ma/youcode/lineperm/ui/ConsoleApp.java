package ma.youcode.lineperm.ui;

import ma.youcode.lineperm.model.FichierProtege;
import ma.youcode.lineperm.model.User;
import ma.youcode.lineperm.service.FileService;
import ma.youcode.lineperm.service.JournalService;
import ma.youcode.lineperm.service.LogAnalyzer;
import ma.youcode.lineperm.service.UserService;

import java.util.Scanner;

public class ConsoleApp {
    private final UserService userService = new UserService();
    private final FileService fileService = new FileService();
    private final LogAnalyzer logAnalyzer = new LogAnalyzer();
    private User currentUser = null;
    private final Scanner scanner = new Scanner(System.in);

    public void start() {
        System.out.println("Bienvenue dans LinePermission !");
        while (true) {
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
                case "ls":
                    handleLs(parts);
                    break;
                case "touch":
                    handleTouch(parts);
                    break;
                case "cat":
                    handleCat(parts);
                    break;
                case "nano":
                    handleNano(parts);
                    break;
                case "chmod":
                    handleChmod(parts);
                    break;
                case "stats":
                    handleStats();
                    break;
                case "exit":
                    System.out.println("Au revoir !");
                    return;
                default:
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

        User user = userService.login(login, password);
        if (user == null) {
            // Même message que pour login inconnu ou mauvais mot de passe
            System.out.println("Login ou mot de passe incorrect.");
        } else {
            currentUser = user;
            System.out.println("Bienvenue " + login + " !");
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

    private void printDenied() {
        System.out.println("Permission denied.");
    }

    private boolean needLogin() {
        if (currentUser == null) {
            System.out.println("Vous devez etre connecte.");
            return false;
        }
        return true;
    }

    private void handleLs(String[] parts) {
        if (!needLogin()) {
            return;
        }
        if (parts.length < 2 || !parts[1].equals("-l")) {
            System.out.println("Usage: ls -l");
            return;
        }
        for (FichierProtege f : fileService.lister()) {
            System.out.println(f.toLsLine());
        }
    }

    private void handleTouch(String[] parts) {
        if (!needLogin()) {
            return;
        }
        if (parts.length < 2) {
            System.out.println("Usage: touch <fichier>");
            return;
        }
        String resultat = fileService.creer(currentUser.getLogin(), parts[1].trim());
        if (resultat.equals("OK")) {
            System.out.println("Fichier cree.");
        } else {
            System.out.println(resultat);
        }
    }

    private void handleCat(String[] parts) {
        if (!needLogin()) {
            return;
        }
        if (parts.length < 2) {
            System.out.println("Usage: cat <fichier>");
            return;
        }
        String nom = parts[1].trim();
        if (!fileService.existe(nom)) {
            System.out.println("Fichier introuvable.");
            return;
        }
        String contenu = fileService.lire(currentUser, nom);
        if (contenu == null) {
            printDenied();
            return;
        }
        System.out.print(contenu);
        if (!contenu.isEmpty() && !contenu.endsWith("\n")) {
            System.out.println();
        }
    }

    private void handleNano(String[] parts) {
        if (!needLogin()) {
            return;
        }
        if (parts.length < 2) {
            System.out.println("Usage: nano <fichier>");
            return;
        }
        String nom = parts[1].trim();
        if (!fileService.existe(nom)) {
            System.out.println("Fichier introuvable.");
            return;
        }
        // on verifie w avant de faire saisir le texte
        if (!fileService.peutEcrire(currentUser, nom)) {
            JournalService.enregistrer(currentUser.getLogin(), "ECRITURE", nom, "REFUSE");
            printDenied();
            return;
        }
        if (fileService.masquerContenu(currentUser, nom)) {
            System.out.println("Edition a l'aveugle (pas de droit r). Tapez EOF pour terminer.");
        } else {
            System.out.println("Saisie (une ligne EOF pour terminer) :");
        }
        StringBuilder sb = new StringBuilder();
        while (true) {
            String ligne = scanner.nextLine();
            if (ligne.equals("EOF")) {
                break;
            }
            sb.append(ligne).append("\n");
        }
        String resultat = fileService.ecrire(currentUser, nom, sb.toString());
        if (resultat.equals("DENIED")) {
            printDenied();
        } else if (resultat.equals("OK")) {
            System.out.println("Fichier enregistre.");
        } else {
            System.out.println(resultat);
        }
    }

    private void handleChmod(String[] parts) {
        if (!needLogin()) {
            return;
        }
        if (parts.length < 2) {
            System.out.println("Usage: chmod [-]r|w|d <fichier>");
            return;
        }
        String[] args = parts[1].trim().split(" ", 2);
        if (args.length < 2) {
            System.out.println("Usage: chmod [-]r|w|d <fichier>");
            return;
        }
        String flag = args[0];
        String nom = args[1].trim();
        boolean retirer = flag.startsWith("-");
        String lettre = retirer ? flag.substring(1) : flag;
        if (lettre.length() != 1) {
            System.out.println("Usage: chmod [-]r|w|d <fichier>");
            return;
        }
        char droit = lettre.charAt(0);
        if (droit != 'r' && droit != 'w' && droit != 'd') {
            System.out.println("Droit inconnu. Utilisez r, w ou d.");
            return;
        }
        if (!fileService.existe(nom)) {
            System.out.println("Fichier introuvable.");
            return;
        }
        String resultat;
        if (retirer) {
            resultat = fileService.retirerDroit(currentUser, nom, droit);
        } else {
            resultat = fileService.donnerDroit(currentUser, nom, droit);
        }
        if (resultat.equals("DENIED")) {
            printDenied();
        } else if (resultat.equals("ALREADY")) {
            if (retirer) {
                System.out.println("Le droit " + droit + " n'etait pas accorde.");
            } else {
                System.out.println("Le droit " + droit + " est deja accorde.");
            }
        } else if (resultat.equals("OK")) {
            if (retirer) {
                System.out.println("Droit " + droit + " retire aux autres.");
            } else {
                System.out.println("Droit " + droit + " donne aux autres.");
            }
        } else {
            System.out.println(resultat);
        }
    }

    private void handleStats() {
        logAnalyzer.charger();
        while (true) {
            System.out.println();
            System.out.println("=== Analyses du journal ===");
            System.out.println("1. Nombre total d'actions");
            System.out.println("2. Nombre d'acces refuses");
            System.out.println("3. Utilisateurs distincts");
            System.out.println("4. Actions par utilisateur");
            System.out.println("5. Top 3 des fichiers consultes");
            System.out.println("6. Acces refuses d'un utilisateur");
            System.out.println("7. Utilisateur le plus actif");
            System.out.println("8. Repartition des actions par type");
            System.out.println("0. Retour");
            System.out.print("Choix : ");

            String saisie = scanner.nextLine().trim();
            if (saisie.equals("0")) {
                return;
            }

            System.out.println();
            switch (saisie) {
                case "1":
                    logAnalyzer.afficherNombreTotalActions();
                    break;
                case "2":
                    logAnalyzer.afficherNombreAccesRefuses();
                    break;
                case "3":
                    logAnalyzer.afficherUtilisateursDistincts();
                    break;
                case "4":
                    logAnalyzer.afficherActionsParUtilisateur();
                    break;
                case "5":
                    logAnalyzer.afficherTop3Fichiers();
                    break;
                case "6":
                    System.out.print("Nom de l'utilisateur : ");
                    String nom = scanner.nextLine().trim();
                    logAnalyzer.afficherAccesRefusesUtilisateur(nom);
                    break;
                case "7":
                    logAnalyzer.afficherUtilisateurLePlusActif();
                    break;
                case "8":
                    logAnalyzer.afficherRepartitionParType();
                    break;
                default:
                    System.out.println("Choix inconnu.");
                    break;
            }
        }
    }
}
