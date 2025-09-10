package com.sh.erpcos.univers.securite.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_sessions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserSession {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "session_id", unique = true, nullable = false, length = 100)
    private String sessionId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "utilisateur_id", nullable = false)
    private Utilisateur utilisateur;
    
    @Column(name = "ip_address", length = 45)
    private String ipAddress;
    
    @Column(name = "user_agent", length = 500)
    private String userAgent;
    
    @Column(name = "date_creation", nullable = false)
    private LocalDateTime dateCreation;
    
    @Column(name = "date_derniere_activite")
    private LocalDateTime dateDerniereActivite;
    
    @Column(name = "date_expiration")
    private LocalDateTime dateExpiration;
    
    @Column(name = "session_active")
    private boolean sessionActive = true;
    
    @Column(name = "type_connexion", length = 50)
    private String typeConnexion = "WEB";
    
    @Column(name = "navigateur", length = 100)
    private String navigateur;
    
    @Column(name = "systeme_exploitation", length = 100)
    private String systemeExploitation;
    
    @Column(name = "localisation", length = 200)
    private String localisation;
    
    @Column(name = "duree_session")
    private Long dureeSession; // en minutes
    
    @PrePersist
    protected void onCreate() {
        if (dateCreation == null) {
            dateCreation = LocalDateTime.now();
        }
        if (dateDerniereActivite == null) {
            dateDerniereActivite = LocalDateTime.now();
        }
        if (dateExpiration == null) {
            // Session expire après 8 heures par défaut
            dateExpiration = LocalDateTime.now().plusHours(8);
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        dateDerniereActivite = LocalDateTime.now();
        if (dateCreation != null && dateDerniereActivite != null) {
            dureeSession = java.time.Duration.between(dateCreation, dateDerniereActivite).toMinutes();
        }
    }
    
    // Méthodes utilitaires
    public boolean isExpired() {
        return dateExpiration != null && LocalDateTime.now().isAfter(dateExpiration);
    }
    
    public boolean isActive() {
        return sessionActive && !isExpired();
    }
    
    public void extendSession(int hours) {
        this.dateExpiration = LocalDateTime.now().plusHours(hours);
    }
    
    public void terminateSession() {
        this.sessionActive = false;
        this.dateDerniereActivite = LocalDateTime.now();
        if (dateCreation != null) {
            dureeSession = java.time.Duration.between(dateCreation, dateDerniereActivite).toMinutes();
        }
    }
    
    // Constructeur pratique
    public UserSession(String sessionId, Utilisateur utilisateur, String ipAddress, String userAgent) {
        this.sessionId = sessionId;
        this.utilisateur = utilisateur;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
        this.dateCreation = LocalDateTime.now();
        this.dateDerniereActivite = LocalDateTime.now();
        this.dateExpiration = LocalDateTime.now().plusHours(8);
        this.sessionActive = true;
    }
}