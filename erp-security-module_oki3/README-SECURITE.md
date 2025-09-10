# Module Univers\Sécurité - ERP Security Module

## 📋 Table des Matières

1. [Vue d'ensemble](#vue-densemble)
2. [Fonctionnalités](#fonctionnalités)
3. [Architecture](#architecture)
4. [Installation](#installation)
5. [Configuration](#configuration)
6. [Utilisation](#utilisation)
7. [API et Services](#api-et-services)
8. [Sécurité](#sécurité)
9. [Maintenance](#maintenance)
10. [Dépannage](#dépannage)

## 🎯 Vue d'ensemble

Le module Univers\Sécurité est un système complet de gestion de la sécurité pour les applications ERP. Il fournit une solution robuste pour la gestion des utilisateurs, des rôles, des permissions, l'audit, les sessions, et bien plus encore.

### Caractéristiques principales

- ✅ **Gestion complète des utilisateurs** (CRUD, activation/désactivation)
- ✅ **Système de rôles et permissions** granulaire
- ✅ **Audit et journalisation** de toutes les actions
- ✅ **Gestion des sessions** utilisateurs
- ✅ **Politique de mots de passe** configurable
- ✅ **Authentification à deux facteurs (2FA)**
- ✅ **Sauvegarde et restauration** des données
- ✅ **Tableau de bord** avec métriques et alertes
- ✅ **Interface moderne** avec Bootstrap 5

## 🚀 Fonctionnalités

### 1. Gestion des Utilisateurs
- Création, modification, suppression d'utilisateurs
- Activation/désactivation des comptes
- Gestion des informations personnelles
- Assignation de rôles multiples
- Recherche et filtrage avancés

### 2. Gestion des Rôles
- Création et gestion de rôles hiérarchiques
- Assignation de permissions aux rôles
- Gestion des rôles parents/enfants
- Activation/désactivation des rôles

### 3. Gestion des Permissions
- Permissions granulaires par module et action
- Génération automatique de permissions
- Organisation par modules (SECURITE, RH, STOCK, etc.)
- Validation des permissions en temps réel

### 4. Système d'Audit
- Journalisation de toutes les actions utilisateurs
- Catégorisation des événements (AUTHENTICATION, AUTHORIZATION, etc.)
- Niveaux d'audit (INFO, WARNING, ERROR, CRITICAL)
- Recherche et filtrage des logs
- Statistiques d'activité

### 5. Gestion des Sessions
- Suivi des sessions actives
- Détection de sessions multiples
- Gestion des sessions expirées
- Nettoyage automatique
- Détection d'activité suspecte

### 6. Politique de Mots de Passe
- Configuration flexible des exigences
- Validation en temps réel
- Historique des mots de passe
- Verrouillage après tentatives échouées
- Politiques prédéfinies (Basique, Standard, Haute Sécurité)

### 7. Authentification à Deux Facteurs (2FA)
- Support TOTP (Google Authenticator)
- Codes de secours
- Méthodes alternatives (SMS, Email)
- Gestion des comptes verrouillés
- Statistiques d'utilisation

### 8. Sauvegarde et Restauration
- Sauvegarde complète ou sélective
- Format ZIP avec métadonnées
- Restauration avec options de remplacement
- Gestion des versions de sauvegarde
- Nettoyage automatique des anciennes sauvegardes

### 9. Tableau de Bord de Sécurité
- Métriques en temps réel
- Alertes de sécurité automatiques
- Activité récente
- Statistiques par période
- Interface responsive et moderne

## 🏗️ Architecture

### Structure du Projet

```
src/main/java/com/sh/erpcos/univers/securite/
├── config/                 # Configuration Spring Security
├── controller/             # Contrôleurs MVC
├── entity/                 # Entités JPA
├── repository/             # Repositories Spring Data
├── service/                # Services métier
└── util/                   # Utilitaires

src/main/resources/templates/securite/
├── dashboard.html          # Tableau de bord principal
├── utilisateurs/           # Templates utilisateurs
├── roles/                  # Templates rôles
├── permissions/            # Templates permissions
└── ...
```

### Entités Principales

- **Utilisateur** : Gestion des comptes utilisateurs
- **Role** : Définition des rôles avec hiérarchie
- **Permission** : Permissions granulaires
- **AuditLog** : Journalisation des actions
- **UserSession** : Gestion des sessions
- **PasswordPolicy** : Politique de mots de passe
- **TwoFactorAuth** : Configuration 2FA

### Services Principaux

- **UtilisateurService** : Gestion des utilisateurs
- **RoleService** : Gestion des rôles
- **PermissionService** : Gestion des permissions
- **AuditService** : Journalisation et audit
- **SessionService** : Gestion des sessions
- **PasswordPolicyService** : Politique de mots de passe
- **TwoFactorAuthService** : Authentification 2FA
- **BackupRestoreService** : Sauvegarde/restauration

## 📦 Installation

### Prérequis

- Java 17+
- Spring Boot 3.x
- Maven 3.6+
- Base de données (MySQL, PostgreSQL, H2)

### Étapes d'installation

1. **Cloner le projet**
```bash
git clone <repository-url>
cd erp-security-module
```

2. **Configuration de la base de données**
```sql
-- Créer la base de données
CREATE DATABASE erp_security;

-- Les tables seront créées automatiquement par JPA
```

3. **Configuration application.properties**
```properties
# Base de données
spring.datasource.url=jdbc:mysql://localhost:3306/erp_security
spring.datasource.username=your_username
spring.datasource.password=your_password

# JPA
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true

# Sécurité
spring.security.user.name=admin
spring.security.user.password=admin123
```

4. **Compilation et exécution**
```bash
mvn clean install
mvn spring-boot:run
```

## ⚙️ Configuration

### Configuration de Base

Le module se configure automatiquement avec des valeurs par défaut. Pour personnaliser :

1. **Politique de mots de passe**
```java
@Autowired
private PasswordPolicyService passwordPolicyService;

// Créer une politique personnalisée
PasswordPolicy policy = new PasswordPolicy();
policy.setLongueurMinimale(12);
policy.setExigerCaracteresSpeciaux(true);
passwordPolicyService.createPolicy(policy);
```

2. **Permissions par défaut**
```java
@Autowired
private PermissionService permissionService;

// Créer les permissions pour un nouveau module
permissionService.creerPermissionsModule("NOUVEAU_MODULE", 
    "LIRE", "CREER", "MODIFIER", "SUPPRIMER");
```

### Configuration Spring Security

Le module utilise Spring Security avec une configuration personnalisée :

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(authz -> authz
                .requestMatchers("/securite/**").hasAnyAuthority(
                    "SECURITE_LIRE", "SECURITE_CREER", "SECURITE_MODIFIER"
                )
                // ... autres configurations
            );
        return http.build();
    }
}
```

## 🎮 Utilisation

### Accès au Module

1. **Tableau de bord principal** : `/securite`
2. **Gestion des utilisateurs** : `/securite/utilisateurs`
3. **Gestion des rôles** : `/securite/roles`
4. **Gestion des permissions** : `/securite/permissions`
5. **Logs d'audit** : `/securite/audit`
6. **Gestion des sessions** : `/securite/sessions`
7. **Politique de mots de passe** : `/securite/politique-mots-de-passe`
8. **Authentification 2FA** : `/securite/2fa`
9. **Sauvegardes** : `/securite/sauvegardes`

### Workflow Typique

1. **Initialisation**
   - Créer les permissions de base
   - Créer les rôles avec permissions
   - Créer les utilisateurs administrateurs

2. **Gestion quotidienne**
   - Surveiller le tableau de bord
   - Vérifier les alertes de sécurité
   - Gérer les utilisateurs et rôles
   - Consulter les logs d'audit

3. **Maintenance**
   - Effectuer des sauvegardes régulières
   - Nettoyer les sessions expirées
   - Analyser les statistiques de sécurité

## 🔧 API et Services

### Services Principaux

#### UtilisateurService
```java
// Créer un utilisateur
Utilisateur user = new Utilisateur();
user.setUsername("john.doe");
user.setEmail("john@example.com");
utilisateurService.createUtilisateur(user);

// Assigner des rôles
utilisateurService.assignerRoles(userId, Set.of(roleId1, roleId2));
```

#### AuditService
```java
// Logger une action
auditService.logAction("admin", "CREATE_USER", "User Management", 
    AuditLog.NiveauAudit.INFO, AuditLog.CategorieAudit.USER_MANAGEMENT);

// Logger un échec
auditService.logFailedAction("user", "LOGIN", "Authentication", 
    "Invalid password", request);
```

#### SessionService
```java
// Créer une session
UserSession session = sessionService.createSession(userId, username, request);

// Vérifier une session
boolean isValid = sessionService.isSessionValid(sessionId);

// Terminer une session
sessionService.terminateSession(sessionId, "User logout");
```

### Endpoints REST (si activés)

```java
@RestController
@RequestMapping("/api/securite")
public class SecuriteApiController {
    
    @GetMapping("/utilisateurs")
    public List<Utilisateur> getUtilisateurs() {
        return utilisateurService.getAllUtilisateurs();
    }
    
    @PostMapping("/utilisateurs")
    public Utilisateur createUtilisateur(@RequestBody Utilisateur user) {
        return utilisateurService.createUtilisateur(user);
    }
}
```

## 🔒 Sécurité

### Bonnes Pratiques Implémentées

1. **Authentification**
   - Mots de passe hachés avec BCrypt
   - Sessions sécurisées
   - Support 2FA

2. **Autorisation**
   - Permissions granulaires
   - Contrôle d'accès basé sur les rôles
   - Validation des permissions en temps réel

3. **Audit et Monitoring**
   - Journalisation complète
   - Détection d'activité suspecte
   - Alertes automatiques

4. **Protection des Données**
   - Sauvegardes chiffrées
   - Nettoyage automatique des données sensibles
   - Politiques de rétention

### Recommandations de Sécurité

1. **Configuration**
   - Changer les mots de passe par défaut
   - Configurer HTTPS en production
   - Activer les logs de sécurité

2. **Surveillance**
   - Monitorer les tentatives de connexion échouées
   - Surveiller les sessions multiples
   - Analyser les logs d'audit régulièrement

3. **Maintenance**
   - Effectuer des sauvegardes régulières
   - Mettre à jour les politiques de mots de passe
   - Nettoyer les anciens logs d'audit

## 🛠️ Maintenance

### Tâches Régulières

1. **Nettoyage des Sessions**
```java
@Scheduled(fixedRate = 300000) // Toutes les 5 minutes
public void cleanupExpiredSessions() {
    sessionService.cleanupExpiredSessions();
    sessionService.cleanupInactiveSessions();
}
```

2. **Nettoyage des Logs d'Audit**
```java
@Scheduled(cron = "0 0 2 * * ?") // Tous les jours à 2h
public void cleanupOldAuditLogs() {
    // Supprimer les logs plus anciens que 90 jours
    LocalDateTime cutoff = LocalDateTime.now().minusDays(90);
    auditService.deleteOldLogs(cutoff);
}
```

3. **Sauvegardes Automatiques**
```java
@Scheduled(cron = "0 0 1 * * ?") // Tous les jours à 1h
public void createDailyBackup() {
    try {
        String backupPath = backupRestoreService.createFullBackup();
        log.info("Sauvegarde quotidienne créée: {}", backupPath);
    } catch (Exception e) {
        log.error("Erreur lors de la sauvegarde quotidienne", e);
    }
}
```

### Monitoring

1. **Métriques à Surveiller**
   - Nombre de sessions actives
   - Tentatives de connexion échouées
   - Actions d'audit par heure
   - Utilisation de l'espace disque

2. **Alertes Recommandées**
   - Plus de 10 tentatives de connexion échouées par heure
   - Sessions multiples pour un utilisateur
   - Comptes 2FA verrouillés
   - Espace disque insuffisant

## 🐛 Dépannage

### Problèmes Courants

1. **Erreur de Connexion à la Base de Données**
```
Solution : Vérifier la configuration dans application.properties
```

2. **Permissions Insuffisantes**
```
Solution : Vérifier que l'utilisateur a les permissions SECURITE_LIRE
```

3. **Sessions Non Créées**
```
Solution : Vérifier la configuration Spring Security
```

4. **2FA Ne Fonctionne Pas**
```
Solution : Vérifier la configuration TOTP et les codes de secours
```

### Logs de Débogage

Activer les logs de débogage :

```properties
logging.level.com.sh.erpcos.univers.securite=DEBUG
logging.level.org.springframework.security=DEBUG
```

### Support

Pour obtenir de l'aide :

1. Consulter les logs d'application
2. Vérifier la configuration
3. Tester avec des données de test
4. Consulter la documentation Spring Security

## 📚 Ressources Supplémentaires

- [Documentation Spring Security](https://spring.io/projects/spring-security)
- [Guide Spring Boot](https://spring.io/guides/gs/spring-boot/)
- [Documentation Thymeleaf](https://www.thymeleaf.org/documentation.html)
- [Bootstrap 5 Documentation](https://getbootstrap.com/docs/5.0/)

## 📄 Licence

Ce module est développé pour l'ERP COS et est destiné à un usage interne.

---

**Version** : 1.0.0  
**Dernière mise à jour** : Décembre 2024  
**Auteur** : Équipe de développement ERP COS
