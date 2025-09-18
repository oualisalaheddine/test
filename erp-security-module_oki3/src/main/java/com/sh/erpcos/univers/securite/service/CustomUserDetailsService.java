package com.sh.erpcos.univers.securite.service;

import com.sh.erpcos.univers.securite.entity.Role;
import com.sh.erpcos.univers.securite.entity.Utilisateur;
import com.sh.erpcos.univers.securite.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sh.erpcos.univers.securite.util.UserDetailsAdapter;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CustomUserDetailsService implements UserDetailsService {
    
    private final UtilisateurRepository utilisateurRepository;
    
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.debug("Tentative de chargement de l'utilisateur avec permissions: {}", username);
        
        Utilisateur utilisateur = utilisateurRepository.findByUsernameWithPermissions(username)
                .orElseThrow(() -> {
                    log.warn("Utilisateur non trouvé: {}", username);
                    return new UsernameNotFoundException("Utilisateur non trouvé: " + username);
                });
        
        log.debug("Utilisateur trouvé: {}", utilisateur.getUsername());
        log.debug("Rôles trouvés: {}", utilisateur.getRoles().size());
        
        for (Role role : utilisateur.getRoles()) {
            log.debug("Rôle: {} avec {} permissions", role.getNom(), role.getPermissions().size());
        }
        
        return UserDetailsAdapter.toUserDetails(utilisateur);
    }
    
    public void updateLastLogin(String username) {
        log.debug("Mise à jour de la dernière connexion pour l'utilisateur: {}", username);
        
        utilisateurRepository.findByUsername(username).ifPresent(utilisateur -> {
            utilisateur.setDerniereConnexion(LocalDateTime.now());
            utilisateurRepository.save(utilisateur);
            log.debug("Dernière connexion mise à jour pour: {}", username);
        });
    }
    
    public boolean utilisateurExiste(String username) {
        return utilisateurRepository.existsByUsername(username);
    }
    
    public boolean emailExiste(String email) {
        return utilisateurRepository.existsByEmail(email);
    }
}
