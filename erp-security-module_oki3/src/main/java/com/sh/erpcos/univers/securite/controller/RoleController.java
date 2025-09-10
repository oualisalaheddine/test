package com.sh.erpcos.univers.securite.controller;

import com.sh.erpcos.univers.securite.entity.Permission;
import com.sh.erpcos.univers.securite.entity.Role;
import com.sh.erpcos.univers.securite.service.PermissionService;
import com.sh.erpcos.univers.securite.service.RoleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/securite/roles")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasAuthority('SECURITE_LIRE')")
public class RoleController {
    
    private final RoleService roleService;
    private final PermissionService permissionService;
    
    @GetMapping
    @PreAuthorize("hasAuthority('SECURITE_LIRE')")
    public String listRoles(Model model) {
        List<Role> roles = roleService.getAllRoles();
        model.addAttribute("roles", roles);
        model.addAttribute("nombreRoles", roles.size());
        model.addAttribute("nombreRolesActifs", roleService.getNombreRolesActifs());
        
        log.info("Affichage de la liste des rôles: {} rôles", roles.size());
        return "securite/roles/liste";
    }
    
    @GetMapping("/actifs")
    @PreAuthorize("hasAuthority('SECURITE_LIRE')")
    public String listRolesActifs(Model model) {
        List<Role> roles = roleService.getRolesActifs();
        model.addAttribute("roles", roles);
        model.addAttribute("nombreRoles", roles.size());
        model.addAttribute("titre", "Rôles Actifs");
        
        log.info("Affichage de la liste des rôles actifs: {} rôles", roles.size());
        return "securite/roles/liste";
    }
    
    @GetMapping("/recherche")
    @PreAuthorize("hasAuthority('SECURITE_LIRE')")
    public String rechercherRoles(@RequestParam String q, Model model) {
        List<Role> roles = roleService.rechercherRoles(q);
        model.addAttribute("roles", roles);
        model.addAttribute("recherche", q);
        model.addAttribute("nombreResultats", roles.size());
        
        log.info("Recherche de rôles avec '{}': {} résultats", q, roles.size());
        return "securite/roles/recherche";
    }
    
    @GetMapping("/nouveau")
    @PreAuthorize("hasAuthority('SECURITE_CREER')")
    public String nouveauRole(Model model) {
        List<Permission> permissions = permissionService.getPermissionsActives();
        model.addAttribute("role", new Role());
        model.addAttribute("permissions", permissions);
        model.addAttribute("titre", "Nouveau Rôle");
        
        return "securite/roles/formulaire";
    }
    
    @PostMapping("/nouveau")
    @PreAuthorize("hasAuthority('SECURITE_CREER')")
    public String creerRole(@ModelAttribute Role role,
                           @RequestParam(required = false) Set<Long> permissionIds,
                           RedirectAttributes redirectAttributes) {
        try {
            // Créer le rôle
            Role savedRole = roleService.createRole(role);
            
            // Assigner les permissions si spécifiées
            if (permissionIds != null && !permissionIds.isEmpty()) {
                roleService.assignerPermissions(savedRole.getId(), permissionIds);
            }
            
            redirectAttributes.addFlashAttribute("successMessage", 
                "Rôle '" + savedRole.getNom() + "' créé avec succès.");
            
            log.info("Rôle créé avec succès: {}", savedRole.getNom());
            return "redirect:/securite/roles";
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Erreur lors de la création: " + e.getMessage());
            log.error("Erreur lors de la création du rôle: {}", e.getMessage());
            return "redirect:/securite/roles/nouveau";
        }
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('SECURITE_LIRE')")
    public String voirRole(@PathVariable Long id, Model model) {
        Role role = roleService.getRoleById(id)
                .orElseThrow(() -> new RuntimeException("Rôle non trouvé"));
        
        model.addAttribute("role", role);
        model.addAttribute("permissions", role.getPermissions());
        model.addAttribute("utilisateurs", roleService.getUtilisateursByRole(role.getNom()));
        
        log.info("Affichage des détails du rôle: {}", role.getNom());
        return "securite/roles/details";
    }
    
    @GetMapping("/{id}/modifier")
    @PreAuthorize("hasAuthority('SECURITE_MODIFIER')")
    public String modifierRole(@PathVariable Long id, Model model) {
        Role role = roleService.getRoleById(id)
                .orElseThrow(() -> new RuntimeException("Rôle non trouvé"));
        
        List<Permission> toutesLesPermissions = permissionService.getPermissionsActives();
        Set<Long> permissionsRole = role.getPermissions().stream()
                .map(Permission::getId)
                .collect(Collectors.toSet());
        
        model.addAttribute("role", role);
        model.addAttribute("permissions", toutesLesPermissions);
        model.addAttribute("permissionsRole", permissionsRole);
        model.addAttribute("titre", "Modifier Rôle");
        
        return "securite/roles/formulaire";
    }
    
    @PostMapping("/{id}/modifier")
    @PreAuthorize("hasAuthority('SECURITE_MODIFIER')")
    public String updateRole(@PathVariable Long id,
                            @ModelAttribute Role role,
                            @RequestParam(required = false) Set<Long> permissionIds,
                            RedirectAttributes redirectAttributes) {
        try {
            // Mettre à jour le rôle
            Role updatedRole = roleService.updateRole(id, role);
            
            // Assigner les permissions
            if (permissionIds != null) {
                roleService.assignerPermissions(id, permissionIds);
            } else {
                // Si aucune permission n'est sélectionnée, vider les permissions
                roleService.assignerPermissions(id, Set.of());
            }
            
            redirectAttributes.addFlashAttribute("successMessage", 
                "Rôle '" + updatedRole.getNom() + "' modifié avec succès.");
            
            log.info("Rôle modifié avec succès: {}", updatedRole.getNom());
            return "redirect:/securite/roles";
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Erreur lors de la modification: " + e.getMessage());
            log.error("Erreur lors de la modification du rôle {}: {}", id, e.getMessage());
            return "redirect:/securite/roles/" + id + "/modifier";
        }
    }
    
    @PostMapping("/{id}/supprimer")
    @PreAuthorize("hasAuthority('SECURITE_SUPPRIMER')")
    public String supprimerRole(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Role role = roleService.getRoleById(id)
                    .orElseThrow(() -> new RuntimeException("Rôle non trouvé"));
            
            roleService.deleteRole(id);
            
            redirectAttributes.addFlashAttribute("successMessage", 
                "Rôle '" + role.getNom() + "' supprimé avec succès.");
            
            log.info("Rôle supprimé avec succès: {}", role.getNom());
            return "redirect:/securite/roles";
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Erreur lors de la suppression: " + e.getMessage());
            log.error("Erreur lors de la suppression du rôle {}: {}", id, e.getMessage());
            return "redirect:/securite/roles";
        }
    }
    
    @PostMapping("/{id}/activer")
    @PreAuthorize("hasAuthority('SECURITE_MODIFIER')")
    public String activerRole(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            roleService.activerRole(id);
            redirectAttributes.addFlashAttribute("successMessage", "Rôle activé avec succès.");
            log.info("Rôle activé: {}", id);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Erreur lors de l'activation: " + e.getMessage());
            log.error("Erreur lors de l'activation du rôle {}: {}", id, e.getMessage());
        }
        return "redirect:/securite/roles";
    }
    
    @PostMapping("/{id}/desactiver")
    @PreAuthorize("hasAuthority('SECURITE_MODIFIER')")
    public String desactiverRole(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            roleService.desactiverRole(id);
            redirectAttributes.addFlashAttribute("successMessage", "Rôle désactivé avec succès.");
            log.info("Rôle désactivé: {}", id);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Erreur lors de la désactivation: " + e.getMessage());
            log.error("Erreur lors de la désactivation du rôle {}: {}", id, e.getMessage());
        }
        return "redirect:/securite/roles";
    }
    
    @GetMapping("/{id}/permissions")
    @PreAuthorize("hasAuthority('SECURITE_LIRE')")
    public String gererPermissionsRole(@PathVariable Long id, Model model) {
        Role role = roleService.getRoleById(id)
                .orElseThrow(() -> new RuntimeException("Rôle non trouvé"));
        
        List<Permission> toutesLesPermissions = permissionService.getPermissionsActives();
        Set<Long> permissionsRole = role.getPermissions().stream()
                .map(Permission::getId)
                .collect(Collectors.toSet());
        
        model.addAttribute("role", role);
        model.addAttribute("permissions", toutesLesPermissions);
        model.addAttribute("permissionsRole", permissionsRole);
        
        return "securite/roles/permissions";
    }
    
    @PostMapping("/{id}/permissions")
    @PreAuthorize("hasAuthority('SECURITE_MODIFIER')")
    public String updatePermissionsRole(@PathVariable Long id,
                                       @RequestParam(required = false) Set<Long> permissionIds,
                                       RedirectAttributes redirectAttributes) {
        try {
            if (permissionIds != null) {
                roleService.assignerPermissions(id, permissionIds);
            } else {
                roleService.assignerPermissions(id, Set.of());
            }
            
            redirectAttributes.addFlashAttribute("successMessage", "Permissions mises à jour avec succès.");
            log.info("Permissions mises à jour pour le rôle: {}", id);
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Erreur lors de la mise à jour des permissions: " + e.getMessage());
            log.error("Erreur lors de la mise à jour des permissions pour le rôle {}: {}", id, e.getMessage());
        }
        
        return "redirect:/securite/roles/" + id + "/permissions";
    }
}
