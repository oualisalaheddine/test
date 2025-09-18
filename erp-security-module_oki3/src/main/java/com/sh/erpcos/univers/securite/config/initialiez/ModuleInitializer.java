package com.sh.erpcos.univers.securite.config.initialiez;



import com.sh.erpcos.univers.securite.entity.Module;
import com.sh.erpcos.univers.securite.entity.Permission;
import com.sh.erpcos.univers.securite.enums.ModuleType;
import com.sh.erpcos.univers.securite.repository.ModuleRepository;
import com.sh.erpcos.univers.securite.repository.PermissionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@Order(1) // S'exécute en premier pour initialiser les modules
@RequiredArgsConstructor
@Slf4j
public class ModuleInitializer implements CommandLineRunner {

    private final ModuleRepository moduleRepository;
    private final PermissionRepository permissionRepository;

    @Override
    @Transactional
    public void run(String... args) {
        log.info("Initialisation des modules système...");
        
        // Initialiser tous les modules définis dans l'enum
        for (ModuleType moduleType : ModuleType.values()) {
            createOrUpdateModule(moduleType);
        }
        
        // Associer les permissions existantes aux modules correspondants
        associateExistingPermissions();
        
        log.info("Initialisation des modules système terminée.");
    }
    
    private void createOrUpdateModule(ModuleType moduleType) {
        String code = moduleType.name();
        
        moduleRepository.findByCode(code).ifPresentOrElse(
            // Module existant, mise à jour
            existingModule -> {
                // Mise à jour uniquement des attributs essentiels
                existingModule.setNom(moduleType.getLibelle());
                existingModule.setIcone(moduleType.getIcone());
                existingModule.setUrlPattern(moduleType.getUrlPattern());
                existingModule.setModuleSysteme(true); // Toujours marqué comme module système
                moduleRepository.save(existingModule);
                log.debug("Module mis à jour: {}", code);
            },
            // Nouveau module à créer
            () -> {
                Module newModule = moduleType.toEntity();
                moduleRepository.save(newModule);
                log.info("Module créé: {}", code);
            }
        );
    }
    
    private void associateExistingPermissions() {
        // Récupérer toutes les permissions qui n'ont pas encore de module associé
        List<Permission> permissionsToUpdate = permissionRepository.findAll().stream()
                .filter(p -> p.getModule() == null && p.getNomModule() != null)
                .toList();
        
        if (!permissionsToUpdate.isEmpty()) {
            log.info("Association de {} permissions existantes à leurs modules", permissionsToUpdate.size());
            
            for (Permission permission : permissionsToUpdate) {
                moduleRepository.findByCode(permission.getNomModule()).ifPresent(module -> {
                    permission.setModule(module);
                    permissionRepository.save(permission);
                    log.debug("Permission {} associée au module {}", permission.getNom(), module.getCode());
                });
            }
        }
    }
}