package com.sh.erpcos.univers.securite.service;

import com.sh.erpcos.univers.securite.entity.Permission;
import com.sh.erpcos.univers.securite.entity.Role;
import com.sh.erpcos.univers.securite.repository.PermissionRepository;
import com.sh.erpcos.univers.securite.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class RoleService {
    
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    
    public List<Role> getAllRoles() {
        return roleRepository.findAll();
    }
    
    public List<Role> getRolesActifs() {
        return roleRepository.findByRoleActif(true);
    }
    
    public List<Role> getRolesHierarchiques() {
        return roleRepository.findAllOrderByHierarchie();
    }
    
    public List<Role> getRolesParents() {
        return roleRepository.findByRoleParentIsNull();
    }
    
    public Optional<Role> getRoleById(Long id) {
        return roleRepository.findById(id);
    }
    
    public Optional<Role> getRoleByNom(String nom) {
        return roleRepository.findByNom(nom);
    }
    
    public Role createRole(Role role) {
        log.info("Création d'un nouveau rôle: {}", role.getNom());
        
        // Vérifier si le rôle existe déjà
        if (roleRepository.existsByNom(role.getNom())) {
            throw new RuntimeException("Un rôle avec ce nom existe déjà");
        }
        
        // Définir le niveau hiérarchique si non défini
        if (role.getNiveauHierarchie() == null) {
            role.setNiveauHierarchie(0);
        }
        
        // Activer le rôle par défaut
        role.setRoleActif(true);
        
        Role savedRole = roleRepository.save(role);
        log.info("Rôle créé avec succès: {}", savedRole.getNom());
        
        return savedRole;
    }
    
    public Role updateRole(Long id, Role roleDetails) {
        log.info("Mise à jour du rôle avec l'ID: {}", id);
        
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Rôle non trouvé avec l'ID: " + id));
        
        // Vérifier si le nouveau nom n'existe pas déjà (sauf pour ce rôle)
        if (!role.getNom().equals(roleDetails.getNom()) && 
            roleRepository.existsByNom(roleDetails.getNom())) {
            throw new RuntimeException("Un rôle avec ce nom existe déjà");
        }
        
        // Mettre à jour les champs
        role.setNom(roleDetails.getNom());
        role.setDescription(roleDetails.getDescription());
        role.setNiveauHierarchie(roleDetails.getNiveauHierarchie());
        role.setRoleActif(roleDetails.isRoleActif());
        
        Role updatedRole = roleRepository.save(role);
        log.info("Rôle mis à jour avec succès: {}", updatedRole.getNom());
        
        return updatedRole;
    }
    
    public void deleteRole(Long id) {
        log.info("Suppression du rôle avec l'ID: {}", id);
        
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Rôle non trouvé avec l'ID: " + id));
        
        // Vérifier s'il y a des rôles enfants
        if (!role.getRolesEnfants().isEmpty()) {
            throw new RuntimeException("Impossible de supprimer un rôle qui a des rôles enfants");
        }
        
        roleRepository.delete(role);
        log.info("Rôle supprimé avec succès: {}", role.getNom());
    }
    
    public void activerRole(Long id) {
        log.info("Activation du rôle avec l'ID: {}", id);
        
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Rôle non trouvé avec l'ID: " + id));
        
        role.setRoleActif(true);
        roleRepository.save(role);
        log.info("Rôle activé avec succès: {}", role.getNom());
    }
    
    public void desactiverRole(Long id) {
        log.info("Désactivation du rôle avec l'ID: {}", id);
        
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Rôle non trouvé avec l'ID: " + id));
        
        role.setRoleActif(false);
        roleRepository.save(role);
        log.info("Rôle désactivé avec succès: {}", role.getNom());
    }
    
    public void ajouterPermission(Long roleId, Integer permissionId) {
        log.info("Ajout de la permission {} au rôle {}", permissionId, roleId);
        
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new RuntimeException("Rôle non trouvé avec l'ID: " + roleId));
        
        Permission permission = permissionRepository.findById(permissionId)
                .orElseThrow(() -> new RuntimeException("Permission non trouvée avec l'ID: " + permissionId));
        
        role.getPermissions().add(permission);
        roleRepository.save(role);
        log.info("Permission ajoutée avec succès");
    }
    
    public void retirerPermission(Long roleId, Integer permissionId) {
        log.info("Retrait de la permission {} du rôle {}", permissionId, roleId);
        
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new RuntimeException("Rôle non trouvé avec l'ID: " + roleId));
        
        Permission permission = permissionRepository.findById(permissionId)
                .orElseThrow(() -> new RuntimeException("Permission non trouvée avec l'ID: " + permissionId));
        
        role.getPermissions().remove(permission);
        roleRepository.save(role);
        log.info("Permission retirée avec succès");
    }
    
    public void assignerPermissions(Long roleId, Set<Integer> permissionIds) {
        log.info("Assignation des permissions {} au rôle {}", permissionIds, roleId);
        
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new RuntimeException("Rôle non trouvé avec l'ID: " + roleId));
        
        Set<Permission> permissions = permissionRepository.findAllById(permissionIds)
                .stream().collect(java.util.stream.Collectors.toSet());
        
        // Vider les permissions existantes et ajouter les nouvelles
        role.getPermissions().clear();
        role.getPermissions().addAll(permissions);
        
        roleRepository.save(role);
        log.info("Permissions assignées avec succès");
    }
    
    public boolean aPermission(Long roleId, String nomPermission) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new RuntimeException("Rôle non trouvé avec l'ID: " + roleId));
        
        return role.getPermissions().stream().anyMatch(permission -> permission.getNom().equals(nomPermission));
    }
    
    public Set<Permission> getToutesPermissionsHierarchiques(Long roleId) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new RuntimeException("Rôle non trouvé avec l'ID: " + roleId));
        
        Set<Permission> toutesPermissions = new HashSet<>(role.getPermissions());
        
        // Ajouter les permissions du rôle parent (héritage hiérarchique)
        if (role.getRoleParent() != null) {
            toutesPermissions.addAll(getToutesPermissionsHierarchiques(role.getRoleParent().getId()));
        }
        
        return toutesPermissions;
    }
    
    public void ajouterRoleEnfant(Long roleParentId, Long roleEnfantId) {
        Role roleParent = roleRepository.findById(roleParentId)
                .orElseThrow(() -> new RuntimeException("Rôle parent non trouvé avec l'ID: " + roleParentId));
        
        Role roleEnfant = roleRepository.findById(roleEnfantId)
                .orElseThrow(() -> new RuntimeException("Rôle enfant non trouvé avec l'ID: " + roleEnfantId));
        
        roleParent.getRolesEnfants().add(roleEnfant);
        roleEnfant.setRoleParent(roleParent);
        roleRepository.save(roleParent);
        roleRepository.save(roleEnfant);
    }
    
    public void retirerRoleEnfant(Long roleParentId, Long roleEnfantId) {
        Role roleParent = roleRepository.findById(roleParentId)
                .orElseThrow(() -> new RuntimeException("Rôle parent non trouvé avec l'ID: " + roleParentId));
        
        Role roleEnfant = roleRepository.findById(roleEnfantId)
                .orElseThrow(() -> new RuntimeException("Rôle enfant non trouvé avec l'ID: " + roleEnfantId));
        
        roleParent.getRolesEnfants().remove(roleEnfant);
        if (roleEnfant.getRoleParent() == roleParent) {
            roleEnfant.setRoleParent(null);
        }
        roleRepository.save(roleParent);
        roleRepository.save(roleEnfant);
    }
    
    public void definirRoleParent(Long roleId, Long roleParentId) {
        log.info("Définition du rôle parent {} pour le rôle {}", roleParentId, roleId);
        
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new RuntimeException("Rôle non trouvé avec l'ID: " + roleId));
        
        Role roleParent = roleRepository.findById(roleParentId)
                .orElseThrow(() -> new RuntimeException("Rôle parent non trouvé avec l'ID: " + roleParentId));
        
        role.setRoleParent(roleParent);
        roleRepository.save(role);
        log.info("Rôle parent défini avec succès");
    }
    
    public void retirerRoleParent(Long roleId) {
        log.info("Retrait du rôle parent pour le rôle {}", roleId);
        
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new RuntimeException("Rôle non trouvé avec l'ID: " + roleId));
        
        role.setRoleParent(null);
        roleRepository.save(role);
        log.info("Rôle parent retiré avec succès");
    }
    
    public List<Role> getRolesByPermission(String nomPermission) {
        return roleRepository.findByPermissionNom(nomPermission);
    }
    
    public List<Role> getRolesByModule(String nomModule) {
        return roleRepository.findByModule(nomModule);
    }
    
    public List<Role> getRolesByNiveauHierarchie(Integer niveau) {
        return roleRepository.findByNiveauHierarchie(niveau);
    }
    
    public List<Role> rechercherRoles(String recherche) {
        return roleRepository.rechercherRoles(recherche);
    }
    
    public long getNombreRolesActifs() {
        return roleRepository.countRolesActifs();
    }
    
    public Set<Permission> getToutesPermissionsDuRole(Long roleId) {
        return getToutesPermissionsHierarchiques(roleId);
    }
    
    public List<com.sh.erpcos.univers.securite.entity.Utilisateur> getUtilisateursByRole(String nomRole) {
        return roleRepository.findUtilisateursByRole(nomRole);
    }
}
