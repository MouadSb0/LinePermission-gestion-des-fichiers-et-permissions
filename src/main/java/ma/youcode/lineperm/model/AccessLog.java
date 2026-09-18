package ma.youcode.lineperm.model;

// Une ligne du fichier access.log devient un objet AccessLog.
// Format : date;heure;utilisateur;action;fichier;resultat
public class AccessLog {
    private final String date;
    private final String heure;
    private final String utilisateur;
    private final String action;
    private final String fichier;
    private final String resultat;

    public AccessLog(String date, String heure, String utilisateur,
                     String action, String fichier, String resultat) {
        this.date = date;
        this.heure = heure;
        this.utilisateur = utilisateur;
        this.action = action;
        this.fichier = fichier;
        this.resultat = resultat;
    }

    // On transforme une ligne texte en objet.
    // Si la ligne est mal formée, on renvoie null.
    public static AccessLog fromLine(String line) {
        if (line == null || line.trim().isEmpty()) {
            return null;
        }
        String[] parts = line.split(";", 6);
        if (parts.length != 6) {
            return null;
        }
        return new AccessLog(parts[0], parts[1], parts[2], parts[3], parts[4], parts[5]);
    }

    public String getDate() {
        return date;
    }

    public String getHeure() {
        return heure;
    }

    public String getUtilisateur() {
        return utilisateur;
    }

    public String getAction() {
        return action;
    }

    public String getFichier() {
        return fichier;
    }

    public String getResultat() {
        return resultat;
    }

    @Override
    public String toString() {
        return date + " " + heure + " | " + utilisateur
                + " | " + action + " | " + fichier + " | " + resultat;
    }
}
