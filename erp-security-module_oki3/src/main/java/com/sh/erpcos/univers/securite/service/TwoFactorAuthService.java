package com.sh.erpcos.univers.securite.service;

import com.sh.erpcos.univers.securite.entity.AuditLog;
import com.sh.erpcos.univers.securite.entity.TwoFactorAuth;
import com.sh.erpcos.univers.securite.entity.Utilisateur;
import com.sh.erpcos.univers.securite.repository.TwoFactorAuthRepository;
import com.sh.erpcos.univers.securite.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.ArrayList;
import java.util.Base64;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class TwoFactorAuthService {
    
    private final TwoFactorAuthRepository twoFactorAuthRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final AuditService auditService;
    private final SecureRandom secureRandom = new SecureRandom();
    
    // Configuration et activation du 2FA
    public TwoFactorAuth setup2FA(Long utilisateurId, TwoFactorAuth.MethodType methodType) {
        Optional<Utilisateur> utilisateurOpt = utilisateurRepository.findById(utilisateurId);
        if (utilisateurOpt.isEmpty()) {
            throw new RuntimeException("Utilisateur non trouvé avec l'ID: " + utilisateurId);
        }
        
        Utilisateur utilisateur = utilisateurOpt.get();
        
        // Vérifier si 2FA existe déjà
        Optional<TwoFactorAuth> existingOpt = twoFactorAuthRepository.findByUtilisateur(utilisateur);
        TwoFactorAuth twoFactorAuth;
        
        if (existingOpt.isPresent()) {
            twoFactorAuth = existingOpt.get();
            log.info("Mise à jour de la configuration 2FA existante pour l'utilisateur: {}", utilisateur.getUsername());
        } else {
            twoFactorAuth = new TwoFactorAuth();
            twoFactorAuth.setUtilisateur(utilisateur);
            log.info("Configuration initiale du 2FA pour l'utilisateur: {}", utilisateur.getUsername());
        }
        
        // Générer une nouvelle clé secrète
        String secretKey = generateSecretKey();
        twoFactorAuth.setSecretKey(secretKey);
        twoFactorAuth.setMethodType(methodType);
        twoFactorAuth.setEnabled(false); // Pas encore activ��
        twoFactorAuth.setVerified(false);
        twoFactorAuth.setDateCreation(LocalDateTime.now());
        
        // Générer des codes de récupération
        String[] backupCodes = generateBackupCodes();
        twoFactorAuth.setBackupCodesArray(backupCodes);
        
        // Générer l'URL du QR code pour TOTP
        if (methodType == TwoFactorAuth.MethodType.TOTP) {
            String qrCodeUrl = generateQRCodeUrl(utilisateur.getUsername(), secretKey);
            twoFactorAuth.setQrCodeUrl(qrCodeUrl);
        }
        
        TwoFactorAuth saved = twoFactorAuthRepository.save(twoFactorAuth);
        
        auditService.logSecurityEvent("2FA_SETUP", 
                                     "Configuration 2FA pour " + utilisateur.getUsername() + " (méthode: " + methodType + ")",
                                     AuditLog.NiveauAudit.INFO);
        
        return saved;
    }
    
    public void enable2FA(Long utilisateurId, String verificationCode) {
        Optional<TwoFactorAuth> twoFactorAuthOpt = twoFactorAuthRepository.findByUtilisateurId(utilisateurId);
        if (twoFactorAuthOpt.isEmpty()) {
            throw new RuntimeException("Configuration 2FA non trouvée pour l'utilisateur ID: " + utilisateurId);
        }
        
        TwoFactorAuth twoFactorAuth = twoFactorAuthOpt.get();
        
        if (twoFactorAuth.isLocked()) {
            throw new RuntimeException("Le compte 2FA est temporairement verrouillé");
        }
        
        // Vérifier le code de vérification
        if (!verifyCode(twoFactorAuth, verificationCode)) {
            twoFactorAuth.incrementFailedAttempts();
            twoFactorAuthRepository.save(twoFactorAuth);
            
            auditService.logSecurityEvent("2FA_ENABLE_FAILED", 
                                         "Échec de l'activation 2FA pour " + twoFactorAuth.getUtilisateur().getUsername(),
                                         AuditLog.NiveauAudit.WARNING);
            
            throw new RuntimeException("Code de vérification incorrect");
        }
        
        // Activer le 2FA
        twoFactorAuth.enable();
        twoFactorAuth.resetFailedAttempts();
        twoFactorAuthRepository.save(twoFactorAuth);
        
        auditService.logSecurityEvent("2FA_ENABLED", 
                                     "2FA activé pour " + twoFactorAuth.getUtilisateur().getUsername(),
                                     AuditLog.NiveauAudit.INFO);
        
        log.info("2FA activé avec succès pour l'utilisateur: {}", twoFactorAuth.getUtilisateur().getUsername());
    }
    
    public void disable2FA(Long utilisateurId, String verificationCode) {
        Optional<TwoFactorAuth> twoFactorAuthOpt = twoFactorAuthRepository.findByUtilisateurId(utilisateurId);
        if (twoFactorAuthOpt.isEmpty()) {
            throw new RuntimeException("Configuration 2FA non trouvée pour l'utilisateur ID: " + utilisateurId);
        }
        
        TwoFactorAuth twoFactorAuth = twoFactorAuthOpt.get();
        
        if (!twoFactorAuth.isEnabled()) {
            throw new RuntimeException("Le 2FA n'est pas activé pour cet utilisateur");
        }
        
        // Vérifier le code de vérification
        if (!verifyCode(twoFactorAuth, verificationCode)) {
            auditService.logSecurityEvent("2FA_DISABLE_FAILED", 
                                         "Échec de la désactivation 2FA pour " + twoFactorAuth.getUtilisateur().getUsername(),
                                         AuditLog.NiveauAudit.WARNING);
            
            throw new RuntimeException("Code de vérification incorrect");
        }
        
        // Désactiver le 2FA
        twoFactorAuth.disable();
        twoFactorAuthRepository.save(twoFactorAuth);
        
        auditService.logSecurityEvent("2FA_DISABLED", 
                                     "2FA désactivé pour " + twoFactorAuth.getUtilisateur().getUsername(),
                                     AuditLog.NiveauAudit.WARNING);
        
        log.info("2FA désactivé pour l'utilisateur: {}", twoFactorAuth.getUtilisateur().getUsername());
    }
    
    // Vérification des codes
    public boolean verifyCode(String username, String code) {
        Optional<TwoFactorAuth> twoFactorAuthOpt = twoFactorAuthRepository.findByUtilisateurUsername(username);
        if (twoFactorAuthOpt.isEmpty()) {
            return false;
        }
        
        return verifyCode(twoFactorAuthOpt.get(), code);
    }
    
    public boolean verifyCode(TwoFactorAuth twoFactorAuth, String code) {
        if (!twoFactorAuth.isEnabled() || twoFactorAuth.isLocked()) {
            return false;
        }
        
        boolean isValid = false;
        
        // Vérifier selon le type de méthode
        switch (twoFactorAuth.getMethodType()) {
            case TOTP:
                isValid = verifyTOTPCode(twoFactorAuth.getSecretKey(), code);
                break;
            case BACKUP_CODES:
                isValid = verifyBackupCode(twoFactorAuth, code);
                break;
            case SMS:
            case EMAIL:
                // Pour SMS et Email, la vérification se ferait avec un code temporaire stocké
                // Pour cette implémentation, on simule la vérification
                isValid = verifyTemporaryCode(twoFactorAuth, code);
                break;
        }
        
        if (isValid) {
            twoFactorAuth.markAsUsed();
            twoFactorAuthRepository.save(twoFactorAuth);
            
            auditService.logSecurityEvent("2FA_VERIFICATION_SUCCESS", 
                                         "Vérification 2FA réussie pour " + twoFactorAuth.getUtilisateur().getUsername(),
                                         AuditLog.NiveauAudit.INFO);
        } else {
            twoFactorAuth.incrementFailedAttempts();
            twoFactorAuthRepository.save(twoFactorAuth);
            
            auditService.logSecurityEvent("2FA_VERIFICATION_FAILED", 
                                         "Échec de vérification 2FA pour " + twoFactorAuth.getUtilisateur().getUsername(),
                                         AuditLog.NiveauAudit.WARNING);
        }
        
        return isValid;
    }
    
    public boolean verifyBackupCode(Long utilisateurId, String backupCode) {
        Optional<TwoFactorAuth> twoFactorAuthOpt = twoFactorAuthRepository.findByUtilisateurId(utilisateurId);
        if (twoFactorAuthOpt.isEmpty()) {
            return false;
        }
        
        return verifyBackupCode(twoFactorAuthOpt.get(), backupCode);
    }
    
    private boolean verifyBackupCode(TwoFactorAuth twoFactorAuth, String backupCode) {
        if (!twoFactorAuth.isBackupCodeValid(backupCode)) {
            return false;
        }
        
        // Marquer le code comme utilisé
        twoFactorAuth.markBackupCodeAsUsed(backupCode);
        twoFactorAuth.markAsUsed();
        twoFactorAuthRepository.save(twoFactorAuth);
        
        auditService.logSecurityEvent("2FA_BACKUP_CODE_USED", 
                                     "Code de récupération utilisé pour " + twoFactorAuth.getUtilisateur().getUsername(),
                                     AuditLog.NiveauAudit.INFO);
        
        return true;
    }
    
    // Gestion des codes de récupération
    public String[] regenerateBackupCodes(Long utilisateurId) {
        Optional<TwoFactorAuth> twoFactorAuthOpt = twoFactorAuthRepository.findByUtilisateurId(utilisateurId);
        if (twoFactorAuthOpt.isEmpty()) {
            throw new RuntimeException("Configuration 2FA non trouvée pour l'utilisateur ID: " + utilisateurId);
        }
        
        TwoFactorAuth twoFactorAuth = twoFactorAuthOpt.get();
        
        // Générer de nouveaux codes
        String[] newBackupCodes = generateBackupCodes();
        twoFactorAuth.setBackupCodesArray(newBackupCodes);
        twoFactorAuth.setCodesUtilises(""); // Réinitialiser les codes utilisés
        
        twoFactorAuthRepository.save(twoFactorAuth);
        
        auditService.logSecurityEvent("2FA_BACKUP_CODES_REGENERATED", 
                                     "Codes de récupération régénérés pour " + twoFactorAuth.getUtilisateur().getUsername(),
                                     AuditLog.NiveauAudit.INFO);
        
        log.info("Codes de récupération régénérés pour l'utilisateur: {}", twoFactorAuth.getUtilisateur().getUsername());
        
        return newBackupCodes;
    }
    
    public int getRemainingBackupCodes(Long utilisateurId) {
        Optional<TwoFactorAuth> twoFactorAuthOpt = twoFactorAuthRepository.findByUtilisateurId(utilisateurId);
        return twoFactorAuthOpt.map(TwoFactorAuth::getRemainingBackupCodes).orElse(0);
    }
    
    // Consultation et statistiques
    public Optional<TwoFactorAuth> get2FAByUserId(Long utilisateurId) {
        return twoFactorAuthRepository.findByUtilisateurId(utilisateurId);
    }
    
    public Optional<TwoFactorAuth> get2FAByUsername(String username) {
        return twoFactorAuthRepository.findByUtilisateurUsername(username);
    }
    
    public List<TwoFactorAuth> getEnabled2FA() {
        return twoFactorAuthRepository.findByEnabledTrueOrderByDateActivationDesc();
    }
    
    public List<TwoFactorAuth> getDisabled2FA() {
        return twoFactorAuthRepository.findByEnabledFalseOrderByDateCreationDesc();
    }
    
    public List<TwoFactorAuth> get2FAByMethod(TwoFactorAuth.MethodType methodType) {
        return twoFactorAuthRepository.findByMethodTypeOrderByDateCreationDesc(methodType);
    }
    
    public List<TwoFactorAuth> getLockedAccounts() {
        return twoFactorAuthRepository.findLockedAccounts(LocalDateTime.now());
    }
    
    public List<TwoFactorAuth> getAccountsWithFailedAttempts() {
        return twoFactorAuthRepository.findAccountsWithFailedAttempts();
    }
    
    public List<TwoFactorAuth> getAccountsWithHighFailedAttempts(int threshold) {
        return twoFactorAuthRepository.findAccountsWithHighFailedAttempts(threshold);
    }
    
    public List<TwoFactorAuth> getRecentlyEnabled(LocalDateTime since) {
        return twoFactorAuthRepository.findRecentlyEnabled(since);
    }
    
    public List<TwoFactorAuth> getRecentlyUsed(LocalDateTime since) {
        return twoFactorAuthRepository.findRecentlyUsed(since);
    }
    
    public List<TwoFactorAuth> getUnusedFor(LocalDateTime since) {
        return twoFactorAuthRepository.findUnusedFor(since);
    }
    
    public List<TwoFactorAuth> getAccountsWithLowBackupCodes(int threshold) {
        return twoFactorAuthRepository.findAccountsWithLowBackupCodes(threshold);
    }
    
    // Statistiques
    public long getEnabled2FACount() {
        return twoFactorAuthRepository.countEnabled2FA();
    }
    
    public long getDisabled2FACount() {
        return twoFactorAuthRepository.countDisabled2FA();
    }
    
    public long getVerified2FACount() {
        return twoFactorAuthRepository.countVerified2FA();
    }
    
    public List<Object[]> getStatisticsByMethodType() {
        return twoFactorAuthRepository.getStatisticsByMethodType();
    }
    
    // Gestion administrative
    public void unlockAccount(Long utilisateurId) {
        Optional<TwoFactorAuth> twoFactorAuthOpt = twoFactorAuthRepository.findByUtilisateurId(utilisateurId);
        if (twoFactorAuthOpt.isEmpty()) {
            throw new RuntimeException("Configuration 2FA non trouvée pour l'utilisateur ID: " + utilisateurId);
        }
        
        TwoFactorAuth twoFactorAuth = twoFactorAuthOpt.get();
        twoFactorAuth.resetFailedAttempts();
        twoFactorAuthRepository.save(twoFactorAuth);
        
        auditService.logSecurityEvent("2FA_ACCOUNT_UNLOCKED", 
                                     "Compte 2FA déverrouillé pour " + twoFactorAuth.getUtilisateur().getUsername(),
                                     AuditLog.NiveauAudit.INFO);
        
        log.info("Compte 2FA déverrouillé pour l'utilisateur: {}", twoFactorAuth.getUtilisateur().getUsername());
    }
    
    public void resetFailedAttempts(Long utilisateurId) {
        Optional<TwoFactorAuth> twoFactorAuthOpt = twoFactorAuthRepository.findByUtilisateurId(utilisateurId);
        if (twoFactorAuthOpt.isPresent()) {
            TwoFactorAuth twoFactorAuth = twoFactorAuthOpt.get();
            twoFactorAuth.resetFailedAttempts();
            twoFactorAuthRepository.save(twoFactorAuth);
            
            log.info("Tentatives d'échec réinitialisées pour l'utilisateur: {}", 
                    twoFactorAuth.getUtilisateur().getUsername());
        }
    }
    
    // Tâches de maintenance automatiques
    @Scheduled(fixedRate = 300000) // Toutes les 5 minutes
    public void unlockExpiredAccounts() {
        int unlockedCount = twoFactorAuthRepository.unlockExpiredAccounts(LocalDateTime.now());
        
        if (unlockedCount > 0) {
            log.info("Déverrouillage automatique: {} comptes 2FA déverrouillés", unlockedCount);
            auditService.logSystemConfig("UNLOCK_EXPIRED_2FA_ACCOUNTS", "2FA_MANAGEMENT", 
                                        unlockedCount + " comptes 2FA déverrouillés automatiquement");
        }
    }
    
    // Méthodes utilitaires privées
    private String generateSecretKey() {
        byte[] buffer = new byte[20];
        secureRandom.nextBytes(buffer);
        return Base64.getEncoder().encodeToString(buffer);
    }
    
    private String[] generateBackupCodes() {
        String[] codes = new String[10];
        for (int i = 0; i < 10; i++) {
            codes[i] = generateBackupCode();
        }
        return codes;
    }
    
    private String generateBackupCode() {
        // Générer un code de 8 caractères alphanumériques
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < 8; i++) {
            code.append(chars.charAt(secureRandom.nextInt(chars.length())));
        }
        return code.toString();
    }
    
    private String generateQRCodeUrl(String username, String secretKey) {
        // Format standard pour les URLs TOTP
        String issuer = "ERP-Security";
        return String.format("otpauth://totp/%s:%s?secret=%s&issuer=%s", 
                           issuer, username, secretKey, issuer);
    }
    
    private boolean verifyTOTPCode(String secretKey, String code) {
        // Implémentation simplifiée - dans un vrai projet, utiliser une bibliothèque TOTP
        // comme Google Authenticator ou Apache Commons Codec
        
        // Pour cette démonstration, on accepte le code "123456" comme valide
        // En production, implémenter la vérification TOTP réelle
        return "123456".equals(code);
    }
    
    private boolean verifyTemporaryCode(TwoFactorAuth twoFactorAuth, String code) {
        // Implémentation simplifiée pour SMS/Email
        // En production, vérifier contre un code temporaire stocké en cache/base
        return "123456".equals(code);
    }
    
    // Validation
    public boolean is2FARequired(String username) {
        Optional<TwoFactorAuth> twoFactorAuthOpt = twoFactorAuthRepository.findByUtilisateurUsername(username);
        return twoFactorAuthOpt.map(TwoFactorAuth::isEnabled).orElse(false);
    }
    
    public boolean is2FASetup(String username) {
        Optional<TwoFactorAuth> twoFactorAuthOpt = twoFactorAuthRepository.findByUtilisateurUsername(username);
        return twoFactorAuthOpt.isPresent();
    }
//added
    public List<TwoFactorAuth> getActive2FAConfigs() {
        return getEnabled2FA(); // Utilise la méthode déjà existante
    }
    public long getActive2FACountByMethod(TwoFactorAuth.MethodType methodType) {
        return twoFactorAuthRepository.countEnabled2FAByMethodType(methodType);
    }
}