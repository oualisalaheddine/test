package com.sh.erpcos.univers.securite.service;

import com.sh.erpcos.univers.securite.entity.Module;
import com.sh.erpcos.univers.securite.entity.Permission;
import com.sh.erpcos.univers.securite.enums.ActionType;
import com.sh.erpcos.univers.securite.enums.ActionSpecifiqueType;
import com.sh.erpcos.univers.securite.enums.ModuleType;
import com.sh.erpcos.univers.securite.repository.ModuleRepository;
import com.sh.erpcos.univers.securite.repository.PermissionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PermissionService {
    
    private final PermissionRepository permissionRepository;
    private final ModuleRepository moduleRepository;
    
    // ====== Méthodes de requête existantes avec adaptations mineures ======
    
    public List<Permission> getAllPermissions() {
        return permissionRepository.findAll();
    }
    
    public List<Permission> getPermissionsActives() {
        return permissionRepository.findByPermissionActif(true);
    }
    
    public List<Permission> getPermissionsOrdernees() {
        return permissionRepository.findAllOrderByModuleEtAction();
    }
    
    public Optional<Permission> getPermissionById(Integer id) {
        return permissionRepository.findById(id);
    }
    
    public Optional<Permission> getPermissionByNom(String nom) {
        return permissionRepository.findByNom(nom);
    }
    
    public List<Permission> getPermissionsByModule(String nomModule) {
        // Compatible avec la nouvelle structure
        return permissionRepository.findByNomModule(nomModule);
    }
    
    public List<Permission> getPermissionsByModuleEntity(Module module) {
        // Nouvelle méthode utilisant l'entité Module directement
        return permissionRepository.findByModule(module);
    }
    
    public List<Permission> getPermissionsActivesByModule(String nomModule) {
        return permissionRepository.findPermissionsActivesByModule(nomModule);
    }
    
    public List<Permission> getPermissionsByAction(String nomAction) {
        return permissionRepository.findByNomAction(nomAction);
    }
    
    public List<Permission> getPermissionsByModuleAndAction(String nomModule, String nomAction) {
        return permissionRepository.findByNomModuleAndNomAction(nomModule, nomAction);
    }
    
    // ====== Méthodes améliorées pour la gestion des modules ======
    
    public List<Module> getAllModules() {
        return moduleRepository.findAll();
    }
    
    public List<Module> getActiveModules() {
        return moduleRepository.findByActifTrue();
    }
    
    public List<String> getModulesDisponibles() {
        // Méthode existante, maintenant retourne les codes des modules actifs
        return moduleRepository.findByActifTrue().stream()
                .map(Module::getCode)
                .collect(Collectors.toList());
    }
    
    // Nouvelles méthodes pour obtenir des informations sur les modules
    public List<ModuleType> getSystemModules() {
        return Arrays.asList(ModuleType.values());
    }
    
    public Optional<Module> getModuleById(Integer id) {
        return moduleRepository.findById(id);
    }
    
    public Optional<Module> getModuleByCode(String code) {
        return moduleRepository.findByCode(code);
    }
    
    // ====== Méthodes améliorées pour la gestion des actions ======
    
    public List<String> getActionsByModule(String nomModule) {
        return permissionRepository.findActionsByModule(nomModule);
    }
    
    public List<String> getStandardActions() {
        return Arrays.stream(ActionType.values())
                .map(Enum::name)
                .collect(Collectors.toList());
    }
    
    public List<String> getSpecificActionsForModule(String moduleCode) {
        try {
            ModuleType moduleType = ModuleType.valueOf(moduleCode);
            return ActionSpecifiqueType.getActionsForModule(moduleType)
                    .stream()
                    .map(Enum::name)
                    .collect(Collectors.toList());
        } catch (IllegalArgumentException e) {
            return Collections.emptyList();
        }
    }
    
    // ====== Méthodes de gestion des permissions ======
    
    public Permission createPermission(Permission permission) {
        log.info("Création d'une nouvelle permission: {}", permission.getNom());
        
        // Vérifier si la permission existe déjà
        if (permissionRepository.existsByNom(permission.getNom())) {
            throw new RuntimeException("Une permission avec ce nom existe déjà");
        }
        
        // Récupérer ou créer le module si nécessaire
        if (permission.getModule() == null && permission.getNomModule() != null) {
            Module module = getOrCreateModule(permission.getNomModule());
            permission.setModule(module);
        }
        
        // Générer l'URL pattern si non défini
        if (permission.getUrlPattern() == null || permission.getUrlPattern().isEmpty()) {
            if (permission.getModule() != null) {
                permission.setUrlPattern(permission.getModule().getUrlPattern());
            } else if (permission.getNomModule() != null) {
                permission.setUrlPattern("/" + permission.getNomModule().toLowerCase() + "/**");
            }
        }
        
        // Activer la permission par défaut
        permission.setPermissionActif(true);
        
        Permission savedPermission = permissionRepository.save(permission);
        log.info("Permission créée avec succès: {}", savedPermission.getNom());
        
        return savedPermission;
    }
    
    public Permission updatePermission(Integer id, Permission permissionDetails) {
        log.info("Mise à jour de la permission avec l'ID: {}", id);
        
        Permission permission = permissionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Permission non trouvée avec l'ID: " + id));
        
        // Vérifier si le nouveau nom n'existe pas déjà (sauf pour cette permission)
        if (!permission.getNom().equals(permissionDetails.getNom()) && 
            permissionRepository.existsByNom(permissionDetails.getNom())) {
            throw new RuntimeException("Une permission avec ce nom existe déjà");
        }
        
        // Récupérer ou créer le module si nécessaire
        if (permissionDetails.getNomModule() != null) {
            Module module = getOrCreateModule(permissionDetails.getNomModule());
            permissionDetails.setModule(module);
        }
        
        // Mettre à jour les champs
        permission.setNom(permissionDetails.getNom());
        permission.setDescription(permissionDetails.getDescription());
        
        // Mise à jour du module - utiliser la relation plutôt que juste le nom
        if (permissionDetails.getModule() != null) {
            permission.setModule(permissionDetails.getModule());
        } else if (permissionDetails.getNomModule() != null) {
            // Cas de compatibilité
            permission.setNomModule(permissionDetails.getNomModule());
            // Tenter de trouver le module correspondant
            moduleRepository.findByCode(permissionDetails.getNomModule())
                .ifPresent(permission::setModule);
        }
        
        permission.setNomAction(permissionDetails.getNomAction());
        
        // Mise à jour de l'URL pattern
        if (permissionDetails.getUrlPattern() != null && !permissionDetails.getUrlPattern().isEmpty()) {
            permission.setUrlPattern(permissionDetails.getUrlPattern());
        } else if (permission.getModule() != null) {
            // Utiliser l'URL pattern du module
            permission.setUrlPattern(permission.getModule().getUrlPattern());
        }
        
        permission.setPermissionActif(permissionDetails.isPermissionActif());
        permission.setNiveauPriorite(permissionDetails.getNiveauPriorite());
        
        Permission updatedPermission = permissionRepository.save(permission);
        log.info("Permission mise à jour avec succès: {}", updatedPermission.getNom());
        
        return updatedPermission;
    }
    
    public void deletePermission(Integer id) {
        log.info("Suppression de la permission avec l'ID: {}", id);
        
        Permission permission = permissionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Permission non trouvée avec l'ID: " + id));
        
        permissionRepository.delete(permission);
        log.info("Permission supprimée avec succès: {}", permission.getNom());
    }
    
    public void activerPermission(Integer id) {
        log.info("Activation de la permission avec l'ID: {}", id);
        
        Permission permission = permissionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Permission non trouvée avec l'ID: " + id));
        
        permission.setPermissionActif(true);
        permissionRepository.save(permission);
        log.info("Permission activée avec succès: {}", permission.getNom());
    }
    
    public void desactiverPermission(Integer id) {
        log.info("Désactivation de la permission avec l'ID: {}", id);
        
        Permission permission = permissionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Permission non trouvée avec l'ID: " + id));
        
        permission.setPermissionActif(false);
        permissionRepository.save(permission);
        log.info("Permission désactivée avec succès: {}", permission.getNom());
    }
    
    // ====== Méthodes de recherche ======
    
    public List<Permission> rechercherPermissions(String recherche) {
        return permissionRepository.rechercherPermissions(recherche);
    }
    
    public List<Permission> getPermissionsAvecUrlPattern() {
        return permissionRepository.findPermissionsAvecUrlPattern();
    }
    
    public long getNombrePermissionsActives() {
        return permissionRepository.countPermissionsActives();
    }
    
    public long getNombrePermissionsActivesByModule(String nomModule) {
        return permissionRepository.countPermissionsActivesByModule(nomModule);
    }
    
    // ====== Méthodes utilitaires ======
    
    // Méthode d'aide pour récupérer ou créer un module
    private Module getOrCreateModule(String moduleCode) {
        return moduleRepository.findByCode(moduleCode)
                .orElseGet(() -> {
                    // Tenter de créer à partir de ModuleType
                    try {
                        ModuleType moduleType = ModuleType.valueOf(moduleCode);
                        Module module = moduleType.toEntity();
                        return moduleRepository.save(module);
                    } catch (IllegalArgumentException e) {
                        // Créer un module personnalisé
                        Module module = new Module();
                        module.setCode(moduleCode);
                        module.setNom(moduleCode); // Utiliser le code comme nom par défaut
                        module.setUrlPattern("/" + moduleCode.toLowerCase() + "/**");
                        module.setActif(true);
                        module.setModuleSysteme(false);
                        return moduleRepository.save(module);
                    }
                });
    }
    
    // Générer automatiquement une URL pattern basée sur le code du module
    public String generateUrlPattern(String moduleCode) {
        return moduleRepository.findByCode(moduleCode)
                .map(Module::getUrlPattern)
                .orElse("/" + moduleCode.toLowerCase() + "/**");
    }
    
    // Générer un nom de permission standard
   
 // Version avec String
    public String generatePermissionName(String moduleCode, String actionName) {
        return Permission.genererNomPermission(moduleCode, actionName);
    }

    // Version avec Module
    public String generatePermissionNameWithModule(Module module, String actionName) {
        return Permission.genererNomPermission(module, actionName);
    }
    // ====== Méthodes de création de permissions standardisées ======
    
    public void creerPermissionsModule(String nomModule, String... actions) {
        log.info("Création des permissions pour le module: {}", nomModule);
        
        // Récupérer ou créer le module
        Module module = getOrCreateModule(nomModule);
        
        for (String action : actions) {
            String nomPermission = Permission.genererNomPermission(module, action); // Utiliser la surcharge avec Module
            String description = "Permission pour " + action + " dans le module " + module.getNom();
            
            // Utiliser le constructeur avec Module
            Permission permission = new Permission(nomPermission, description, module, action);
            createPermission(permission);
        }
        
        log.info("Permissions créées pour le module {}: {}", nomModule, actions.length);
    }
    
  
    
    // Version améliorée utilisant les enums
    public void creerPermissionsStandardModule(ModuleType moduleType) {
        log.info("Création des permissions standard pour le module: {}", moduleType.name());
        
        // Récupérer ou créer le module
        Module module = getOrCreateModule(moduleType.name());
        
        // Ajouter les permissions standard pour ce module
        for (ActionType actionType : ActionType.values()) {
            String nomPermission = Permission.genererNomPermission(moduleType.name(), actionType.name());
            String description = "Permission pour " + actionType.getLibelle().toLowerCase() + 
                               " dans le module " + moduleType.getLibelle();
            
            if (!permissionRepository.existsByNom(nomPermission)) {
                Permission permission = new Permission();
                permission.setNom(nomPermission);
                permission.setDescription(description);
                permission.setModule(module);
                permission.setNomAction(actionType.name());
                permission.setUrlPattern(module.getUrlPattern());
                permission.setPermissionActif(true);
                permission.setNiveauPriorite(actionType.getNiveauPriorite());
                permissionRepository.save(permission);
            }
        }
        
        // Ajouter les permissions spécifiques pour ce module
        for (ActionSpecifiqueType actionType : ActionSpecifiqueType.getActionsForModule(moduleType)) {
            String nomPermission = Permission.genererNomPermission(moduleType.name(), actionType.name());
            String description = "Permission pour " + actionType.getLibelle() + 
                               " dans le module " + moduleType.getLibelle();
            
            if (!permissionRepository.existsByNom(nomPermission)) {
                Permission permission = new Permission();
                permission.setNom(nomPermission);
                permission.setDescription(description);
                permission.setModule(module);
                permission.setNomAction(actionType.name());
                permission.setUrlPattern(module.getUrlPattern());
                permission.setPermissionActif(true);
                permission.setNiveauPriorite(actionType.getNiveauPriorite());
                permissionRepository.save(permission);
            }
        }
        
        log.info("Permissions standard créées pour le module: {}", moduleType.name());
    }
    
    // Les méthodes existantes pour des modules spécifiques
    public void creerPermissionsModuleContact() {
        creerPermissionsModule("CONTACT", "LIRE", "CREER", "MODIFIER", "SUPPRIMER", "EXPORTER");
        // Version alternative utilisant les enums
        // creerPermissionsStandardModule(ModuleType.CONTACT);
    }
    
    public void creerPermissionsModuleSecurite() {
        creerPermissionsModule("SECURITE", "LIRE", "CREER", "MODIFIER", "SUPPRIMER", "ASSIGNER_ROLES", "GESTION_PERMISSIONS");
        // Version alternative utilisant les enums
        // creerPermissionsStandardModule(ModuleType.SECURITE);
    }
    
    public void creerPermissionsModuleComptabilite() {
        creerPermissionsModule("COMPTABILITE", "LIRE", "CREER", "MODIFIER", "SUPPRIMER", "VALIDER", "EXPORTER", "IMPORTER");
        // Version alternative utilisant les enums
        // creerPermissionsStandardModule(ModuleType.COMPTABILITE);
    }
    
    public void creerPermissionsModuleRH() {
        creerPermissionsModule("RH", "LIRE", "CREER", "MODIFIER", "SUPPRIMER", "VALIDER_CONGE", "GESTION_SALAIRE");
        // Version alternative utilisant les enums
        // creerPermissionsStandardModule(ModuleType.RH);
    }
    
    public void creerPermissionsModuleStock() {
        creerPermissionsModule("STOCK", "LIRE", "CREER", "MODIFIER", "SUPPRIMER", "ENTREE", "SORTIE", "INVENTAIRE");
        // Version alternative utilisant les enums
        // creerPermissionsStandardModule(ModuleType.STOCK);
    }
    
    public void creerPermissionsModuleVente() {
        creerPermissionsModule("VENTE", "LIRE", "CREER", "MODIFIER", "SUPPRIMER", "VALIDER", "FACTURER", "REMBOURSER");
        // Version alternative utilisant les enums
        // creerPermissionsStandardModule(ModuleType.VENTE);
    }
    
    public void creerPermissionsModuleAchat() {
        creerPermissionsModule("ACHAT", "LIRE", "CREER", "MODIFIER", "SUPPRIMER", "VALIDER", "RECEPTIONNER", "RETOURNER");
        // Version alternative utilisant les enums
        // creerPermissionsStandardModule(ModuleType.ACHAT);
    }
    
    // Nouvelle méthode utilisant les enums
    public void creerToutesPermissionsStandard() {
        for (ModuleType moduleType : ModuleType.values()) {
            creerPermissionsStandardModule(moduleType);
        }
        log.info("Toutes les permissions standard ont été créées");
    }
    
    // Méthode existante pour la compatibilité
    public void creerToutesPermissionsModules() {
        creerPermissionsModuleContact();
        creerPermissionsModuleSecurite();
        creerPermissionsModuleComptabilite();
        creerPermissionsModuleRH();
        creerPermissionsModuleStock();
        creerPermissionsModuleVente();
        creerPermissionsModuleAchat();
        log.info("Toutes les permissions des modules ont été créées");
    }
    
    public List<com.sh.erpcos.univers.securite.entity.Role> getRolesByPermission(String nomPermission) {
        return permissionRepository.findRolesByPermission(nomPermission);
    }
}