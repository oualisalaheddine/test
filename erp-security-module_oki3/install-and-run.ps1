# Script d'installation et de démarrage de l'application ERP Sécurité

Write-Host "=== Installation et Démarrage ERP Sécurité ===" -ForegroundColor Green

# 1. Vérifier Java
Write-Host "`n1. Vérification de Java..." -ForegroundColor Yellow
try {
    $javaVersion = java -version 2>&1
    Write-Host "✓ Java installé: $($javaVersion[0])" -ForegroundColor Green
} catch {
    Write-Host "✗ Java non trouvé! Veuillez installer Java 17+" -ForegroundColor Red
    exit 1
}

# 2. Télécharger et installer Maven si nécessaire
Write-Host "`n2. Vérification de Maven..." -ForegroundColor Yellow
try {
    $mvnVersion = mvn -version 2>&1
    Write-Host "✓ Maven installé: $($mvnVersion[0])" -ForegroundColor Green
} catch {
    Write-Host "✗ Maven non trouvé! Tentative d'installation..." -ForegroundColor Yellow
    
    # Créer un wrapper Maven simple
    Write-Host "Création d'un wrapper Maven..." -ForegroundColor Cyan
    
    # Télécharger Maven wrapper
    $mavenWrapperUrl = "https://repo.maven.apache.org/maven2/io/takari/maven-wrapper/0.5.6/maven-wrapper-0.5.6.jar"
    $mavenWrapperJar = "maven-wrapper.jar"
    
    try {
        Invoke-WebRequest -Uri $mavenWrapperUrl -OutFile $mavenWrapperJar -UseBasicParsing
        Write-Host "✓ Maven wrapper téléchargé" -ForegroundColor Green
    } catch {
        Write-Host "✗ Impossible de télécharger Maven wrapper" -ForegroundColor Red
        Write-Host "Veuillez installer Maven manuellement ou utiliser un IDE" -ForegroundColor Yellow
        exit 1
    }
}

# 3. Compiler l'application
Write-Host "`n3. Compilation de l'application..." -ForegroundColor Yellow
try {
    if (Test-Path "mvnw.cmd") {
        Write-Host "Utilisation du wrapper Maven..." -ForegroundColor Cyan
        & .\mvnw.cmd clean compile -q
    } else {
        Write-Host "Utilisation de Maven système..." -ForegroundColor Cyan
        mvn clean compile -q
    }
    Write-Host "✓ Compilation réussie!" -ForegroundColor Green
} catch {
    Write-Host "✗ Erreur de compilation: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host "Vérifiez les erreurs ci-dessus" -ForegroundColor Yellow
    exit 1
}

# 4. Vérifier la base de données
Write-Host "`n4. Vérification de la base de données..." -ForegroundColor Yellow
if (Test-Path "database/init.sql") {
    Write-Host "✓ Script d'initialisation trouvé" -ForegroundColor Green
    Write-Host "Assurez-vous que votre base de données est configurée dans application.properties" -ForegroundColor Cyan
} else {
    Write-Host "✗ Script d'initialisation non trouvé!" -ForegroundColor Red
}

# 5. Démarrer l'application
Write-Host "`n5. Démarrage de l'application..." -ForegroundColor Yellow
Write-Host "L'application va démarrer sur http://localhost:8088/erp" -ForegroundColor Cyan
Write-Host "Appuyez sur Ctrl+C pour arrêter l'application" -ForegroundColor Yellow

try {
    if (Test-Path "mvnw.cmd") {
        & .\mvnw.cmd spring-boot:run -Dspring-boot.run.jvmArguments="-Dserver.port=8088"
    } else {
        mvn spring-boot:run -Dspring-boot.run.jvmArguments="-Dserver.port=8088"
    }
} catch {
    Write-Host "✗ Erreur lors du démarrage: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host "Vérifiez la configuration de la base de données" -ForegroundColor Yellow
}

Write-Host "`n=== Script terminé ===" -ForegroundColor Green
