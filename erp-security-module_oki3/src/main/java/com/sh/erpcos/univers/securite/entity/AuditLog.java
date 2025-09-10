package com.sh.erpcos.univers.securite.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuditLog {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "utilisateur_id")
    private Long utilisateurId;
    
    @Column(name = "username", length = 50)
    private String username;
    
    @Column(name = "action", nullable = false, length = 100)
    private String action;
    
    @Column(name = "ressource", length = 200)
    private String ressource;
    
    @Column(name = "ressource_id")
    private Long ressourceId;
    
    @Column(name = "details", columnDefinition = "TEXT")
    private String details;
    
    @Column(name = "ip_address", length = 45)
    private String ipAddress;
    
    @Column(name = "user_agent", length = 500)
    private String userAgent;
    
    @Column(name = "session_id", length = 100)
    private String sessionId;
    
    @Column(name = "date_action", nullable = false)
    private LocalDateTime dateAction;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "niveau", nullable = false)
    private NiveauAudit niveau;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "categorie", nullable = false)
    private CategorieAudit categorie;
    
    @Column(name = "succes")
    private boolean succes = true;
    
    @Column(name = "message_erreur", length = 500)
    private String messageErreur;
    
    public enum NiveauAudit {
        INFO, WARNING, ERROR, CRITICAL
    }
    
    public enum CategorieAudit {
        AUTHENTICATION, AUTHORIZATION, DATA_ACCESS, DATA_MODIFICATION, 
        SYSTEM_CONFIG, SECURITY_EVENT, USER_MANAGEMENT, ROLE_MANAGEMENT, 
        PERMISSION_MANAGEMENT, SESSION_MANAGEMENT
    }
    
    @PrePersist
    protected void onCreate() {
        if (dateAction == null) {
            dateAction = LocalDateTime.now();
        }
    }
    
    // Constructeurs pratiques
    public AuditLog(String username, String action, String ressource, NiveauAudit niveau, CategorieAudit categorie) {
        this.username = username;
        this.action = action;
        this.ressource = ressource;
        this.niveau = niveau;
        this.categorie = categorie;
        this.dateAction = LocalDateTime.now();
    }
    
    public AuditLog(Long utilisateurId, String username, String action, String ressource, 
                   NiveauAudit niveau, CategorieAudit categorie, String ipAddress) {
        this.utilisateurId = utilisateurId;
        this.username = username;
        this.action = action;
        this.ressource = ressource;
        this.niveau = niveau;
        this.categorie = categorie;
        this.ipAddress = ipAddress;
        this.dateAction = LocalDateTime.now();
    }
}
