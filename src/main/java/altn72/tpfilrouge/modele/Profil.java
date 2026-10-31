package altn72.tpfilrouge.modele;

public enum Profil {
    RESPONSABLE("RESPONSABLE"),
    AGENT("Agent");

    private final String libelle;

    Profil(String libelle) {
        this.libelle = libelle;
    }

    public String getLibelle() {
        return libelle;
    }
}
