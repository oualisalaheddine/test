package com.sh.erpcos.univers.securite.service;

import com.sh.erpcos.univers.securite.entity.*;
import com.sh.erpcos.univers.securite.repository.*;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

@Service
//@RequiredArgsConstructor
@Slf4j
@Transactional
public class BackupRestoreService {
    
    private final UtilisateurRepository utilisateurRepository;
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final AuditLogRepository auditLogRepository;
    private final UserSessionRepository userSessionRepository;
    private final PasswordPolicyRepository passwordPolicyRepository;
    private final TwoFactorAuthRepository twoFactorAuthRepository;
    private final AuditService auditService;
    
    private  ObjectMapper objectMapper;
    @PostConstruct
    private void init() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }
    
    @Autowired
    public BackupRestoreService(UtilisateurRepository utilisateurRepository,
                               RoleRepository roleRepository,
                               PermissionRepository permissionRepository,
                               AuditLogRepository auditLogRepository,
                               UserSessionRepository userSessionRepository,
                               PasswordPolicyRepository passwordPolicyRepository,
                               TwoFactorAuthRepository twoFactorAuthRepository,
                               AuditService auditService) {
        this.utilisateurRepository = utilisateurRepository;
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
        this.auditLogRepository = auditLogRepository;
        this.userSessionRepository = userSessionRepository;
        this.passwordPolicyRepository = passwordPolicyRepository;
        this.twoFactorAuthRepository = twoFactorAuthRepository;
        this.auditService = auditService;
        
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }
    
    // Sauvegarde complète
    public String createFullBackup() throws IOException {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String backupFileName = "erp_security_backup_" + timestamp + ".zip";
        
        log.info("Début de la sauvegarde complète: {}", backupFileName);
        
        try (FileOutputStream fos = new FileOutputStream(backupFileName);
             ZipOutputStream zos = new ZipOutputStream(fos)) {
            
            // Sauvegarder les utilisateurs
            backupUtilisateurs(zos);
            
            // Sauvegarder les rôles
            backupRoles(zos);
            
            // Sauvegarder les permissions
            backupPermissions(zos);
            
            // Sauvegarder les politiques de mot de passe
            backupPasswordPolicies(zos);
            
            // Sauvegarder la configuration 2FA (sans les clés secrètes)
            backup2FAConfig(zos);
            
            // Sauvegarder les logs d'audit (derniers 30 jours)
            backupAuditLogs(zos, LocalDateTime.now().minusDays(30));
            
            // Sauvegarder les sessions actives
            backupActiveSessions(zos);
            
            // Créer un fichier de métadonnées
            createBackupMetadata(zos, timestamp);
      
        //added
        } catch (IOException e) {
            log.error("Erreur lors de la création de la sauvegarde complète: {}", backupFileName, e);
            // Optionnel: supprimer le fichier partiel en cas d'erreur
            new File(backupFileName).delete();
            throw e; // Rethrow pour que l'appelant gère l'exception
        }   
        
        //endadded
        auditService.logSystemConfig("FULL_BACKUP_CREATED", "BACKUP_RESTORE", 
                                    "Sauvegarde complète créée: " + backupFileName);
        
        log.info("Sauvegarde complète terminée: {}", backupFileName);
        return backupFileName;
    }
    
    // Sauvegarde partielle (utilisateurs et rôles uniquement)
    public String createUserRoleBackup() throws IOException {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String backupFileName = "erp_security_users_roles_backup_" + timestamp + ".zip";
        
        log.info("Début de la sauvegarde utilisateurs/rôles: {}", backupFileName);
        
        try (FileOutputStream fos = new FileOutputStream(backupFileName);
             ZipOutputStream zos = new ZipOutputStream(fos)) {
            
            backupUtilisateurs(zos);
            backupRoles(zos);
            backupPermissions(zos);
            createBackupMetadata(zos, timestamp, "USER_ROLE_BACKUP");
        }
        
        auditService.logSystemConfig("USER_ROLE_BACKUP_CREATED", "BACKUP_RESTORE", 
                                    "Sauvegarde utilisateurs/rôles créée: " + backupFileName);
        
        log.info("Sauvegarde utilisateurs/rôles terminée: {}", backupFileName);
        return backupFileName;
    }
    
    // Sauvegarde des logs d'audit
    public String createAuditLogBackup(LocalDateTime dateDebut, LocalDateTime dateFin) throws IOException {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String backupFileName = "erp_security_audit_logs_backup_" + timestamp + ".zip";
        
        log.info("Début de la sauvegarde des logs d'audit: {}", backupFileName);
        
        try (FileOutputStream fos = new FileOutputStream(backupFileName);
             ZipOutputStream zos = new ZipOutputStream(fos)) {
            
            backupAuditLogs(zos, dateDebut, dateFin);
            createBackupMetadata(zos, timestamp, "AUDIT_LOG_BACKUP");
        }
        
        auditService.logSystemConfig("AUDIT_LOG_BACKUP_CREATED", "BACKUP_RESTORE", 
                                    "Sauvegarde des logs d'audit créée: " + backupFileName);
        
        log.info("Sauvegarde des logs d'audit terminée: {}", backupFileName);
        return backupFileName;
    }
    
    // Restauration complète
    @Transactional
    public void restoreFullBackup(String backupFileName, boolean overwriteExisting) throws IOException {
        log.info("Début de la restauration complète depuis: {}", backupFileName);
        
        // Vérifier que le fichier existe
        File backupFile = new File(backupFileName);
        if (!backupFile.exists()) {
            throw new FileNotFoundException("Fichier de sauvegarde non trouvé: " + backupFileName);
        }
        
        Map<String, Object> metadata = null;
        
        try (FileInputStream fis = new FileInputStream(backupFile);
             ZipInputStream zis = new ZipInputStream(fis)) {
            
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                String entryName = entry.getName();
                
                switch (entryName) {
                    case "metadata.json":
                        metadata = readMetadata(zis);
                        break;
                    case "permissions.json":
                        restorePermissions(zis, overwriteExisting);
                        break;
                    case "roles.json":
                        restoreRoles(zis, overwriteExisting);
                        break;
                    case "utilisateurs.json":
                        restoreUtilisateurs(zis, overwriteExisting);
                        break;
                    case "password_policies.json":
                        restorePasswordPolicies(zis, overwriteExisting);
                        break;
                    case "2fa_config.json":
                        restore2FAConfig(zis, overwriteExisting);
                        break;
                    case "audit_logs.json":
                        if (overwriteExisting) {
                            restoreAuditLogs(zis);
                        }
                        break;
                    case "active_sessions.json":
                        if (overwriteExisting) {
                            restoreActiveSessions(zis);
                        }
                        break;
                }
                
                zis.closeEntry();
            }
        }
        
        auditService.logSystemConfig("FULL_BACKUP_RESTORED", "BACKUP_RESTORE", 
                                    "Restauration complète depuis: " + backupFileName + 
                                    " (overwrite: " + overwriteExisting + ")");
        
        log.info("Restauration complète terminée depuis: {}", backupFileName);
    }
    
    // Restauration partielle (utilisateurs et rôles)
    @Transactional
    public void restoreUserRoleBackup(String backupFileName, boolean overwriteExisting) throws IOException {
        log.info("Début de la restauration utilisateurs/rôles depuis: {}", backupFileName);
        
        File backupFile = new File(backupFileName);
        if (!backupFile.exists()) {
            throw new FileNotFoundException("Fichier de sauvegarde non trouvé: " + backupFileName);
        }
        
        try (FileInputStream fis = new FileInputStream(backupFile);
             ZipInputStream zis = new ZipInputStream(fis)) {
            
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                String entryName = entry.getName();
                
                switch (entryName) {
                    case "permissions.json":
                        restorePermissions(zis, overwriteExisting);
                        break;
                    case "roles.json":
                        restoreRoles(zis, overwriteExisting);
                        break;
                    case "utilisateurs.json":
                        restoreUtilisateurs(zis, overwriteExisting);
                        break;
                }
                
                zis.closeEntry();
            }
        }
        
        auditService.logSystemConfig("USER_ROLE_BACKUP_RESTORED", "BACKUP_RESTORE", 
                                    "Restauration utilisateurs/rôles depuis: " + backupFileName);
        
        log.info("Restauration utilisateurs/rôles terminée depuis: {}", backupFileName);
    }
    
    // Méthodes privées de sauvegarde
    private void backupUtilisateurs(ZipOutputStream zos) throws IOException {
        List<Utilisateur> utilisateurs = utilisateurRepository.findAll();
        
        // Nettoyer les données sensibles pour la sauvegarde
        List<Map<String, Object>> utilisateursData = new ArrayList<>();
        for (Utilisateur user : utilisateurs) {
            Map<String, Object> userData = new HashMap<>();
            userData.put("id", user.getId());
            userData.put("username", user.getUsername());
            userData.put("nom", user.getNom());
            userData.put("prenom", user.getPrenom());
            userData.put("email", user.getEmail());
            userData.put("dateCreation", user.getDateCreation());
            userData.put("derniereConnexion", user.getDerniereConnexion());
            userData.put("compteActif", user.isCompteActif());
            userData.put("compteNonExpire", user.isCompteNonExpire());
            userData.put("compteNonVerrouille", user.isCompteNonVerrouille());
            userData.put("credentialsNonExpire", user.isCredentialsNonExpire());
            
            // Sauvegarder les IDs des rôles
            List<Long> roleIds = user.getRoles().stream()
                    .map(Role::getId)
                    .toList();
            userData.put("roleIds", roleIds);
            
            utilisateursData.add(userData);
        }
        
        writeJsonToZip(zos, "utilisateurs.json", utilisateursData);
        log.debug("Sauvegarde de {} utilisateurs", utilisateurs.size());
    }
    
    private void backupRoles(ZipOutputStream zos) throws IOException {
        List<Role> roles = roleRepository.findAll();
        
        List<Map<String, Object>> rolesData = new ArrayList<>();
        for (Role role : roles) {
            Map<String, Object> roleData = new HashMap<>();
            roleData.put("id", role.getId());
            roleData.put("nom", role.getNom());
            roleData.put("description", role.getDescription());
            roleData.put("niveauHierarchie", role.getNiveauHierarchie());
            roleData.put("roleParentId", role.getRoleParent() != null ? role.getRoleParent().getId() : null);
            roleData.put("roleActif", role.isRoleActif());
            roleData.put("dateCreation", role.getDateCreation());
            
            // Sauvegarder les IDs des permissions
            List<Long> permissionIds = role.getPermissions().stream()
                    .map(Permission::getId)
                    .toList();
            roleData.put("permissionIds", permissionIds);
            
            rolesData.add(roleData);
        }
        
        writeJsonToZip(zos, "roles.json", rolesData);
        log.debug("Sauvegarde de {} rôles", roles.size());
    }
    
    private void backupPermissions(ZipOutputStream zos) throws IOException {
        List<Permission> permissions = permissionRepository.findAll();
        writeJsonToZip(zos, "permissions.json", permissions);
        log.debug("Sauvegarde de {} permissions", permissions.size());
    }
    
    private void backupPasswordPolicies(ZipOutputStream zos) throws IOException {
        List<PasswordPolicy> policies = passwordPolicyRepository.findAll();
        writeJsonToZip(zos, "password_policies.json", policies);
        log.debug("Sauvegarde de {} politiques de mot de passe", policies.size());
    }
    
    private void backup2FAConfig(ZipOutputStream zos) throws IOException {
        List<TwoFactorAuth> configs = twoFactorAuthRepository.findAll();
        
        // Nettoyer les données sensibles
        List<Map<String, Object>> configsData = new ArrayList<>();
        for (TwoFactorAuth config : configs) {
            Map<String, Object> configData = new HashMap<>();
            configData.put("id", config.getId());
            configData.put("utilisateurId", config.getUtilisateur().getId());
            configData.put("enabled", config.isEnabled());
            configData.put("verified", config.isVerified());
            configData.put("methodType", config.getMethodType());
            configData.put("phoneNumber", config.getPhoneNumber());
            configData.put("emailBackup", config.getEmailBackup());
            configData.put("dateCreation", config.getDateCreation());
            configData.put("dateActivation", config.getDateActivation());
            // Ne pas sauvegarder secretKey, backupCodes, etc. pour des raisons de sécurité
            
            configsData.add(configData);
        }
        
        writeJsonToZip(zos, "2fa_config.json", configsData);
        log.debug("Sauvegarde de {} configurations 2FA", configs.size());
    }
    
    private void backupAuditLogs(ZipOutputStream zos, LocalDateTime dateDebut) throws IOException {
        backupAuditLogs(zos, dateDebut, LocalDateTime.now());
    }
    
    private void backupAuditLogs(ZipOutputStream zos, LocalDateTime dateDebut, LocalDateTime dateFin) throws IOException {
        List<AuditLog> logs = auditLogRepository.findByDateActionBetweenOrderByDateActionDesc(dateDebut, dateFin);
        writeJsonToZip(zos, "audit_logs.json", logs);
        log.debug("Sauvegarde de {} logs d'audit", logs.size());
    }
    /** rplaced 
    private void backupActiveSessions(ZipOutputStream zos) throws IOException {
        List<UserSession> sessions = userSessionRepository.findActiveSessionsAt(LocalDateTime.now());
        
        // Nettoyer les données sensibles
        List<Map<String, Object>> sessionsData = new ArrayList<>();
        for (UserSession session : sessions) {
            Map<String, Object> sessionData = new HashMap<>();
            sessionData.put("utilisateurId", session.getUtilisateur().getId());
            sessionData.put("ipAddress", session.getIpAddress());
            sessionData.put("userAgent", session.getUserAgent());
            sessionData.put("dateCreation", session.getDateCreation());
            sessionData.put("dateDerniereActivite", session.getDateDerniereActivite());
            sessionData.put("typeConnexion", session.getTypeConnexion());
            sessionData.put("navigateur", session.getNavigateur());
            sessionData.put("systemeExploitation", session.getSystemeExploitation());
            // Ne pas sauvegarder sessionId pour des raisons de sécurité
            
            sessionsData.add(sessionData);
        }
        
        writeJsonToZip(zos, "active_sessions.json", sessionsData);
        log.debug("Sauvegarde de {} sessions actives", sessions.size());
    }
    **/
    /** par **/
    private void backupActiveSessions(ZipOutputStream zos) throws IOException {
        // Correction de l'appel à la méthode du repository
        List<UserSession> sessions = userSessionRepository
                .findBySessionActiveTrueAndDateExpirationAfterOrderByDateCreationDesc(LocalDateTime.now());
        
        // Nettoyer les données sensibles
        List<Map<String, Object>> sessionsData = new ArrayList<>();
        for (UserSession session : sessions) {
            Map<String, Object> sessionData = new HashMap<>();
            sessionData.put("utilisateurId", session.getUtilisateur().getId());
            sessionData.put("ipAddress", session.getIpAddress());
            sessionData.put("userAgent", session.getUserAgent());
            sessionData.put("dateCreation", session.getDateCreation());
            sessionData.put("dateDerniereActivite", session.getDateDerniereActivite());
            sessionData.put("typeConnexion", session.getTypeConnexion());
            sessionData.put("navigateur", session.getNavigateur());
            sessionData.put("systemeExploitation", session.getSystemeExploitation());
            // Ne pas sauvegarder sessionId pour des raisons de sécurité
            
            sessionsData.add(sessionData);
        }
        
        writeJsonToZip(zos, "active_sessions.json", sessionsData);
        log.debug("Sauvegarde de {} sessions actives", sessions.size());
    }
    private void createBackupMetadata(ZipOutputStream zos, String timestamp) throws IOException {
        createBackupMetadata(zos, timestamp, "FULL_BACKUP");
    }
    
    private void createBackupMetadata(ZipOutputStream zos, String timestamp, String backupType) throws IOException {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("backupType", backupType);
        metadata.put("timestamp", timestamp);
        metadata.put("createdAt", LocalDateTime.now());
        metadata.put("version", "1.0");
        metadata.put("application", "ERP Security Module");
        
        writeJsonToZip(zos, "metadata.json", metadata);
    }
    
    // Méthodes privées de restauration
    private void restorePermissions(ZipInputStream zis, boolean overwriteExisting) throws IOException {
        List<Permission> permissions = readJsonFromZip(zis, Permission.class);
        
        for (Permission permission : permissions) {
            Optional<Permission> existing = permissionRepository.findByNom(permission.getNom());
            
            if (existing.isPresent()) {
                if (overwriteExisting) {
                    Permission existingPermission = existing.get();
                    existingPermission.setDescription(permission.getDescription());
                    existingPermission.setNomModule(permission.getNomModule());
                    existingPermission.setNomAction(permission.getNomAction());
                    existingPermission.setUrlPattern(permission.getUrlPattern());
                    existingPermission.setPermissionActif(permission.isPermissionActif());
                    existingPermission.setNiveauPriorite(permission.getNiveauPriorite());
                    permissionRepository.save(existingPermission);
                }
            } else {
                permission.setId(null); // Laisser JPA générer un nouvel ID
                permissionRepository.save(permission);
            }
        }
        
        log.debug("Restauration de {} permissions", permissions.size());
    }
    
    private void restoreRoles(ZipInputStream zis, boolean overwriteExisting) throws IOException {
        // Implémentation similaire pour les rôles
        // Note: Gérer les relations parent-enfant et les permissions
        log.debug("Restauration des rôles (implémentation à compléter)");
    }
    
    private void restoreUtilisateurs(ZipInputStream zis, boolean overwriteExisting) throws IOException {
        // Implémentation similaire pour les utilisateurs
        // Note: Ne pas restaurer les mots de passe, les régénérer
        log.debug("Restauration des utilisateurs (implémentation à compléter)");
    }
    
    private void restorePasswordPolicies(ZipInputStream zis, boolean overwriteExisting) throws IOException {
        List<PasswordPolicy> policies = readJsonFromZip(zis, PasswordPolicy.class);
        
        for (PasswordPolicy policy : policies) {
            Optional<PasswordPolicy> existing = passwordPolicyRepository.findByNomPolitique(policy.getNomPolitique());
            
            if (existing.isPresent()) {
                if (overwriteExisting) {
                    PasswordPolicy existingPolicy = existing.get();
                    // Mettre à jour les champs
                    existingPolicy.setDescription(policy.getDescription());
                    existingPolicy.setLongueurMinimale(policy.getLongueurMinimale());
                    // ... autres champs
                    passwordPolicyRepository.save(existingPolicy);
                }
            } else {
                policy.setId(null);
                passwordPolicyRepository.save(policy);
            }
        }
        
        log.debug("Restauration de {} politiques de mot de passe", policies.size());
    }
    
    private void restore2FAConfig(ZipInputStream zis, boolean overwriteExisting) throws IOException {
        // Restaurer uniquement la configuration de base, pas les clés secrètes
        log.debug("Restauration de la configuration 2FA (implémentation à compléter)");
    }
    
    private void restoreAuditLogs(ZipInputStream zis) throws IOException {
        List<AuditLog> logs = readJsonFromZip(zis, AuditLog.class);
        
        for (AuditLog log : logs) {
            log.setId(null); // Nouveau ID
            auditLogRepository.save(log);
        }
        
        log.debug("Restauration de {} logs d'audit", logs.size());
    }
    
    private void restoreActiveSessions(ZipInputStream zis) throws IOException {
        // Ne pas restaurer les sessions actives pour des raisons de sécurité
        log.debug("Restauration des sessions actives ignorée pour des raisons de sécurité");
    }
    
    // Méthodes utilitaires
    private void writeJsonToZip(ZipOutputStream zos, String fileName, Object data) throws IOException {
        ZipEntry entry = new ZipEntry(fileName);
        zos.putNextEntry(entry);
        
        String json = objectMapper.writeValueAsString(data);
        zos.write(json.getBytes());
        
        zos.closeEntry();
    }
    
    private <T> List<T> readJsonFromZip(ZipInputStream zis, Class<T> clazz) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len;
        while ((len = zis.read(buffer)) > 0) {
            baos.write(buffer, 0, len);
        }
        
        String json = baos.toString();
        return objectMapper.readValue(json, 
                objectMapper.getTypeFactory().constructCollectionType(List.class, clazz));
    }
    
    private Map<String, Object> readMetadata(ZipInputStream zis) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len;
        while ((len = zis.read(buffer)) > 0) {
            baos.write(buffer, 0, len);
        }
        
        String json = baos.toString();
        return objectMapper.readValue(json, Map.class);
    }
    
    // Validation et vérification
    public boolean validateBackupFile(String backupFileName) {
        try {
            File backupFile = new File(backupFileName);
            if (!backupFile.exists()) {
                return false;
            }
            
            try (FileInputStream fis = new FileInputStream(backupFile);
                 ZipInputStream zis = new ZipInputStream(fis)) {
                
                boolean hasMetadata = false;
                ZipEntry entry;
                while ((entry = zis.getNextEntry()) != null) {
                    if ("metadata.json".equals(entry.getName())) {
                        hasMetadata = true;
                        break;
                    }
                    zis.closeEntry();
                }
                
                return hasMetadata;
            }
        } catch (IOException e) {
            log.error("Erreur lors de la validation du fichier de sauvegarde", e);
            return false;
        }
    }
    
    public Map<String, Object> getBackupInfo(String backupFileName) throws IOException {
        File backupFile = new File(backupFileName);
        if (!backupFile.exists()) {
            throw new FileNotFoundException("Fichier de sauvegarde non trouvé: " + backupFileName);
        }
        
        try (FileInputStream fis = new FileInputStream(backupFile);
             ZipInputStream zis = new ZipInputStream(fis)) {
            
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if ("metadata.json".equals(entry.getName())) {
                    return readMetadata(zis);
                }
                zis.closeEntry();
            }
        }
        
        throw new IOException("Métadonnées non trouvées dans le fichier de sauvegarde");
    }
    //added
 // Méthode pour lister les fichiers de sauvegarde
    public List<String> listBackups() {
        // Vous pouvez définir ici le répertoire où sont stockées vos sauvegardes.
        // Ici, nous utilisons le répertoire courant.
        File backupDir = new File(System.getProperty("user.dir"));
        
        // Filtrer les fichiers ZIP dont le nom commence par "erp_security_"
        File[] backupFiles = backupDir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.startsWith("erp_security_") && name.endsWith(".zip");
            }
        });
        
        if (backupFiles == null) {
            return Collections.emptyList();
        }
        
        // Convertir en liste, trier par date de modification décroissante
        List<File> backups = Arrays.asList(backupFiles);
        backups.sort((f1, f2) -> Long.compare(f2.lastModified(), f1.lastModified()));
        
        // Retourner la liste des noms de fichiers
        return backups.stream().map(File::getName).collect(Collectors.toList());
    }
}