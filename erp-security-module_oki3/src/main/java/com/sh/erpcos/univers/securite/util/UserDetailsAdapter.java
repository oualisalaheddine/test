package com.sh.erpcos.univers.securite.util;
import java.util.ArrayList;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import com.sh.erpcos.univers.securite.entity.Permission;
import com.sh.erpcos.univers.securite.entity.Role;
import com.sh.erpcos.univers.securite.entity.Utilisateur;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class UserDetailsAdapter {

    public static UserDetails toUserDetails(Utilisateur utilisateur) {
        log.debug("Conversion de l'utilisateur en UserDetails: {}", utilisateur.getUsername());
        
        // Liste pour stocker toutes les autorisations
        List<GrantedAuthority> authorities = new ArrayList<>();
        
        for (Role role : utilisateur.getRoles()) {
            // Ajouter le rôle lui-même comme autorité
            String roleAuthority = role.getNom().startsWith("ROLE_") ? role.getNom() : "ROLE_" + role.getNom();
            authorities.add(new SimpleGrantedAuthority(roleAuthority));
            log.debug("Ajout du rôle: {}", roleAuthority);
            
            // Ajouter chaque permission comme autorité
            for (Permission permission : role.getPermissions()) {
                if (permission.isPermissionActif()) {
                    authorities.add(new SimpleGrantedAuthority(permission.getNom()));
                    log.debug("Ajout de la permission: {}", permission.getNom());
                }
            }
        }
        
        // Afficher toutes les autorisations
        for (GrantedAuthority auth : authorities) {
            log.debug("Autorité finale: {}", auth.getAuthority());
        }
        
        return new User(
                utilisateur.getUsername(),
                utilisateur.getPassword(),
                utilisateur.isCompteActif(),
                utilisateur.isCompteNonExpire(),
                utilisateur.isCredentialsNonExpire(),
                utilisateur.isCompteNonVerrouille(),
                authorities
        );
    }
}