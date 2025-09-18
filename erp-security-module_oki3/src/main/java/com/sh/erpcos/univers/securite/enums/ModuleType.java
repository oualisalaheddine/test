package com.sh.erpcos.univers.securite.enums;

import com.sh.erpcos.univers.securite.entity.Module;

public enum ModuleType {
    SECURITE("Sécurité", "fas fa-shield-alt", "/securite/**"),
    COMPTABILITE("Comptabilité", "fas fa-calculator", "/comptabilite/**"),
    RH("Ressources Humaines", "fas fa-users", "/rh/**"),
    STOCK("Stock", "fas fa-boxes", "/stock/**"),
    VENTE("Vente", "fas fa-shopping-cart", "/vente/**"),
    ACHAT("Achat", "fas fa-shopping-bag", "/achat/**"),
    CONTACT("Contact", "fas fa-address-book", "/contact/**");
    
    private final String libelle;
    private final String icone;
    private final String urlPattern;
    
    ModuleType(String libelle, String icone, String urlPattern) {
        this.libelle = libelle;
        this.icone = icone;
        this.urlPattern = urlPattern;
    }
    
    // Getters
    public String getLibelle() { return libelle; }
    public String getIcone() { return icone; }
    public String getUrlPattern() { return urlPattern; }
    
    // Méthode pour convertir en entité Module sans utilise @Builder dans l'entity
    public Module toEntity() {
    	Module module = new Module();
        module.setCode(this.name());
        module.setNom(this.libelle);
        module.setIcone(this.icone);
        module.setUrlPattern(this.urlPattern);
        module.setActif(true);
        module.setModuleSysteme(true);
        return module;
    }
}