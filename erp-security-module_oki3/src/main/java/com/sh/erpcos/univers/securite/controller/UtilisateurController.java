package com.sh.erpcos.univers.securite.controller;

import com.sh.erpcos.univers.securite.entity.Role;
import com.sh.erpcos.univers.securite.entity.Utilisateur;
import com.sh.erpcos.univers.securite.service.RoleService;
import com.sh.erpcos.univers.securite.service.UtilisateurService;
import com.sh.erpcos.univers.securite.util.UserDetailsAdapter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/securite/utilisateurs")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasAuthority('SECURITE_LIRE')")
public class UtilisateurController {
    
    private final UtilisateurService utilisateurService;
    private final RoleService roleService;
    
    @GetMapping
    @PreAuthorize("hasAuthority('SECURITE_LIRE')")
    public String listUtilisateurs(Model model) {
        List<Utilisateur> utilisateurs = utilisateurService.getAllUtilisateurs();
        model.addAttribute("utilisateurs", utilisateurs);
        model.addAttribute("nombreUtilisateurs", utilisateurs.size());
        model.addAttribute("nombreUtilisateursActifs", utilisateurService.getNombreUtilisateursActifs());
        
        log.info("Affichage de la liste des utilisateurs: {} utilisateurs", utilisateurs.size());
        return "securite/utilisateurs/liste";
    }
    
    @GetMapping("/actifs")
    @PreAuthorize("hasAuthority('SECURITE_LIRE')")
    public String listUtilisateursActifs(Model model) {
        List<Utilisateur> utilisateurs = utilisateurService.getUtilisateursActifs();
        model.addAttribute("utilisateurs", utilisateurs);
        model.addAttribute("nombreUtilisateurs", utilisateurs.size());
        model.addAttribute("titre", "Utilisateurs Actifs");
        
        log.info("Affichage de la liste des utilisateurs actifs: {} utilisateurs", utilisateurs.size());
        return "securite/utilisateurs/liste";
    }
    
    @GetMapping("/recherche")
    @PreAuthorize("hasAuthority('SECURITE_LIRE')")
    public String rechercherUtilisateurs(@RequestParam String q, Model model) {
        List<Utilisateur> utilisateurs = utilisateurService.rechercherUtilisateurs(q);
        model.addAttribute("utilisateurs", utilisateurs);
        model.addAttribute("recherche", q);
        model.addAttribute("nombreResultats", utilisateurs.size());
        
        log.info("Recherche d'utilisateurs avec '{}': {} résultats", q, utilisateurs.size());
        return "securite/utilisateurs/recherche";
    }
    
    @GetMapping("/nouveau")
    @PreAuthorize("hasAuthority('SECURITE_CREER')")
    public String nouveauUtilisateur(Model model) {
        List<Role> roles = roleService.getRolesActifs();
        model.addAttribute("utilisateur", new Utilisateur());
        model.addAttribute("roles", roles);
        model.addAttribute("titre", "Nouvel Utilisateur");
        
        return "securite/utilisateurs/formulaire";
    }
    
    @PostMapping("/nouveau")
    @PreAuthorize("hasAuthority('SECURITE_CREER')")
    public String creerUtilisateur(@ModelAttribute Utilisateur utilisateur,
                                  @RequestParam(required = false) Set<Long> roleIds,
                                  RedirectAttributes redirectAttributes,
                                  HttpServletRequest request) {
        try {
            // Créer l'utilisateur
            Utilisateur savedUtilisateur = utilisateurService.createUtilisateur(utilisateur);
            
            // Assigner les rôles si spécifiés
            if (roleIds != null && !roleIds.isEmpty()) {
                utilisateurService.assignerRoles(savedUtilisateur.getId(), roleIds);
            }
            
            redirectAttributes.addFlashAttribute("successMessage", 
                "Utilisateur '" + savedUtilisateur.getUsername() + "' créé avec succès.");
            
            log.info("Utilisateur créé avec succès: {}", savedUtilisateur.getUsername());
            return "redirect:" + request.getContextPath() + "/securite/utilisateurs";
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Erreur lors de la création: " + e.getMessage());
            log.error("Erreur lors de la création de l'utilisateur: {}", e.getMessage());
            return "redirect:" + request.getContextPath() + "/securite/utilisateurs/nouveau";
        }
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('SECURITE_LIRE')")
    public String voirUtilisateur(@PathVariable Long id, Model model) {
        Utilisateur utilisateur = utilisateurService.getUtilisateurById(id)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
        
        model.addAttribute("utilisateur", utilisateur);
        model.addAttribute("roles", utilisateur.getRoles());
        model.addAttribute("permissions", UserDetailsAdapter.toUserDetails(utilisateur).getAuthorities());
        
        log.info("Affichage des détails de l'utilisateur: {}", utilisateur.getUsername());
        return "securite/utilisateurs/details";
    }
    
    @GetMapping("/{id}/modifier")
    @PreAuthorize("hasAuthority('SECURITE_MODIFIER')")
    public String modifierUtilisateur(@PathVariable Long id, Model model) {
        Utilisateur utilisateur = utilisateurService.getUtilisateurById(id)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
        
        List<Role> tousLesRoles = roleService.getRolesActifs();
        Set<Long> rolesUtilisateur = utilisateur.getRoles().stream()
                .map(Role::getId)
                .collect(Collectors.toSet());
        
        model.addAttribute("utilisateur", utilisateur);
        model.addAttribute("roles", tousLesRoles);
        model.addAttribute("rolesUtilisateur", rolesUtilisateur);
        model.addAttribute("titre", "Modifier Utilisateur");
        
        return "securite/utilisateurs/formulaire";
    }
    
    @PostMapping("/{id}/modifier")
    @PreAuthorize("hasAuthority('SECURITE_MODIFIER')")
    public String updateUtilisateur(@PathVariable Long id,
                                   @ModelAttribute Utilisateur utilisateur,
                                   @RequestParam(required = false) Set<Long> roleIds,
                                   RedirectAttributes redirectAttributes,
                                   HttpServletRequest request) {
        try {
            // Mettre à jour l'utilisateur
            Utilisateur updatedUtilisateur = utilisateurService.updateUtilisateur(id, utilisateur);
            
            // Assigner les rôles
            if (roleIds != null) {
                utilisateurService.assignerRoles(id, roleIds);
            } else {
                // Si aucun rôle n'est sélectionné, vider les rôles
                utilisateurService.assignerRoles(id, Set.of());
            }
            
            redirectAttributes.addFlashAttribute("successMessage", 
                "Utilisateur '" + updatedUtilisateur.getUsername() + "' modifié avec succès.");
            
            log.info("Utilisateur modifié avec succès: {}", updatedUtilisateur.getUsername());
            return "redirect:" + request.getContextPath() + "/securite/utilisateurs";
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Erreur lors de la modification: " + e.getMessage());
            log.error("Erreur lors de la modification de l'utilisateur {}: {}", id, e.getMessage());
            return "redirect:" + request.getContextPath() + "/securite/utilisateurs/" + id + "/modifier";
        }
    }
    
    @PostMapping("/{id}/supprimer")
    @PreAuthorize("hasAuthority('SECURITE_SUPPRIMER')")
    public String supprimerUtilisateur(@PathVariable Long id, RedirectAttributes redirectAttributes, HttpServletRequest request) {
        try {
            Utilisateur utilisateur = utilisateurService.getUtilisateurById(id)
                    .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
            
            utilisateurService.deleteUtilisateur(id);
            
            redirectAttributes.addFlashAttribute("successMessage", 
                "Utilisateur '" + utilisateur.getUsername() + "' supprimé avec succès.");
            
            log.info("Utilisateur supprimé avec succès: {}", utilisateur.getUsername());
            return "redirect:" + request.getContextPath() + "/securite/utilisateurs";
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Erreur lors de la suppression: " + e.getMessage());
            log.error("Erreur lors de la suppression de l'utilisateur {}: {}", id, e.getMessage());
            return "redirect:" + request.getContextPath() + "/securite/utilisateurs";
        }
    }
    
    @PostMapping("/{id}/activer")
    @PreAuthorize("hasAuthority('SECURITE_MODIFIER')")
    public String activerUtilisateur(@PathVariable Long id, RedirectAttributes redirectAttributes, HttpServletRequest request) {
        try {
            utilisateurService.activerUtilisateur(id);
            redirectAttributes.addFlashAttribute("successMessage", "Utilisateur activé avec succès.");
            log.info("Utilisateur activé: {}", id);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Erreur lors de l'activation: " + e.getMessage());
            log.error("Erreur lors de l'activation de l'utilisateur {}: {}", id, e.getMessage());
        }
        return "redirect:" + request.getContextPath() + "/securite/utilisateurs";
    }
    
    @PostMapping("/{id}/desactiver")
    @PreAuthorize("hasAuthority('SECURITE_MODIFIER')")
    public String desactiverUtilisateur(@PathVariable Long id, RedirectAttributes redirectAttributes, HttpServletRequest request) {
        try {
            utilisateurService.desactiverUtilisateur(id);
            redirectAttributes.addFlashAttribute("successMessage", "Utilisateur désactivé avec succès.");
            log.info("Utilisateur désactivé: {}", id);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Erreur lors de la désactivation: " + e.getMessage());
            log.error("Erreur lors de la désactivation de l'utilisateur {}: {}", id, e.getMessage());
        }
        return "redirect:" + request.getContextPath() + "/securite/utilisateurs";
    }
    
    @GetMapping("/{id}/roles")
    @PreAuthorize("hasAuthority('SECURITE_LIRE')")
    public String gererRolesUtilisateur(@PathVariable Long id, Model model) {
        Utilisateur utilisateur = utilisateurService.getUtilisateurById(id)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
        
        List<Role> tousLesRoles = roleService.getRolesActifs();
        Set<Long> rolesUtilisateur = utilisateur.getRoles().stream()
                .map(Role::getId)
                .collect(Collectors.toSet());
        
        model.addAttribute("utilisateur", utilisateur);
        model.addAttribute("roles", tousLesRoles);
        model.addAttribute("rolesUtilisateur", rolesUtilisateur);
        
        return "securite/utilisateurs/roles";
    }
    
    @PostMapping("/{id}/roles")
    @PreAuthorize("hasAuthority('SECURITE_MODIFIER')")
    public String updateRolesUtilisateur(@PathVariable Long id,
                                        @RequestParam(required = false) Set<Long> roleIds,
                                        RedirectAttributes redirectAttributes,
                                        HttpServletRequest request) {
        try {
            if (roleIds != null) {
                utilisateurService.assignerRoles(id, roleIds);
            } else {
                utilisateurService.assignerRoles(id, Set.of());
            }
            
            redirectAttributes.addFlashAttribute("successMessage", "Rôles mis à jour avec succès.");
            log.info("Rôles mis à jour pour l'utilisateur: {}", id);
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Erreur lors de la mise à jour des rôles: " + e.getMessage());
            log.error("Erreur lors de la mise à jour des rôles pour l'utilisateur {}: {}", id, e.getMessage());
        }
        
        return "redirect:" + request.getContextPath() + "/securite/utilisateurs/" + id + "/roles";
    }
}
