package com.sh.erpcos.univers.securite.util;

import com.sh.erpcos.univers.securite.entity.Utilisateur;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.stream.Collectors;

/**
 * Classe utilitaire pour adapter l'entité Utilisateur en UserDetails
 * Utilise le pattern Adapter pour séparer les responsabilités
 */
public class UserDetailsAdapter {
    
    /**
     * Convertit une entité Utilisateur en UserDetails
     * 
     * @param utilisateur l'entité utilisateur
     * @return UserDetails pour Spring Security
     */
    public static UserDetails toUserDetails(Utilisateur utilisateur) {
        Collection<? extends GrantedAuthority> authorities = getAuthorities(utilisateur);
        
        return User.builder()
                .username(utilisateur.getUsername())
                .password(utilisateur.getPassword())
                .authorities(authorities)
                .accountExpired(!utilisateur.isCompteNonExpire())
                .accountLocked(!utilisateur.isCompteNonVerrouille())
                .credentialsExpired(!utilisateur.isCredentialsNonExpire())
                .disabled(!utilisateur.isCompteActif())
                .build();
    }
    
    /**
     * Extrait les autorités (permissions) de l'utilisateur
     * 
     * @param utilisateur l'entité utilisateur
     * @return collection d'autorités
     */
    private static Collection<? extends GrantedAuthority> getAuthorities(Utilisateur utilisateur) {
        return utilisateur.getRoles().stream()
                .flatMap(role -> role.getPermissions().stream())
                .map(permission -> new SimpleGrantedAuthority(permission.getNom()))
                .collect(Collectors.toList());
    }
}
