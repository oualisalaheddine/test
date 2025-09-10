package com.sh.erpcos.univers.securite.controller;

import com.sh.erpcos.univers.securite.entity.Permission;
import com.sh.erpcos.univers.securite.entity.Role;
import com.sh.erpcos.univers.securite.entity.Utilisateur;
import com.sh.erpcos.univers.securite.service.PermissionService;
import com.sh.erpcos.univers.securite.service.RoleService;
import com.sh.erpcos.univers.securite.service.UtilisateurService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Controller
@RequestMapping("/securite/test")
@RequiredArgsConstructor
@Slf4j
public class TestDataController {
    
    private final PermissionService permissionService;
    private final RoleService roleService;
    private final UtilisateurService utilisateurService;
    
    @GetMapping
    public String testDataPage(Model model) {
        long nbPermissions = permissionService.getNombrePermissionsActives();
        long nbRoles = roleService.getNombreRolesActifs();
        long nbUtilisateurs = utilisateurService.getNombreUtilisateursActifs();
        
        model.addAttribute("nbPermissions", nbPermissions);
        model.addAttribute("nbRoles", nbRoles);
        model.addAttribute("nbUtilisateurs", nbUtilisateurs);
        
        return "securite/test-data";
    }
    
    @PostMapping("/init-permissions")
    public String initPermissions(RedirectAttributes redirectAttributes) {
        try {
            log.info("Initialisation des permissions de test...");
            
            // Créer toutes les permissions pour tous les modules
            permissionService.creerToutesPermissionsModules();
            
            redirectAttributes.addFlashAttribute("successMessage", 
                "Permissions initialisées avec succès !");
            
            log.info("Permissions initialisées avec succès");
            return "redirect:/erp/securite/test";
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Erreur lors de l'initialisation des permissions: " + e.getMessage());
            log.error("Erreur lors de l'initialisation des permissions: {}", e.getMessage());
            return "redirect:/erp/securite/test";
        }
    }
    
    @PostMapping("/init-roles")
    public String initRoles(RedirectAttributes redirectAttributes) {
        try {
            log.info("Initialisation des rôles de test...");
            
            // Rôle Administrateur
            Role adminRole = new Role();
            adminRole.setNom("ADMIN");
            adminRole.setDescription("Administrateur système avec tous les droits");
            adminRole.setNiveauHierarchie(0);
            roleService.createRole(adminRole);
            
            // Rôle Manager
            Role managerRole = new Role();
            managerRole.setNom("MANAGER");
            managerRole.setDescription("Manager avec droits étendus");
            managerRole.setNiveauHierarchie(1);
            roleService.createRole(managerRole);
            
            // Rôle Utilisateur
            Role userRole = new Role();
            userRole.setNom("USER");
            userRole.setDescription("Utilisateur standard");
            userRole.setNiveauHierarchie(2);
            roleService.createRole(userRole);
            
            // Rôle Lecteur
            Role readerRole = new Role();
            readerRole.setNom("READER");
            readerRole.setDescription("Lecteur avec droits de consultation uniquement");
            readerRole.setNiveauHierarchie(3);
            roleService.createRole(readerRole);
            
            redirectAttributes.addFlashAttribute("successMessage", 
                "Rôles initialisés avec succès !");
            
            log.info("Rôles initialisés avec succès");
            return "redirect:/erp/securite/test";
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Erreur lors de l'initialisation des rôles: " + e.getMessage());
            log.error("Erreur lors de l'initialisation des rôles: {}", e.getMessage());
            return "redirect:/erp/securite/test";
        }
    }
    
    @PostMapping("/init-users")
    public String initUsers(RedirectAttributes redirectAttributes) {
        try {
            log.info("Initialisation des utilisateurs de test...");
            
            // Récupérer les rôles
            Role adminRole = roleService.getRoleByNom("ADMIN").orElse(null);
            Role managerRole = roleService.getRoleByNom("MANAGER").orElse(null);
            Role userRole = roleService.getRoleByNom("USER").orElse(null);
            Role readerRole = roleService.getRoleByNom("READER").orElse(null);
            
            // Utilisateur Admin
            Utilisateur admin = new Utilisateur();
            admin.setUsername("admin");
            admin.setPassword("admin123");
            admin.setNom("Administrateur");
            admin.setPrenom("Système");
            admin.setEmail("admin@erp.com");
            admin.setCompteActif(true);
            
            Utilisateur savedAdmin = utilisateurService.createUtilisateur(admin);
            if (adminRole != null) {
                utilisateurService.assignerRoles(savedAdmin.getId(), Set.of(adminRole.getId()));
            }
            
            // Utilisateur Manager
            Utilisateur manager = new Utilisateur();
            manager.setUsername("manager");
            manager.setPassword("manager123");
            manager.setNom("Manager");
            manager.setPrenom("Test");
            manager.setEmail("manager@erp.com");
            manager.setCompteActif(true);
            
            Utilisateur savedManager = utilisateurService.createUtilisateur(manager);
            if (managerRole != null) {
                utilisateurService.assignerRoles(savedManager.getId(), Set.of(managerRole.getId()));
            }
            
            // Utilisateur Standard
            Utilisateur user = new Utilisateur();
            user.setUsername("user");
            user.setPassword("user123");
            user.setNom("Utilisateur");
            user.setPrenom("Standard");
            user.setEmail("user@erp.com");
            user.setCompteActif(true);
            
            Utilisateur savedUser = utilisateurService.createUtilisateur(user);
            if (userRole != null) {
                utilisateurService.assignerRoles(savedUser.getId(), Set.of(userRole.getId()));
            }
            
            // Utilisateur Lecteur
            Utilisateur reader = new Utilisateur();
            reader.setUsername("reader");
            reader.setPassword("reader123");
            reader.setNom("Lecteur");
            reader.setPrenom("Test");
            reader.setEmail("reader@erp.com");
            reader.setCompteActif(true);
            
            Utilisateur savedReader = utilisateurService.createUtilisateur(reader);
            if (readerRole != null) {
                utilisateurService.assignerRoles(savedReader.getId(), Set.of(readerRole.getId()));
            }
            
            redirectAttributes.addFlashAttribute("successMessage", 
                "Utilisateurs de test créés avec succès !\n" +
                "Admin: admin/admin123\n" +
                "Manager: manager/manager123\n" +
                "User: user/user123\n" +
                "Reader: reader/reader123");
            
            log.info("Utilisateurs de test créés avec succès");
            return "redirect:/erp/securite/test";
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Erreur lors de l'initialisation des utilisateurs: " + e.getMessage());
            log.error("Erreur lors de l'initialisation des utilisateurs: {}", e.getMessage());
            return "redirect:/erp/securite/test";
        }
    }
    
    @PostMapping("/assign-permissions")
    public String assignPermissions(RedirectAttributes redirectAttributes) {
        try {
            log.info("Assignation des permissions aux rôles...");
            
            // Récupérer les rôles
            Role adminRole = roleService.getRoleByNom("ADMIN").orElse(null);
            Role managerRole = roleService.getRoleByNom("MANAGER").orElse(null);
            Role userRole = roleService.getRoleByNom("USER").orElse(null);
            Role readerRole = roleService.getRoleByNom("READER").orElse(null);
            
            // Récupérer toutes les permissions
            var allPermissions = permissionService.getPermissionsActives();
            Set<Long> allPermissionIds = new HashSet<>();
            allPermissions.forEach(p -> allPermissionIds.add(p.getId()));
            
            // Admin: toutes les permissions
            if (adminRole != null) {
                roleService.assignerPermissions(adminRole.getId(), allPermissionIds);
            }
            
            // Manager: permissions de lecture, création, modification (pas de suppression)
            if (managerRole != null) {
                Set<Long> managerPermissionIds = new HashSet<>();
                allPermissions.stream()
                    .filter(p -> !p.getNomAction().equals("SUPPRIMER"))
                    .forEach(p -> managerPermissionIds.add(p.getId()));
                roleService.assignerPermissions(managerRole.getId(), managerPermissionIds);
            }
            
            // User: permissions de lecture et création
            if (userRole != null) {
                Set<Long> userPermissionIds = new HashSet<>();
                allPermissions.stream()
                    .filter(p -> Arrays.asList("LIRE", "CREER").contains(p.getNomAction()))
                    .forEach(p -> userPermissionIds.add(p.getId()));
                roleService.assignerPermissions(userRole.getId(), userPermissionIds);
            }
            
            // Reader: permissions de lecture uniquement
            if (readerRole != null) {
                Set<Long> readerPermissionIds = new HashSet<>();
                allPermissions.stream()
                    .filter(p -> p.getNomAction().equals("LIRE"))
                    .forEach(p -> readerPermissionIds.add(p.getId()));
                roleService.assignerPermissions(readerRole.getId(), readerPermissionIds);
            }
            
            redirectAttributes.addFlashAttribute("successMessage", 
                "Permissions assignées aux rôles avec succès !");
            
            log.info("Permissions assignées aux rôles avec succès");
            return "redirect:/erp/securite/test";
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Erreur lors de l'assignation des permissions: " + e.getMessage());
            log.error("Erreur lors de l'assignation des permissions: {}", e.getMessage());
            return "redirect:/erp/securite/test";
        }
    }
    
    @PostMapping("/init-all")
    public String initAll(RedirectAttributes redirectAttributes) {
        try {
            log.info("Initialisation complète des données de test...");
            
            // Initialiser dans l'ordre
            initPermissions(redirectAttributes);
            initRoles(redirectAttributes);
            assignPermissions(redirectAttributes);
            initUsers(redirectAttributes);
            
            redirectAttributes.addFlashAttribute("successMessage", 
                "Initialisation complète terminée avec succès !\n" +
                "Vous pouvez maintenant vous connecter avec les comptes de test.");
            
            log.info("Initialisation complète terminée avec succès");
            return "redirect:/erp/securite/test";
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Erreur lors de l'initialisation complète: " + e.getMessage());
            log.error("Erreur lors de l'initialisation complète: {}", e.getMessage());
            return "redirect:/erp/securite/test";
        }
    }
}
