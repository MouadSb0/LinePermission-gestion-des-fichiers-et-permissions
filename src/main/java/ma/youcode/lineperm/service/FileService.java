package ma.youcode.lineperm.service;

import ma.youcode.lineperm.access.ControleAcces;
import ma.youcode.lineperm.model.FichierProtege;
import ma.youcode.lineperm.model.User;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class FileService {
    private static final String DROITS_FILE = "fichiers.txt";
    private static final String DATA_DIR = "data";

    private final List<FichierProtege> fichiers = new ArrayList<>();

    public FileService() {
        loadFichiers();
    }

    private void loadFichiers() {
        Path path = Paths.get(DROITS_FILE);
        if (!Files.exists(path)) {
            return;
        }
        try {
            List<String> lines = Files.readAllLines(path);
            for (String line : lines) {
                if (line.trim().isEmpty()) {
                    continue;
                }
                FichierProtege f = FichierProtege.fromStorageLine(line);
                if (f != null) {
                    fichiers.add(f);
                }
            }
        } catch (IOException e) {
            System.err.println("Erreur lors du chargement des fichiers : " + e.getMessage());
        }
    }

    // prive : personne ne doit ecrire le fichier de droits sans passer par le service
    private void saveFichiers() {
        Path path = Paths.get(DROITS_FILE);
        StringBuilder sb = new StringBuilder();
        for (FichierProtege f : fichiers) {
            sb.append(f.toStorageLine()).append("\n");
        }
        try {
            Files.writeString(path, sb.toString());
        } catch (IOException e) {
            System.err.println("Erreur lors de la sauvegarde des droits : " + e.getMessage());
        }
    }

    public List<FichierProtege> lister() {
        return fichiers;
    }

    private FichierProtege trouver(String nom) {
        for (FichierProtege f : fichiers) {
            if (f.getNom().equals(nom)) {
                return f;
            }
        }
        return null;
    }

    public boolean existe(String nom) {
        return trouver(nom) != null;
    }

    // touch : cree un fichier vide en rwd|---, le createur est proprietaire
    public String creer(String login, String nom) {
        if (nom == null || nom.trim().isEmpty()) {
            return "Le nom du fichier ne peut pas etre vide.";
        }
        nom = nom.trim();
        if (nom.contains("/") || nom.contains("\\") || nom.contains("..")) {
            return "Le nom ne peut pas contenir un chemin.";
        }
        if (trouver(nom) != null) {
            return "Ce nom de fichier est deja pris.";
        }
        try {
            Path dataDir = Paths.get(DATA_DIR);
            if (!Files.exists(dataDir)) {
                Files.createDirectories(dataDir);
            }
            Path contenu = dataDir.resolve(nom);
            Files.writeString(contenu, "");
        } catch (IOException e) {
            return "Erreur lors de la creation du fichier.";
        }
        FichierProtege nouveau = new FichierProtege(nom, login);
        fichiers.add(nouveau);
        saveFichiers();
        JournalService.enregistrer(login, "CREATION", nom, "OK");
        return "OK";
    }

    // null = acces refuse, chaine vide = fichier vide
    public String lire(User user, String nom) {
        FichierProtege f = trouver(nom);
        if (f == null) {
            return "NOT_FOUND";
        }
        if (!ControleAcces.estAutorise(user, f, 'r')) {
            JournalService.enregistrer(user.getLogin(), "LECTURE", nom, "REFUSE");
            return null;
        }
        Path contenu = Paths.get(DATA_DIR).resolve(nom);
        try {
            if (!Files.exists(contenu)) {
                JournalService.enregistrer(user.getLogin(), "LECTURE", nom, "OK");
                return "";
            }
            String texte = Files.readString(contenu);
            JournalService.enregistrer(user.getLogin(), "LECTURE", nom, "OK");
            return texte;
        } catch (IOException e) {
            return "";
        }
    }

    public boolean peutEcrire(User user, String nom) {
        FichierProtege f = trouver(nom);
        if (f == null) {
            return false;
        }
        return ControleAcces.estAutorise(user, f, 'w');
    }

    public boolean peutLire(User user, String nom) {
        FichierProtege f = trouver(nom);
        if (f == null) {
            return false;
        }
        return ControleAcces.estAutorise(user, f, 'r');
    }

    // cas rwd|-w- : nano ne doit pas montrer le contenu actuel
    public boolean masquerContenu(User user, String nom) {
        return peutEcrire(user, nom) && !peutLire(user, nom);
    }

    // ecrire le contenu dans data/ apres verification du droit w
    public String ecrire(User user, String nom, String contenu) {
        FichierProtege f = trouver(nom);
        if (f == null) {
            return "NOT_FOUND";
        }
        if (!ControleAcces.estAutorise(user, f, 'w')) {
            JournalService.enregistrer(user.getLogin(), "ECRITURE", nom, "REFUSE");
            return "DENIED";
        }
        try {
            Path dataDir = Paths.get(DATA_DIR);
            if (!Files.exists(dataDir)) {
                Files.createDirectories(dataDir);
            }
            Path fichier = dataDir.resolve(nom);
            Files.writeString(fichier, contenu);
        } catch (IOException e) {
            return "Erreur lors de l'ecriture.";
        }
        saveFichiers();
        JournalService.enregistrer(user.getLogin(), "ECRITURE", nom, "OK");
        return "OK";
    }

    // chmod n'agit que sur le bloc autres. Seul le proprietaire peut le faire.
    public String donnerDroit(User user, String nom, char droit) {
        FichierProtege f = trouver(nom);
        if (f == null) {
            return "NOT_FOUND";
        }
        if (!ControleAcces.estProprietaire(user, f)) {
            JournalService.enregistrer(user.getLogin(), "CHMOD", nom, "REFUSE");
            return "DENIED";
        }
        boolean deja = false;
        if (droit == 'r') {
            deja = f.isrAutres();
            f.setrAutres(true);
        } else if (droit == 'w') {
            deja = f.iswAutres();
            f.setwAutres(true);
        } else if (droit == 'd') {
            deja = f.isdAutres();
            f.setdAutres(true);
        } else {
            return "UNKNOWN_RIGHT";
        }
        saveFichiers();
        JournalService.enregistrer(user.getLogin(), "CHMOD", nom, "OK");
        if (deja) {
            return "ALREADY";
        }
        return "OK";
    }

    public String retirerDroit(User user, String nom, char droit) {
        FichierProtege f = trouver(nom);
        if (f == null) {
            return "NOT_FOUND";
        }
        if (!ControleAcces.estProprietaire(user, f)) {
            JournalService.enregistrer(user.getLogin(), "CHMOD", nom, "REFUSE");
            return "DENIED";
        }
        boolean dejaAbsent = false;
        if (droit == 'r') {
            dejaAbsent = !f.isrAutres();
            f.setrAutres(false);
        } else if (droit == 'w') {
            dejaAbsent = !f.iswAutres();
            f.setwAutres(false);
        } else if (droit == 'd') {
            dejaAbsent = !f.isdAutres();
            f.setdAutres(false);
        } else {
            return "UNKNOWN_RIGHT";
        }
        saveFichiers();
        JournalService.enregistrer(user.getLogin(), "CHMOD", nom, "OK");
        if (dejaAbsent) {
            return "ALREADY";
        }
        return "OK";
    }
}
