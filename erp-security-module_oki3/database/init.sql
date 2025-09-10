-- Script d'initialisation de la base de données ERP Security
-- PostgreSQL

-- Création de la base de données
CREATE DATABASE erp_security_db;

-- Connexion à la base de données
\c erp_security_db;

-- Création de l'utilisateur (optionnel, pour la production)
-- CREATE USER erp_user WITH PASSWORD 'your_secure_password';
-- GRANT ALL PRIVILEGES ON DATABASE erp_security_db TO erp_user;

-- Extension pour les UUID (optionnel)
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Tables seront créées automatiquement par Hibernate avec spring.jpa.hibernate.ddl-auto=update

-- Insertion de données de test (optionnel, si vous voulez des données de base)

-- Permissions de base pour le module Sécurité
INSERT INTO permissions (nom, description, nom_module, nom_action, url_pattern, permission_actif, niveau_priorite) VALUES
('SECURITE_LIRE', 'Permission pour lire les données de sécurité', 'SECURITE', 'LIRE', '/securite/**', true, 1),
('SECURITE_CREER', 'Permission pour créer des données de sécurité', 'SECURITE', 'CREER', '/securite/**', true, 2),
('SECURITE_MODIFIER', 'Permission pour modifier les données de sécurité', 'SECURITE', 'MODIFIER', '/securite/**', true, 3),
('SECURITE_SUPPRIMER', 'Permission pour supprimer les données de sécurité', 'SECURITE', 'SUPPRIMER', '/securite/**', true, 4),
('SECURITE_ASSIGNER_ROLES', 'Permission pour assigner des rôles', 'SECURITE', 'ASSIGNER_ROLES', '/securite/**', true, 5),
('SECURITE_GESTION_PERMISSIONS', 'Permission pour gérer les permissions', 'SECURITE', 'GESTION_PERMISSIONS', '/securite/**', true, 6);

-- Permissions pour le module Contact
INSERT INTO permissions (nom, description, nom_module, nom_action, url_pattern, permission_actif, niveau_priorite) VALUES
('CONTACT_LIRE', 'Permission pour lire les contacts', 'CONTACT', 'LIRE', '/contact/**', true, 1),
('CONTACT_CREER', 'Permission pour créer des contacts', 'CONTACT', 'CREER', '/contact/**', true, 2),
('CONTACT_MODIFIER', 'Permission pour modifier les contacts', 'CONTACT', 'MODIFIER', '/contact/**', true, 3),
('CONTACT_SUPPRIMER', 'Permission pour supprimer les contacts', 'CONTACT', 'SUPPRIMER', '/contact/**', true, 4),
('CONTACT_EXPORTER', 'Permission pour exporter les contacts', 'CONTACT', 'EXPORTER', '/contact/**', true, 5);

-- Permissions pour le module Comptabilité
INSERT INTO permissions (nom, description, nom_module, nom_action, url_pattern, permission_actif, niveau_priorite) VALUES
('COMPTABILITE_LIRE', 'Permission pour lire les données comptables', 'COMPTABILITE', 'LIRE', '/comptabilite/**', true, 1),
('COMPTABILITE_CREER', 'Permission pour créer des données comptables', 'COMPTABILITE', 'CREER', '/comptabilite/**', true, 2),
('COMPTABILITE_MODIFIER', 'Permission pour modifier les données comptables', 'COMPTABILITE', 'MODIFIER', '/comptabilite/**', true, 3),
('COMPTABILITE_SUPPRIMER', 'Permission pour supprimer les données comptables', 'COMPTABILITE', 'SUPPRIMER', '/comptabilite/**', true, 4),
('COMPTABILITE_VALIDER', 'Permission pour valider les opérations comptables', 'COMPTABILITE', 'VALIDER', '/comptabilite/**', true, 5),
('COMPTABILITE_EXPORTER', 'Permission pour exporter les données comptables', 'COMPTABILITE', 'EXPORTER', '/comptabilite/**', true, 6),
('COMPTABILITE_IMPORTER', 'Permission pour importer des données comptables', 'COMPTABILITE', 'IMPORTER', '/comptabilite/**', true, 7);

-- Permissions pour le module RH
INSERT INTO permissions (nom, description, nom_module, nom_action, url_pattern, permission_actif, niveau_priorite) VALUES
('RH_LIRE', 'Permission pour lire les données RH', 'RH', 'LIRE', '/rh/**', true, 1),
('RH_CREER', 'Permission pour créer des données RH', 'RH', 'CREER', '/rh/**', true, 2),
('RH_MODIFIER', 'Permission pour modifier les données RH', 'RH', 'MODIFIER', '/rh/**', true, 3),
('RH_SUPPRIMER', 'Permission pour supprimer les données RH', 'RH', 'SUPPRIMER', '/rh/**', true, 4),
('RH_VALIDER_CONGE', 'Permission pour valider les congés', 'RH', 'VALIDER_CONGE', '/rh/**', true, 5),
('RH_GESTION_SALAIRE', 'Permission pour gérer les salaires', 'RH', 'GESTION_SALAIRE', '/rh/**', true, 6);

-- Permissions pour le module Stock
INSERT INTO permissions (nom, description, nom_module, nom_action, url_pattern, permission_actif, niveau_priorite) VALUES
('STOCK_LIRE', 'Permission pour lire les données de stock', 'STOCK', 'LIRE', '/stock/**', true, 1),
('STOCK_CREER', 'Permission pour créer des données de stock', 'STOCK', 'CREER', '/stock/**', true, 2),
('STOCK_MODIFIER', 'Permission pour modifier les données de stock', 'STOCK', 'MODIFIER', '/stock/**', true, 3),
('STOCK_SUPPRIMER', 'Permission pour supprimer les données de stock', 'STOCK', 'SUPPRIMER', '/stock/**', true, 4),
('STOCK_ENTREE', 'Permission pour les entrées de stock', 'STOCK', 'ENTREE', '/stock/**', true, 5),
('STOCK_SORTIE', 'Permission pour les sorties de stock', 'STOCK', 'SORTIE', '/stock/**', true, 6),
('STOCK_INVENTAIRE', 'Permission pour les inventaires', 'STOCK', 'INVENTAIRE', '/stock/**', true, 7);

-- Permissions pour le module Vente
INSERT INTO permissions (nom, description, nom_module, nom_action, url_pattern, permission_actif, niveau_priorite) VALUES
('VENTE_LIRE', 'Permission pour lire les données de vente', 'VENTE', 'LIRE', '/vente/**', true, 1),
('VENTE_CREER', 'Permission pour créer des données de vente', 'VENTE', 'CREER', '/vente/**', true, 2),
('VENTE_MODIFIER', 'Permission pour modifier les données de vente', 'VENTE', 'MODIFIER', '/vente/**', true, 3),
('VENTE_SUPPRIMER', 'Permission pour supprimer les données de vente', 'VENTE', 'SUPPRIMER', '/vente/**', true, 4),
('VENTE_VALIDER', 'Permission pour valider les ventes', 'VENTE', 'VALIDER', '/vente/**', true, 5),
('VENTE_FACTURER', 'Permission pour facturer', 'VENTE', 'FACTURER', '/vente/**', true, 6),
('VENTE_REMBOURSER', 'Permission pour les remboursements', 'VENTE', 'REMBOURSER', '/vente/**', true, 7);

-- Permissions pour le module Achat
INSERT INTO permissions (nom, description, nom_module, nom_action, url_pattern, permission_actif, niveau_priorite) VALUES
('ACHAT_LIRE', 'Permission pour lire les données d''achat', 'ACHAT', 'LIRE', '/achat/**', true, 1),
('ACHAT_CREER', 'Permission pour créer des données d''achat', 'ACHAT', 'CREER', '/achat/**', true, 2),
('ACHAT_MODIFIER', 'Permission pour modifier les données d''achat', 'ACHAT', 'MODIFIER', '/achat/**', true, 3),
('ACHAT_SUPPRIMER', 'Permission pour supprimer les données d''achat', 'ACHAT', 'SUPPRIMER', '/achat/**', true, 4),
('ACHAT_VALIDER', 'Permission pour valider les achats', 'ACHAT', 'VALIDER', '/achat/**', true, 5),
('ACHAT_RECEPTIONNER', 'Permission pour réceptionner les achats', 'ACHAT', 'RECEPTIONNER', '/achat/**', true, 6),
('ACHAT_RETOURNER', 'Permission pour les retours d''achat', 'ACHAT', 'RETOURNER', '/achat/**', true, 7);

-- Rôles de base
INSERT INTO roles (nom, description, niveau_hierarchie, role_actif) VALUES
('ADMIN', 'Administrateur système avec tous les droits', 0, true),
('MANAGER', 'Manager avec droits étendus', 1, true),
('USER', 'Utilisateur standard', 2, true),
('READER', 'Lecteur avec droits de consultation uniquement', 3, true);

-- Utilisateur administrateur par défaut (mot de passe: admin123)
INSERT INTO utilisateurs (username, password, nom, prenom, email, date_creation, compte_actif, compte_non_expire, compte_non_verrouille, credentials_non_expire) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDa', 'Administrateur', 'Système', 'admin@erp.com', CURRENT_TIMESTAMP, true, true, true, true);

-- Attribution des permissions au rôle ADMIN
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.nom = 'ADMIN';

-- Attribution du rôle ADMIN à l'utilisateur admin
INSERT INTO utilisateur_roles (utilisateur_id, role_id)
SELECT u.id, r.id
FROM utilisateurs u, roles r
WHERE u.username = 'admin' AND r.nom = 'ADMIN';

-- Attribution des permissions au rôle MANAGER (toutes sauf SUPPRIMER)
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.nom = 'MANAGER' AND p.nom_action != 'SUPPRIMER';

-- Attribution des permissions au rôle USER (LIRE et CREER uniquement)
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.nom = 'USER' AND p.nom_action IN ('LIRE', 'CREER');

-- Attribution des permissions au rôle READER (LIRE uniquement)
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.nom = 'READER' AND p.nom_action = 'LIRE';

-- Affichage des statistiques
SELECT 
    'Permissions créées' as type, COUNT(*) as nombre FROM permissions
UNION ALL
SELECT 
    'Rôles créés' as type, COUNT(*) as nombre FROM roles
UNION ALL
SELECT 
    'Utilisateurs créés' as type, COUNT(*) as nombre FROM utilisateurs;

-- Affichage des permissions par module
SELECT 
    nom_module, 
    COUNT(*) as nombre_permissions,
    STRING_AGG(nom_action, ', ' ORDER BY nom_action) as actions
FROM permissions 
GROUP BY nom_module 
ORDER BY nom_module;

-- Affichage des rôles et leurs permissions
SELECT 
    r.nom as role,
    COUNT(rp.permission_id) as nombre_permissions,
    STRING_AGG(p.nom, ', ' ORDER BY p.nom) as permissions
FROM roles r
LEFT JOIN role_permissions rp ON r.id = rp.role_id
LEFT JOIN permissions p ON rp.permission_id = p.id
GROUP BY r.id, r.nom
ORDER BY r.niveau_hierarchie, r.nom;
