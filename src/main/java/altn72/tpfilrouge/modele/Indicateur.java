package altn72.tpfilrouge.modele;

public enum Indicateur {
    VERT("Vert"),
    ORANGE("Orange"),
    ROUGE("Rouge"),
    PRIS_EN_CHARGE("Pris en charge");

    private final String libelle;

    Indicateur(String libelle) {
        this.libelle = libelle;
    }

    public String getLibelle() {
        return libelle;
    }
}
