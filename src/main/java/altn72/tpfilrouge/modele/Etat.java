package altn72.tpfilrouge.modele;

public enum Etat {
    EN_SERVICE("En_service"),
    EN_PANNE("En_panne"),
    HORS_SERVICE("Hors_service");

    private final String libelle;

    Etat(String libelle) {
        this.libelle = libelle;
    }

    public String getLibelle() {
        return libelle;
    }
}
