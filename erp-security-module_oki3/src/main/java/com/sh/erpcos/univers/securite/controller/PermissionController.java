package com.sh.erpcos.univers.securite.controller;

import com.sh.erpcos.univers.securite.entity.Permission;
import com.sh.erpcos.univers.securite.service.PermissionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

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
        model.addAttribute("permission", new Permission());
        model.addAttribute("titre", "Nouvelle Permission");
        model.addAttribute("modules", permissionService.getModulesDisponibles());
        
        return "securite/permissions/formulaire";
    }
    
    @PostMapping("/nouveau")
    @PreAuthorize("hasAuthority('SECURITE_CREER')")
    public String creerPermission(@ModelAttribute Permission permission,
                                 RedirectAttributes redirectAttributes) {
        try {
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
    public String voirPermission(@PathVariable Long id, Model model) {
        Permission permission = permissionService.getPermissionById(id)
                .orElseThrow(() -> new RuntimeException("Permission non trouvée"));
        
        model.addAttribute("permission", permission);
        model.addAttribute("roles", permissionService.getRolesByPermission(permission.getNom()));
        
        log.info("Affichage des détails de la permission: {}", permission.getNom());
        return "securite/permissions/details";
    }
    
    @GetMapping("/{id}/modifier")
    @PreAuthorize("hasAuthority('SECURITE_MODIFIER')")
    public String modifierPermission(@PathVariable Long id, Model model) {
        Permission permission = permissionService.getPermissionById(id)
                .orElseThrow(() -> new RuntimeException("Permission non trouvée"));
        
        model.addAttribute("permission", permission);
        model.addAttribute("titre", "Modifier Permission");
        model.addAttribute("modules", permissionService.getModulesDisponibles());
        
        return "securite/permissions/formulaire";
    }
    
    @PostMapping("/{id}/modifier")
    @PreAuthorize("hasAuthority('SECURITE_MODIFIER')")
    public String updatePermission(@PathVariable Long id,
                                  @ModelAttribute Permission permission,
                                  RedirectAttributes redirectAttributes) {
        try {
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
    public String supprimerPermission(@PathVariable Long id, RedirectAttributes redirectAttributes) {
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
    public String activerPermission(@PathVariable Long id, RedirectAttributes redirectAttributes) {
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
    public String desactiverPermission(@PathVariable Long id, RedirectAttributes redirectAttributes) {
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
}
