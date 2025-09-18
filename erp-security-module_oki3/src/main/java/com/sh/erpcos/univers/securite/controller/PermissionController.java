package com.sh.erpcos.univers.securite.controller;

import com.sh.erpcos.univers.securite.entity.Module;
import com.sh.erpcos.univers.securite.entity.Permission;
import com.sh.erpcos.univers.securite.enums.ActionSpecifiqueType;
import com.sh.erpcos.univers.securite.enums.ActionType;
import com.sh.erpcos.univers.securite.enums.ModuleType;
import com.sh.erpcos.univers.securite.service.PermissionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.stream.Collectors;
import java.util.Arrays;

@Controller
@RequestMapping("/securite/permissions")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasAuthority('SECURITE_LIRE')")
public class PermissionController {
    
    private final PermissionService permissionService;
    
    @GetMapping
    @PreAuthorize("hasAuthority('SECURITE_LIRE')")
    public String listPermissions(Model model) {
        List<Permission> permissions = permissionService.getAllPermissions();
        model.addAttribute("permissions", permissions);
        model.addAttribute("nombrePermissions", permissions.size());
        model.addAttribute("nombrePermissionsActives", permissionService.getNombrePermissionsActives());
        
        log.info("Affichage de la liste des permissions: {} permissions", permissions.size());
        return "securite/permissions/liste";
    }
    
    @GetMapping("/actives")
    @PreAuthorize("hasAuthority('SECURITE_LIRE')")
    public String listPermissionsActives(Model model) {
        List<Permission> permissions = permissionService.getPermissionsActives();
        model.addAttribute("permissions", permissions);
        model.addAttribute("nombrePermissions", permissions.size());
        model.addAttribute("titre", "Permissions Actives");
        
        log.info("Affichage de la liste des permissions actives: {} permissions", permissions.size());
        return "securite/permissions/liste";
    }
    
    @GetMapping("/recherche")
    @PreAuthorize("hasAuthority('SECURITE_LIRE')")
    public String rechercherPermissions(@RequestParam String q, Model model) {
        List<Permission> permissions = permissionService.rechercherPermissions(q);
        model.addAttribute("permissions", permissions);
        model.addAttribute("recherche", q);
        model.addAttribute("nombreResultats", permissions.size());
        
        log.info("Recherche de permissions avec '{}': {} résultats", q, permissions.size());
        return "securite/permissions/recherche";
    }
    
    @GetMapping("/par-module")
    @PreAuthorize("hasAuthority('SECURITE_LIRE')")
    public String listPermissionsParModule(@RequestParam String module, Model model) {
        List<Permission> permissions = permissionService.getPermissionsByModule(module);
        model.addAttribute("permissions", permissions);
        model.addAttribute("module", module);
        model.addAttribute("nombrePermissions", permissions.size());
        
        log.info("Affichage des permissions pour le module '{}': {} permissions", module, permissions.size());
        return "securite/permissions/par-module";
    }
    
    @GetMapping("/nouveau")
    @PreAuthorize("hasAuthority('SECURITE_CREER')")
    public String nouvellePermission(Model model) {
        Permission permission = new Permission();
        permission.setPermissionActif(true); // Active par défaut
        
        model.addAttribute("permission", permission);
        model.addAttribute("titre", "Nouvelle Permission");
        
        // Liste des modules disponibles
        List<String> allModules = permissionService.getModulesDisponibles();
        
        // Séparation des modules système et personnalisés (si vous avez cette distinction)
        List<String> systemModules = Arrays.stream(ModuleType.values())
                .map(Enum::name)
                .collect(Collectors.toList());
        
        model.addAttribute("modules", allModules);
        model.addAttribute("systemModules", systemModules);
        
        // Actions standard disponibles
        model.addAttribute("standardActions", permissionService.getStandardActions());
        
        return "securite/permissions/formulaire";
    }
    
    @PostMapping("/nouveau")
    @PreAuthorize("hasAuthority('SECURITE_CREER')")
    public String creerPermission(
            @ModelAttribute Permission permission,
            @RequestParam(required = false) Integer moduleId,
            RedirectAttributes redirectAttributes) {
        try {
            // Associer le module si un ID est fourni
            if (moduleId != null) {
                permissionService.getModuleById(moduleId).ifPresent(permission::setModule);
            }
            
            Permission savedPermission = permissionService.createPermission(permission);
            
            redirectAttributes.addFlashAttribute("successMessage", 
                "Permission '" + savedPermission.getNom() + "' créée avec succès.");
            
            log.info("Permission créée avec succès: {}", savedPermission.getNom());
            return "redirect:/securite/permissions";
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Erreur lors de la création: " + e.getMessage());
            log.error("Erreur lors de la création de la permission: {}", e.getMessage());
            return "redirect:/securite/permissions/nouveau";
        }
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('SECURITE_LIRE')")
    public String voirPermission(@PathVariable Integer id, Model model) {
        Permission permission = permissionService.getPermissionById(id)
                .orElseThrow(() -> new RuntimeException("Permission non trouvée"));
        
        model.addAttribute("permission", permission);
        model.addAttribute("roles", permissionService.getRolesByPermission(permission.getNom()));
        
        log.info("Affichage des détails de la permission: {}", permission.getNom());
        return "securite/permissions/details";
    }
    
    @GetMapping("/{id}/modifier")
    @PreAuthorize("hasAuthority('SECURITE_MODIFIER')")
    public String modifierPermission(@PathVariable Integer id, Model model) {
        Permission permission = permissionService.getPermissionById(id)
                .orElseThrow(() -> new RuntimeException("Permission non trouvée"));
        
        model.addAttribute("permission", permission);
        model.addAttribute("titre", "Modifier Permission");
        
        // Récupération des modules (séparés en modules système et personnalisés)
        List<Module> allModules = permissionService.getAllModules();
        List<Module> systemModules = allModules.stream()
                .filter(Module::isModuleSysteme)
                .collect(Collectors.toList());
        List<Module> customModules = allModules.stream()
                .filter(m -> !m.isModuleSysteme())
                .collect(Collectors.toList());
        
        model.addAttribute("systemModules", systemModules);
        model.addAttribute("customModules", customModules);
        
        // Pour rétrocompatibilité
        model.addAttribute("modules", permissionService.getModulesDisponibles());
        
        // Actions standard disponibles
        model.addAttribute("standardActions", permissionService.getStandardActions());
        
        return "securite/permissions/formulaire";
    }
    
    @PostMapping("/{id}/modifier")
    @PreAuthorize("hasAuthority('SECURITE_MODIFIER')")
    public String updatePermission(
            @PathVariable Integer id,
            @ModelAttribute Permission permission,
            @RequestParam(required = false) Integer moduleId,
            RedirectAttributes redirectAttributes) {
        try {
            // Associer le module si un ID est fourni
            if (moduleId != null) {
                permissionService.getModuleById(moduleId).ifPresent(permission::setModule);
            }
            
            Permission updatedPermission = permissionService.updatePermission(id, permission);
            
            redirectAttributes.addFlashAttribute("successMessage", 
                "Permission '" + updatedPermission.getNom() + "' modifiée avec succès.");
            
            log.info("Permission modifiée avec succès: {}", updatedPermission.getNom());
            return "redirect:/securite/permissions";
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Erreur lors de la modification: " + e.getMessage());
            log.error("Erreur lors de la modification de la permission {}: {}", id, e.getMessage());
            return "redirect:/securite/permissions/" + id + "/modifier";
        }
    }
    
   
    
    @PostMapping("/{id}/supprimer")
    @PreAuthorize("hasAuthority('SECURITE_SUPPRIMER')")
    public String supprimerPermission(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        try {
            Permission permission = permissionService.getPermissionById(id)
                    .orElseThrow(() -> new RuntimeException("Permission non trouvée"));
            
            permissionService.deletePermission(id);
            
            redirectAttributes.addFlashAttribute("successMessage", 
                "Permission '" + permission.getNom() + "' supprimée avec succès.");
            
            log.info("Permission supprimée avec succès: {}", permission.getNom());
            return "redirect:/securite/permissions";
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Erreur lors de la suppression: " + e.getMessage());
            log.error("Erreur lors de la suppression de la permission {}: {}", id, e.getMessage());
            return "redirect:/securite/permissions";
        }
    }
    
    @PostMapping("/{id}/activer")
    @PreAuthorize("hasAuthority('SECURITE_MODIFIER')")
    public String activerPermission(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        try {
            permissionService.activerPermission(id);
            redirectAttributes.addFlashAttribute("successMessage", "Permission activée avec succès.");
            log.info("Permission activée: {}", id);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Erreur lors de l'activation: " + e.getMessage());
            log.error("Erreur lors de l'activation de la permission {}: {}", id, e.getMessage());
        }
        return "redirect:/securite/permissions";
    }
    
    @PostMapping("/{id}/desactiver")
    @PreAuthorize("hasAuthority('SECURITE_MODIFIER')")
    public String desactiverPermission(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        try {
            permissionService.desactiverPermission(id);
            redirectAttributes.addFlashAttribute("successMessage", "Permission désactivée avec succès.");
            log.info("Permission désactivée: {}", id);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Erreur lors de la désactivation: " + e.getMessage());
            log.error("Erreur lors de la désactivation de la permission {}: {}", id, e.getMessage());
        }
        return "redirect:/securite/permissions";
    }
    
    @GetMapping("/generer-module")
    @PreAuthorize("hasAuthority('SECURITE_CREER')")
    public String genererPermissionsModule(@RequestParam String nomModule, 
                                          @RequestParam String actions,
                                          RedirectAttributes redirectAttributes) {
        try {
            String[] actionsArray = actions.split(",");
            permissionService.creerPermissionsModule(nomModule, actionsArray);
            
            redirectAttributes.addFlashAttribute("successMessage", 
                "Permissions générées avec succès pour le module '" + nomModule + "'");
            
            log.info("Permissions générées pour le module: {}", nomModule);
            return "redirect:/securite/permissions";
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Erreur lors de la génération: " + e.getMessage());
            log.error("Erreur lors de la génération des permissions pour le module {}: {}", nomModule, e.getMessage());
            return "redirect:/securite/permissions";
        }
    }
    
    @GetMapping("/generer-standard")
    @PreAuthorize("hasAuthority('SECURITE_CREER')")
    public String genererPermissionsStandard(@RequestParam(required = false) String moduleType,
                                           RedirectAttributes redirectAttributes) {
        try {
            if (moduleType != null && !moduleType.isEmpty()) {
                // Générer pour un module spécifique
                ModuleType type = ModuleType.valueOf(moduleType);
                permissionService.creerPermissionsStandardModule(type);
                
                redirectAttributes.addFlashAttribute("successMessage", 
                    "Permissions standard générées avec succès pour le module '" + type.getLibelle() + "'");
                
                log.info("Permissions standard générées pour le module: {}", type.name());
            } else {
                // Générer pour tous les modules
                permissionService.creerToutesPermissionsStandard();
                
                redirectAttributes.addFlashAttribute("successMessage", 
                    "Toutes les permissions standard ont été générées avec succès");
                
                log.info("Toutes les permissions standard ont été générées");
            }
            
            return "redirect:/securite/permissions";
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Erreur lors de la génération: " + e.getMessage());
            log.error("Erreur lors de la génération des permissions standard: {}", e.getMessage());
            return "redirect:/securite/permissions";
        }
    }
    
    @GetMapping("/modules")
    @PreAuthorize("hasAuthority('SECURITE_LIRE')")
    public String listModules(Model model) {
        List<Module> modules = permissionService.getAllModules();
        model.addAttribute("modules", modules);
        model.addAttribute("systemModules", modules.stream().filter(Module::isModuleSysteme).collect(Collectors.toList()));
        model.addAttribute("customModules", modules.stream().filter(m -> !m.isModuleSysteme()).collect(Collectors.toList()));
        model.addAttribute("nombreModules", modules.size());
        
        log.info("Affichage de la liste des modules: {} modules", modules.size());
        return "securite/modules/liste";
    }
}