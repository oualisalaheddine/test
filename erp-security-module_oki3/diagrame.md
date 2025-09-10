## Table: utilisateurs
- id (bigint, PK)
- compte_actif (boolean)
- compte_non_expire (boolean)
- compte_non_verrouille (boolean)
- credentials_non_expire (boolean)
- date_creation (timestamp)
- derniere_connexion (timestamp)
- email (varchar(100))
- nom (varchar(100))
- password (varchar(255))
- prenom (varchar(100))
- username (varchar(50))

## Table: roles
- id (bigint, PK)
- nom (varchar(50))
- description (varchar(200))
- niveau_hierarchie (integer)
- role_actif (boolean)
- role_parent_id (bigint, FK -> roles.id)

## Table: permissions
- id (bigint, PK)
- description (varchar(200))
- niveau_priorite (integer)
- nom (varchar(100))
- nom_action (varchar(50))
- nom_module (varchar(50))
- permission_actif (boolean)
- url_pattern (varchar(200))

## Table: utilisateur_roles
- utilisateur_id (bigint, FK -> utilisateurs.id, PK)
- role_id (bigint, FK -> roles.id, PK)

## Table: role_permissions
- role_id (bigint, FK -> roles.id, PK)
- permission_id (bigint, FK -> permissions.id, PK)

## Relations
- utilisateurs 1:N utilisateur_roles
- roles 1:N utilisateur_roles
- roles 1:N role_permissions
- permissions 1:N role_permissions
- roles (parent) 1:N roles (enfant)  // Hiérarchie