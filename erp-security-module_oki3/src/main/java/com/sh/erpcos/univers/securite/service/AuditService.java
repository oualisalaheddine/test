package com.sh.erpcos.univers.securite.service;

import com.sh.erpcos.univers.securite.entity.AuditLog;
import com.sh.erpcos.univers.securite.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AuditService {
    
    private final AuditLogRepository auditLogRepository;
    
    // Méthodes de logging asynchrones
    @Async
    public CompletableFuture<Void> logAsync(String action, String ressource, AuditLog.NiveauAudit niveau, 
                                           AuditLog.CategorieAudit categorie, String details) {
        try {
            log(action, ressource, niveau, categorie, details, true, null);
        } catch (Exception e) {
            log.error("Erreur lors de l'audit asynchrone", e);
        }
        return CompletableFuture.completedFuture(null);
    }
    
    @Async
    public CompletableFuture<Void> logAsync(String action, String ressource, AuditLog.NiveauAudit niveau, 
                                           AuditLog.CategorieAudit categorie, String details, 
                                           HttpServletRequest request) {
        try {
            log(action, ressource, niveau, categorie, details, true, null, request);
        } catch (Exception e) {
            log.error("Erreur lors de l'audit asynchrone avec requête", e);
        }
        return CompletableFuture.completedFuture(null);
    }
    
    // Méthodes de logging synchrones
    public AuditLog log(String action, String ressource, AuditLog.NiveauAudit niveau, 
                       AuditLog.CategorieAudit categorie) {
        return log(action, ressource, niveau, categorie, null, true, null);
    }
    
    public AuditLog log(String action, String ressource, AuditLog.NiveauAudit niveau, 
                       AuditLog.CategorieAudit categorie, String details) {
        return log(action, ressource, niveau, categorie, details, true, null);
    }
    
    public AuditLog log(String action, String ressource, AuditLog.NiveauAudit niveau, 
                       AuditLog.CategorieAudit categorie, String details, boolean succes, String messageErreur) {
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = (auth != null && auth.isAuthenticated()) ? auth.getName() : "SYSTEM";
        
        AuditLog auditLog = new AuditLog();
        auditLog.setUsername(username);
        auditLog.setAction(action);
        auditLog.setRessource(ressource);
        auditLog.setNiveau(niveau);
        auditLog.setCategorie(categorie);
        auditLog.setDetails(details);
        auditLog.setSucces(succes);
        auditLog.setMessageErreur(messageErreur);
        auditLog.setDateAction(LocalDateTime.now());
        
        return auditLogRepository.save(auditLog);
    }
    
    public AuditLog log(String action, String ressource, AuditLog.NiveauAudit niveau, 
                       AuditLog.CategorieAudit categorie, String details, boolean succes, 
                       String messageErreur, HttpServletRequest request) {
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = (auth != null && auth.isAuthenticated()) ? auth.getName() : "SYSTEM";
        
        AuditLog auditLog = new AuditLog();
        auditLog.setUsername(username);
        auditLog.setAction(action);
        auditLog.setRessource(ressource);
        auditLog.setNiveau(niveau);
        auditLog.setCategorie(categorie);
        auditLog.setDetails(details);
        auditLog.setSucces(succes);
        auditLog.setMessageErreur(messageErreur);
        auditLog.setDateAction(LocalDateTime.now());
        
        if (request != null) {
            auditLog.setIpAddress(getClientIpAddress(request));
            auditLog.setUserAgent(request.getHeader("User-Agent"));
            auditLog.setSessionId(request.getSession().getId());
        }
        
        return auditLogRepository.save(auditLog);
    }
    
    // Méthodes spécialisées pour différents types d'événements
    public AuditLog logAuthentication(String username, String action, boolean succes, String details, HttpServletRequest request) {
        AuditLog auditLog = new AuditLog();
        auditLog.setUsername(username);
        auditLog.setAction(action);
        auditLog.setRessource("AUTHENTICATION");
        auditLog.setNiveau(succes ? AuditLog.NiveauAudit.INFO : AuditLog.NiveauAudit.WARNING);
        auditLog.setCategorie(AuditLog.CategorieAudit.AUTHENTICATION);
        auditLog.setDetails(details);
        auditLog.setSucces(succes);
        auditLog.setDateAction(LocalDateTime.now());
        
        if (request != null) {
            auditLog.setIpAddress(getClientIpAddress(request));
            auditLog.setUserAgent(request.getHeader("User-Agent"));
            auditLog.setSessionId(request.getSession().getId());
        }
        
        return auditLogRepository.save(auditLog);
    }
    
    public AuditLog logDataAccess(String ressource, Long ressourceId, String action, String details) {
        return log(action, ressource + (ressourceId != null ? ":" + ressourceId : ""), 
                  AuditLog.NiveauAudit.INFO, AuditLog.CategorieAudit.DATA_ACCESS, details);
    }
    
    public AuditLog logDataModification(String ressource, Long ressourceId, String action, String details) {
        AuditLog auditLog = log(action, ressource + (ressourceId != null ? ":" + ressourceId : ""), 
                               AuditLog.NiveauAudit.INFO, AuditLog.CategorieAudit.DATA_MODIFICATION, details);
        auditLog.setRessourceId(ressourceId);
        return auditLogRepository.save(auditLog);
    }
    
    public AuditLog logSecurityEvent(String action, String details, AuditLog.NiveauAudit niveau) {
        return log(action, "SECURITY", niveau, AuditLog.CategorieAudit.SECURITY_EVENT, details);
    }
    
    public AuditLog logUserManagement(String action, String targetUser, String details) {
        return log(action, "USER:" + targetUser, AuditLog.NiveauAudit.INFO, 
                  AuditLog.CategorieAudit.USER_MANAGEMENT, details);
    }
    
    public AuditLog logRoleManagement(String action, String roleName, String details) {
        return log(action, "ROLE:" + roleName, AuditLog.NiveauAudit.INFO, 
                  AuditLog.CategorieAudit.ROLE_MANAGEMENT, details);
    }
    
    public AuditLog logPermissionManagement(String action, String permissionName, String details) {
        return log(action, "PERMISSION:" + permissionName, AuditLog.NiveauAudit.INFO, 
                  AuditLog.CategorieAudit.PERMISSION_MANAGEMENT, details);
    }
    
    public AuditLog logSessionManagement(String action, String sessionId, String details) {
        return log(action, "SESSION:" + sessionId, AuditLog.NiveauAudit.INFO, 
                  AuditLog.CategorieAudit.SESSION_MANAGEMENT, details);
    }
    
    public AuditLog logSystemConfig(String action, String configName, String details) {
        return log(action, "CONFIG:" + configName, AuditLog.NiveauAudit.WARNING, 
                  AuditLog.CategorieAudit.SYSTEM_CONFIG, details);
    }
    
    // Méthodes de recherche et consultation
    public List<AuditLog> getLogsByUser(String username) {
        return auditLogRepository.findByUsernameOrderByDateActionDesc(username);
    }
    
    public Page<AuditLog> getLogsByUser(String username, Pageable pageable) {
        return auditLogRepository.findByUsernameOrderByDateActionDesc(username, pageable);
    }
    
    public List<AuditLog> getLogsByPeriod(LocalDateTime dateDebut, LocalDateTime dateFin) {
        return auditLogRepository.findByDateActionBetweenOrderByDateActionDesc(dateDebut, dateFin);
    }
    
    public Page<AuditLog> getLogsByPeriod(LocalDateTime dateDebut, LocalDateTime dateFin, Pageable pageable) {
        return auditLogRepository.findByDateActionBetweenOrderByDateActionDesc(dateDebut, dateFin, pageable);
    }
    
    public List<AuditLog> getLogsByLevel(AuditLog.NiveauAudit niveau) {
        return auditLogRepository.findByNiveauOrderByDateActionDesc(niveau);
    }
    
    public List<AuditLog> getLogsByCategory(AuditLog.CategorieAudit categorie) {
        return auditLogRepository.findByCategorieOrderByDateActionDesc(categorie);
    }
    
    public Page<AuditLog> searchLogs(String username, String action, String ressource, 
                                    AuditLog.NiveauAudit niveau, AuditLog.CategorieAudit categorie,
                                    LocalDateTime dateDebut, LocalDateTime dateFin, Boolean succes,
                                    Pageable pageable) {
        return auditLogRepository.rechercheAvancee(username, action, ressource, niveau, categorie, 
                                                  dateDebut, dateFin, succes, pageable);
    }
    
    public Page<AuditLog> getRecentLogs(Pageable pageable) {
        return auditLogRepository.findAllByOrderByDateActionDesc(pageable);
    }
    
    public Page<AuditLog> getRecentErrorLogs(Pageable pageable) {
        return auditLogRepository.findBySuccesFalseOrderByDateActionDesc(pageable);
    }
    
    public Page<AuditLog> getRecentCriticalLogs(Pageable pageable) {
        return auditLogRepository.findByNiveauOrderByDateActionDesc(AuditLog.NiveauAudit.CRITICAL, pageable);
    }
    
    // Méthodes de statistiques
    public long getLogCountByPeriod(LocalDateTime dateDebut, LocalDateTime dateFin) {
        return auditLogRepository.countByDateActionBetween(dateDebut, dateFin);
    }
    
    public List<Object[]> getStatisticsByLevel(LocalDateTime dateDebut, LocalDateTime dateFin) {
        return auditLogRepository.getStatistiquesParNiveau(dateDebut, dateFin);
    }
    
    public List<Object[]> getStatisticsByCategory(LocalDateTime dateDebut, LocalDateTime dateFin) {
        return auditLogRepository.getStatistiquesParCategorie(dateDebut, dateFin);
    }
    
    public List<Object[]> getStatisticsByUser(LocalDateTime dateDebut, LocalDateTime dateFin) {
        return auditLogRepository.getStatistiquesParUtilisateur(dateDebut, dateFin);
    }
    
    public List<Object[]> getStatisticsByAction(LocalDateTime dateDebut, LocalDateTime dateFin) {
        return auditLogRepository.getStatistiquesParAction(dateDebut, dateFin);
    }
    
    // Détection d'activités suspectes
    public List<Object[]> getSuspiciousActivities(LocalDateTime dateDebut, long seuilTentatives) {
        return auditLogRepository.findActivitesSuspectes(AuditLog.CategorieAudit.AUTHENTICATION, dateDebut, seuilTentatives);
    }
    
    public List<AuditLog> getFailedLoginAttempts() {
        return auditLogRepository.findByCategorieAndSuccesFalseOrderByDateActionDesc(AuditLog.CategorieAudit.AUTHENTICATION);
    }
    
    public List<AuditLog> getUserActivity(String username, LocalDateTime dateDebut, LocalDateTime dateFin) {
        return auditLogRepository.findByUsernameAndDateActionBetweenOrderByDateActionDesc(username, dateDebut, dateFin);
    }
    
    // Génération de rapports
    public Map<String, Object> generateSecurityReport(LocalDateTime dateDebut, LocalDateTime dateFin) {
        Map<String, Object> report = new HashMap<>();
        
        report.put("totalLogs", getLogCountByPeriod(dateDebut, dateFin));
        report.put("statistiquesByLevel", getStatisticsByLevel(dateDebut, dateFin));
        report.put("statistiquesByCategory", getStatisticsByCategory(dateDebut, dateFin));
        report.put("statistiquesByUser", getStatisticsByUser(dateDebut, dateFin));
        report.put("statistiquesByAction", getStatisticsByAction(dateDebut, dateFin));
        report.put("suspiciousActivities", getSuspiciousActivities(dateDebut, 5));
        report.put("failedLogins", getFailedLoginAttempts().size());
        
        return report;
    }
    
    // Maintenance et nettoyage
    @Transactional
    public void cleanOldLogs(LocalDateTime dateLimit) {
        log.info("Nettoyage des logs antérieurs à {}", dateLimit);
        auditLogRepository.deleteByDateActionBefore(dateLimit);
        logSystemConfig("CLEAN_OLD_LOGS", "AUDIT_LOGS", 
                       "Nettoyage des logs antérieurs à " + dateLimit);
    }
    
    // Utilitaires
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty() && !"unknown".equalsIgnoreCase(xForwardedFor)) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty() && !"unknown".equalsIgnoreCase(xRealIp)) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }
    
    // Méthodes de validation et vérification
    public boolean isValidAuditLog(AuditLog auditLog) {
        return auditLog != null && 
               auditLog.getAction() != null && !auditLog.getAction().isEmpty() &&
               auditLog.getNiveau() != null &&
               auditLog.getCategorie() != null &&
               auditLog.getDateAction() != null;
    }
    
    // Export des logs
    public List<AuditLog> exportLogs(LocalDateTime dateDebut, LocalDateTime dateFin, 
                                    AuditLog.NiveauAudit niveau, AuditLog.CategorieAudit categorie) {
        return auditLogRepository.rechercheAvancee(null, null, null, niveau, categorie, 
                                                  dateDebut, dateFin, null, Pageable.unpaged()).getContent();
    }
    
    //added
    /**
     * Compte le nombre total d'actions d'audit depuis une date donnée
     * @param sinceDate Date de début de la période
     * @return Nombre total de logs depuis la date spécifiée
     */
    public long getTotalActionsSince(LocalDateTime sinceDate) {
        return auditLogRepository.countByDateActionAfter(sinceDate);
    }

    /**
     * Compte le nombre d'actions d'audit en échec depuis une date donnée
     * @param sinceDate Date de début de la période
     * @return Nombre de logs d'échec depuis la date spécifiée
     */
    public long getFailedActionsCountSince(LocalDateTime sinceDate) {
        return auditLogRepository.countBySuccesFalseAndDateActionAfter(sinceDate);
    }
    
 // Retourne les top utilisateurs par activité depuis une date donnée avec pagination
    public Page<Object[]> getTopUsersByActivity(LocalDateTime since, Pageable pageable) {
        return auditLogRepository.findTopUsersByActivity(since, pageable);
    }

  
    
    
}