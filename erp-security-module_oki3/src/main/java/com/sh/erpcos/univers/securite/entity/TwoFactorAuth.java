package com.sh.erpcos.univers.securite.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "two_factor_auth")
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class TwoFactorAuth {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "utilisateur_id", nullable = false, unique = true)
    private Utilisateur utilisateur;
    
    @Column(name = "secret_key", nullable = false, length = 32)
    private String secretKey;
    
    @Column(name = "backup_codes", columnDefinition = "TEXT")
    private String backupCodes; // Codes de récupération séparés par des virgules
    
    @Column(name = "qr_code_url", length = 500)
    private String qrCodeUrl;
    
    @Column(name = "is_enabled")
    private boolean enabled = false;
    
    @Column(name = "is_verified")
    private boolean verified = false;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "method_type", nullable = false)
    private MethodType methodType = MethodType.TOTP;
    
    @Column(name = "phone_number", length = 20)
    private String phoneNumber; // Pour SMS
    
    @Column(name = "email_backup", length = 100)
    private String emailBackup; // Email de secours
    
    @Column(name = "date_creation", nullable = false)
    private LocalDateTime dateCreation;
    
    @Column(name = "date_activation")
    private LocalDateTime dateActivation;
    
    @Column(name = "date_derniere_utilisation")
    private LocalDateTime dateDerniereUtilisation;
    
    @Column(name = "tentatives_echec")
    private Integer tentativesEchec = 0;
    
    @Column(name = "date_dernier_echec")
    private LocalDateTime dateDernierEchec;
    
    @Column(name = "verrouille_jusqu")
    private LocalDateTime verrouilleJusqu;
    
    @Column(name = "codes_utilises", columnDefinition = "TEXT")
    private String codesUtilises; // Codes de récupération déjà utilisés
    
    public enum MethodType {
        TOTP("Time-based One-Time Password"),
        SMS("SMS"),
        EMAIL("Email"),
        BACKUP_CODES("Codes de récupération");
        
        private final String description;
        
        MethodType(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    @PrePersist
    protected void onCreate() {
        if (dateCreation == null) {
            dateCreation = LocalDateTime.now();
        }
    }
    
    // Méthodes utilitaires
    public boolean isLocked() {
        return verrouilleJusqu != null && LocalDateTime.now().isBefore(verrouilleJusqu);
    }
    
    public void incrementFailedAttempts() {
        this.tentativesEchec++;
        this.dateDernierEchec = LocalDateTime.now();
        
        // Verrouiller après 5 tentatives échouées
        if (this.tentativesEchec >= 5) {
            this.verrouilleJusqu = LocalDateTime.now().plusMinutes(30);
        }
    }
    
    public void resetFailedAttempts() {
        this.tentativesEchec = 0;
        this.dateDernierEchec = null;
        this.verrouilleJusqu = null;
    }
    
    public void markAsUsed() {
        this.dateDerniereUtilisation = LocalDateTime.now();
        resetFailedAttempts();
    }
    
    public void enable() {
        this.enabled = true;
        this.verified = true;
        this.dateActivation = LocalDateTime.now();
    }
    
    public void disable() {
        this.enabled = false;
        this.verified = false;
        this.dateActivation = null;
    }
    
    // Gestion des codes de récupération
    public String[] getBackupCodesArray() {
        if (backupCodes == null || backupCodes.isEmpty()) {
            return new String[0];
        }
        return backupCodes.split(",");
    }
    
    public void setBackupCodesArray(String[] codes) {
        if (codes == null || codes.length == 0) {
            this.backupCodes = "";
        } else {
            this.backupCodes = String.join(",", codes);
        }
    }
    
    public String[] getUsedCodesArray() {
        if (codesUtilises == null || codesUtilises.isEmpty()) {
            return new String[0];
        }
        return codesUtilises.split(",");
    }
    
    public void markBackupCodeAsUsed(String code) {
        String[] usedCodes = getUsedCodesArray();
        String[] newUsedCodes = new String[usedCodes.length + 1];
        System.arraycopy(usedCodes, 0, newUsedCodes, 0, usedCodes.length);
        newUsedCodes[usedCodes.length] = code;
        
        if (newUsedCodes.length == 1) {
            this.codesUtilises = code;
        } else {
            this.codesUtilises = String.join(",", newUsedCodes);
        }
    }
    
    public boolean isBackupCodeUsed(String code) {
        String[] usedCodes = getUsedCodesArray();
        for (String usedCode : usedCodes) {
            if (usedCode.equals(code)) {
                return true;
            }
        }
        return false;
    }
    
    public boolean isBackupCodeValid(String code) {
        if (isBackupCodeUsed(code)) {
            return false;
        }
        
        String[] backupCodesArray = getBackupCodesArray();
        for (String backupCode : backupCodesArray) {
            if (backupCode.equals(code)) {
                return true;
            }
        }
        return false;
    }
    
    public int getRemainingBackupCodes() {
        String[] allCodes = getBackupCodesArray();
        String[] usedCodes = getUsedCodesArray();
        return allCodes.length - usedCodes.length;
    }
    
    // Constructeur pratique
    public TwoFactorAuth(Utilisateur utilisateur, String secretKey, MethodType methodType) {
        this.utilisateur = utilisateur;
        this.secretKey = secretKey;
        this.methodType = methodType;
        this.dateCreation = LocalDateTime.now();
        this.enabled = false;
        this.verified = false;
        this.tentativesEchec = 0;
    }
}