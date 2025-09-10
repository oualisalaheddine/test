package com.sh.erpcos.univers.securite.service;

import com.sh.erpcos.univers.securite.entity.Permission;
import com.sh.erpcos.univers.securite.repository.PermissionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PermissionService {
    
    private final PermissionRepository permissionRepository;
    
    public List<Permission> getAllPermissions() {
        return permissionRepository.findAll();
    }
    
    public List<Permission> getPermissionsActives() {
        return permissionRepository.findByPermissionActif(true);
    }
    
    public List<Permission> getPermissionsOrdernees() {
        return permissionRepository.findAllOrderByModuleEtAction();
    }
    
    public Optional<Permission> getPermissionById(Long id) {
        return permissionRepository.findById(id);
    }
    
    public Optional<Permission> getPermissionByNom(String nom) {
        return permissionRepository.findByNom(nom);
    }
    
    public List<Permission> getPermissionsByModule(String nomModule) {
        return permissionRepository.findByNomModule(nomModule);
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
    
    public List<String> getModulesDisponibles() {
        return permissionRepository.findModulesDisponibles();
    }
    
    public List<String> getActionsByModule(String nomModule) {
        return permissionRepository.findActionsByModule(nomModule);
    }
    
    public Permission createPermission(Permission permission) {
        log.info("Création d'une nouvelle permission: {}", permission.getNom());
        
        // Vérifier si la permission existe déjà
        if (permissionRepository.existsByNom(permission.getNom())) {
            throw new RuntimeException("Une permission avec ce nom existe déjà");
        }
        
        // Activer la permission par défaut
        permission.setPermissionActif(true);
        
        Permission savedPermission = permissionRepository.save(permission);
        log.info("Permission créée avec succès: {}", savedPermission.getNom());
        
        return savedPermission;
    }
    
    public Permission updatePermission(Long id, Permission permissionDetails) {
        log.info("Mise à jour de la permission avec l'ID: {}", id);
        
        Permission permission = permissionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Permission non trouvée avec l'ID: " + id));
        
        // Vérifier si le nouveau nom n'existe pas déjà (sauf pour cette permission)
        if (!permission.getNom().equals(permissionDetails.getNom()) && 
            permissionRepository.existsByNom(permissionDetails.getNom())) {
            throw new RuntimeException("Une permission avec ce nom existe déjà");
        }
        
        // Mettre à jour les champs
        permission.setNom(permissionDetails.getNom());
        permission.setDescription(permissionDetails.getDescription());
        permission.setNomModule(permissionDetails.getNomModule());
        permission.setNomAction(permissionDetails.getNomAction());
        permission.setUrlPattern(permissionDetails.getUrlPattern());
        permission.setPermissionActif(permissionDetails.isPermissionActif());
        permission.setNiveauPriorite(permissionDetails.getNiveauPriorite());
        
        Permission updatedPermission = permissionRepository.save(permission);
        log.info("Permission mise à jour avec succès: {}", updatedPermission.getNom());
        
        return updatedPermission;
    }
    
    public void deletePermission(Long id) {
        log.info("Suppression de la permission avec l'ID: {}", id);
        
        Permission permission = permissionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Permission non trouvée avec l'ID: " + id));
        
        permissionRepository.delete(permission);
        log.info("Permission supprimée avec succès: {}", permission.getNom());
    }
    
    public void activerPermission(Long id) {
        log.info("Activation de la permission avec l'ID: {}", id);
        
        Permission permission = permissionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Permission non trouvée avec l'ID: " + id));
        
        permission.setPermissionActif(true);
        permissionRepository.save(permission);
        log.info("Permission activée avec succès: {}", permission.getNom());
    }
    
    public void desactiverPermission(Long id) {
        log.info("Désactivation de la permission avec l'ID: {}", id);
        
        Permission permission = permissionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Permission non trouvée avec l'ID: " + id));
        
        permission.setPermissionActif(false);
        permissionRepository.save(permission);
        log.info("Permission désactivée avec succès: {}", permission.getNom());
    }
    
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
    
    // Méthodes utilitaires pour créer des permissions standard
    public void creerPermissionsModule(String nomModule, String... actions) {
        log.info("Création des permissions pour le module: {}", nomModule);
        
        for (String action : actions) {
            String nomPermission = Permission.genererNomPermission(nomModule, action);
            String description = "Permission pour " + action + " dans le module " + nomModule;
            
            Permission permission = new Permission(nomPermission, description, nomModule, action);
            createPermission(permission);
        }
        
        log.info("Permissions créées pour le module {}: {}", nomModule, actions.length);
    }
    
    public void creerPermissionsModuleContact() {
        creerPermissionsModule("CONTACT", "LIRE", "CREER", "MODIFIER", "SUPPRIMER", "EXPORTER");
    }
    
    public void creerPermissionsModuleSecurite() {
        creerPermissionsModule("SECURITE", "LIRE", "CREER", "MODIFIER", "SUPPRIMER", "ASSIGNER_ROLES", "GESTION_PERMISSIONS");
    }
    
    public void creerPermissionsModuleComptabilite() {
        creerPermissionsModule("COMPTABILITE", "LIRE", "CREER", "MODIFIER", "SUPPRIMER", "VALIDER", "EXPORTER", "IMPORTER");
    }
    
    public void creerPermissionsModuleRH() {
        creerPermissionsModule("RH", "LIRE", "CREER", "MODIFIER", "SUPPRIMER", "VALIDER_CONGE", "GESTION_SALAIRE");
    }
    
    public void creerPermissionsModuleStock() {
        creerPermissionsModule("STOCK", "LIRE", "CREER", "MODIFIER", "SUPPRIMER", "ENTREE", "SORTIE", "INVENTAIRE");
    }
    
    public void creerPermissionsModuleVente() {
        creerPermissionsModule("VENTE", "LIRE", "CREER", "MODIFIER", "SUPPRIMER", "VALIDER", "FACTURER", "REMBOURSER");
    }
    
    public void creerPermissionsModuleAchat() {
        creerPermissionsModule("ACHAT", "LIRE", "CREER", "MODIFIER", "SUPPRIMER", "VALIDER", "RECEPTIONNER", "RETOURNER");
    }
    
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
