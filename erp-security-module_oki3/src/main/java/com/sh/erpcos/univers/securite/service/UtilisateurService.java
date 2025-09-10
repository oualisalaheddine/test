package com.sh.erpcos.univers.securite.service;

import com.sh.erpcos.univers.securite.entity.Role;
import com.sh.erpcos.univers.securite.entity.Utilisateur;
import com.sh.erpcos.univers.securite.repository.RoleRepository;
import com.sh.erpcos.univers.securite.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UtilisateurService {
    
    private final UtilisateurRepository utilisateurRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    
    public List<Utilisateur> getAllUtilisateurs() {
        return utilisateurRepository.findAll();
    }
    
    public List<Utilisateur> getUtilisateursActifs() {
        return utilisateurRepository.findByCompteActif(true);
    }
    
    public Optional<Utilisateur> getUtilisateurById(Long id) {
        return utilisateurRepository.findById(id);
    }
    
    public Optional<Utilisateur> getUtilisateurByUsername(String username) {
        return utilisateurRepository.findByUsername(username);
    }
    
    public Optional<Utilisateur> getUtilisateurByEmail(String email) {
        return utilisateurRepository.findByEmail(email);
    }
    
    public Utilisateur createUtilisateur(Utilisateur utilisateur) {
        log.info("Création d'un nouvel utilisateur: {}", utilisateur.getUsername());
        
        // Vérifier si l'utilisateur existe déjà
        if (utilisateurRepository.existsByUsername(utilisateur.getUsername())) {
            throw new RuntimeException("Un utilisateur avec ce nom d'utilisateur existe déjà");
        }
        
        if (utilisateurRepository.existsByEmail(utilisateur.getEmail())) {
            throw new RuntimeException("Un utilisateur avec cet email existe déjà");
        }
        
        // Encoder le mot de passe
        utilisateur.setPassword(passwordEncoder.encode(utilisateur.getPassword()));
        
        // Définir la date de création
        utilisateur.setDateCreation(LocalDateTime.now());
        
        // Activer le compte par défaut
        utilisateur.setCompteActif(true);
        utilisateur.setCompteNonExpire(true);
        utilisateur.setCompteNonVerrouille(true);
        utilisateur.setCredentialsNonExpire(true);
        
        Utilisateur savedUtilisateur = utilisateurRepository.save(utilisateur);
        log.info("Utilisateur créé avec succès: {}", savedUtilisateur.getUsername());
        
        return savedUtilisateur;
    }
    
    public Utilisateur updateUtilisateur(Long id, Utilisateur utilisateurDetails) {
        log.info("Mise à jour de l'utilisateur avec l'ID: {}", id);
        
        Utilisateur utilisateur = utilisateurRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé avec l'ID: " + id));
        
        // Mettre à jour les champs de base
        utilisateur.setNom(utilisateurDetails.getNom());
        utilisateur.setPrenom(utilisateurDetails.getPrenom());
        utilisateur.setEmail(utilisateurDetails.getEmail());
        
        // Mettre à jour le mot de passe seulement s'il a changé
        if (utilisateurDetails.getPassword() != null && !utilisateurDetails.getPassword().isEmpty()) {
            utilisateur.setPassword(passwordEncoder.encode(utilisateurDetails.getPassword()));
        }
        
        // Mettre à jour les statuts du compte
        utilisateur.setCompteActif(utilisateurDetails.isCompteActif());
        utilisateur.setCompteNonExpire(utilisateurDetails.isCompteNonExpire());
        utilisateur.setCompteNonVerrouille(utilisateurDetails.isCompteNonVerrouille());
        utilisateur.setCredentialsNonExpire(utilisateurDetails.isCredentialsNonExpire());
        
        Utilisateur updatedUtilisateur = utilisateurRepository.save(utilisateur);
        log.info("Utilisateur mis à jour avec succès: {}", updatedUtilisateur.getUsername());
        
        return updatedUtilisateur;
    }
    
    public void deleteUtilisateur(Long id) {
        log.info("Suppression de l'utilisateur avec l'ID: {}", id);
        
        Utilisateur utilisateur = utilisateurRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé avec l'ID: " + id));
        
        utilisateurRepository.delete(utilisateur);
        log.info("Utilisateur supprimé avec succès: {}", utilisateur.getUsername());
    }
    
    public void activerUtilisateur(Long id) {
        log.info("Activation de l'utilisateur avec l'ID: {}", id);
        
        Utilisateur utilisateur = utilisateurRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé avec l'ID: " + id));
        
        utilisateur.setCompteActif(true);
        utilisateurRepository.save(utilisateur);
        log.info("Utilisateur activé avec succès: {}", utilisateur.getUsername());
    }
    
    public void desactiverUtilisateur(Long id) {
        log.info("Désactivation de l'utilisateur avec l'ID: {}", id);
        
        Utilisateur utilisateur = utilisateurRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé avec l'ID: " + id));
        
        utilisateur.setCompteActif(false);
        utilisateurRepository.save(utilisateur);
        log.info("Utilisateur désactivé avec succès: {}", utilisateur.getUsername());
    }
    
    public void ajouterRole(Long utilisateurId, Long roleId) {
        log.info("Ajout du rôle {} à l'utilisateur {}", roleId, utilisateurId);
        
        Utilisateur utilisateur = utilisateurRepository.findById(utilisateurId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé avec l'ID: " + utilisateurId));
        
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new RuntimeException("Rôle non trouvé avec l'ID: " + roleId));
        
        utilisateur.getRoles().add(role);
        utilisateurRepository.save(utilisateur);
        log.info("Rôle ajouté avec succès");
    }
    
    public void retirerRole(Long utilisateurId, Long roleId) {
        log.info("Retrait du rôle {} de l'utilisateur {}", roleId, utilisateurId);
        
        Utilisateur utilisateur = utilisateurRepository.findById(utilisateurId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé avec l'ID: " + utilisateurId));
        
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new RuntimeException("Rôle non trouvé avec l'ID: " + roleId));
        
        utilisateur.getRoles().remove(role);
        utilisateurRepository.save(utilisateur);
        log.info("Rôle retiré avec succès");
    }
    
    public void assignerRoles(Long utilisateurId, Set<Long> roleIds) {
        log.info("Assignation des rôles {} à l'utilisateur {}", roleIds, utilisateurId);
        
        Utilisateur utilisateur = utilisateurRepository.findById(utilisateurId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé avec l'ID: " + utilisateurId));
        
        Set<Role> roles = roleRepository.findAllById(roleIds).stream().collect(java.util.stream.Collectors.toSet());
        
        // Vider les rôles existants et ajouter les nouveaux
        utilisateur.getRoles().clear();
        utilisateur.getRoles().addAll(roles);
        
        utilisateurRepository.save(utilisateur);
        log.info("Rôles assignés avec succès");
    }
    
    public boolean aRole(Long utilisateurId, String nomRole) {
        Utilisateur utilisateur = utilisateurRepository.findById(utilisateurId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé avec l'ID: " + utilisateurId));
        
        return utilisateur.getRoles().stream().anyMatch(role -> role.getNom().equals(nomRole));
    }
    
    public boolean aPermission(Long utilisateurId, String nomPermission) {
        Utilisateur utilisateur = utilisateurRepository.findById(utilisateurId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé avec l'ID: " + utilisateurId));
        
        return utilisateur.getRoles().stream()
                .flatMap(role -> role.getPermissions().stream())
                .anyMatch(permission -> permission.getNom().equals(nomPermission));
    }
    
    public List<Utilisateur> rechercherUtilisateurs(String recherche) {
        return utilisateurRepository.rechercherUtilisateurs(recherche);
    }
    
    public List<Utilisateur> getUtilisateursByRole(String nomRole) {
        return utilisateurRepository.findByRoleNom(nomRole);
    }
    
    public List<Utilisateur> getUtilisateursByPermission(String nomPermission) {
        return utilisateurRepository.findByPermissionNom(nomPermission);
    }
    
    public List<Utilisateur> getUtilisateursByModule(String nomModule) {
        return utilisateurRepository.findByModule(nomModule);
    }
    
    public long getNombreUtilisateursActifs() {
        return utilisateurRepository.countUtilisateursActifs();
    }
    
    public List<Utilisateur> getUtilisateursRecents() {
        return utilisateurRepository.findUtilisateursRecents();
    }
}
