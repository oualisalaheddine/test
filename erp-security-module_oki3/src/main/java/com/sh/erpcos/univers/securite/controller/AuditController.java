package com.sh.erpcos.univers.securite.controller;

import com.sh.erpcos.univers.securite.entity.AuditLog;
import com.sh.erpcos.univers.securite.service.AuditService;
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

@Controller
//@RequestMapping("/securite/audit")
@RequestMapping("/audit")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasAuthority('SECURITE_LIRE')")
public class AuditController {
    
    private final AuditService auditService;
    
    @GetMapping
    public String index(Model model,
                       @RequestParam(defaultValue = "0") int page,
                       @RequestParam(defaultValue = "20") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<AuditLog> auditLogs = auditService.getRecentLogs(pageable);
        
        model.addAttribute("auditLogs", auditLogs);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", auditLogs.getTotalPages());
        model.addAttribute("totalElements", auditLogs.getTotalElements());
        
        // Statistiques rapides
        LocalDateTime derniereSemaine = LocalDateTime.now().minusDays(7);
        LocalDateTime maintenant = LocalDateTime.now();
        
        model.addAttribute("logsLastWeek", auditService.getLogCountByPeriod(derniereSemaine, maintenant));
        model.addAttribute("errorLogsPage", auditService.getRecentErrorLogs(PageRequest.of(0, 5)));
        model.addAttribute("criticalLogsPage", auditService.getRecentCriticalLogs(PageRequest.of(0, 5)));
        
        return "securite/audit/index";
    }
    
    @GetMapping("/search")
    public String search(Model model,
                        @RequestParam(required = false) String username,
                        @RequestParam(required = false) String action,
                        @RequestParam(required = false) String ressource,
                        @RequestParam(required = false) AuditLog.NiveauAudit niveau,
                        @RequestParam(required = false) AuditLog.CategorieAudit categorie,
                        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateDebut,
                        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateFin,
                        @RequestParam(required = false) Boolean succes,
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "20") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<AuditLog> auditLogs = auditService.searchLogs(username, action, ressource, niveau, 
                                                          categorie, dateDebut, dateFin, succes, pageable);
        
        model.addAttribute("auditLogs", auditLogs);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", auditLogs.getTotalPages());
        model.addAttribute("totalElements", auditLogs.getTotalElements());
        
        // Conserver les paramètres de recherche
        model.addAttribute("searchUsername", username);
        model.addAttribute("searchAction", action);
        model.addAttribute("searchRessource", ressource);
        model.addAttribute("searchNiveau", niveau);
        model.addAttribute("searchCategorie", categorie);
        model.addAttribute("searchDateDebut", dateDebut);
        model.addAttribute("searchDateFin", dateFin);
        model.addAttribute("searchSucces", succes);
        
        // Énumérations pour les filtres
        model.addAttribute("niveaux", AuditLog.NiveauAudit.values());
        model.addAttribute("categories", AuditLog.CategorieAudit.values());
        
        return "securite/audit/search";
    }
    
    @GetMapping("/user/{username}")
    public String userActivity(@PathVariable String username,
                              Model model,
                              @RequestParam(defaultValue = "0") int page,
                              @RequestParam(defaultValue = "20") int size,
                              @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateDebut,
                              @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateFin) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<AuditLog> auditLogs = auditService.getLogsByUser(username, pageable);
        
        model.addAttribute("auditLogs", auditLogs);
        model.addAttribute("username", username);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", auditLogs.getTotalPages());
        model.addAttribute("totalElements", auditLogs.getTotalElements());
        
        // Activité détaillée si période spécifiée
        if (dateDebut != null && dateFin != null) {
            List<AuditLog> detailedActivity = auditService.getUserActivity(username, dateDebut, dateFin);
            model.addAttribute("detailedActivity", detailedActivity);
            model.addAttribute("dateDebut", dateDebut);
            model.addAttribute("dateFin", dateFin);
        }
        
        return "securite/audit/user-activity";
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
        
        // Statistiques générales
        model.addAttribute("totalLogs", auditService.getLogCountByPeriod(dateDebut, dateFin));
        model.addAttribute("statistiquesByLevel", auditService.getStatisticsByLevel(dateDebut, dateFin));
        model.addAttribute("statistiquesByCategory", auditService.getStatisticsByCategory(dateDebut, dateFin));
        model.addAttribute("statistiquesByUser", auditService.getStatisticsByUser(dateDebut, dateFin));
        model.addAttribute("statistiquesByAction", auditService.getStatisticsByAction(dateDebut, dateFin));
        
        // Activités suspectes
        model.addAttribute("suspiciousActivities", auditService.getSuspiciousActivities(dateDebut, 5));
        model.addAttribute("failedLogins", auditService.getFailedLoginAttempts());
        
        model.addAttribute("dateDebut", dateDebut);
        model.addAttribute("dateFin", dateFin);
        
        return "securite/audit/statistics";
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
        
        Map<String, Object> report = auditService.generateSecurityReport(dateDebut, dateFin);
        
        model.addAttribute("report", report);
        model.addAttribute("dateDebut", dateDebut);
        model.addAttribute("dateFin", dateFin);
        model.addAttribute("generatedAt", LocalDateTime.now());
        
        return "securite/audit/report";
    }
    
    @GetMapping("/export")
    @PreAuthorize("hasAuthority('SECURITE_EXPORTER')")
    @ResponseBody
    public List<AuditLog> exportLogs(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateDebut,
                                    @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateFin,
                                    @RequestParam(required = false) AuditLog.NiveauAudit niveau,
                                    @RequestParam(required = false) AuditLog.CategorieAudit categorie) {
        
        // Période par défaut : derniers 30 jours
        if (dateDebut == null) {
            dateDebut = LocalDateTime.now().minusDays(30);
        }
        if (dateFin == null) {
            dateFin = LocalDateTime.now();
        }
        
        List<AuditLog> logs = auditService.exportLogs(dateDebut, dateFin, niveau, categorie);
        
        // Log de l'export
        auditService.logDataAccess("AUDIT_LOGS", null, "EXPORT", 
                                  "Export de " + logs.size() + " logs d'audit");
        
        return logs;
    }
    
    @PostMapping("/cleanup")
    @PreAuthorize("hasAuthority('SECURITE_SUPPRIMER')")
    public String cleanupOldLogs(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateLimit,
                               RedirectAttributes redirectAttributes) {
        
        try {
            auditService.cleanOldLogs(dateLimit);
            redirectAttributes.addFlashAttribute("successMessage", 
                    "Nettoyage des logs antérieurs au " + dateLimit + " effectué avec succès");
        } catch (Exception e) {
            log.error("Erreur lors du nettoyage des logs", e);
            redirectAttributes.addFlashAttribute("errorMessage", 
                    "Erreur lors du nettoyage des logs: " + e.getMessage());
        }
        
        return "redirect:/securite/audit";
    }
    
    @GetMapping("/errors")
    public String errorLogs(Model model,
                          @RequestParam(defaultValue = "0") int page,
                          @RequestParam(defaultValue = "20") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<AuditLog> errorLogs = auditService.getRecentErrorLogs(pageable);
        
        model.addAttribute("auditLogs", errorLogs);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", errorLogs.getTotalPages());
        model.addAttribute("totalElements", errorLogs.getTotalElements());
        model.addAttribute("pageTitle", "Logs d'erreur");
        
        return "securite/audit/errors";
    }
    
    @GetMapping("/critical")
    public String criticalLogs(Model model,
                             @RequestParam(defaultValue = "0") int page,
                             @RequestParam(defaultValue = "20") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<AuditLog> criticalLogs = auditService.getRecentCriticalLogs(pageable);
        
        model.addAttribute("auditLogs", criticalLogs);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", criticalLogs.getTotalPages());
        model.addAttribute("totalElements", criticalLogs.getTotalElements());
        model.addAttribute("pageTitle", "Logs critiques");
        
        return "securite/audit/critical";
    }
    
    @GetMapping("/security-events")
    public String securityEvents(Model model,
                               @RequestParam(defaultValue = "0") int page,
                               @RequestParam(defaultValue = "20") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        List<AuditLog> securityEvents = auditService.getLogsByCategory(AuditLog.CategorieAudit.SECURITY_EVENT);
        
        // Convertir en page pour la cohérence
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), securityEvents.size());
        List<AuditLog> pageContent = securityEvents.subList(start, end);
        
        model.addAttribute("auditLogs", pageContent);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", (int) Math.ceil((double) securityEvents.size() / size));
        model.addAttribute("totalElements", securityEvents.size());
        model.addAttribute("pageTitle", "Événements de sécurité");
        
        return "securite/audit/security-events";
    }
    
    @GetMapping("/failed-logins")
    public String failedLogins(Model model) {
        List<AuditLog> failedLogins = auditService.getFailedLoginAttempts();
        
        model.addAttribute("failedLogins", failedLogins);
        model.addAttribute("totalFailedLogins", failedLogins.size());
        
        // Activités suspectes (plus de 5 tentatives échouées)
        LocalDateTime derniereSemaine = LocalDateTime.now().minusDays(7);
        List<Object[]> suspiciousActivities = auditService.getSuspiciousActivities(derniereSemaine, 5);
        model.addAttribute("suspiciousActivities", suspiciousActivities);
        
        return "securite/audit/failed-logins";
    }
}