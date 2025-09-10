package com.sh.erpcos.univers.securite.service;

import com.sh.erpcos.univers.securite.entity.AuditLog;
import com.sh.erpcos.univers.securite.entity.TwoFactorAuth;
import com.sh.erpcos.univers.securite.entity.UserSession;
import com.sh.erpcos.univers.securite.entity.Utilisateur;
import com.sh.erpcos.univers.securite.repository.AuditLogRepository;
import com.sh.erpcos.univers.securite.repository.TwoFactorAuthRepository;
import com.sh.erpcos.univers.securite.repository.UserSessionRepository;
import com.sh.erpcos.univers.securite.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Map;
import java.util.HashMap;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class SessionService {
    
    private final UserSessionRepository userSessionRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final TwoFactorAuthRepository twoFactorAuthRepository;;
    private final AuditService auditService;
    private final AuditLogRepository auditLogRepository;
    
    // Création et gestion des sessions
    public UserSession createSession(String username, HttpServletRequest request) {
        Optional<Utilisateur> utilisateurOpt = utilisateurRepository.findByUsername(username);
        if (utilisateurOpt.isEmpty()) {
            log.warn("Tentative de création de session pour utilisateur inexistant: {}", username);
            return null;
        }
        
        Utilisateur utilisateur = utilisateurOpt.get();
        HttpSession httpSession = request.getSession();
        
        UserSession userSession = new UserSession();
        userSession.setSessionId(httpSession.getId());
        userSession.setUtilisateur(utilisateur);
        userSession.setIpAddress(getClientIpAddress(request));
        userSession.setUserAgent(request.getHeader("User-Agent"));
        userSession.setDateCreation(LocalDateTime.now());
        userSession.setDateDerniereActivite(LocalDateTime.now());
        userSession.setDateExpiration(LocalDateTime.now().plusHours(8)); // 8 heures par défaut
        userSession.setSessionActive(true);
        userSession.setTypeConnexion("WEB");
        
        // Extraire des informations du User-Agent
        String userAgent = request.getHeader("User-Agent");
        if (userAgent != null) {
            userSession.setNavigateur(extractBrowser(userAgent));
            userSession.setSystemeExploitation(extractOS(userAgent));
        }
        
        UserSession savedSession = userSessionRepository.save(userSession);
        
        // Log de l'événement
        auditService.logSessionManagement("SESSION_CREATED", savedSession.getSessionId(), 
                                         "Nouvelle session créée pour " + username);
        
        log.info("Session créée pour l'utilisateur {} avec l'ID {}", username, savedSession.getSessionId());
        return savedSession;
    }
    
    public void updateSessionActivity(String sessionId) {
        Optional<UserSession> sessionOpt = userSessionRepository.findBySessionId(sessionId);
        if (sessionOpt.isPresent()) {
            UserSession session = sessionOpt.get();
            session.setDateDerniereActivite(LocalDateTime.now());
            userSessionRepository.save(session);
        }
    }
    
    public void terminateSession(String sessionId) {
        Optional<UserSession> sessionOpt = userSessionRepository.findBySessionId(sessionId);
        if (sessionOpt.isPresent()) {
            UserSession session = sessionOpt.get();
            session.terminateSession();
            userSessionRepository.save(session);
            
            auditService.logSessionManagement("SESSION_TERMINATED", sessionId, 
                                             "Session terminée pour " + session.getUtilisateur().getUsername());
            
            log.info("Session terminée: {}", sessionId);
        }
    }
    
    public void terminateAllUserSessions(Long utilisateurId) {
        List<UserSession> activeSessions = userSessionRepository.findByUtilisateurIdAndSessionActiveTrueAndDateExpirationAfterOrderByDateCreationDesc(utilisateurId, LocalDateTime.now());
        
        for (UserSession session : activeSessions) {
            session.terminateSession();
            userSessionRepository.save(session);
        }
        
        Optional<Utilisateur> utilisateurOpt = utilisateurRepository.findById(utilisateurId);
        String username = utilisateurOpt.map(Utilisateur::getUsername).orElse("UNKNOWN");
        
        auditService.logSessionManagement("ALL_SESSIONS_TERMINATED", "USER:" + utilisateurId, 
                                         "Toutes les sessions terminées pour " + username);
        
        log.info("Toutes les sessions terminées pour l'utilisateur ID: {}", utilisateurId);
    }
    
    public void extendSession(String sessionId, int hours) {
        Optional<UserSession> sessionOpt = userSessionRepository.findBySessionId(sessionId);
        if (sessionOpt.isPresent()) {
            UserSession session = sessionOpt.get();
            session.extendSession(hours);
            userSessionRepository.save(session);
            
            auditService.logSessionManagement("SESSION_EXTENDED", sessionId, 
                                             "Session prolongée de " + hours + " heures");
            
            log.info("Session {} prolongée de {} heures", sessionId, hours);
        }
    }
    
    // Consultation des sessions
    public Optional<UserSession> getSessionById(String sessionId) {
        return userSessionRepository.findBySessionId(sessionId);
    }
    
    public List<UserSession> getActiveSessionsForUser(Long utilisateurId) {
        return userSessionRepository.findByUtilisateurIdAndSessionActiveTrueAndDateExpirationAfterOrderByDateCreationDesc(utilisateurId, LocalDateTime.now());
    }
    
    public List<UserSession> getAllActiveSessions() {
        return userSessionRepository.findBySessionActiveTrueAndDateExpirationAfterOrderByDateCreationDesc(LocalDateTime.now());
    }
    
    public Page<UserSession> getAllActiveSessionsPage(Pageable pageable) {
        return userSessionRepository.findBySessionActiveTrueAndDateExpirationAfter(LocalDateTime.now(), pageable);
    }
    
    public List<UserSession> getSessionsByUser(Long utilisateurId) {
        return userSessionRepository.findByUtilisateurIdOrderByDateCreationDesc(utilisateurId);
    }
    
    public List<UserSession> getSessionsByIpAddress(String ipAddress) {
        return userSessionRepository.findByIpAddressOrderByDateCreationDesc(ipAddress);
    }
    
    public List<UserSession> getSessionsByPeriod(LocalDateTime dateDebut, LocalDateTime dateFin) {
        return userSessionRepository.findByDateCreationBetweenOrderByDateCreationDesc(dateDebut, dateFin);
    }
    
    public Page<UserSession> getSessionsByPeriod(LocalDateTime dateDebut, LocalDateTime dateFin, Pageable pageable) {
        return userSessionRepository.findByDateCreationBetween(dateDebut, dateFin, pageable);
    }
    
    // Recherche avancée
    public Page<UserSession> searchSessions(Long utilisateurId, String ipAddress, Boolean sessionActive,
                                           LocalDateTime dateDebut, LocalDateTime dateFin, Pageable pageable) {
        return userSessionRepository.rechercheAvancee(utilisateurId, ipAddress, sessionActive, 
                                                     dateDebut, dateFin, pageable);
    }
    
    // Statistiques
    public long getActiveSessionCount() {
        return userSessionRepository.countBySessionActiveTrueAndDateExpirationAfter(LocalDateTime.now());
    }
    
    public long getActiveUserCount() {
        return userSessionRepository.countActiveUsers(LocalDateTime.now());
    }
    
    public long getSessionCountByPeriod(LocalDateTime dateDebut, LocalDateTime dateFin) {
        return userSessionRepository.countByDateCreationBetween(dateDebut, dateFin);
    }
    
    public List<Object[]> getSessionStatsByUser(LocalDateTime dateDebut, LocalDateTime dateFin) {
        return userSessionRepository.getSessionStatsByUser(dateDebut, dateFin);
    }
    
    public List<Object[]> getSessionStatsByIP(LocalDateTime dateDebut, LocalDateTime dateFin) {
        return userSessionRepository.getSessionStatsByIP(dateDebut, dateFin);
    }
    
    public List<Object[]> getSessionStatsByBrowser(LocalDateTime dateDebut, LocalDateTime dateFin) {
        return userSessionRepository.getSessionStatsByBrowser(dateDebut, dateFin);
    }
    
    public Double getAverageSessionDuration(LocalDateTime dateDebut, LocalDateTime dateFin) {
        return userSessionRepository.getAverageSessionDuration(dateDebut, dateFin);
    }
    
    public Page<UserSession> getLongestSessions(Pageable pageable) {
        return userSessionRepository.findByDureeSessionIsNotNullOrderByDureeSessionDesc(pageable);
    }
    
    // Détection d'activités suspectes
    public List<Object[]> getSuspiciousSessions(LocalDateTime dateDebut, long intervalleMinutes) {
        return userSessionRepository.findSuspiciousSessions(dateDebut, intervalleMinutes);
    }
    
    // Génération de rapports
    public Map<String, Object> generateSessionReport(LocalDateTime dateDebut, LocalDateTime dateFin) {
        Map<String, Object> report = new HashMap<>();
        
        report.put("totalSessions", getSessionCountByPeriod(dateDebut, dateFin));
        report.put("activeSessions", getActiveSessionCount());
        report.put("activeUsers", getActiveUserCount());
        report.put("averageDuration", getAverageSessionDuration(dateDebut, dateFin));
        report.put("sessionsByUser", getSessionStatsByUser(dateDebut, dateFin));
        report.put("sessionsByIP", getSessionStatsByIP(dateDebut, dateFin));
        report.put("sessionsByBrowser", getSessionStatsByBrowser(dateDebut, dateFin));
        report.put("suspiciousSessions", getSuspiciousSessions(dateDebut, 30));
        
        return report;
    }
    
    // Tâches de maintenance automatiques
    @Scheduled(fixedRate = 300000) // Toutes les 5 minutes
    public void cleanupExpiredSessions() {
        List<UserSession> expiredSessions = userSessionRepository.findBySessionActiveTrueAndDateExpirationLessThanEqual(LocalDateTime.now());
        
        for (UserSession session : expiredSessions) {
            session.terminateSession();
            userSessionRepository.save(session);
        }
        
        if (!expiredSessions.isEmpty()) {
            log.info("Nettoyage automatique: {} sessions expirées terminées", expiredSessions.size());
            auditService.logSystemConfig("CLEANUP_EXPIRED_SESSIONS", "SESSION_MANAGEMENT", 
                                        expiredSessions.size() + " sessions expirées nettoyées");
        }
    }
    
    @Scheduled(cron = "0 0 2 * * ?") // Tous les jours à 2h du matin
    public void cleanupOldInactiveSessions() {
        LocalDateTime dateLimit = LocalDateTime.now().minusDays(30); // Garder 30 jours
        int deletedCount = userSessionRepository.deleteOldInactiveSessions(dateLimit);
        
        if (deletedCount > 0) {
            log.info("Nettoyage automatique: {} anciennes sessions inactives supprimées", deletedCount);
            auditService.logSystemConfig("CLEANUP_OLD_SESSIONS", "SESSION_MANAGEMENT", 
                                        deletedCount + " anciennes sessions supprimées");
        }
    }
    
    // Validation et vérification
    public boolean isSessionValid(String sessionId) {
        Optional<UserSession> sessionOpt = userSessionRepository.findBySessionId(sessionId);
        return sessionOpt.map(UserSession::isActive).orElse(false);
    }
    
    public boolean isUserSessionLimitExceeded(Long utilisateurId, int maxSessions) {
        List<UserSession> activeSessions = getActiveSessionsForUser(utilisateurId);
        return activeSessions.size() >= maxSessions;
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
    
    private String extractBrowser(String userAgent) {
        if (userAgent == null) return "Unknown";
        
        if (userAgent.contains("Chrome")) return "Chrome";
        if (userAgent.contains("Firefox")) return "Firefox";
        if (userAgent.contains("Safari")) return "Safari";
        if (userAgent.contains("Edge")) return "Edge";
        if (userAgent.contains("Opera")) return "Opera";
        
        return "Other";
    }
    
    private String extractOS(String userAgent) {
        if (userAgent == null) return "Unknown";
        
        if (userAgent.contains("Windows")) return "Windows";
        if (userAgent.contains("Mac OS")) return "macOS";
        if (userAgent.contains("Linux")) return "Linux";
        if (userAgent.contains("Android")) return "Android";
        if (userAgent.contains("iOS")) return "iOS";
        
        return "Other";
    }
    
    // Added
    // Retourne les sessions suspectes depuis une date donnée selon un seuil de tentatives (ici 5, par exemple)
    public List<UserSession> getSuspiciousSessions(LocalDateTime since) {
        long seuilTentatives = 5L; // Vous pouvez rendre ce seuil paramétrable si nécessaire
        
     // Étape 1 : Récupérer les IDs de sessions suspectes
        List<String> sessionIds = auditLogRepository.findSuspiciousSessionIds(since, seuilTentatives);
        
        if (sessionIds.isEmpty()) {
            return List.of(); // Aucune session suspecte
        }
        
        // Étape 2 : Récupérer les sessions complètes
        return userSessionRepository.findBySessionIds(sessionIds);
        
        
       // return auditLogRepository.findSuspiciousSessions(since, seuilTentatives);
    }
    
   
    public long getSessionsCountSince(LocalDateTime since) {
        return getSessionCountByPeriod(since, LocalDateTime.now());
    }
   
}