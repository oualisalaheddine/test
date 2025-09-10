package com.sh.erpcos.univers.securite.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "permissions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Permission {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false, length = 100)
    private String nom;
    
    @Column(length = 200)
    private String description;
    
    @Column(name = "nom_module", nullable = false, length = 50)
    private String nomModule;
    
    @Column(name = "nom_action", nullable = false, length = 50)
    private String nomAction;
    
    @Column(name = "url_pattern", length = 200)
    private String urlPattern;
    
    @Column(name = "permission_actif")
    private boolean permissionActif = true;
    
    @Column(name = "niveau_priorite")
    private Integer niveauPriorite = 0;
    
    // Constructeur pratique pour créer des permissions
    public Permission(String nom, String description, String nomModule, String nomAction) {
        this.nom = nom;
        this.description = description;
        this.nomModule = nomModule;
        this.nomAction = nomAction;
        this.urlPattern = "/" + nomModule.toLowerCase() + "/**";
    }
    
    public Permission(String nom, String description, String nomModule, String nomAction, String urlPattern) {
        this.nom = nom;
        this.description = description;
        this.nomModule = nomModule;
        this.nomAction = nomAction;
        this.urlPattern = urlPattern;
    }
    
    // Méthode pour générer le nom de permission standard
    public static String genererNomPermission(String module, String action) {
        return module.toUpperCase() + "_" + action.toUpperCase();
    }
    
    // Méthode pour vérifier si la permission correspond à une URL
    public boolean correspondUrl(String url) {
        if (urlPattern == null || urlPattern.isEmpty()) {
            return false;
        }
        
        // Logique simple de correspondance d'URL
        return url.matches(urlPattern.replace("**", ".*"));
    }
    
    @Override
    public String toString() {
        return nom + " (" + nomModule + ":" + nomAction + ")";
    }
}
