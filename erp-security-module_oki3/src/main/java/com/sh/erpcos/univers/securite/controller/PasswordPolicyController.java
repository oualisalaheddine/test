package com.sh.erpcos.univers.securite.controller;

import com.sh.erpcos.univers.securite.entity.PasswordPolicy;
import com.sh.erpcos.univers.securite.service.PasswordPolicyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/securite/password-policies")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasAuthority('SECURITE_LIRE')")
public class PasswordPolicyController {
    
    private final PasswordPolicyService passwordPolicyService;
    
    @GetMapping
    public String index(Model model) {
        List<PasswordPolicy> activePolicies = passwordPolicyService.getActivePolicies();
        List<PasswordPolicy> inactivePolicies = passwordPolicyService.getInactivePolicies();
        Optional<PasswordPolicy> defaultPolicy = passwordPolicyService.getDefaultPolicy();
        
        model.addAttribute("activePolicies", activePolicies);
        model.addAttribute("inactivePolicies", inactivePolicies);
        model.addAttribute("defaultPolicy", defaultPolicy.orElse(null));
        model.addAttribute("totalPolicies", activePolicies.size() + inactivePolicies.size());
        
        return "securite/password-policies/index";
    }
    
    @GetMapping("/create")
    @PreAuthorize("hasAuthority('SECURITE_CREER')")
    public String createForm(Model model) {
        model.addAttribute("passwordPolicy", new PasswordPolicy());
        model.addAttribute("isEdit", false);
        return "securite/password-policies/form";
    }
    
    @PostMapping("/create")
    @PreAuthorize("hasAuthority('SECURITE_CREER')")
    public String create(@Valid @ModelAttribute PasswordPolicy passwordPolicy,
                        BindingResult bindingResult,
                        Model model,
                        RedirectAttributes redirectAttributes) {
        
        if (bindingResult.hasErrors()) {
            model.addAttribute("isEdit", false);
            return "securite/password-policies/form";
        }
        
        try {
            PasswordPolicy savedPolicy = passwordPolicyService.createPolicy(passwordPolicy);
            redirectAttributes.addFlashAttribute("successMessage", 
                    "Politique de mot de passe '" + savedPolicy.getNomPolitique() + "' créée avec succès");
            return "redirect:/securite/password-policies";
        } catch (Exception e) {
            log.error("Erreur lors de la création de la politique", e);
            model.addAttribute("errorMessage", "Erreur lors de la création: " + e.getMessage());
            model.addAttribute("isEdit", false);
            return "securite/password-policies/form";
        }
    }
    
    @GetMapping("/edit/{id}")
    @PreAuthorize("hasAuthority('SECURITE_MODIFIER')")
    public String editForm(@PathVariable Long id, Model model) {
        Optional<PasswordPolicy> policyOpt = passwordPolicyService.getPolicyById(id);
        
        if (policyOpt.isPresent()) {
            model.addAttribute("passwordPolicy", policyOpt.get());
            model.addAttribute("isEdit", true);
            return "securite/password-policies/form";
        } else {
            model.addAttribute("errorMessage", "Politique non trouvée");
            return "redirect:/securite/password-policies";
        }
    }
    
    @PostMapping("/edit/{id}")
    @PreAuthorize("hasAuthority('SECURITE_MODIFIER')")
    public String update(@PathVariable Long id,
                        @Valid @ModelAttribute PasswordPolicy passwordPolicy,
                        BindingResult bindingResult,
                        Model model,
                        RedirectAttributes redirectAttributes) {
        
        if (bindingResult.hasErrors()) {
            model.addAttribute("isEdit", true);
            return "securite/password-policies/form";
        }
        
        try {
            PasswordPolicy updatedPolicy = passwordPolicyService.updatePolicy(id, passwordPolicy);
            redirectAttributes.addFlashAttribute("successMessage", 
                    "Politique '" + updatedPolicy.getNomPolitique() + "' mise à jour avec succès");
            return "redirect:/securite/password-policies";
        } catch (Exception e) {
            log.error("Erreur lors de la mise à jour de la politique", e);
            model.addAttribute("errorMessage", "Erreur lors de la mise à jour: " + e.getMessage());
            model.addAttribute("isEdit", true);
            return "securite/password-policies/form";
        }
    }
    
    @GetMapping("/view/{id}")
    public String view(@PathVariable Long id, Model model) {
        Optional<PasswordPolicy> policyOpt = passwordPolicyService.getPolicyById(id);
        
        if (policyOpt.isPresent()) {
            PasswordPolicy policy = policyOpt.get();
            model.addAttribute("passwordPolicy", policy);
            model.addAttribute("validationMessage", policy.getMessageValidation());
            return "securite/password-policies/view";
        } else {
            model.addAttribute("errorMessage", "Politique non trouvée");
            return "redirect:/securite/password-policies";
        }
    }
    
    @PostMapping("/delete/{id}")
    @PreAuthorize("hasAuthority('SECURITE_SUPPRIMER')")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Optional<PasswordPolicy> policyOpt = passwordPolicyService.getPolicyById(id);
            if (policyOpt.isPresent()) {
                String policyName = policyOpt.get().getNomPolitique();
                passwordPolicyService.deletePolicy(id);
                redirectAttributes.addFlashAttribute("successMessage", 
                        "Politique '" + policyName + "' supprimée avec succès");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Politique non trouvée");
            }
        } catch (Exception e) {
            log.error("Erreur lors de la suppression de la politique", e);
            redirectAttributes.addFlashAttribute("errorMessage", 
                    "Erreur lors de la suppression: " + e.getMessage());
        }
        
        return "redirect:/securite/password-policies";
    }
    
    @PostMapping("/activate/{id}")
    @PreAuthorize("hasAuthority('SECURITE_MODIFIER')")
    public String activate(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            passwordPolicyService.activatePolicy(id);
            redirectAttributes.addFlashAttribute("successMessage", "Politique activée avec succès");
        } catch (Exception e) {
            log.error("Erreur lors de l'activation de la politique", e);
            redirectAttributes.addFlashAttribute("errorMessage", 
                    "Erreur lors de l'activation: " + e.getMessage());
        }
        
        return "redirect:/securite/password-policies";
    }
    
    @PostMapping("/deactivate/{id}")
    @PreAuthorize("hasAuthority('SECURITE_MODIFIER')")
    public String deactivate(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            passwordPolicyService.deactivatePolicy(id);
            redirectAttributes.addFlashAttribute("successMessage", "Politique désactivée avec succès");
        } catch (Exception e) {
            log.error("Erreur lors de la désactivation de la politique", e);
            redirectAttributes.addFlashAttribute("errorMessage", 
                    "Erreur lors de la désactivation: " + e.getMessage());
        }
        
        return "redirect:/securite/password-policies";
    }
    
    @PostMapping("/set-default/{id}")
    @PreAuthorize("hasAuthority('SECURITE_MODIFIER')")
    public String setAsDefault(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            passwordPolicyService.setAsDefault(id);
            redirectAttributes.addFlashAttribute("successMessage", 
                    "Politique définie comme par défaut avec succès");
        } catch (Exception e) {
            log.error("Erreur lors de la définition de la politique par défaut", e);
            redirectAttributes.addFlashAttribute("errorMessage", 
                    "Erreur lors de la définition par défaut: " + e.getMessage());
        }
        
        return "redirect:/securite/password-policies";
    }
    
    @GetMapping("/test")
    public String testForm(Model model) {
        Optional<PasswordPolicy> defaultPolicy = passwordPolicyService.getDefaultPolicy();
        
        if (defaultPolicy.isPresent()) {
            model.addAttribute("defaultPolicy", defaultPolicy.get());
            model.addAttribute("validationMessage", defaultPolicy.get().getMessageValidation());
        } else {
            model.addAttribute("errorMessage", "Aucune politique par défaut définie");
        }
        
        return "securite/password-policies/test";
    }
    
    @PostMapping("/test")
    @ResponseBody
    public TestResult testPassword(@RequestParam String password,
                                  @RequestParam(required = false) String username,
                                  @RequestParam(required = false) Long policyId) {
        
        try {
            boolean isValid;
            List<String> errors;
            
            if (policyId != null) {
                Optional<PasswordPolicy> policyOpt = passwordPolicyService.getPolicyById(policyId);
                if (policyOpt.isPresent()) {
                    isValid = passwordPolicyService.validatePassword(password, username, policyOpt.get());
                    errors = passwordPolicyService.getPasswordValidationErrors(password, username, policyOpt.get());
                } else {
                    return new TestResult(false, List.of("Politique non trouvée"));
                }
            } else {
                isValid = passwordPolicyService.validatePassword(password, username);
                errors = passwordPolicyService.getPasswordValidationErrors(password, username);
            }
            
            return new TestResult(isValid, errors);
        } catch (Exception e) {
            log.error("Erreur lors du test du mot de passe", e);
            return new TestResult(false, List.of("Erreur lors du test: " + e.getMessage()));
        }
    }
    
    @GetMapping("/search")
    public String search(Model model,
                        @RequestParam(required = false) String nom,
                        @RequestParam(required = false) Boolean actif,
                        @RequestParam(required = false) Integer longueurMin,
                        @RequestParam(required = false) Integer longueurMax) {
        
        List<PasswordPolicy> policies = passwordPolicyService.searchPolicies(nom, actif, longueurMin, longueurMax);
        
        model.addAttribute("policies", policies);
        model.addAttribute("searchNom", nom);
        model.addAttribute("searchActif", actif);
        model.addAttribute("searchLongueurMin", longueurMin);
        model.addAttribute("searchLongueurMax", longueurMax);
        model.addAttribute("totalResults", policies.size());
        
        return "securite/password-policies/search";
    }
    
    @GetMapping("/strict")
    public String strictPolicies(Model model,
                               @RequestParam(defaultValue = "8") int minLength) {
        
        List<PasswordPolicy> strictPolicies = passwordPolicyService.getStrictPolicies(minLength);
        
        model.addAttribute("policies", strictPolicies);
        model.addAttribute("minLength", minLength);
        model.addAttribute("pageTitle", "Politiques strictes");
        
        return "securite/password-policies/strict";
    }
    
    @GetMapping("/with-history")
    public String policiesWithHistory(Model model) {
        List<PasswordPolicy> policies = passwordPolicyService.getPoliciesWithHistory();
        
        model.addAttribute("policies", policies);
        model.addAttribute("pageTitle", "Politiques avec historique");
        
        return "securite/password-policies/with-history";
    }
    
    @GetMapping("/with-expiration")
    public String policiesWithExpiration(Model model) {
        List<PasswordPolicy> policies = passwordPolicyService.getPoliciesWithExpiration();
        
        model.addAttribute("policies", policies);
        model.addAttribute("pageTitle", "Politiques avec expiration");
        
        return "securite/password-policies/with-expiration";
    }
    
    @PostMapping("/initialize-default")
    @PreAuthorize("hasAuthority('SECURITE_CREER')")
    public String initializeDefault(RedirectAttributes redirectAttributes) {
        try {
            passwordPolicyService.initializeDefaultPolicy();
            redirectAttributes.addFlashAttribute("successMessage", 
                    "Politique par défaut initialisée avec succès");
        } catch (Exception e) {
            log.error("Erreur lors de l'initialisation de la politique par défaut", e);
            redirectAttributes.addFlashAttribute("errorMessage", 
                    "Erreur lors de l'initialisation: " + e.getMessage());
        }
        
        return "redirect:/securite/password-policies";
    }
    
    // Classe interne pour les résultats de test
    public static class TestResult {
        private final boolean valid;
        private final List<String> errors;
        
        public TestResult(boolean valid, List<String> errors) {
            this.valid = valid;
            this.errors = errors;
        }
        
        public boolean isValid() {
            return valid;
        }
        
        public List<String> getErrors() {
            return errors;
        }
    }
}