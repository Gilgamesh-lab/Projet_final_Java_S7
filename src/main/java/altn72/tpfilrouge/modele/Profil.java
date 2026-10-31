package altn72.tpfilrouge.modele;

public enum Profil {
    Responsable("Responsable"),
    AGENT("Agent");

    private final String libelle;

    Profil(String libelle) {
        this.libelle = libelle;
    }

    public String getLibelle() {
        return libelle;
    }
}
