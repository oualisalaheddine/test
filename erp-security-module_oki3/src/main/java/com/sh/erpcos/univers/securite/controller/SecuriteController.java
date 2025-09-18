package com.sh.erpcos.univers.securite.controller;

import com.sh.erpcos.univers.securite.entity.AuditLog;
import com.sh.erpcos.univers.securite.entity.UserSession;
import com.sh.erpcos.univers.securite.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/securite")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasAuthority('SECURITE_LIRE')")
public class SecuriteController {
    
    private final UtilisateurService utilisateurService;
    private final RoleService roleService;
    private final PermissionService permissionService;
    private final AuditService auditService;
    private final SessionService sessionService;
    private final PasswordPolicyService passwordPolicyService;
    private final TwoFactorAuthService twoFactorAuthService;
    private final BackupRestoreService backupRestoreService;
    
    @GetMapping
    public String dashboardSecurite(Model model) {
        // Statistiques générales
        long nombreUtilisateurs = utilisateurService.getAllUtilisateurs().size();
        long nombreUtilisateursActifs = utilisateurService.getNombreUtilisateursActifs();
        long nombreRoles = roleService.getAllRoles().size();
        long nombreRolesActifs = roleService.getNombreRolesActifs();
        long nombrePermissions = permissionService.getAllPermissions().size();
        long nombrePermissionsActives = permissionService.getNombrePermissionsActives();
        
        // Statistiques avancées
        long sessionsActives = sessionService.getActiveSessionCount();
        long auditLogsAujourdhui = auditService.getTotalActionsSince(LocalDateTime.now().minusDays(1));
        long auditLogsEchecs = auditService.getFailedActionsCountSince(LocalDateTime.now().minusDays(1));
        long utilisateurs2FA = twoFactorAuthService.getEnabled2FACount();
        
        model.addAttribute("nombreUtilisateurs", nombreUtilisateurs);
        model.addAttribute("nombreUtilisateursActifs", nombreUtilisateursActifs);
        model.addAttribute("nombreRoles", nombreRoles);
        model.addAttribute("nombreRolesActifs", nombreRolesActifs);
        model.addAttribute("nombrePermissions", nombrePermissions);
        model.addAttribute("nombrePermissionsActives", nombrePermissionsActives);
        model.addAttribute("sessionsActives", sessionsActives);
        model.addAttribute("auditLogsAujourdhui", auditLogsAujourdhui);
        model.addAttribute("auditLogsEchecs", auditLogsEchecs);
        model.addAttribute("utilisateurs2FA", utilisateurs2FA);
        
        // Utilisateurs récents
        model.addAttribute("utilisateursRecents", utilisateurService.getUtilisateursRecents());
        
        // Modules disponibles
        model.addAttribute("modules", permissionService.getModulesDisponibles());
        
        // Statistiques Pont Bascule
        model.addAttribute("pontBasculeStats", getPontBasculeStatistics());
        
        // Alertes de sécurité
        model.addAttribute("alertes", getSecurityAlerts());
        
        // Activité récente
        model.addAttribute("activiteRecente", getRecentActivity());
        
        // Statistiques par période
        model.addAttribute("statistiquesPeriode", getPeriodStatistics());
        
        // Top utilisateurs actifs
        model.addAttribute("topUtilisateursActifs", auditService.getTopUsersByActivity(
            LocalDateTime.now().minusDays(7), org.springframework.data.domain.PageRequest.of(0, 5)));
        
        // Sessions suspectes
        model.addAttribute("sessionsSuspectes", sessionService.getSuspiciousSessions(LocalDateTime.now().minusDays(1)));
        
        // Comptes verrouillés 2FA
        model.addAttribute("comptesVerrouilles2FA", twoFactorAuthService.getLockedAccounts());
        
        // Sauvegardes récentes
        model.addAttribute("sauvegardesRecentes", backupRestoreService.listBackups().stream().limit(5).toList());
        
        log.info("Affichage du tableau de bord de sécurité avancé");
        return "securite/dashboard";
    }
    
    @GetMapping("/modules")
    public String gestionModules(Model model) {
        model.addAttribute("modules", permissionService.getModulesDisponibles());
        return "securite/modules";
    }
    
    @GetMapping("/audit")
    public String auditLogs(Model model) {
        // Logs d'audit récents
        model.addAttribute("auditLogsRecents", auditService.getLogsByPeriod(
        		LocalDateTime.now().minusDays(7), LocalDateTime.now()));
        // Statistiques d'audit
        model.addAttribute("statistiquesAudit", getAuditStatistics());
        
        return "securite/audit";
    }
    
    @GetMapping("/sessions")
    public String gestionSessions(Model model) {

    	/** ----replaced by  
    	// Sessions actives
        model.addAttribute("sessionsActives", sessionService.getActiveSessions());
        
        // Sessions récentes
        model.addAttribute("sessionsRecentes", sessionService.getSessionsSince(LocalDateTime.now().minusDays(1)));
        
         ------**/
    	
     // Sessions actives
        model.addAttribute("sessionsActives", sessionService.getAllActiveSessions());
        
        // Sessions récentes (depuis 1 jour)
        model.addAttribute("sessionsRecentes", sessionService.getSessionsByPeriod(
                LocalDateTime.now().minusDays(1), LocalDateTime.now()));
        
        // end by -î
        
        // Statistiques de sessions
        model.addAttribute("statistiquesSessions", getSessionStatistics());
        
        return "securite/sessions";
    }
    
    @GetMapping("/politique-mots-de-passe")
    public String politiqueMotsDePasse(Model model) {
    	model.addAttribute("politiqueActive", passwordPolicyService.getDefaultPolicy());
    	model.addAttribute("toutesPolitiques", passwordPolicyService.getAllPolicies());
        model.addAttribute("exigences", passwordPolicyService.getPasswordRequirements());
        
     
        
        
        return "securite/politique-mots-de-passe";
    }
    
    @GetMapping("/2fa")
    public String gestion2FA(Model model) {
        model.addAttribute("configurations2FA", twoFactorAuthService.getActive2FAConfigs());
        model.addAttribute("statistiques2FA", get2FAStatistics());
        model.addAttribute("comptesVerrouilles", twoFactorAuthService.getLockedAccounts());
        
        return "securite/2fa";
    }
    
    @GetMapping("/sauvegardes")
    public String gestionSauvegardes(Model model) {
        model.addAttribute("sauvegardes", backupRestoreService.listBackups());
        return "securite/sauvegardes";
    }
    
    // Méthodes privées pour les données du dashboard
    private List<Map<String, Object>> getSecurityAlerts() {
        List<Map<String, Object>> alertes = new java.util.ArrayList<>();
        
        // Vérifier les échecs de connexion récents
        long echecsRecents = auditService.getFailedActionsCountSince(LocalDateTime.now().minusHours(1));
        if (echecsRecents > 10) {
            Map<String, Object> alerte = new HashMap<>();
            alerte.put("type", "warning");
            alerte.put("titre", "Tentatives de connexion échouées");
            alerte.put("message", echecsRecents + " tentatives échouées dans la dernière heure");
            alerte.put("timestamp", LocalDateTime.now());
            alertes.add(alerte);
        }
        
        // Vérifier les sessions multiples
        List<UserSession> sessionsSuspectes = sessionService.getSuspiciousSessions(LocalDateTime.now().minusHours(1));
        if (!sessionsSuspectes.isEmpty()) {
            Map<String, Object> alerte = new HashMap<>();
            alerte.put("type", "info");
            alerte.put("titre", "Activité suspecte détectée");
            alerte.put("message", sessionsSuspectes.size() + " sessions avec des patterns suspects");
            alerte.put("timestamp", LocalDateTime.now());
            alertes.add(alerte);
        }
        
        // Vérifier les comptes 2FA verrouillés
        List<com.sh.erpcos.univers.securite.entity.TwoFactorAuth> comptesVerrouilles = twoFactorAuthService.getLockedAccounts();
        if (!comptesVerrouilles.isEmpty()) {
            Map<String, Object> alerte = new HashMap<>();
            alerte.put("type", "danger");
            alerte.put("titre", "Comptes 2FA verrouillés");
            alerte.put("message", comptesVerrouilles.size() + " comptes verrouillés temporairement");
            alerte.put("timestamp", LocalDateTime.now());
            alertes.add(alerte);
        }
        
        return alertes;
    }
    
    private List<Map<String, Object>> getRecentActivity() {
        List<Map<String, Object>> activite = new java.util.ArrayList<>();
        
        // Dernières actions d'audit
        List<AuditLog> logsRecents = auditService.getLogsByPeriod(
            LocalDateTime.now().minusHours(6), LocalDateTime.now());
        
        for (AuditLog log : logsRecents.stream().limit(10).toList()) {
            Map<String, Object> activiteItem = new HashMap<>();
            activiteItem.put("type", "audit");
            activiteItem.put("utilisateur", log.getUsername());
            activiteItem.put("action", log.getAction());
            activiteItem.put("ressource", log.getRessource());
            activiteItem.put("timestamp", log.getDateAction());
            activiteItem.put("succes", log.getSucces());
            activite.add(activiteItem);
        }
        
        return activite;
    }
    
    private Map<String, Object> getPeriodStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        // Statistiques des 7 derniers jours
        LocalDateTime ilYA7Jours = LocalDateTime.now().minusDays(7);
        
        stats.put("actions7Jours", auditService.getTotalActionsSince(ilYA7Jours));
        stats.put("echecs7Jours", auditService.getFailedActionsCountSince(ilYA7Jours));
        stats.put("sessions7Jours", sessionService.getSessionsCountSince(ilYA7Jours));
        stats.put("utilisateursActifs7Jours", auditService.getTopUsersByActivity(
            ilYA7Jours, org.springframework.data.domain.PageRequest.of(0, 1)).getTotalElements());
        
        return stats;
    }
    
    private Map<String, Object> getAuditStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        LocalDateTime aujourdhui = LocalDateTime.now().minusDays(1);
        LocalDateTime cetteSemaine = LocalDateTime.now().minusDays(7);
        LocalDateTime ceMois = LocalDateTime.now().minusDays(30);
        
        stats.put("aujourdhui", auditService.getTotalActionsSince(aujourdhui));
        stats.put("cetteSemaine", auditService.getTotalActionsSince(cetteSemaine));
        stats.put("ceMois", auditService.getTotalActionsSince(ceMois));
        stats.put("echecsAujourdhui", auditService.getFailedActionsCountSince(aujourdhui));
        stats.put("echecsCetteSemaine", auditService.getFailedActionsCountSince(cetteSemaine));
        stats.put("echecsCeMois", auditService.getFailedActionsCountSince(ceMois));
        
        return stats;
    }
    
    private Map<String, Object> getSessionStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        stats.put("sessionsActives", sessionService.getActiveSessionCount());
        stats.put("sessionsAujourdhui", sessionService.getSessionsCountSince(LocalDateTime.now().minusDays(1)));
        stats.put("sessionsCetteSemaine", sessionService.getSessionsCountSince(LocalDateTime.now().minusDays(7)));
        stats.put("sessionsSuspectes", sessionService.getSuspiciousSessions(LocalDateTime.now().minusDays(1)).size());
        
        return stats;
    }
    
    private Map<String, Object> get2FAStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        stats.put("total2FA", twoFactorAuthService.getEnabled2FACount());
        stats.put("2FATOTP", twoFactorAuthService.getActive2FACountByMethod(com.sh.erpcos.univers.securite.entity.TwoFactorAuth.MethodType.TOTP));
        stats.put("2FASMS", twoFactorAuthService.getActive2FACountByMethod(com.sh.erpcos.univers.securite.entity.TwoFactorAuth.MethodType.SMS));
        stats.put("2FAEmail", twoFactorAuthService.getActive2FACountByMethod(com.sh.erpcos.univers.securite.entity.TwoFactorAuth.MethodType.EMAIL));
        stats.put("comptesVerrouilles", twoFactorAuthService.getLockedAccounts().size());
        stats.put("activationsRecentes", twoFactorAuthService.getRecentlyEnabled(LocalDateTime.now().minusDays(7)).size());
        
        return stats;
    }
    
    private Map<String, Object> getPontBasculeStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        // Statistiques simulées pour le Pont Bascule
        // Dans une vraie application, ces données viendraient d'un service PontBasculeService
        stats.put("pesagesAujourdhui", 45);
        stats.put("pesagesCetteSemaine", 320);
        stats.put("pesagesCeMois", 1280);
        stats.put("vehiculesEnregistres", 156);
        stats.put("chauffeursActifs", 89);
        stats.put("derniereActivite", "Pesage véhicule #VH-2024-001 - 15:30");
        stats.put("statutSysteme", "Actif");
        stats.put("alertesActives", 2);
        stats.put("poidsTotalJour", "2,450 tonnes");
        stats.put("moyennePesage", "54.4 tonnes");
        
        return stats;
    }
}
