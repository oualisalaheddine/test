package com.sh.erpcos.univers.securite.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;

@Controller
@RequiredArgsConstructor
@Slf4j
public class MainController {
    
    @GetMapping("/")
    public String home() {
        return "redirect:/erp/dashboard";
    }
    
    @GetMapping("/login")
    public String login(@RequestParam(value = "error", required = false) String error,
                       @RequestParam(value = "logout", required = false) String logout,
                       @RequestParam(value = "expired", required = false) String expired,
                       Model model) {
        
        if (error != null) {
            model.addAttribute("errorMessage", "Nom d'utilisateur ou mot de passe incorrect.");
        }
        
        if (logout != null) {
            model.addAttribute("successMessage", "Vous avez été déconnecté avec succès.");
        }
        
        if (expired != null) {
            model.addAttribute("errorMessage", "Votre session a expiré. Veuillez vous reconnecter.");
        }
        
        return "login";
    }
    
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
     // Initialiser avec une liste vide par défaut
        model.addAttribute("authorities", Collections.emptyList());
        
        if (authentication != null && authentication.isAuthenticated()) {
            model.addAttribute("username", authentication.getName());
            model.addAttribute("authorities", authentication.getAuthorities());
            model.addAttribute("serverTime", new java.util.Date());
            log.info("Accès au dashboard pour l'utilisateur: {}", authentication.getName());
        }
        
        return "dashboard";
    }
    /**
    @GetMapping("/error/403")
    public String accessDenied(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication != null) {
            model.addAttribute("username", authentication.getName());
            model.addAttribute("authorities", authentication.getAuthorities());
        }
        
        return "error/403";
    }
    
    @GetMapping("/error/404")
    public String notFound() {
        return "error/404";
    }
    
    @GetMapping("/error/500")
    public String serverError() {
        return "error/500";
    }
    **/
    
    @GetMapping("/error")
    public String handleError(HttpServletRequest request, Model model) {
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        
        if (status != null) {
            int statusCode = Integer.parseInt(status.toString());
            
            model.addAttribute("statusCode", statusCode);
            
            if (statusCode == HttpStatus.NOT_FOUND.value()) {
                return "error/404";
            } else if (statusCode == HttpStatus.FORBIDDEN.value()) {
                return "error/403";
            } else if (statusCode == HttpStatus.INTERNAL_SERVER_ERROR.value()) {
                return "error/500";
            }
         // Ajouter la journalisation des erreurs
         // (Ajoutez des logs dans votre gestionnaire d'erreurs pour suivre les problèmes :)
           
            String errorPath = (String) request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI);
            String errorMessage = (String) request.getAttribute(RequestDispatcher.ERROR_MESSAGE);
            log.error("Erreur {} sur le chemin {}: {}", statusCode, errorPath, errorMessage);
        }
        
        return "error/generic";
    }
    
    // Reste du code...
}

