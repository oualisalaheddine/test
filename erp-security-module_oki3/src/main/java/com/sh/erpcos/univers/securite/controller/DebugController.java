package com.sh.erpcos.univers.securite.controller;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sh.erpcos.univers.securite.repository.PermissionRepository;
import com.sh.erpcos.univers.securite.repository.RoleRepository;
import com.sh.erpcos.univers.securite.repository.UtilisateurRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/debug")
@RequiredArgsConstructor
@Slf4j
public class DebugController {

    private final UtilisateurRepository utilisateurRepository;
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    
    @GetMapping("/utilisateur/{username}")
    public Map<String, Object> getUtilisateurInfo(@PathVariable String username) {
        Map<String, Object> result = new HashMap<>();
        
        utilisateurRepository.findByUsername(username).ifPresent(u -> {
            result.put("username", u.getUsername());
            result.put("enabled", u.isCompteActif());
            result.put("roles", u.getRoles().stream().map(r -> {
                Map<String, Object> roleInfo = new HashMap<>();
                roleInfo.put("nom", r.getNom());
                roleInfo.put("permissions", r.getPermissions().stream().map(p -> {
                    Map<String, Object> permInfo = new HashMap<>();
                    permInfo.put("nom", p.getNom());
                    permInfo.put("actif", p.isPermissionActif());
                    return permInfo;
                }).collect(Collectors.toList()));
                return roleInfo;
            }).collect(Collectors.toList()));
        });
        
        return result;
    }
}