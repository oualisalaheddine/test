# ERP Security Module

Un module de sécurité complet et avancé pour système ERP, développé avec Spring Boot, Spring Security et PostgreSQL. Ce module fournit une gestion complète de la sécurité avec authentification, autorisation, audit, gestion des sessions et bien plus.

## 🚀 Fonctionnalités Principales

### ✅ Gestion des Utilisateurs
- **CRUD complet** : Création, lecture, mise à jour, suppression d'utilisateurs
- **Gestion des statuts** : Activation/désactivation, verrouillage, expiration des comptes
- **Recherche avancée** : Filtrage par nom, email, rôle, statut, date de création
- **Pagination** : Interface paginée pour de grandes quantités d'utilisateurs
- **Statistiques** : Tableaux de bord avec métriques utilisateurs

### ✅ Système de Rôles Hiérarchique
- **Hiérarchie flexible** : Rôles parents/enfants avec héritage des permissions
- **Niveaux hiérarchiques** : Organisation structurée des rôles
- **Gestion dynamique** : Création, modification, suppression de rôles
- **Attribution automatique** : Héritage des permissions des rôles parents

### ✅ Permissions Granulaires
- **Permissions par module** : Sécurité, Contact, Comptabilité, RH, Stock, Vente, Achat, Pont Bascule
- **Actions détaillées** : LIRE, CREER, MODIFIER, SUPPRIMER, VALIDER, EXPORTER, IMPORTER
- **Patterns d'URL** : Contrôle d'accès basé sur les URLs
- **Priorités** : Gestion des niveaux de priorité des permissions

### ✅ Audit et Traçabilité Complets
- **Logging automatique** : Toutes les actions sont enregistrées
- **Catégorisation** : Authentication, Authorization, Data Access, Security Events, etc.
- **Niveaux de gravité** : INFO, WARNING, ERROR, CRITICAL
- **Recherche avancée** : Filtrage par utilisateur, action, période, niveau
- **Détection d'activités suspectes** : Alertes automatiques
- **Rapports de sécurité** : Génération de rapports détaillés
- **Export de données** : Export des logs pour analyse externe

### ✅ Gestion Avancée des Sessions
- **Suivi en temps réel** : Sessions actives, utilisateurs connectés
- **Informations détaillées** : IP, navigateur, OS, durée, localisation
- **Contrôle administratif** : Terminaison forcée, prolongation de sessions
- **Détection d'anomalies** : Sessions suspectes, connexions multiples
- **Statistiques** : Rapports d'utilisation, sessions les plus longues
- **Nettoyage automatique** : Suppression des sessions expirées

### ✅ Politiques de Mots de Passe Sophistiquées
- **R��gles configurables** : Longueur, complexité, caractères spéciaux
- **Validation avancée** : Anti-dictionnaire, anti-informations personnelles
- **Historique** : Empêcher la réutilisation des anciens mots de passe
- **Expiration** : Renouvellement obligatoire périodique
- **Test en temps réel** : Validation instantanée lors de la saisie
- **Politiques multiples** : Différentes règles selon les rôles

### ✅ Authentification à Deux Facteurs (2FA)
- **Méthodes multiples** : TOTP (Google Authenticator), SMS, Email
- **Codes de récupération** : Codes de sauvegarde en cas de perte
- **Gestion des échecs** : Verrouillage temporaire après tentatives échouées
- **Configuration flexible** : Activation/désactivation par utilisateur
- **Statistiques 2FA** : Adoption, utilisation, incidents

### ✅ Sauvegarde et Restauration
- **Sauvegarde complète** : Tous les données de sécurité
- **Sauvegarde partielle** : Utilisateurs et rôles uniquement
- **Sauvegarde des logs** : Archivage des journaux d'audit
- **Restauration sélective** : Choix des éléments à restaurer
- **Format sécurisé** : Chiffrement des données sensibles
- **Métadonnées** : Informations sur les sauvegardes

### ✅ Interface Web Moderne
- **Design responsive** : Compatible mobile et desktop
- **Bootstrap 5** : Interface moderne et intuitive
- **Navigation contextuelle** : Menus adaptés aux permissions
- **Tableaux de bord** : Vues d'ensemble avec métriques
- **Formulaires intelligents** : Validation côté client et serveur
- **Notifications** : Messages de succès/erreur contextuels

## 🛠️ Architecture Technique

### Technologies Utilisées
- **Backend** : Spring Boot 3.5.4, Java 17+
- **Sécurité** : Spring Security 6 avec authentification personnalisée
- **Base de données** : PostgreSQL avec Spring Data JPA
- **ORM** : Hibernate avec requêtes JPA (pas de SQL natif)
- **Frontend** : Thymeleaf, Bootstrap 5, JavaScript
- **Build** : Maven 3.6+
- **Validation** : Bean Validation (JSR-303)
- **Logging** : SLF4J avec Logback

### Pattern MVC Classique
```
Controller → Service → Repository → Entity
     ↓         ↓         ↓         ↓
   Web      Business   Data     Database
  Layer     Logic     Access     Layer
```

### Structure du Projet
```
src/main/java/com/sh/erpcos/
├── ErpSecurityApplication.java          # Application principale
├── univers/securite/                    # Module de sécurité
│   ├── config/
│   │   └── SecurityConfig.java          # Configuration Spring Security
│   ├── controller/                      # Contrôleurs MVC
│   │   ├── MainController.java
│   │   ├── UtilisateurController.java
│   │   ├── RoleController.java
│   │   ├── PermissionController.java
│   │   ├── AuditController.java
│   │   ├── SessionController.java
│   │   ├── PasswordPolicyController.java
│   │   └── SecuriteController.java
│   ├── entity/                          # Entités JPA
│   │   ├── Utilisateur.java
│   │   ├── Role.java
│   │   ├── Permission.java
│   │   ├── AuditLog.java
│   │   ├── UserSession.java
│   │   ├── PasswordPolicy.java
│   │   └── TwoFactorAuth.java
│   ├── repository/                      # Repositories JPA
│   │   ├── UtilisateurRepository.java
│   │   ├── RoleRepository.java
│   │   ├── PermissionRepository.java
│   │   ├── AuditLogRepository.java
│   │   ├── UserSessionRepository.java
│   │   ├── PasswordPolicyRepository.java
│   │   └── TwoFactorAuthRepository.java
│   ├── service/                         # Services métier
│   │   ├── UtilisateurService.java
│   │   ├── RoleService.java
│   │   ├── PermissionService.java
│   │   ├── AuditService.java
│   │   ├── SessionService.java
│   │   ├── PasswordPolicyService.java
│   │   ├── TwoFactorAuthService.java
│   │   ├── BackupRestoreService.java
│   │   └── CustomUserDetailsService.java
│   └── util/                           # Utilitaires
└── module/                             # Autres modules ERP
    ├── pontbascule/                    # Module pont bascule (existant)
    └── contact/                        # Module contact (futur)
```

## 📋 Prérequis

- **Java 17** ou supérieur
- **PostgreSQL 12** ou supérieur  
- **Maven 3.6** ou supérieur
- **Navigateur moderne** (Chrome, Firefox, Safari, Edge)

## 🔧 Installation et Configuration

### 1. Cloner le Projet
```bash
git clone <repository-url>
cd erp-security-module
```

### 2. Configuration de la Base de Données

#### Créer la base de données PostgreSQL :
```sql
CREATE DATABASE erpcos;
CREATE USER erp_user WITH PASSWORD 'your_secure_password';
GRANT ALL PRIVILEGES ON DATABASE erpcos TO erp_user;
```

#### Configurer application.properties :
```properties
# Configuration PostgreSQL
spring.datasource.url=jdbc:postgresql://localhost:5432/erpcos
spring.datasource.username=erp_user
spring.datasource.password=your_secure_password
spring.datasource.driver-class-name=org.postgresql.Driver

# Configuration JPA/Hibernate
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect

# Configuration Thymeleaf
spring.thymeleaf.cache=false
spring.thymeleaf.prefix=classpath:/templates/
spring.thymeleaf.suffix=.html

# Configuration du serveur
server.port=8088
server.servlet.context-path=/erp

# Configuration des logs
logging.level.com.sh.erpcos=INFO
logging.level.org.springframework.security=WARN
```

### 3. Compilation et Lancement
```bash
# Compilation
mvn clean compile

# Tests (optionnel)
mvn test

# Lancement
mvn spring-boot:run
```

L'application sera accessible à : `http://localhost:8088/erp`

## 🎯 Utilisation

### Première Connexion

1. **Accéder à l'application** : `http://localhost:8088/erp/login`
2. **Compte administrateur par défaut** :
   - Login : `admin`
   - Mot de passe : `admin123`

⚠️ **Important** : Changez immédiatement le mot de passe par défaut !

### Initialisation des Données

1. Connectez-vous avec le compte administrateur
2. Allez dans **Sécurité** > **Données de Test**
3. Cliquez sur **"Initialiser Toutes les Données"**

Cela créera :
- Les permissions pour tous les modules
- Les rôles de base (Admin, Manager, User, Reader)
- Des utilisateurs de test
- Une politique de mot de passe par défaut

### Comptes de Test Créés

| Rôle | Login | Mot de passe | Permissions |
|------|-------|--------------|-------------|
| **Administrateur** | admin | admin123 | Toutes les permissions |
| **Manager** | manager | manager123 | Lecture, Création, Modification |
| **Utilisateur** | user | user123 | Lecture, Création |
| **Lecteur** | reader | reader123 | Lecture uniquement |

## 🔐 Modules et Permissions

### Modules Disponibles
- **SECURITE** : Gestion des utilisateurs, rôles, permissions, audit
- **PONTBASCULE** : Gestion des ponts bascules (module existant)
- **CONTACT** : Gestion des contacts et clients
- **COMPTABILITE** : Gestion comptable et financière
- **RH** : Ressources Humaines
- **STOCK** : Gestion des stocks et inventaires
- **VENTE** : Gestion des ventes et facturation
- **ACHAT** : Gestion des achats et fournisseurs

### Actions Disponibles
- **LIRE** : Consultation des données
- **CREER** : Création de nouvelles entrées
- **MODIFIER** : Modification des données existantes
- **SUPPRIMER** : Suppression de données
- **VALIDER** : Validation d'opérations
- **EXPORTER** : Export de données
- **IMPORTER** : Import de données
- **ASSIGNER_ROLES** : Attribution de rôles (sécurité)
- **GESTION_PERMISSIONS** : Gestion des permissions (sécurité)

## 📊 Fonctionnalités Avancées

### Audit et Monitoring
- **Logs en temps réel** : Toutes les actions sont tracées
- **Tableaux de bord** : Métriques de sécurité en temps réel
- **Alertes automatiques** : Détection d'activités suspectes
- **Rapports périodiques** : Génération automatique de rapports

### Gestion des Sessions
- **Monitoring actif** : Vue en temps réel des sessions
- **Contrôle administratif** : Terminaison forcée de sessions
- **Détection d'anomalies** : Connexions multiples, IPs suspectes
- **Statistiques d'usage** : Rapports d'utilisation détaillés

### Sécurité Avancée
- **Politiques de mots de passe** : Règles configurables et strictes
- **2FA optionnel** : Authentification à deux facteurs
- **Verrouillage automatique** : Protection contre les attaques par force brute
- **Chiffrement** : Protection des données sensibles

## 🔧 Configuration Avancée

### Personnalisation des Permissions
```java
// Ajouter de nouvelles permissions
@Service
public class CustomPermissionService {
    
    public void creerPermissionsModulePersonnalise(String nomModule, String... actions) {
        for (String action : actions) {
            Permission permission = new Permission(
                Permission.genererNomPermission(nomModule, action),
                "Permission " + action + " pour " + nomModule,
                nomModule,
                action
            );
            permissionRepository.save(permission);
        }
    }
}
```

### Ajout de Nouveaux Modules
1. Créer le package `com.sh.erpcos.module.votre_module`
2. Ajouter les permissions dans `PermissionService`
3. Configurer les URLs dans `SecurityConfig`
4. Créer les contrôleurs et vues correspondants

### Configuration de Production

```properties
# Désactiver les logs de développement
spring.jpa.show-sql=false
logging.level.com.sh.erpcos=WARN

# Configuration HTTPS
server.ssl.enabled=true
server.ssl.key-store=classpath:keystore.p12
server.ssl.key-store-password=your_keystore_password

# Configuration des sessions
server.servlet.session.timeout=30m
server.servlet.session.cookie.secure=true
server.servlet.session.cookie.http-only=true

# Configuration de la base de données pour la production
spring.datasource.hikari.maximum-pool-size=20
spring.datasource.hikari.minimum-idle=5
```

## 🚨 Sécurité et Bonnes Pratiques

### Recommandations de Sécurité
1. **Changez tous les mots de passe par défaut** immédiatement
2. **Activez HTTPS** en production
3. **Configurez des politiques de mots de passe strictes**
4. **Activez le 2FA** pour les comptes administrateurs
5. **Surveillez les logs d'audit** régulièrement
6. **Effectuez des sauvegardes** périodiques
7. **Mettez à jour** les dépendances régulièrement

### Monitoring et Maintenance
- **Logs d'audit** : Vérifiez quotidiennement les activités suspectes
- **Sessions actives** : Surveillez les connexions anormales
- **Performances** : Surveillez l'utilisation des ressources
- **Sauvegardes** : Testez régulièrement les procédures de restauration

## 🐛 Dépannage

### Problèmes Courants

#### 1. Erreur de connexion à la base de données
```
Caused by: org.postgresql.util.PSQLException: Connection refused
```
**Solution** :
- Vérifiez que PostgreSQL est démarré
- Vérifiez les paramètres de connexion dans `application.properties`
- Vérifiez que la base de données `erpcos` existe

#### 2. Erreur 403 - Accès refusé
```
Access Denied: User does not have required permission
```
**Solution** :
- Vérifiez que l'utilisateur a les permissions nécessaires
- Consultez les logs d'audit pour plus de détails
- Vérifiez la configuration des rôles et permissions

#### 3. Sessions qui expirent trop rapidement
**Solution** :
```properties
# Augmenter la durée des sessions
server.servlet.session.timeout=60m
```

#### 4. Problèmes de performance
**Solution** :
- Activez la pagination sur les grandes listes
- Optimisez les requêtes JPA
- Augmentez la taille du pool de connexions

### Logs et Debugging

#### Activer les logs détaillés :
```properties
logging.level.com.sh.erpcos=DEBUG
logging.level.org.springframework.security=DEBUG
logging.level.org.hibernate.SQL=DEBUG
```

#### Consulter les logs d'audit :
- Interface web : `/erp/securite/audit`
- Base de données : table `audit_logs`
- Fichiers de logs : `logs/application.log`

## 📈 Évolutions Futures

### Fonctionnalités Prévues
- **API REST** : Exposition des services via API
- **Intégration LDAP/AD** : Authentification centralisée
- **SSO (Single Sign-On)** : Authentification unique
- **Notifications push** : Alertes en temps réel
- **Rapports avancés** : Tableaux de bord personnalisables
- **Mobile App** : Application mobile dédiée

### Modules ERP Additionnels
- **Gestion de projet** : Suivi des projets et tâches
- **CRM** : Gestion de la relation client
- **Production** : Gestion de la production industrielle
- **Maintenance** : Gestion de la maintenance préventive
- **Qualité** : Gestion de la qualité et conformité

## 📞 Support et Contribution

### Obtenir de l'Aide
1. **Documentation** : Consultez ce README et les commentaires du code
2. **Logs d'audit** : Vérifiez les logs pour diagnostiquer les problèmes
3. **Tests** : Utilisez les comptes de test pour reproduire les problèmes

### Contribuer au Projet
1. **Fork** le projet
2. **Créez une branche** pour votre fonctionnalité
3. **Committez** vos changements
4. **Testez** votre code
5. **Soumettez une Pull Request**

### Standards de Code
- **Java** : Suivre les conventions Oracle
- **Spring** : Utiliser les annotations et bonnes pratiques Spring
- **JPA** : Pas de SQL natif, uniquement des requêtes JPA
- **Tests** : Couvrir les nouvelles fonctionnalités
- **Documentation** : Commenter le code complexe

## 📄 Licence

Ce projet est sous licence MIT. Voir le fichier `LICENSE` pour plus de détails.

---

**ERP Security Module** - Un système de sécurité robuste, flexible et complet pour vos applications ERP d'entreprise.

*Développé avec ❤️ en Java et Spring Boot*