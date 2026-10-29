package altn72.tpfilrouge.modele;

public enum Statut {
    NOUVEAU("Nouveau"),
    EN_COURS("En cours"),
    RESOLU("Résolu");

    private final String libelle;

    Statut(String libelle) {
        this.libelle = libelle;
    }

    public String getLibelle() {
        return libelle;
    }
}
