# ERP Security Module

Un module de sécurité complet pour ERP web avec gestion des utilisateurs, rôles et permissions basé sur Spring Boot, Spring Security et PostgreSQL.

## 🚀 Fonctionnalités

### ✅ Gestion des Utilisateurs
- Création, modification, suppression d'utilisateurs
- Activation/désactivation de comptes
- Gestion des informations personnelles (nom, prénom, email)
- Suivi des connexions et dates de création

### ✅ Système de Rôles Hiérarchique
- Rôles avec niveaux hiérarchiques
- Héritage des permissions des rôles parents
- Gestion des rôles enfants et parents
- Activation/désactivation de rôles

### ✅ Permissions Granulaires
- Permissions par module (Sécurité, Contact, Comptabilité, RH, Stock, Vente, Achat)
- Permissions par action (LIRE, CREER, MODIFIER, SUPPRIMER, etc.)
- Gestion des patterns d'URL
- Priorités et niveaux de permissions

### ✅ Interface Web Moderne
- Interface responsive avec Bootstrap 5
- Navigation sécurisée par permissions
- Dashboard avec statistiques
- Formulaires de gestion avec validation

### ✅ Sécurité Avancée
- Authentification par formulaire
- Encodage des mots de passe avec BCrypt
- Sessions sécurisées
- Protection CSRF
- Gestion des accès refusés

## 🛠️ Technologies Utilisées

- **Backend**: Spring Boot 3.2.0
- **Sécurité**: Spring Security 6
- **Base de données**: PostgreSQL
- **ORM**: Spring Data JPA / Hibernate
- **Frontend**: Thymeleaf + Bootstrap 5
- **Build**: Maven
- **Java**: 17+

## 📋 Prérequis

- Java 17 ou supérieur
- PostgreSQL 12 ou supérieur
- Maven 3.6 ou supérieur

## 🔧 Installation

### 1. Cloner le projet
```bash
git clone <repository-url>
cd erp-security-module
```

### 2. Configuration de la base de données

Créer une base de données PostgreSQL :
```sql
CREATE DATABASE erp_security_db;
CREATE USER erp_user WITH PASSWORD 'your_password';
GRANT ALL PRIVILEGES ON DATABASE erp_security_db TO erp_user;
```

### 3. Configuration de l'application

Modifier le fichier `src/main/resources/application.properties` :
```properties
# Configuration de la base de données PostgreSQL
spring.datasource.url=jdbc:postgresql://localhost:5432/erp_security_db
spring.datasource.username=erp_user
spring.datasource.password=your_password
spring.datasource.driver-class-name=org.postgresql.Driver

# Configuration JPA/Hibernate
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
```

### 4. Compilation et lancement
```bash
mvn clean install
mvn spring-boot:run
```

L'application sera accessible à l'adresse : `http://localhost:8080/erp`

## 🧪 Initialisation des Données de Test

### Accès à la page de test
1. Connectez-vous avec un compte administrateur
2. Allez dans le menu "Sécurité" > "Test Data"
3. Cliquez sur "Initialiser Toutes les Données"

### Comptes de test créés

| Rôle | Login | Mot de passe | Permissions |
|------|-------|--------------|-------------|
| **Administrateur** | admin | admin123 | Toutes les permissions |
| **Manager** | manager | manager123 | Lecture, Création, Modification |
| **Utilisateur** | user | user123 | Lecture, Création |
| **Lecteur** | reader | reader123 | Lecture uniquement |

## 📁 Structure du Projet

```
src/main/java/com/sh/erpcos/
├── ErpSecurityApplication.java
├── univers/securite/
│   ├── config/
│   │   └── SecurityConfig.java
│   ├── controller/
│   │   ├── MainController.java
│   │   ├── UtilisateurController.java
│   │   └── TestDataController.java
│   ├── entity/
│   │   ├── Utilisateur.java
│   │   ├── Role.java
│   │   └── Permission.java
│   ├── repository/
│   │   ├── UtilisateurRepository.java
│   │   ├── RoleRepository.java
│   │   └── PermissionRepository.java
│   └── service/
│       ├── CustomUserDetailsService.java
│       ├── UtilisateurService.java
│       ├── RoleService.java
│       └── PermissionService.java
└── module/
    └── contact/
        └── (modules futurs)

src/main/resources/
├── templates/
│   ├── login.html
│   ├── dashboard.html
│   ├── layout/
│   │   └── base.html
│   └── securite/
│       └── test-data.html
└── application.properties
```

## 🔐 Modules et Permissions

### Modules Disponibles
- **Sécurité** (`SECURITE_*`) : Gestion des utilisateurs, rôles, permissions
- **Contact** (`CONTACT_*`) : Gestion des contacts et clients
- **Comptabilité** (`COMPTABILITE_*`) : Gestion comptable et financière
- **RH** (`RH_*`) : Ressources Humaines
- **Stock** (`STOCK_*`) : Gestion des stocks et inventaires
- **Vente** (`VENTE_*`) : Gestion des ventes et facturation
- **Achat** (`ACHAT_*`) : Gestion des achats et fournisseurs

### Actions Disponibles
- **LIRE** : Consultation des données
- **CREER** : Création de nouvelles entrées
- **MODIFIER** : Modification des données existantes
- **SUPPRIMER** : Suppression de données
- **VALIDER** : Validation d'opérations
- **EXPORTER** : Export de données
- **IMPORTER** : Import de données

## 🎯 Utilisation

### 1. Connexion
- Accédez à `http://localhost:8080/erp/login`
- Utilisez un des comptes de test créés

### 2. Navigation
- Le menu latéral affiche uniquement les modules auxquels vous avez accès
- Les permissions sont vérifiées à chaque page

### 3. Gestion des Utilisateurs
- Menu "Sécurité" > "Utilisateurs"
- Création, modification, suppression d'utilisateurs
- Attribution de rôles aux utilisateurs

### 4. Gestion des Rôles
- Menu "Sécurité" > "Rôles"
- Création de rôles hiérarchiques
- Attribution de permissions aux rôles

### 5. Gestion des Permissions
- Menu "Sécurité" > "Permissions"
- Visualisation et gestion des permissions par module

## 🔧 Configuration Avancée

### Personnalisation des Permissions
Pour ajouter de nouvelles permissions, modifiez le service `PermissionService` :

```java
public void creerPermissionsModulePersonnalise(String nomModule, String... actions) {
    creerPermissionsModule(nomModule, actions);
}
```

### Ajout de Nouveaux Modules
1. Créez le package `com.sh.erpcos.module.votre_module`
2. Ajoutez les permissions dans `PermissionService`
3. Configurez les URLs dans `SecurityConfig`
4. Créez les contrôleurs et vues correspondants

### Personnalisation de l'Interface
- Modifiez les templates Thymeleaf dans `src/main/resources/templates/`
- Personnalisez les styles CSS dans les templates
- Ajoutez des icônes Bootstrap ou personnalisées

## 🚨 Sécurité

### Bonnes Pratiques
- Changez les mots de passe par défaut en production
- Utilisez HTTPS en production
- Configurez des logs de sécurité appropriés
- Effectuez des audits réguliers des permissions

### Configuration de Production
```properties
# Désactiver les logs SQL
spring.jpa.show-sql=false

# Activer HTTPS
server.ssl.enabled=true
server.ssl.key-store=classpath:keystore.p12
server.ssl.key-store-password=your_password

# Configuration des sessions
server.servlet.session.timeout=30m
```

## 🐛 Dépannage

### Problèmes Courants

1. **Erreur de connexion à la base de données**
   - Vérifiez les paramètres de connexion PostgreSQL
   - Assurez-vous que la base de données existe

2. **Erreur 403 - Accès refusé**
   - Vérifiez que l'utilisateur a les permissions nécessaires
   - Consultez les logs pour plus de détails

3. **Problèmes de compilation**
   - Vérifiez que Java 17+ est installé
   - Exécutez `mvn clean install`

### Logs
Les logs sont configurés pour afficher les informations de sécurité :
```properties
logging.level.com.sh.erpcos=DEBUG
logging.level.org.springframework.security=DEBUG
```

## 📞 Support

Pour toute question ou problème :
1. Consultez les logs de l'application
2. Vérifiez la configuration de la base de données
3. Testez avec les comptes de test fournis

## 📄 Licence

Ce projet est sous licence MIT. Voir le fichier LICENSE pour plus de détails.

---

**ERP Security Module** - Un système de sécurité robuste et flexible pour vos applications ERP.
