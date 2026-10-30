package altn72.tpfilrouge.modele;

public enum Profil {
    Responsable_exploitation("Responsable d'exploitation"),
    AGENT("Agent");

    private final String libelle;

    Profil(String libelle) {
        this.libelle = libelle;
    }

    public String getLibelle() {
        return libelle;
    }
}
