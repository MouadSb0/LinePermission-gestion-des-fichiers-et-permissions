package ma.youcode.lineperm.model;

public class FichierProtege {
    private int id;
    private String nom;
    private String chemin;
    private int proprietaireId;
    private String rights;

    public FichierProtege(
            String nom,
            String chemin,
            int proprietaireId,
            String rights
    ) {
        this(0, nom, chemin, proprietaireId, rights);
    }

    public FichierProtege(
            int id,
            String nom,
            String chemin,
            int proprietaireId,
            String rights
    ) {
        this.id = id;
        this.nom = nom;
        this.chemin = chemin;
        this.proprietaireId = proprietaireId;
        this.rights = rights;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getChemin() {
        return chemin;
    }

    public void setChemin(String chemin) {
        this.chemin = chemin;
    }

    public int getProprietaireId() {
        return proprietaireId;
    }

    public void setProprietaireId(int proprietaireId) {
        this.proprietaireId = proprietaireId;
    }

    public String getRights() {
        return rights;
    }

    public void setRights(String rights) {
        this.rights = rights;
    }
}
