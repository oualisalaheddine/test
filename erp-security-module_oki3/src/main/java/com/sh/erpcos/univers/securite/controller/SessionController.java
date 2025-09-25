package com.sh.erpcos.univers.securite.controller;

import com.sh.erpcos.univers.securite.entity.UserSession;
import com.sh.erpcos.univers.securite.service.SessionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
//@RequestMapping("/securite/sessions")
@RequestMapping("/sessions")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasAuthority('SECURITE_LIRE')")
public class SessionController {
    
    private final SessionService sessionService;
    
    @GetMapping
    public String index(Model model,
                       @RequestParam(defaultValue = "0") int page,
                       @RequestParam(defaultValue = "20") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<UserSession> activeSessions = sessionService.getAllActiveSessionsPage(pageable);
        
        model.addAttribute("sessions", activeSessions);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", activeSessions.getTotalPages());
        model.addAttribute("totalElements", activeSessions.getTotalElements());
        
        // Statistiques rapides
        model.addAttribute("activeSessionCount", sessionService.getActiveSessionCount());
        model.addAttribute("activeUserCount", sessionService.getActiveUserCount());
        
        LocalDateTime derniereSemaine = LocalDateTime.now().minusDays(7);
        LocalDateTime maintenant = LocalDateTime.now();
        model.addAttribute("sessionsLastWeek", sessionService.getSessionCountByPeriod(derniereSemaine, maintenant));
        
        return "securite/sessions/dashboard";
    }
    
    @GetMapping("/search")
    public String search(Model model,
                        @RequestParam(required = false) Long utilisateurId,
                        @RequestParam(required = false) String ipAddress,
                        @RequestParam(required = false) Boolean sessionActive,
                        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateDebut,
                        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateFin,
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "20") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<UserSession> sessions = sessionService.searchSessions(utilisateurId, ipAddress, sessionActive, 
                                                                  dateDebut, dateFin, pageable);
        
        model.addAttribute("sessions", sessions);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", sessions.getTotalPages());
        model.addAttribute("totalElements", sessions.getTotalElements());
        
        // Conserver les paramètres de recherche
        model.addAttribute("searchUtilisateurId", utilisateurId);
        model.addAttribute("searchIpAddress", ipAddress);
        model.addAttribute("searchSessionActive", sessionActive);
        model.addAttribute("searchDateDebut", dateDebut);
        model.addAttribute("searchDateFin", dateFin);
        
        return "securite/sessions/search";
    }
    
    @GetMapping("/user/{utilisateurId}")
    public String userSessions(@PathVariable Long utilisateurId,
                              Model model,
                              @RequestParam(defaultValue = "0") int page,
                              @RequestParam(defaultValue = "20") int size) {
        
        List<UserSession> userSessions = sessionService.getSessionsByUser(utilisateurId);
        List<UserSession> activeSessions = sessionService.getActiveSessionsForUser(utilisateurId);
        
        // Pagination manuelle pour les sessions utilisateur
        int start = page * size;
        int end = Math.min(start + size, userSessions.size());
        List<UserSession> pageContent = userSessions.subList(start, end);
        
        model.addAttribute("sessions", pageContent);
        model.addAttribute("activeSessions", activeSessions);
        model.addAttribute("utilisateurId", utilisateurId);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", (int) Math.ceil((double) userSessions.size() / size));
        model.addAttribute("totalElements", userSessions.size());
        model.addAttribute("activeSessionCount", activeSessions.size());
        
        return "securite/sessions/user-sessions";
    }
    
    @GetMapping("/ip/{ipAddress}")
    public String ipSessions(@PathVariable String ipAddress, Model model) {
        List<UserSession> ipSessions = sessionService.getSessionsByIpAddress(ipAddress);
        
        model.addAttribute("sessions", ipSessions);
        model.addAttribute("ipAddress", ipAddress);
        model.addAttribute("totalSessions", ipSessions.size());
        
        return "securite/sessions/ip-sessions";
    }
    
    @GetMapping("/statistics")
    public String statistics(Model model,
                           @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateDebut,
                           @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateFin) {
        
        // Période par défaut : derniers 30 jours
        if (dateDebut == null) {
            dateDebut = LocalDateTime.now().minusDays(30);
        }
        if (dateFin == null) {
            dateFin = LocalDateTime.now();
        }
        
        Map<String, Object> report = sessionService.generateSessionReport(dateDebut, dateFin);
        
        model.addAttribute("report", report);
        model.addAttribute("dateDebut", dateDebut);
        model.addAttribute("dateFin", dateFin);
        
        // Sessions les plus longues
        Pageable pageable = PageRequest.of(0, 10);
        Page<UserSession> longestSessions = sessionService.getLongestSessions(pageable);
        model.addAttribute("longestSessions", longestSessions.getContent());
        
        return "securite/sessions/statistics";
    }
    
    @GetMapping("/suspicious")
    public String suspiciousSessions(Model model,
                                   @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateDebut,
                                   @RequestParam(required = false) Long intervalleMinutes) {
        
        // Paramètres par défaut
        if (dateDebut == null) {
            dateDebut = LocalDateTime.now().minusDays(7);
        }
        if (intervalleMinutes == null) {
            intervalleMinutes = 30L; // 30 minutes
        }
        
        List<Object[]> suspiciousSessions = sessionService.getSuspiciousSessions(dateDebut, intervalleMinutes);
        
        model.addAttribute("suspiciousSessions", suspiciousSessions);
        model.addAttribute("dateDebut", dateDebut);
        model.addAttribute("intervalleMinutes", intervalleMinutes);
        
        return "securite/sessions/suspicious";
    }
    
    @PostMapping("/terminate/{sessionId}")
    @PreAuthorize("hasAuthority('SECURITE_MODIFIER')")
    public String terminateSession(@PathVariable String sessionId,
                                 RedirectAttributes redirectAttributes) {
        
        try {
            Optional<UserSession> sessionOpt = sessionService.getSessionById(sessionId);
            if (sessionOpt.isPresent()) {
                UserSession session = sessionOpt.get();
                sessionService.terminateSession(sessionId);
                
                redirectAttributes.addFlashAttribute("successMessage", 
                        "Session terminée avec succès pour l'utilisateur: " + 
                        session.getUtilisateur().getUsername());
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", 
                        "Session non trouvée: " + sessionId);
            }
        } catch (Exception e) {
            log.error("Erreur lors de la terminaison de la session", e);
            redirectAttributes.addFlashAttribute("errorMessage", 
                    "Erreur lors de la terminaison de la session: " + e.getMessage());
        }
        
        return "redirect:/securite/sessions";
    }
    
    @PostMapping("/terminate-user/{utilisateurId}")
    @PreAuthorize("hasAuthority('SECURITE_MODIFIER')")
    public String terminateAllUserSessions(@PathVariable Long utilisateurId,
                                         RedirectAttributes redirectAttributes) {
        
        try {
            sessionService.terminateAllUserSessions(utilisateurId);
            redirectAttributes.addFlashAttribute("successMessage", 
                    "Toutes les sessions de l'utilisateur ont été terminées avec succès");
        } catch (Exception e) {
            log.error("Erreur lors de la terminaison des sessions utilisateur", e);
            redirectAttributes.addFlashAttribute("errorMessage", 
                    "Erreur lors de la terminaison des sessions: " + e.getMessage());
        }
        
        return "redirect:/securite/sessions/user/" + utilisateurId;
    }
    
    @PostMapping("/extend/{sessionId}")
    @PreAuthorize("hasAuthority('SECURITE_MODIFIER')")
    public String extendSession(@PathVariable String sessionId,
                              @RequestParam(defaultValue = "2") int hours,
                              RedirectAttributes redirectAttributes) {
        
        try {
            Optional<UserSession> sessionOpt = sessionService.getSessionById(sessionId);
            if (sessionOpt.isPresent()) {
                UserSession session = sessionOpt.get();
                sessionService.extendSession(sessionId, hours);
                
                redirectAttributes.addFlashAttribute("successMessage", 
                        "Session prolongée de " + hours + " heures pour l'utilisateur: " + 
                        session.getUtilisateur().getUsername());
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", 
                        "Session non trouvée: " + sessionId);
            }
        } catch (Exception e) {
            log.error("Erreur lors de la prolongation de la session", e);
            redirectAttributes.addFlashAttribute("errorMessage", 
                    "Erreur lors de la prolongation de la session: " + e.getMessage());
        }
        
        return "redirect:/securite/sessions";
    }
    
    @GetMapping("/details/{sessionId}")
    public String sessionDetails(@PathVariable String sessionId, Model model) {
        Optional<UserSession> sessionOpt = sessionService.getSessionById(sessionId);
        
        if (sessionOpt.isPresent()) {
            UserSession session = sessionOpt.get();
            model.addAttribute("session", session);
            
            // Vérifier si la session est valide
            model.addAttribute("isValid", sessionService.isSessionValid(sessionId));
            
            return "securite/sessions/details";
        } else {
            model.addAttribute("errorMessage", "Session non trouvée: " + sessionId);
            return "redirect:/securite/sessions";
        }
    }
    
    @GetMapping("/active")
    public String activeSessions(Model model,
                               @RequestParam(defaultValue = "0") int page,
                               @RequestParam(defaultValue = "20") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<UserSession> activeSessions = sessionService.getAllActiveSessionsPage(pageable);
        
        model.addAttribute("sessions", activeSessions);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", activeSessions.getTotalPages());
        model.addAttribute("totalElements", activeSessions.getTotalElements());
        model.addAttribute("pageTitle", "Sessions actives");
        
        return "securite/sessions/active";
    }
    
    @GetMapping("/history")
    public String sessionHistory(Model model,
                               @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateDebut,
                               @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateFin,
                               @RequestParam(defaultValue = "0") int page,
                               @RequestParam(defaultValue = "20") int size) {
        
        // Période par défaut : derniers 7 jours
        if (dateDebut == null) {
            dateDebut = LocalDateTime.now().minusDays(7);
        }
        if (dateFin == null) {
            dateFin = LocalDateTime.now();
        }
        
        Pageable pageable = PageRequest.of(page, size);
        Page<UserSession> sessions = sessionService.getSessionsByPeriod(dateDebut, dateFin, pageable);
        
        model.addAttribute("sessions", sessions);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", sessions.getTotalPages());
        model.addAttribute("totalElements", sessions.getTotalElements());
        model.addAttribute("dateDebut", dateDebut);
        model.addAttribute("dateFin", dateFin);
        model.addAttribute("pageTitle", "Historique des sessions");
        
        return "securite/sessions/history";
    }
    
    @PostMapping("/cleanup-expired")
    @PreAuthorize("hasAuthority('SECURITE_SUPPRIMER')")
    public String cleanupExpiredSessions(RedirectAttributes redirectAttributes) {
        
        try {
            // Le nettoyage automatique est déjà géré par la tâche planifiée
            // Ici on peut forcer un nettoyage manuel
            sessionService.cleanupExpiredSessions();
            
            redirectAttributes.addFlashAttribute("successMessage", 
                    "Nettoyage des sessions expirées effectué avec succès");
        } catch (Exception e) {
            log.error("Erreur lors du nettoyage des sessions expirées", e);
            redirectAttributes.addFlashAttribute("errorMessage", 
                    "Erreur lors du nettoyage des sessions: " + e.getMessage());
        }
        
        return "redirect:/securite/sessions";
    }
    
    @GetMapping("/report")
    @PreAuthorize("hasAuthority('SECURITE_EXPORTER')")
    public String generateReport(Model model,
                               @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateDebut,
                               @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateFin) {
        
        // Période par défaut : derniers 30 jours
        if (dateDebut == null) {
            dateDebut = LocalDateTime.now().minusDays(30);
        }
        if (dateFin == null) {
            dateFin = LocalDateTime.now();
        }
        
        Map<String, Object> report = sessionService.generateSessionReport(dateDebut, dateFin);
        
        model.addAttribute("report", report);
        model.addAttribute("dateDebut", dateDebut);
        model.addAttribute("dateFin", dateFin);
        model.addAttribute("generatedAt", LocalDateTime.now());
        
        return "securite/sessions/report";
    }
}