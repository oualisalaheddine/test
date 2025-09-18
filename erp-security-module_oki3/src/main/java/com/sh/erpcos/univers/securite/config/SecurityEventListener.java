package com.sh.erpcos.univers.securite.config;

import com.sh.erpcos.univers.securite.service.AuditService;
import com.sh.erpcos.univers.securite.service.UtilisateurService;
import com.sh.erpcos.univers.securite.service.SessionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AbstractAuthenticationFailureEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.authentication.event.LogoutSuccessEvent;
import org.springframework.security.web.session.HttpSessionDestroyedEvent;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Écouteur d'événements de sécurité pour suivre les tentatives de connexion et déconnexions
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SecurityEventListener {

    private final UtilisateurService utilisateurService;
    private final SessionService sessionService;
    private final AuditService auditService;

    /**
     * Écouteur pour les connexions réussies
     */
    @EventListener
    public void onAuthenticationSuccess(AuthenticationSuccessEvent event) {
        String username = event.getAuthentication().getName();
        log.debug("Authentification réussie pour l'utilisateur: {}", username);
        
        // Réinitialiser les tentatives échouées
        try {
            utilisateurService.getUtilisateurByUsername(username).ifPresent(utilisateur -> {
                // Si vous avez un compteur de tentatives échouées, le réinitialiser ici
                // utilisateur.setTentativesEchoueesConnexion(0);
                // utilisateurRepository.save(utilisateur);
            });
        } catch (Exception e) {
            log.error("Erreur lors de la réinitialisation des tentatives échouées", e);
        }

        // Créer une nouvelle session utilisateur dans notre système
        try {
            HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
            sessionService.createSession(username, request);
        } catch (Exception e) {
            log.error("Erreur lors de la création d'une session utilisateur", e);
        }
    }

    /**
     * Écouteur pour les échecs de connexion
     */
    @EventListener
    public void onAuthenticationFailure(AbstractAuthenticationFailureEvent event) {
        String username = event.getAuthentication().getName();
        log.debug("Échec d'authentification pour l'utilisateur: {}, cause: {}", 
                username, event.getException().getMessage());
        
        // Incrémenter les tentatives échouées
        try {
            utilisateurService.getUtilisateurByUsername(username).ifPresent(utilisateur -> {
                // Si vous avez un compteur de tentatives échouées, l'incrémenter ici
                // utilisateur.setTentativesEchoueesConnexion(utilisateur.getTentativesEchoueesConnexion() + 1);
                // 
                // Si dépasse le seuil, verrouiller le compte
                // if (utilisateur.getTentativesEchoueesConnexion() >= 5) {
                //     utilisateur.setCompteNonVerrouille(false);
                // }
                // utilisateurRepository.save(utilisateur);
            });
        } catch (Exception e) {
            log.error("Erreur lors du traitement de l'échec d'authentification", e);
        }
    }

    /**
     * Écouteur pour les déconnexions
     */
    @EventListener
    public void onLogoutSuccess(LogoutSuccessEvent event) {
        String username = event.getAuthentication().getName();
        log.debug("Déconnexion réussie pour l'utilisateur: {}", username);
        
        // Terminer la session utilisateur dans notre système
        try {
            HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
            String sessionId = request.getSession().getId();
            sessionService.terminateSession(sessionId);
        } catch (Exception e) {
            log.error("Erreur lors de la terminaison de la session utilisateur", e);
        }
    }

    /**
     * Écouteur pour les sessions détruites
     */
    @EventListener
    public void onSessionDestroyed(HttpSessionDestroyedEvent event) {
        String sessionId = event.getSession().getId();
        log.debug("Session détruite: {}", sessionId);
        
        // Terminer la session utilisateur dans notre système
        try {
            sessionService.terminateSession(sessionId);
        } catch (Exception e) {
            log.error("Erreur lors de la terminaison de la session utilisateur", e);
        }
    }
}