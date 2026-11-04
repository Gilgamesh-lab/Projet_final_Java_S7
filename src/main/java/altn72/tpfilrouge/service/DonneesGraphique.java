package altn72.tpfilrouge.service;

import java.util.List;

public class DonneesGraphique {

    private final List<String> libelles;
    private final List<Number> valeurs;

    public DonneesGraphique(List<String> libelles, List<Number> valeurs) {
        this.libelles = libelles;
        this.valeurs = valeurs;
    }

    public List<String> getLibelles() {
        return libelles;
    }

    public List<Number> getValeurs() {
        return valeurs;
    }

    public boolean isVide() {
        return libelles.isEmpty();
    }
}
