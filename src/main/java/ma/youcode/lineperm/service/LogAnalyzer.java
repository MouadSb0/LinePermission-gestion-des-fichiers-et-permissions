package ma.youcode.lineperm.service;

import ma.youcode.lineperm.model.AccessLog;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class LogAnalyzer {
    private static final String LOG_FILE = "access.log";
    private List<AccessLog> logs;
    private StatsService statsService;

    public LogAnalyzer() {
        charger();
    }

    public void charger() {
        this.logs = chargerLogs();
        this.statsService = new StatsService(this.logs);
        System.out.println("Journal charge : " + logs.size() + " ligne(s).");
    }

    private List<AccessLog> chargerLogs() {
        Path path = Paths.get(LOG_FILE);
        if (!Files.exists(path)) {
            System.err.println("Fichier " + LOG_FILE + " introuvable. Aucune statistique disponible.");
            return Collections.emptyList();
        }
        try (Stream<String> lignes = Files.lines(path)) {
            return lignes
                    .filter(ligne -> !ligne.trim().isEmpty())
                    .map(AccessLog::fromLine)
                    .filter(log -> log != null)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            System.err.println("Erreur lors du chargement du journal : " + e.getMessage());
            return Collections.emptyList();
        }
    }

    public void afficherNombreTotalActions() {
        System.out.println("Nombre total d'actions : " + statsService.getNombreTotalActions());
    }

    public void afficherNombreAccesRefuses() {
        System.out.println("Nombre d'acces refuses : " + statsService.getNombreAccesRefuses());
    }

    public void afficherUtilisateursDistincts() {
        List<String> utilisateurs = statsService.getUtilisateursDistincts();
        System.out.println("Utilisateurs distincts :");
        if (utilisateurs.isEmpty()) {
            System.out.println(" (aucun)");
            return;
        }
        utilisateurs.forEach(nom -> System.out.println(" - " + nom));
    }

    public void afficherActionsParUtilisateur() {
        Map<String, Long> parUtilisateur = statsService.getActionsParUtilisateur();
        System.out.println("Actions par utilisateur :");
        if (parUtilisateur.isEmpty()) {
            System.out.println(" (aucune)");
            return;
        }
        parUtilisateur.forEach((nom, nb) -> System.out.println(" - " + nom + " : " + nb));
    }

    public void afficherTop3Fichiers() {
        List<Map.Entry<String, Long>> top3 = statsService.getTop3Fichiers();
        System.out.println("Top 3 des fichiers consultes :");
        if (top3.isEmpty()) {
            System.out.println(" (aucun)");
            return;
        }
        top3.forEach(entree -> System.out.println(" - " + entree.getKey() + " : " + entree.getValue()));
    }

    public void afficherAccesRefusesUtilisateur(String nom) {
        List<AccessLog> refuses = statsService.getAccesRefusesUtilisateur(nom);
        System.out.println("Acces refuses de " + nom + " :");
        if (refuses.isEmpty()) {
            System.out.println(" (aucun)");
            return;
        }
        refuses.forEach(log -> System.out.println(" - " + log));
    }

    public void afficherUtilisateurLePlusActif() {
        Optional<Map.Entry<String, Long>> plusActif = statsService.getUtilisateurLePlusActif();
        if (plusActif.isPresent()) {
            Map.Entry<String, Long> entree = plusActif.get();
            System.out.println("Utilisateur le plus actif : " + entree.getKey()
                    + " (" + entree.getValue() + " action(s))");
        } else {
            System.out.println("Aucun utilisateur dans le journal.");
        }
    }

    public void afficherRepartitionParType() {
        Map<String, Long> parType = statsService.getRepartitionParType();
        System.out.println("Repartition des actions par type :");
        if (parType.isEmpty()) {
            System.out.println(" (aucune)");
            return;
        }
        parType.forEach((action, nb) -> System.out.println(" - " + action + " : " + nb));
    }
}
