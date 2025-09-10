package com.sh.erpcos.univers.securite.service;

import com.sh.erpcos.univers.securite.entity.PasswordPolicy;
import com.sh.erpcos.univers.securite.repository.PasswordPolicyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.ArrayList;
import java.util.Arrays;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PasswordPolicyService {
    
    private final PasswordPolicyRepository passwordPolicyRepository;
    private final AuditService auditService;
    
    // Gestion des politiques
    public List<PasswordPolicy> getAllPolicies() {
        return passwordPolicyRepository.findAll();
    }
    
    public List<PasswordPolicy> getActivePolicies() {
        return passwordPolicyRepository.findByPolitiqueActiveTrueOrderByNomPolitique();
    }
    
    public List<PasswordPolicy> getInactivePolicies() {
        return passwordPolicyRepository.findByPolitiqueActiveFalseOrderByNomPolitique();
    }
    
    public Optional<PasswordPolicy> getPolicyById(Long id) {
        return passwordPolicyRepository.findById(id);
    }
    
    public Optional<PasswordPolicy> getPolicyByName(String nomPolitique) {
        return passwordPolicyRepository.findByNomPolitique(nomPolitique);
    }
    
    public Optional<PasswordPolicy> getDefaultPolicy() {
        return passwordPolicyRepository.findByPolitiqueParDefautTrue();
    }
    
    public PasswordPolicy createPolicy(PasswordPolicy policy) {
        log.info("Création d'une nouvelle politique de mot de passe: {}", policy.getNomPolitique());
        
        // Vérifier si une politique avec ce nom existe déjà
        if (passwordPolicyRepository.existsByNomPolitique(policy.getNomPolitique())) {
            throw new RuntimeException("Une politique avec ce nom existe déjà: " + policy.getNomPolitique());
        }
        
        // Si c'est la première politique, la marquer comme par défaut
        if (passwordPolicyRepository.count() == 0) {
            policy.setPolitiqueParDefaut(true);
        }
        
        // Si cette politique est marquée comme par défaut, désactiver les autres
        if (policy.isPolitiqueParDefaut()) {
            resetDefaultPolicies();
        }
        
        policy.setDateCreation(LocalDateTime.now());
        policy.setDateModification(LocalDateTime.now());
        
        PasswordPolicy savedPolicy = passwordPolicyRepository.save(policy);
        
        auditService.logSystemConfig("CREATE_PASSWORD_POLICY", policy.getNomPolitique(), 
                                    "Nouvelle politique de mot de passe créée");
        
        log.info("Politique de mot de passe créée avec succès: {}", savedPolicy.getNomPolitique());
        return savedPolicy;
    }
    
    public PasswordPolicy updatePolicy(Long id, PasswordPolicy policyDetails) {
        log.info("Mise à jour de la politique de mot de passe avec l'ID: {}", id);
        
        PasswordPolicy policy = passwordPolicyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Politique non trouvée avec l'ID: " + id));
        
        String oldName = policy.getNomPolitique();
        
        // Mettre à jour les champs
        policy.setNomPolitique(policyDetails.getNomPolitique());
        policy.setDescription(policyDetails.getDescription());
        policy.setLongueurMinimale(policyDetails.getLongueurMinimale());
        policy.setLongueurMaximale(policyDetails.getLongueurMaximale());
        policy.setExigerMajuscules(policyDetails.isExigerMajuscules());
        policy.setExigerMinuscules(policyDetails.isExigerMinuscules());
        policy.setExigerChiffres(policyDetails.isExigerChiffres());
        policy.setExigerCaracteresSpeciaux(policyDetails.isExigerCaracteresSpeciaux());
        policy.setCaracteresSpeciauxAutorises(policyDetails.getCaracteresSpeciauxAutorises());
        policy.setInterdireMotsCommuns(policyDetails.isInterdireMotsCommuns());
        policy.setInterdireInformationsPersonnelles(policyDetails.isInterdireInformationsPersonnelles());
        policy.setInterdireRepetitionCaracteres(policyDetails.isInterdireRepetitionCaracteres());
        policy.setMaxRepetitionCaracteres(policyDetails.getMaxRepetitionCaracteres());
        policy.setInterdireSequences(policyDetails.isInterdireSequences());
        policy.setHistoriqueMotesPasse(policyDetails.getHistoriqueMotesPasse());
        policy.setDureeValiditeJours(policyDetails.getDureeValiditeJours());
        policy.setAvertissementExpirationJours(policyDetails.getAvertissementExpirationJours());
        policy.setTentativesMaxEchec(policyDetails.getTentativesMaxEchec());
        policy.setDureeVerrouillageMintes(policyDetails.getDureeVerrouillageMintes());
        policy.setPolitiqueActive(policyDetails.isPolitiqueActive());
        
        // Si cette politique devient par défaut, désactiver les autres
        if (policyDetails.isPolitiqueParDefaut() && !policy.isPolitiqueParDefaut()) {
            resetDefaultPolicies();
        }
        policy.setPolitiqueParDefaut(policyDetails.isPolitiqueParDefaut());
        
        policy.setDateModification(LocalDateTime.now());
        
        PasswordPolicy updatedPolicy = passwordPolicyRepository.save(policy);
        
        auditService.logSystemConfig("UPDATE_PASSWORD_POLICY", policy.getNomPolitique(), 
                                    "Politique de mot de passe mise à jour (anciennement: " + oldName + ")");
        
        log.info("Politique de mot de passe mise à jour avec succès: {}", updatedPolicy.getNomPolitique());
        return updatedPolicy;
    }
    
    public void deletePolicy(Long id) {
        log.info("Suppression de la politique de mot de passe avec l'ID: {}", id);
        
        PasswordPolicy policy = passwordPolicyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Politique non trouvée avec l'ID: " + id));
        
        // Ne pas supprimer la politique par défaut s'il n'y en a qu'une
        if (policy.isPolitiqueParDefaut() && passwordPolicyRepository.countActivePolicies() <= 1) {
            throw new RuntimeException("Impossible de supprimer la seule politique active");
        }
        
        String policyName = policy.getNomPolitique();
        passwordPolicyRepository.delete(policy);
        
        // Si c'était la politique par défaut, en définir une autre
        if (policy.isPolitiqueParDefaut()) {
            setFirstActiveAsDefault();
        }
        
        auditService.logSystemConfig("DELETE_PASSWORD_POLICY", policyName, 
                                    "Politique de mot de passe supprimée");
        
        log.info("Politique de mot de passe supprimée avec succès: {}", policyName);
    }
    
    public void activatePolicy(Long id) {
        log.info("Activation de la politique de mot de passe avec l'ID: {}", id);
        
        PasswordPolicy policy = passwordPolicyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Politique non trouvée avec l'ID: " + id));
        
        policy.setPolitiqueActive(true);
        policy.setDateModification(LocalDateTime.now());
        passwordPolicyRepository.save(policy);
        
        auditService.logSystemConfig("ACTIVATE_PASSWORD_POLICY", policy.getNomPolitique(), 
                                    "Politique de mot de passe activée");
        
        log.info("Politique de mot de passe activée avec succ��s: {}", policy.getNomPolitique());
    }
    
    public void deactivatePolicy(Long id) {
        log.info("Désactivation de la politique de mot de passe avec l'ID: {}", id);
        
        PasswordPolicy policy = passwordPolicyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Politique non trouvée avec l'ID: " + id));
        
        // Ne pas désactiver s'il n'y a qu'une politique active
        if (passwordPolicyRepository.countActivePolicies() <= 1) {
            throw new RuntimeException("Impossible de désactiver la seule politique active");
        }
        
        policy.setPolitiqueActive(false);
        policy.setDateModification(LocalDateTime.now());
        
        // Si c'était la politique par défaut, en définir une autre
        if (policy.isPolitiqueParDefaut()) {
            policy.setPolitiqueParDefaut(false);
            setFirstActiveAsDefault();
        }
        
        passwordPolicyRepository.save(policy);
        
        auditService.logSystemConfig("DEACTIVATE_PASSWORD_POLICY", policy.getNomPolitique(), 
                                    "Politique de mot de passe désactivée");
        
        log.info("Politique de mot de passe désactivée avec succès: {}", policy.getNomPolitique());
    }
    
    public void setAsDefault(Long id) {
        log.info("Définition de la politique par défaut avec l'ID: {}", id);
        
        PasswordPolicy policy = passwordPolicyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Politique non trouvée avec l'ID: " + id));
        
        if (!policy.isPolitiqueActive()) {
            throw new RuntimeException("Impossible de définir une politique inactive comme par défaut");
        }
        
        // Désactiver toutes les autres politiques par défaut
        resetDefaultPolicies();
        
        policy.setPolitiqueParDefaut(true);
        policy.setDateModification(LocalDateTime.now());
        passwordPolicyRepository.save(policy);
        
        auditService.logSystemConfig("SET_DEFAULT_PASSWORD_POLICY", policy.getNomPolitique(), 
                                    "Politique définie comme par défaut");
        
        log.info("Politique définie comme par défaut: {}", policy.getNomPolitique());
    }
    
    // Validation des mots de passe
    public boolean validatePassword(String password, String username) {
        Optional<PasswordPolicy> policyOpt = getDefaultPolicy();
        if (policyOpt.isEmpty()) {
            log.warn("Aucune politique de mot de passe par défaut trouvée");
            return true; // Pas de politique = pas de validation
        }
        
        return validatePassword(password, username, policyOpt.get());
    }
    
    public boolean validatePassword(String password, String username, PasswordPolicy policy) {
        if (policy == null || !policy.isPolitiqueActive()) {
            return true;
        }
        
        // Validation de base
        if (!policy.validerMotDePasse(password)) {
            return false;
        }
        
        // Vérifier les informations personnelles si activé
        if (policy.isInterdireInformationsPersonnelles() && username != null) {
            if (containsPersonalInfo(password, username)) {
                return false;
            }
        }
        
        // Vérifier les mots communs si activé
        if (policy.isInterdireMotsCommuns()) {
            if (isCommonPassword(password)) {
                return false;
            }
        }
        
        return true;
    }
    
    public List<String> getPasswordValidationErrors(String password, String username) {
        Optional<PasswordPolicy> policyOpt = getDefaultPolicy();
        if (policyOpt.isEmpty()) {
            return new ArrayList<>();
        }
        
        return getPasswordValidationErrors(password, username, policyOpt.get());
    }
    
    public List<String> getPasswordValidationErrors(String password, String username, PasswordPolicy policy) {
        List<String> errors = new ArrayList<>();
        
        if (policy == null || !policy.isPolitiqueActive()) {
            return errors;
        }
        
        if (password == null || password.isEmpty()) {
            errors.add("Le mot de passe ne peut pas être vide");
            return errors;
        }
        
        // Vérifier la longueur
        if (password.length() < policy.getLongueurMinimale()) {
            errors.add("Le mot de passe doit contenir au moins " + policy.getLongueurMinimale() + " caractères");
        }
        
        if (password.length() > policy.getLongueurMaximale()) {
            errors.add("Le mot de passe ne peut pas dépasser " + policy.getLongueurMaximale() + " caractères");
        }
        
        // Vérifier les majuscules
        if (policy.isExigerMajuscules() && !password.matches(".*[A-Z].*")) {
            errors.add("Le mot de passe doit contenir au moins une lettre majuscule");
        }
        
        // Vérifier les minuscules
        if (policy.isExigerMinuscules() && !password.matches(".*[a-z].*")) {
            errors.add("Le mot de passe doit contenir au moins une lettre minuscule");
        }
        
        // Vérifier les chiffres
        if (policy.isExigerChiffres() && !password.matches(".*[0-9].*")) {
            errors.add("Le mot de passe doit contenir au moins un chiffre");
        }
        
        // Vérifier les caractères spéciaux
        if (policy.isExigerCaracteresSpeciaux()) {
            String regex = ".*[" + java.util.regex.Pattern.quote(policy.getCaracteresSpeciauxAutorises()) + "].*";
            if (!password.matches(regex)) {
                errors.add("Le mot de passe doit contenir au moins un caractère spécial (" + 
                          policy.getCaracteresSpeciauxAutorises() + ")");
            }
        }
        
        // Vérifier la répétition de caractères
        if (policy.isInterdireRepetitionCaracteres() && hasRepeatingCharacters(password, policy.getMaxRepetitionCaracteres())) {
            errors.add("Le mot de passe ne peut pas contenir plus de " + policy.getMaxRepetitionCaracteres() + 
                      " caractères identiques consécutifs");
        }
        
        // Vérifier les séquences
        if (policy.isInterdireSequences() && hasSequences(password)) {
            errors.add("Le mot de passe ne peut pas contenir de séquences (ex: 123, abc)");
        }
        
        // Vérifier les informations personnelles
        if (policy.isInterdireInformationsPersonnelles() && username != null && containsPersonalInfo(password, username)) {
            errors.add("Le mot de passe ne peut pas contenir d'informations personnelles");
        }
        
        // Vérifier les mots communs
        if (policy.isInterdireMotsCommuns() && isCommonPassword(password)) {
            errors.add("Le mot de passe est trop commun, veuillez en choisir un autre");
        }
        
        return errors;
    }
    
    // Recherche et statistiques
    public List<PasswordPolicy> searchPolicies(String nom, Boolean actif, Integer longueurMin, Integer longueurMax) {
        return passwordPolicyRepository.rechercheAvancee(nom, actif, longueurMin, longueurMax);
    }
    
    public List<PasswordPolicy> getStrictPolicies(int minLength) {
        return passwordPolicyRepository.findStrictPolicies(minLength);
    }
    
    public List<PasswordPolicy> getPoliciesWithHistory() {
        return passwordPolicyRepository.findPoliciesWithHistory();
    }
    
    public List<PasswordPolicy> getPoliciesWithExpiration() {
        return passwordPolicyRepository.findPoliciesWithExpiration();
    }
    
    // Initialisation des données par défaut
    public void initializeDefaultPolicy() {
        if (passwordPolicyRepository.count() == 0) {
            PasswordPolicy defaultPolicy = PasswordPolicy.createDefaultPolicy();
            createPolicy(defaultPolicy);
            log.info("Politique de mot de passe par défaut initialisée");
        }
    }
    
    // Méthodes utilitaires privées
    private void resetDefaultPolicies() {
        List<PasswordPolicy> defaultPolicies = passwordPolicyRepository.findByPolitiqueParDefautTrue()
                .map(List::of)
                .orElse(new ArrayList<>());
        
        for (PasswordPolicy policy : defaultPolicies) {
            policy.setPolitiqueParDefaut(false);
            passwordPolicyRepository.save(policy);
        }
    }
    
    private void setFirstActiveAsDefault() {
        List<PasswordPolicy> activePolicies = getActivePolicies();
        if (!activePolicies.isEmpty()) {
            PasswordPolicy firstActive = activePolicies.get(0);
            firstActive.setPolitiqueParDefaut(true);
            passwordPolicyRepository.save(firstActive);
        }
    }
    
    private boolean hasRepeatingCharacters(String password, int maxRepetition) {
        if (maxRepetition <= 0) return false;
        
        int count = 1;
        char previousChar = password.charAt(0);
        
        for (int i = 1; i < password.length(); i++) {
            if (password.charAt(i) == previousChar) {
                count++;
                if (count > maxRepetition) {
                    return true;
                }
            } else {
                count = 1;
                previousChar = password.charAt(i);
            }
        }
        
        return false;
    }
    
    private boolean hasSequences(String password) {
        String lower = password.toLowerCase();
        
        // Vérifier les séquences numériques et alphabétiques
        for (int i = 0; i < lower.length() - 2; i++) {
            char c1 = lower.charAt(i);
            char c2 = lower.charAt(i + 1);
            char c3 = lower.charAt(i + 2);
            
            // Séquences croissantes ou décroissantes
            if ((c2 == c1 + 1 && c3 == c2 + 1) || (c2 == c1 - 1 && c3 == c2 - 1)) {
                return true;
            }
        }
        
        return false;
    }
    
    private boolean containsPersonalInfo(String password, String username) {
        String lowerPassword = password.toLowerCase();
        String lowerUsername = username.toLowerCase();
        
        // Vérifier si le mot de passe contient le nom d'utilisateur
        return lowerPassword.contains(lowerUsername) || lowerUsername.contains(lowerPassword);
    }
    
    private boolean isCommonPassword(String password) {
        // Liste des mots de passe les plus communs
        String[] commonPasswords = {
            "password", "123456", "password123", "admin", "qwerty", "letmein",
            "welcome", "monkey", "1234567890", "abc123", "111111", "123123",
            "password1", "1234", "12345", "dragon", "master", "hello",
            "login", "welcome123", "admin123", "root", "pass", "test"
        };
        
        String lowerPassword = password.toLowerCase();
        for (String common : commonPasswords) {
            if (lowerPassword.equals(common) || lowerPassword.contains(common)) {
                return true;
            }
        }
        
        return false;
    }
    	//Added
 // Ajouter cette méthode dans PasswordPolicyService
    public List<String> getPasswordRequirements() {
     //   Optional<PasswordPolicy> policyOpt = getDefaultPolicy();
    
        return getDefaultPolicy()
                .map(p -> Arrays.stream(p.getMessageValidation().split("\\R")) // gère \n, \r\n, etc.
                                .skip(1)                                       // on saute l’en-tête
                                .map(String::trim)
                                .map(s -> s.replaceFirst("^[-•]\\s*", ""))     // optionnel: enlève la puce "- " ou "• "
                                .filter(s -> !s.isEmpty())
                                .collect(Collectors.toList()))                 // en Java 16+, .toList() possible
                .orElse(List.of("Aucune politique configurée"));
    }
}