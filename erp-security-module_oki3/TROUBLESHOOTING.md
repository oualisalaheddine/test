# 🔧 Guide de Dépannage - ERP Security Module

Ce guide vous aide à résoudre les problèmes courants rencontrés lors de l'installation et de l'exécution du projet ERP Security Module.

## 🚨 Problèmes Identifiés et Solutions

### 1. **Maven non installé**

**Symptôme :**
```
mvn : Le terme «mvn» n'est pas reconnu comme nom d'applet de commande
```

**Solutions :**

#### Option A : Installation automatique (recommandée)
```powershell
# Exécuter en tant qu'administrateur
.\install-maven.ps1
```

#### Option B : Installation manuelle
1. Télécharger Maven depuis : https://maven.apache.org/download.cgi
2. Extraire dans `C:\Program Files\Apache\maven`
3. Ajouter `C:\Program Files\Apache\maven\bin` au PATH
4. Redémarrer le terminal

#### Option C : Via Chocolatey
```powershell
# Installer Chocolatey d'abord si nécessaire
Set-ExecutionPolicy Bypass -Scope Process -Force; [System.Net.ServicePointManager]::SecurityProtocol = [System.Net.ServicePointManager]::SecurityProtocol -bor 3072; iex ((New-Object System.Net.WebClient).DownloadString('https://community.chocolatey.org/install.ps1'))

# Installer Maven
choco install maven -y
```

### 2. **PostgreSQL non installé**

**Symptôme :**
```
psql : Le terme «psql» n'est pas reconnu
```

**Solutions :**

#### Option A : Via winget
```powershell
winget install PostgreSQL.PostgreSQL
```

#### Option B : Téléchargement manuel
1. Aller sur : https://www.postgresql.org/download/windows/
2. Télécharger et installer PostgreSQL
3. Noter le mot de passe de l'utilisateur postgres

#### Option C : Via Chocolatey
```powershell
choco install postgresql -y
```

### 3. **Base de données non configurée**

**Symptôme :**
```
org.postgresql.util.PSQLException: FATAL: database "erp_security_db" does not exist
```

**Solution :**
```powershell
# Exécuter le script de configuration
.\setup-database.ps1
```

### 4. **Templates d'erreur manquants**

**Symptôme :**
```
org.thymeleaf.exceptions.TemplateInputException: Error resolving template [error/403]
```

**Solution :**
Les templates d'erreur ont été créés automatiquement :
- `src/main/resources/templates/error/403.html`
- `src/main/resources/templates/error/404.html`
- `src/main/resources/templates/error/500.html`

### 5. **Erreurs de compilation**

**Symptôme :**
```
[ERROR] Failed to execute goal org.apache.maven.plugins:maven-compiler-plugin
```

**Solutions :**

#### Vérifier la version Java
```powershell
java -version
# Doit afficher Java 17 ou supérieur
```

#### Nettoyer et recompiler
```powershell
mvn clean compile
```

#### Vérifier les dépendances
```powershell
mvn dependency:resolve
```

### 6. **Erreurs de connexion à la base de données**

**Symptôme :**
```
org.postgresql.util.PSQLException: Connection to localhost:5432 refused
```

**Solutions :**

#### Vérifier que PostgreSQL est démarré
```powershell
# Vérifier le service PostgreSQL
Get-Service postgresql*

# Démarrer le service si nécessaire
Start-Service postgresql*
```

#### Vérifier la configuration
```properties
# Dans src/main/resources/application.properties
spring.datasource.url=jdbc:postgresql://localhost:5432/erp_security_db
spring.datasource.username=postgres
spring.datasource.password=votre_mot_de_passe
```

### 7. **Erreurs de sécurité Spring**

**Symptôme :**
```
org.springframework.security.authentication.BadCredentialsException
```

**Solutions :**

#### Vérifier les utilisateurs dans la base de données
```sql
-- Se connecter à PostgreSQL
psql -U postgres -d erp_security_db

-- Vérifier les utilisateurs
SELECT username, email, compte_actif FROM utilisateurs;

-- Vérifier les rôles
SELECT * FROM roles;

-- Vérifier les permissions
SELECT * FROM permissions;
```

#### Réinitialiser les données de test
1. Se connecter à l'application : http://localhost:8080/erp/login
2. Utiliser : admin / admin123
3. Aller dans Sécurité > Test Data
4. Cliquer sur "Initialiser Toutes les Données"

### 8. **Erreurs de port déjà utilisé**

**Symptôme :**
```
Web server failed to start. Port 8080 was already in use.
```

**Solutions :**

#### Changer le port
```properties
# Dans application.properties
server.port=8081
```

#### Ou libérer le port
```powershell
# Trouver le processus qui utilise le port 8080
netstat -ano | findstr :8080

# Tuer le processus (remplacer PID par l'ID du processus)
taskkill /PID PID /F
```

## 🔍 Diagnostic Automatique

### Script de diagnostic
```powershell
# Créer un script de diagnostic
$diagnostic = @"
=== Diagnostic ERP Security Module ===

Java Version:
$(java -version 2>&1)

Maven Version:
$(mvn -version 2>&1)

PostgreSQL Version:
$(psql --version 2>&1)

Fichiers de configuration:
$(Test-Path "src\main\resources\application.properties")
$(Test-Path "src\main\resources\templates\error\403.html")
$(Test-Path "src\main\resources\templates\error\404.html")
$(Test-Path "src\main\resources\templates\error\500.html")

Services PostgreSQL:
$(Get-Service postgresql* | Format-Table -AutoSize)

Ports utilisés:
$(netstat -ano | findstr :8080)
$(netstat -ano | findstr :5432)
"@

$diagnostic | Out-File -FilePath "diagnostic.txt" -Encoding UTF8
Write-Host $diagnostic
```

## 📋 Checklist de Résolution

- [ ] Maven installé et dans le PATH
- [ ] Java 17+ installé
- [ ] PostgreSQL installé et démarré
- [ ] Base de données créée
- [ ] Fichier `application.properties` configuré
- [ ] Templates d'erreur présents
- [ ] Port 8080 libre
- [ ] Données de test initialisées

## 🆘 Support

Si vous rencontrez des problèmes non résolus :

1. **Consulter les logs :**
   ```powershell
   mvn spring-boot:run > logs.txt 2>&1
   ```

2. **Vérifier la configuration :**
   - Fichier `application.properties`
   - Connexion à PostgreSQL
   - Variables d'environnement

3. **Tester étape par étape :**
   ```powershell
   # 1. Compilation
   mvn clean compile
   
   # 2. Tests
   mvn test
   
   # 3. Démarrage
   mvn spring-boot:run
   ```

4. **Créer un rapport de diagnostic :**
   ```powershell
   .\diagnostic.ps1 > diagnostic_report.txt
   ```

## 🔄 Réinitialisation Complète

Si rien ne fonctionne, voici comment tout réinitialiser :

```powershell
# 1. Nettoyer le projet
mvn clean

# 2. Supprimer la base de données
psql -U postgres -c "DROP DATABASE IF EXISTS erp_security_db;"

# 3. Recréer la base de données
.\setup-database.ps1

# 4. Recompiler et démarrer
mvn clean compile
mvn spring-boot:run
```

---

**Note :** Ce guide est mis à jour régulièrement. Si vous trouvez de nouveaux problèmes, n'hésitez pas à les signaler pour améliorer ce guide.
