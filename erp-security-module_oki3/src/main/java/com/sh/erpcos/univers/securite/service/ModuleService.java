package com.sh.erpcos.univers.securite.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import com.sh.erpcos.univers.securite.entity.Module;
import com.sh.erpcos.univers.securite.entity.Permission;

import org.springframework.stereotype.Service;

import com.sh.erpcos.univers.securite.enums.ActionSpecifiqueType;
import com.sh.erpcos.univers.securite.enums.ActionType;
import com.sh.erpcos.univers.securite.enums.ModuleType;
import com.sh.erpcos.univers.securite.repository.ModuleRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ModuleService {
    private final ModuleRepository moduleRepository;
    
    public List<Module> getAllModules() {
        return moduleRepository.findAll();
    }
    
    public List<Module> getActiveModules() {
        return moduleRepository.findByActifTrue();
    }
    
    public Optional<Module> getModuleById(Integer id) {
        return moduleRepository.findById(id);
    }
    
    public Optional<Module> getModuleByCode(String code) {
        return moduleRepository.findByCode(code);
    }
    
    public Module getOrCreateModuleFromType(ModuleType moduleType) {
        return moduleRepository.findByCode(moduleType.name())
                .orElseGet(() -> {
                    Module module = moduleType.toEntity();
                    return moduleRepository.save(module);
                });
    }
    
    public Module createModule(Module module) {
        module.setModuleSysteme(false); // Les modules créés manuellement ne sont pas systèmes
        return moduleRepository.save(module);
    }
    
    public Module updateModule(int id, Module module) {
        Module existingModule = moduleRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Module non trouvé avec l'ID: " + id));
        
        // Ne pas modifier le statut "système" pour les modules système
        if (existingModule.isModuleSysteme()) {
            module.setModuleSysteme(true);
            module.setCode(existingModule.getCode()); // Ne pas permettre de changer le code des modules système
        }
        
        module.setId(id);
        return moduleRepository.save(module);
    }
    
    // Méthode pour désactiver un module plutôt que le supprimer
    public void disableModule(Integer id) {
        Module module = moduleRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Module non trouvé avec l'ID: " + id));
        
        if (module.isModuleSysteme()) {
            throw new IllegalStateException("Impossible de désactiver un module système");
        }
        
        module.setActif(false);
        moduleRepository.save(module);
    }
    
    // Génération des permissions standard pour un module
    public List<Permission> generateStandardPermissionsForModule(Module module) {
        List<Permission> permissions = new ArrayList<>();
        
        // Déterminer le moduleType correspondant
        ModuleType moduleType = null;
        try {
            moduleType = ModuleType.valueOf(module.getCode());
        } catch (IllegalArgumentException e) {
            // Module personnalisé, pas dans l'enum
        }
        
        // Ajouter les permissions standard pour tous les modules
        for (ActionType action : ActionType.values()) {
            Permission permission = new Permission();
            permission.setNom(module.getCode() + "_" + action.name());
            permission.setDescription("Permission pour " + action.getLibelle().toLowerCase() + 
                                      " les données de " + module.getNom().toLowerCase());
            permission.setModule(module);
            permission.setNomAction(action.name());
            permission.setUrlPattern(module.getUrlPattern());
            permission.setPermissionActif(true);
            permission.setNiveauPriorite(action.getNiveauPriorite());
            permissions.add(permission);
        }
        
        // Ajouter les permissions spécifiques si c'est un module système
        if (moduleType != null) {
            for (ActionSpecifiqueType actionSpecifique : ActionSpecifiqueType.getActionsForModule(moduleType)) {
                Permission permission = new Permission();
                permission.setNom(module.getCode() + "_" + actionSpecifique.name());
                permission.setDescription("Permission pour " + actionSpecifique.getLibelle().toLowerCase());
                permission.setModule(module);
                permission.setNomAction(actionSpecifique.name());
                permission.setUrlPattern(module.getUrlPattern());
                permission.setPermissionActif(true);
                permission.setNiveauPriorite(actionSpecifique.getNiveauPriorite());
                permissions.add(permission);
            }
        }
        
        return permissions;
    }
}