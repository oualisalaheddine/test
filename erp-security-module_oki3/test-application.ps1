# Script de test pour diagnostiquer les problèmes de l'application

Write-Host "=== Diagnostic de l'Application ERP Sécurité ===" -ForegroundColor Green

# Vérifier Java
Write-Host "`n1. Vérification de Java..." -ForegroundColor Yellow
try {
    $javaVersion = java -version 2>&1
    Write-Host "Java installé: $($javaVersion[0])" -ForegroundColor Green
} catch {
    Write-Host "Java non trouvé!" -ForegroundColor Red
    exit 1
}

# Vérifier Maven
Write-Host "`n2. Vérification de Maven..." -ForegroundColor Yellow
try {
    $mvnVersion = mvn -version 2>&1
    Write-Host "Maven installé: $($mvnVersion[0])" -ForegroundColor Green
} catch {
    Write-Host "Maven non trouvé! Tentative avec le wrapper..." -ForegroundColor Yellow
    if (Test-Path "mvnw.cmd") {
        Write-Host "Wrapper Maven trouvé: mvnw.cmd" -ForegroundColor Green
    } else {
        Write-Host "Aucun wrapper Maven trouvé!" -ForegroundColor Red
    }
}

# Vérifier la compilation
Write-Host "`n3. Tentative de compilation..." -ForegroundColor Yellow
try {
    if (Test-Path "mvnw.cmd") {
        Write-Host "Utilisation du wrapper Maven..." -ForegroundColor Cyan
        & .\mvnw.cmd compile -q
        Write-Host "Compilation réussie!" -ForegroundColor Green
    } else {
        Write-Host "Utilisation de Maven système..." -ForegroundColor Cyan
        mvn compile -q
        Write-Host "Compilation réussie!" -ForegroundColor Green
    }
} catch {
    Write-Host "Erreur de compilation: $($_.Exception.Message)" -ForegroundColor Red
}

# Vérifier les fichiers de configuration
Write-Host "`n4. Vérification des fichiers de configuration..." -ForegroundColor Yellow
if (Test-Path "src/main/resources/application.properties") {
    Write-Host "application.properties trouvé" -ForegroundColor Green
    $props = Get-Content "src/main/resources/application.properties"
    Write-Host "Configuration:" -ForegroundColor Cyan
    $props | ForEach-Object { Write-Host "  $_" -ForegroundColor Gray }
} else {
    Write-Host "application.properties non trouvé!" -ForegroundColor Red
}

# Vérifier la base de données
Write-Host "`n5. Vérification de la base de données..." -ForegroundColor Yellow
if (Test-Path "database/init.sql") {
    Write-Host "Script d'initialisation de la base trouvé" -ForegroundColor Green
} else {
    Write-Host "Script d'initialisation de la base non trouvé!" -ForegroundColor Red
}

# Vérifier les templates
Write-Host "`n6. Vérification des templates..." -ForegroundColor Yellow
$templates = @(
    "src/main/resources/templates/securite/dashboard.html",
    "src/main/resources/templates/securite/utilisateurs/liste.html",
    "src/main/resources/templates/securite/roles/liste.html"
)

foreach ($template in $templates) {
    if (Test-Path $template) {
        Write-Host "✓ $template" -ForegroundColor Green
    } else {
        Write-Host "✗ $template" -ForegroundColor Red
    }
}

Write-Host "`n=== Diagnostic termine ===" -ForegroundColor Green
Write-Host "Si des erreurs sont detectees, corrigez-les avant de demarrer l'application." -ForegroundColor Yellow
