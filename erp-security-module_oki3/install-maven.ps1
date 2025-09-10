# Script d'installation de Maven pour Windows
# Exécutez ce script en tant qu'administrateur

Write-Host "=== Installation de Maven ===" -ForegroundColor Green

# Vérifier si Maven est déjà installé
try {
    $mavenVersion = mvn -version 2>$null
    if ($mavenVersion) {
        Write-Host "Maven est déjà installé :" -ForegroundColor Yellow
        Write-Host $mavenVersion -ForegroundColor Cyan
        exit 0
    }
} catch {
    Write-Host "Maven n'est pas installé. Installation en cours..." -ForegroundColor Yellow
}

# Vérifier si Chocolatey est installé
try {
    $chocoVersion = choco -version 2>$null
    if ($chocoVersion) {
        Write-Host "Chocolatey détecté. Installation de Maven via Chocolatey..." -ForegroundColor Green
        choco install maven -y
        Write-Host "Maven installé avec succès via Chocolatey!" -ForegroundColor Green
        exit 0
    }
} catch {
    Write-Host "Chocolatey non détecté. Installation manuelle..." -ForegroundColor Yellow
}

# Installation manuelle de Maven
Write-Host "Installation manuelle de Maven..." -ForegroundColor Yellow

# Créer le dossier d'installation
$mavenDir = "C:\Program Files\Apache\maven"
if (!(Test-Path $mavenDir)) {
    New-Item -ItemType Directory -Path $mavenDir -Force | Out-Null
}

# URL de téléchargement de Maven
$mavenUrl = "https://archive.apache.org/dist/maven/maven-3/3.9.5/binaries/apache-maven-3.9.5-bin.zip"
$zipPath = "$env:TEMP\maven.zip"

Write-Host "Téléchargement de Maven..." -ForegroundColor Yellow
try {
    Invoke-WebRequest -Uri $mavenUrl -OutFile $zipPath
    Write-Host "Téléchargement terminé!" -ForegroundColor Green
} catch {
    Write-Host "Erreur lors du téléchargement: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

Write-Host "Extraction de Maven..." -ForegroundColor Yellow
try {
    Expand-Archive -Path $zipPath -DestinationPath "C:\Program Files\Apache" -Force
    Write-Host "Extraction terminée!" -ForegroundColor Green
} catch {
    Write-Host "Erreur lors de l'extraction: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

# Nettoyer le fichier temporaire
Remove-Item $zipPath -Force

# Configurer les variables d'environnement
Write-Host "Configuration des variables d'environnement..." -ForegroundColor Yellow

$mavenHome = "C:\Program Files\Apache\apache-maven-3.9.5"
$currentPath = [Environment]::GetEnvironmentVariable("PATH", "Machine")

if ($currentPath -notlike "*$mavenHome\bin*") {
    $newPath = "$currentPath;$mavenHome\bin"
    [Environment]::SetEnvironmentVariable("PATH", $newPath, "Machine")
    [Environment]::SetEnvironmentVariable("MAVEN_HOME", $mavenHome, "Machine")
    Write-Host "Variables d'environnement configurées!" -ForegroundColor Green
} else {
    Write-Host "Maven déjà dans le PATH!" -ForegroundColor Green
}

Write-Host "=== Installation terminée ===" -ForegroundColor Green
Write-Host "Redémarrez votre terminal pour que les changements prennent effet." -ForegroundColor Yellow
Write-Host "Ou exécutez: refreshenv" -ForegroundColor Cyan
