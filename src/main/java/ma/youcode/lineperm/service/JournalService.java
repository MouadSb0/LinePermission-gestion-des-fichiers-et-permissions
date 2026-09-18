package ma.youcode.lineperm.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

// Ecrit une ligne dans access.log après chaque action (lecture, écriture, droits...).
public class JournalService {
    private static final String LOG_FILE = "access.log";
    private static final DateTimeFormatter HEURE = DateTimeFormatter.ofPattern("HH:mm");

    public static void enregistrer(String utilisateur, String action, String fichier, String resultat) {
        String date = LocalDate.now().toString();
        String heure = LocalTime.now().format(HEURE);
        String ligne = date + ";" + heure + ";" + utilisateur + ";" + action + ";" + fichier + ";" + resultat + "\n";
        try {
            Path path = Paths.get(LOG_FILE);
            Files.writeString(path, ligne, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            System.err.println("Erreur lors de l'ecriture du journal : " + e.getMessage());
        }
    }
}
