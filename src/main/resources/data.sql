-- Dates d'ouverture relatives à l'instant présent pour que les indicateurs
-- d'urgence (vert < 48 h, orange < 72 h, rouge >= 72 h) restent démontrables.

-- Mots de passe hachés en BCrypt : responsable / responsable123, agent / agent123
INSERT INTO utilisateur (identifiant, mot_de_passe, prenom, profil) VALUES
('responsable', '$2a$10$pJ/UJbnSFghLWMaT/Fz9tudkraVSAPHx0QYu5cJXRTemn32QEQfEK', 'Camille', 'RESPONSABLE'),
('agent', '$2a$10$AJHL2F1dWDifWSli0SHko.MuXyhF3YSar2YWKMDoN0qyMlwrwK502', 'Alex', 'AGENT');

INSERT INTO station (id, identifiant, nom) VALUES
(1, 'STA-001', 'Gare Centrale'),
(2, 'STA-002', 'Place du Marché'),
(3, 'STA-003', 'Université'),
(4, 'STA-004', 'Aéroport');

INSERT INTO equipement (id, reference, libelle, type, etat, station_id) VALUES
(1, 'ASC-001', 'Ascenseur quai A', 'Ascenseur', 'EN_PANNE', 1),
(2, 'ESC-001', 'Escalator hall principal', 'Escalier mécanique', 'EN_PANNE', 1),
(3, 'POR-001', 'Portique entrée Nord', 'Portique', 'EN_PANNE', 2),
(4, 'DIS-001', 'Distributeur de titres 1', 'Distributeur', 'EN_PANNE', 2),
(5, 'ECR-001', 'Écran d''information quai 1', 'Écran d''information', 'EN_SERVICE', 3),
(6, 'ASC-002', 'Ascenseur sortie Sud', 'Ascenseur', 'EN_PANNE', 3),
(7, 'DIS-002', 'Distributeur de titres 2', 'Distributeur', 'EN_SERVICE', 4),
(8, 'POR-002', 'Portique terminal 1', 'Portique', 'EN_SERVICE', 4);

INSERT INTO prestataire (id, identifiant, nom, email, telephone) VALUES
(1, 'PRE-001', 'Ascenseurs Dupont', 'contact@ascenseurs-dupont.example', '0102030405'),
(2, 'PRE-002', 'TechniServices', 'support@techniservices.example', '0198765432'),
(3, 'PRE-003', 'MécaTrans', 'interventions@mecatrans.example', '0147258369');

-- Incidents non affectés : cas limites des indicateurs d'urgence
INSERT INTO incident (id, identifiant, equipement_id, description, date_ouverture, statut, prestataire_id, date_resolution) VALUES
(1, 'INC-001', 1, 'Ascenseur bloqué entre deux étages', NOW() - INTERVAL 5 HOUR, 'NOUVEAU', NULL, NULL),
(2, 'INC-002', 2, 'Escalator à l''arrêt, bruit anormal', NOW() - INTERVAL 47 HOUR, 'NOUVEAU', NULL, NULL),
(3, 'INC-003', 3, 'Portique qui ne s''ouvre plus', NOW() - INTERVAL 48 HOUR, 'NOUVEAU', NULL, NULL),
(4, 'INC-004', 4, 'Écran tactile du distributeur inopérant', NOW() - INTERVAL 71 HOUR, 'NOUVEAU', NULL, NULL),
(5, 'INC-005', 6, 'Porte de l''ascenseur ne se ferme pas', NOW() - INTERVAL 72 HOUR, 'NOUVEAU', NULL, NULL),
-- Incidents affectés à un prestataire
(6, 'INC-006', 1, 'Câble de traction à contrôler', NOW() - INTERVAL 80 HOUR, 'EN_COURS', 1, NULL),
(7, 'INC-007', 3, 'Lecteur de titres défaillant', NOW() - INTERVAL 20 HOUR, 'EN_COURS', 2, NULL),
-- Incidents résolus (dates de résolution distinctes pour l'ordre de l'historique)
(8, 'INC-008', 5, 'Écran éteint', NOW() - INTERVAL 10 DAY, 'RESOLU', 2, NOW() - INTERVAL 9 DAY),
(9, 'INC-009', 7, 'Distributeur sans monnaie rendue', NOW() - INTERVAL 6 DAY, 'RESOLU', 3, NOW() - INTERVAL 4 DAY),
(10, 'INC-010', 2, 'Marche bloquée sur l''escalator', NOW() - INTERVAL 3 DAY, 'RESOLU', 1, NOW() - INTERVAL 1 DAY);

-- Incident INC-010 volontairement non évalué pour démontrer F10
INSERT INTO evaluation (incident_id, note, commentaire, date_evaluation) VALUES
(8, 4, 'Intervention rapide et propre.', NOW() - INTERVAL 8 DAY),
(9, 2, 'Résolution tardive, a nécessité un second passage.', NOW() - INTERVAL 3 DAY);
