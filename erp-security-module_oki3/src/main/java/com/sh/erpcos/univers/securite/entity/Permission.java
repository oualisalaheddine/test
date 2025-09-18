package com.sh.erpcos.univers.securite.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import lombok.AllArgsConstructor;

@Entity
@Table(name = "permissions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Permission {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @Column(unique = true, nullable = false, length = 100)
    private String nom;
    
    @Column(length = 200)
    private String description;
    
    @Column(name = "nom_module",nullable = false, length = 30)
    private String nomModule;
    
    @Column(name = "nom_action", nullable = false, length = 50)
    private String nomAction;
    
    @Column(name = "url_pattern", length = 200)
    private String urlPattern;
    
    @Column(name = "permission_actif")
    private boolean permissionActif = true;
    
    @Column(name = "niveau_priorite")
    private Integer niveauPriorite = 0;
    
 // Ajout de la relation avec Module
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "module_id")
    private Module module;
    
    /**
     * Cette méthode vérifie d'abord si l'objet Module est présent
		Si oui, elle renvoie module.getCode() qui contient le code du module (SECURITE, RH, etc.)
		Si non, elle renvoie la valeur du champ nomModule (pour les anciennes données)
		Cela garantit que le code qui appelle permission.getNomModule() fonctionne toujours, qu'il y ait un objet Module associé ou non
     * @return
     */
    public String getNomModule() {
        return (module != null) ? module.getCode() : nomModule;
    }
    /**
     * Cette méthode met simplement à jour le champ nomModule
		Elle ne modifie pas l'objet module pour éviter des incohérences entre nomModule et module.getCode()
		C'est volontaire car la relation devrait être gérée via setModule()
     * @param nomModule
     */
    public void setNomModule(String nomModule) {
        this.nomModule = nomModule;
        // Ne pas modifier le module ici pour éviter les incohérences
    }
    
    // Nouvelles méthodes pour gérer la relation
    /**
     * Ces méthodes (getModule et setModule) sont pour la nouvelle approche orientée relation
	   setModule() met à jour à la fois la relation ET le champ nomModule pour maintenir la cohérence
	   Cela assure que getNomModule() renvoie toujours la bonne valeur
     * @return
     */
    public Module getModule() {
        return module;
    }
    
    public void setModule(Module module) {
        this.module = module;
        if (module != null) {
            this.nomModule = module.getCode();
        }
    }
    /**
    // Constructeur pratique pour créer des permissions
    public Permission(String nom, String description, String nomModule, String nomAction) {
        this.nom = nom;
        this.description = description;
        this.nomModule = nomModule;
        this.nomAction = nomAction;
        this.urlPattern = "/" + nomModule.toLowerCase() + "/**";
    }
    **/
 // Nouveau constructeur avec Module
    public Permission(String nom, String description, Module module, String nomAction) {
        this.nom = nom;
        this.description = description;
        this.module = module;
        this.nomModule = module.getCode();
        this.nomAction = nomAction;
        this.urlPattern = module.getUrlPattern();
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
    
    // Nouvelle surcharge de la méthode précédente
    public static String genererNomPermission(Module module, String action) {
        return module.getCode().toUpperCase() + "_" + action.toUpperCase();
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
    	//précedent return
    	//return nom + " (" + nomModule + ":" + nomAction + ")";
    	//Mise à jour pour utiliser la relation Module si disponible :
    	String moduleStr = (module != null) ? module.getCode() : nomModule;
    	return nom + " (" + moduleStr + ":" + nomAction + ")";
    }
}
