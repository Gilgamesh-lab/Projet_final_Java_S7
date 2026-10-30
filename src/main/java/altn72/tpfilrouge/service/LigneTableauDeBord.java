package altn72.tpfilrouge.service;

import altn72.tpfilrouge.modele.Incident;
import altn72.tpfilrouge.modele.Indicateur;

public class LigneTableauDeBord {

    private final Incident incident;
    private final Indicateur indicateur;
    private final long heuresDepuisOuverture;

    public LigneTableauDeBord(Incident incident, Indicateur indicateur, long heuresDepuisOuverture) {
        this.incident = incident;
        this.indicateur = indicateur;
        this.heuresDepuisOuverture = heuresDepuisOuverture;
    }

    public Incident getIncident() {
        return incident;
    }

    public Indicateur getIndicateur() {
        return indicateur;
    }

    public long getHeuresDepuisOuverture() {
        return heuresDepuisOuverture;
    }
}
