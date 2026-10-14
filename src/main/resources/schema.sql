DROP TABLE IF EXISTS evaluation;
DROP TABLE IF EXISTS incident;
DROP TABLE IF EXISTS prestataire;
DROP TABLE IF EXISTS equipement;
DROP TABLE IF EXISTS station;
DROP TABLE IF EXISTS utilisateur;

CREATE TABLE station (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    identifiant VARCHAR(100) NOT NULL UNIQUE,
    nom VARCHAR(100) NOT NULL
);

CREATE TABLE equipement (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    reference VARCHAR(100) NOT NULL UNIQUE,
    libelle VARCHAR(255) NOT NULL,
    type VARCHAR(50) NOT NULL,
    etat ENUM('En service', 'En panne', 'Hors service') NOT NULL DEFAULT 'En service',
    station_id BIGINT UNSIGNED NOT NULL,
    FOREIGN KEY (station_id) REFERENCES station(id) ON DELETE RESTRICT
);

CREATE TABLE prestataire (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    identifiant VARCHAR(100) NOT NULL UNIQUE,
    nom VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL,
    telephone VARCHAR(20) NOT NULL
);

CREATE TABLE incident (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    identifiant VARCHAR(100) NOT NULL UNIQUE,
    equipement_id BIGINT UNSIGNED NOT NULL,
    description TEXT NOT NULL,
    date_ouverture DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    statut ENUM('Nouveau', 'En cours', 'Résolu') NOT NULL DEFAULT 'Nouveau',
    prestataire_id BIGINT UNSIGNED DEFAULT NULL,
    date_resolution DATETIME DEFAULT NULL,
    FOREIGN KEY (equipement_id) REFERENCES equipement(id) ON DELETE RESTRICT,
    FOREIGN KEY (prestataire_id) REFERENCES prestataire(id) ON DELETE RESTRICT,
    CONSTRAINT ck_incident_resolution_apres_ouverture
        CHECK (date_resolution IS NULL OR date_resolution >= date_ouverture)
);

CREATE TABLE evaluation (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    incident_id BIGINT UNSIGNED NOT NULL UNIQUE,
    note INT NOT NULL,
    commentaire TEXT DEFAULT NULL,
    date_evaluation DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (incident_id) REFERENCES incident(id) ON DELETE CASCADE,
    CONSTRAINT ck_evaluation_note CHECK (note BETWEEN 1 AND 5)
);

CREATE TABLE utilisateur (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    identifiant VARCHAR(100) NOT NULL UNIQUE,
    mot_de_passe VARCHAR(255) NOT NULL,
    prenom VARCHAR(100) NOT NULL,
    profil ENUM('Responsable d''exploitation', 'Agent') NOT NULL
);
