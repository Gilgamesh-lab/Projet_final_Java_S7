-- Dates d'ouverture relatives à l'instant présent pour que les indicateurs
-- d'urgence (vert < 48 h, orange < 72 h, rouge >= 72 h) restent démontrables.

-- Mots de passe hachés en BCrypt : responsable / responsable123, agent / agent123
INSERT INTO utilisateur (identifiant, mot_de_passe, prenom, profil) VALUES
('responsable', '$2a$10$pJ/UJbnSFghLWMaT/Fz9tudkraVSAPHx0QYu5cJXRTemn32QEQfEK', 'Camille', 'RESPONSABLE'),
('agent', '$2a$10$AJHL2F1dWDifWSli0SHko.MuXyhF3YSar2YWKMDoN0qyMlwrwK502', 'Alex', 'AGENT');

INSERT INTO station (identifiant, nom) VALUES
('STA-001', 'Gare Centrale'),
('STA-002', 'Place du Marché'),
('STA-003', 'Université'),
('STA-004', 'Aéroport');

INSERT INTO equipement (reference, libelle, type, etat, station_id) VALUES
('ASC-001', 'Ascenseur quai A', 'Ascenseur', 'EN_PANNE', 1),
('ESC-001', 'Escalator hall principal', 'Escalier mécanique', 'EN_PANNE', 1),
('POR-001', 'Portique entrée Nord', 'Portique', 'EN_PANNE', 2),
('DIS-001', 'Distributeur de titres 1', 'Distributeur', 'EN_PANNE', 2),
('ECR-001', 'Écran d''information quai 1', 'Écran d''information', 'EN_SERVICE', 3),
('ASC-002', 'Ascenseur sortie Sud', 'Ascenseur', 'EN_PANNE', 3),
('DIS-002', 'Distributeur de titres 2', 'Distributeur', 'EN_SERVICE', 4),
('POR-002', 'Portique terminal 1', 'Portique', 'EN_SERVICE', 4);

INSERT INTO prestataire (identifiant, nom, email, telephone) VALUES
('PRE-001', 'Ascenseurs Dupont', 'contact@ascenseurs-dupont.example', '0102030405'),
('PRE-002', 'TechniServices', 'support@techniservices.example', '0198765432'),
('PRE-003', 'MécaTrans', 'interventions@mecatrans.example', '0147258369');

-- Incidents non affectés : cas limites des indicateurs d'urgence
INSERT INTO incident (identifiant, equipement_id, description, date_ouverture, statut, prestataire_id, date_resolution) VALUES
('INC-001', 1, 'Ascenseur bloqué entre deux étages', NOW() - INTERVAL 5 HOUR, 'NOUVEAU', NULL, NULL),
('INC-002', 2, 'Escalator à l''arrêt, bruit anormal', NOW() - INTERVAL 47 HOUR, 'NOUVEAU', NULL, NULL),
('INC-003', 3, 'Portique qui ne s''ouvre plus', NOW() - INTERVAL 48 HOUR, 'NOUVEAU', NULL, NULL),
('INC-004', 4, 'Écran tactile du distributeur inopérant', NOW() - INTERVAL 71 HOUR, 'NOUVEAU', NULL, NULL),
('INC-005', 6, 'Porte de l''ascenseur ne se ferme pas', NOW() - INTERVAL 72 HOUR, 'NOUVEAU', NULL, NULL),
-- Incidents affectés à un prestataire
('INC-006', 1, 'Câble de traction à contrôler', NOW() - INTERVAL 80 HOUR, 'EN_COURS', 1, NULL),
('INC-007', 3, 'Lecteur de titres défaillant', NOW() - INTERVAL 20 HOUR, 'EN_COURS', 2, NULL),
-- Incidents résolus (dates de résolution distinctes pour l'ordre de l'historique)
('INC-008', 5, 'Écran éteint', NOW() - INTERVAL 10 DAY, 'RESOLU', 2, NOW() - INTERVAL 9 DAY),
('INC-009', 7, 'Distributeur sans monnaie rendue', NOW() - INTERVAL 6 DAY, 'RESOLU', 3, NOW() - INTERVAL 4 DAY),
('INC-010', 2, 'Marche bloquée sur l''escalator', NOW() - INTERVAL 3 DAY, 'RESOLU', 1, NOW() - INTERVAL 1 DAY),
-- Équipement fragile : ASC-001 et ASC-002 ont 3 incidents sur 30 jours, DIS-001 et ECR-001 restent sous le seuil
('INC-011', 1, 'Voyant de panne allumé', NOW() - INTERVAL 15 DAY, 'RESOLU', 1, NOW() - INTERVAL 14 DAY),
('INC-012', 6, 'Bouton d''appel inopérant', NOW() - INTERVAL 25 DAY, 'RESOLU', 1, NOW() - INTERVAL 24 DAY),
('INC-013', 6, 'Affichage de l''étage erroné', NOW() - INTERVAL 10 DAY, 'RESOLU', 1, NOW() - INTERVAL 8 DAY),
('INC-014', 4, 'Monnayeur bloqué', NOW() - INTERVAL 20 DAY, 'RESOLU', 2, NOW() - INTERVAL 19 DAY),
('INC-015', 4, 'Imprimante de tickets en panne', NOW() - INTERVAL 30 DAY - INTERVAL 1 MINUTE, 'RESOLU', 2, NOW() - INTERVAL 29 DAY),
('INC-016', 5, 'Écran figé', NOW() - INTERVAL 45 DAY, 'RESOLU', 2, NOW() - INTERVAL 44 DAY),
('INC-017', 5, 'Texte illisible sur l''écran', NOW() - INTERVAL 46 DAY, 'RESOLU', 2, NOW() - INTERVAL 45 DAY);

-- Incident INC-010 volontairement non évalué pour démontrer F10
INSERT INTO evaluation (incident_id, note, commentaire, date_evaluation)
SELECT id, 4, 'Intervention rapide et propre.', NOW() - INTERVAL 8 DAY FROM incident WHERE identifiant = 'INC-008';
INSERT INTO evaluation (incident_id, note, commentaire, date_evaluation)
SELECT id, 2, 'Résolution tardive, a nécessité un second passage.', NOW() - INTERVAL 3 DAY FROM incident WHERE identifiant = 'INC-009';
