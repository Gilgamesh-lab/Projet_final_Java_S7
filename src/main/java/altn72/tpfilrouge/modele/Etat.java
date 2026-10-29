package altn72.tpfilrouge.modele;

public enum Etat {
    EN_SERVICE("En service"),
    EN_PANNE("En panne"),
    HORS_SERVICE("Hors service");

    private final String libelle;

    Etat(String libelle) {
        this.libelle = libelle;
    }

    public String getLibelle() {
        return libelle;
    }
}
