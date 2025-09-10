# Script de configuration de la base de données PostgreSQL
# Exécutez ce script en tant qu'administrateur

Write-Host "=== Configuration de la base de données PostgreSQL ===" -ForegroundColor Green

# Vérifier si PostgreSQL est installé
try {
    $psqlVersion = psql --version 2>$null
    if ($psqlVersion) {
        Write-Host "PostgreSQL détecté :" -ForegroundColor Green
        Write-Host $psqlVersion -ForegroundColor Cyan
    } else {
        Write-Host "PostgreSQL non détecté. Installation recommandée..." -ForegroundColor Yellow
        Write-Host "Téléchargez PostgreSQL depuis : https://www.postgresql.org/download/windows/" -ForegroundColor Cyan
        Write-Host "Ou utilisez : winget install PostgreSQL.PostgreSQL" -ForegroundColor Cyan
        exit 1
    }
} catch {
    Write-Host "PostgreSQL non installé. Veuillez l'installer d'abord." -ForegroundColor Red
    exit 1
}

# Demander les informations de connexion
Write-Host "`nConfiguration de la base de données :" -ForegroundColor Yellow

$host = Read-Host "Hôte PostgreSQL (défaut: localhost)"
if ([string]::IsNullOrEmpty($host)) { $host = "localhost" }

$port = Read-Host "Port PostgreSQL (défaut: 5432)"
if ([string]::IsNullOrEmpty($port)) { $port = "5432" }

$username = Read-Host "Nom d'utilisateur PostgreSQL (défaut: postgres)"
if ([string]::IsNullOrEmpty($username)) { $username = "postgres" }

$password = Read-Host "Mot de passe PostgreSQL" -AsSecureString
$passwordPlain = [Runtime.InteropServices.Marshal]::PtrToStringAuto([Runtime.InteropServices.Marshal]::SecureStringToBSTR($password))

$databaseName = Read-Host "Nom de la base de données (défaut: erp_security_db)"
if ([string]::IsNullOrEmpty($databaseName)) { $databaseName = "erp_security_db" }

# Créer le fichier de configuration
Write-Host "`nCréation du fichier de configuration..." -ForegroundColor Yellow

$configContent = @"
# Configuration de la base de données PostgreSQL
spring.datasource.url=jdbc:postgresql://$host`:$port/$databaseName
spring.datasource.username=$username
spring.datasource.password=$passwordPlain
spring.datasource.driver-class-name=org.postgresql.Driver

# Configuration JPA/Hibernate
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect

# Configuration Thymeleaf
spring.thymeleaf.cache=false
spring.thymeleaf.prefix=classpath:/templates/
spring.thymeleaf.suffix=.html

# Configuration Spring Security
spring.security.user.name=admin
spring.security.user.password=admin123

# Configuration du serveur
server.port=8080
server.servlet.context-path=/erp

# Configuration des logs
logging.level.com.sh.erpcos=DEBUG
logging.level.org.springframework.security=DEBUG

# Configuration pour le développement
spring.devtools.restart.enabled=true
spring.devtools.livereload.enabled=true
"@

$configContent | Out-File -FilePath "src\main\resources\application.properties" -Encoding UTF8

Write-Host "Fichier de configuration créé : src\main\resources\application.properties" -ForegroundColor Green

# Créer la base de données
Write-Host "`nCréation de la base de données..." -ForegroundColor Yellow

$env:PGPASSWORD = $passwordPlain

try {
    # Vérifier si la base de données existe déjà
    $dbExists = psql -h $host -p $port -U $username -d postgres -t -c "SELECT 1 FROM pg_database WHERE datname='$databaseName';" 2>$null
    
    if ($dbExists -and $dbExists.Trim() -eq "1") {
        Write-Host "La base de données '$databaseName' existe déjà." -ForegroundColor Yellow
        $overwrite = Read-Host "Voulez-vous la supprimer et la recréer ? (o/N)"
        if ($overwrite -eq "o" -or $overwrite -eq "O") {
            Write-Host "Suppression de la base de données existante..." -ForegroundColor Yellow
            psql -h $host -p $port -U $username -d postgres -c "DROP DATABASE IF EXISTS $databaseName;"
            Write-Host "Base de données supprimée." -ForegroundColor Green
        } else {
            Write-Host "Utilisation de la base de données existante." -ForegroundColor Green
            exit 0
        }
    }
    
    # Créer la base de données
    psql -h $host -p $port -U $username -d postgres -c "CREATE DATABASE $databaseName;"
    Write-Host "Base de données '$databaseName' créée avec succès!" -ForegroundColor Green
    
    # Exécuter le script d'initialisation
    Write-Host "`nExécution du script d'initialisation..." -ForegroundColor Yellow
    psql -h $host -p $port -U $username -d $databaseName -f "database\init.sql"
    Write-Host "Script d'initialisation exécuté avec succès!" -ForegroundColor Green
    
} catch {
    Write-Host "Erreur lors de la création de la base de données: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
} finally {
    # Nettoyer le mot de passe de la variable d'environnement
    Remove-Item Env:PGPASSWORD -ErrorAction SilentlyContinue
}

Write-Host "`n=== Configuration terminée ===" -ForegroundColor Green
Write-Host "Base de données: $databaseName" -ForegroundColor Cyan
Write-Host "Utilisateur: $username" -ForegroundColor Cyan
Write-Host "Hôte: $host`:$port" -ForegroundColor Cyan
Write-Host "`nVous pouvez maintenant démarrer l'application avec: mvn spring-boot:run" -ForegroundColor Yellow
