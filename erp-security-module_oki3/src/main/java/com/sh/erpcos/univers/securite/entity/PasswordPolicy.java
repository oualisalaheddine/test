package com.sh.erpcos.univers.securite.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "password_policies")
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class PasswordPolicy {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "nom_politique", unique = true, nullable = false, length = 100)
    private String nomPolitique;
    
    @Column(name = "description", length = 500)
    private String description;
    
    @Column(name = "longueur_minimale")
    private Integer longueurMinimale = 8;
    
    @Column(name = "longueur_maximale")
    private Integer longueurMaximale = 128;
    
    @Column(name = "exiger_majuscules")
    private boolean exigerMajuscules = true;
    
    @Column(name = "exiger_minuscules")
    private boolean exigerMinuscules = true;
    
    @Column(name = "exiger_chiffres")
    private boolean exigerChiffres = true;
    
    @Column(name = "exiger_caracteres_speciaux")
    private boolean exigerCaracteresSpeciaux = true;
    
    @Column(name = "caracteres_speciaux_autorises", length = 100)
    private String caracteresSpeciauxAutorises = "!@#$%^&*()_+-=[]{}|;:,.<>?";
    
    @Column(name = "interdire_mots_communs")
    private boolean interdireMotsCommuns = true;
    
    @Column(name = "interdire_informations_personnelles")
    private boolean interdireInformationsPersonnelles = true;
    
    @Column(name = "interdire_repetition_caracteres")
    private boolean interdireRepetitionCaracteres = true;
    
    @Column(name = "max_repetition_caracteres")
    private Integer maxRepetitionCaracteres = 3;
    
    @Column(name = "interdire_sequences")
    private boolean interdireSequences = true; // ex: 123456, abcdef
    
    /**
    pas de table d’historique des mots de passe pour le moment
    pour appliquer ce contrôle il faut stocker séparément les anciens mots de passe 
    dans la table prévue pour la prochaine version.
    **/
    @Column(name = "historique_mots_passe")
    private Integer historiqueMotesPasse = 5; // nombre de derniers mots de passe à retenir  
    
    @Column(name = "duree_validite_jours")
    private Integer dureeValiditeJours = 90; // 0 = pas d'expiration
    
    @Column(name = "avertissement_expiration_jours")
    private Integer avertissementExpirationJours = 7;
    
    @Column(name = "tentatives_max_echec")
    private Integer tentativesMaxEchec = 5;
    
    @Column(name = "duree_verrouillage_minutes")
    private Integer dureeVerrouillageMintes = 30;
    
    @Column(name = "politique_active")
    private boolean politiqueActive = true;
    
    @Column(name = "politique_par_defaut")
    private boolean politiqueParDefaut = false;
    
    @Column(name = "date_creation")
    private LocalDateTime dateCreation;
    
    @Column(name = "date_modification")
    private LocalDateTime dateModification;
    
    @Column(name = "cree_par", length = 50)
    private String creePar;
    
    @Column(name = "modifie_par", length = 50)
    private String modifiePar;
    
    @PrePersist
    protected void onCreate() {
        dateCreation = LocalDateTime.now();
        dateModification = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        dateModification = LocalDateTime.now();
    }
    
    // Méthodes de validation
    public boolean validerMotDePasse(String motDePasse) {
        if (motDePasse == null || motDePasse.isEmpty()) {
            return false;
        }
        
        // Vérifier la longueur
        if (motDePasse.length() < longueurMinimale || motDePasse.length() > longueurMaximale) {
            return false;
        }
        
        // Vérifier les majuscules
        if (exigerMajuscules && !motDePasse.matches(".*[A-Z].*")) {
            return false;
        }
        
        // Vérifier les minuscules
        if (exigerMinuscules && !motDePasse.matches(".*[a-z].*")) {
            return false;
        }
        
        // Vérifier les chiffres
        if (exigerChiffres && !motDePasse.matches(".*[0-9].*")) {
            return false;
        }
        
        // Vérifier les caractères spéciaux
        if (exigerCaracteresSpeciaux) {
            String regex = ".*[" + java.util.regex.Pattern.quote(caracteresSpeciauxAutorises) + "].*";
            if (!motDePasse.matches(regex)) {
                return false;
            }
        }
        
        // Vérifier la répétition de caractères
        if (interdireRepetitionCaracteres && hasRepeatingCharacters(motDePasse)) {
            return false;
        }
        
        // Vérifier les séquences
        if (interdireSequences && hasSequences(motDePasse)) {
            return false;
        }
        
        return true;
    }
    
    private boolean hasRepeatingCharacters(String motDePasse) {
        if (maxRepetitionCaracteres <= 0) return false;
        
        int count = 1;
        char previousChar = motDePasse.charAt(0);
        
        for (int i = 1; i < motDePasse.length(); i++) {
            if (motDePasse.charAt(i) == previousChar) {
                count++;
                if (count > maxRepetitionCaracteres) {
                    return true;
                }
            } else {
                count = 1;
                previousChar = motDePasse.charAt(i);
            }
        }
        
        return false;
    }
    
    private boolean hasSequences(String motDePasse) {
        String lower = motDePasse.toLowerCase();
        
        // Vérifier les séquences numériques
        for (int i = 0; i < lower.length() - 2; i++) {
            if (Character.isDigit(lower.charAt(i))) {
                char c1 = lower.charAt(i);
                char c2 = lower.charAt(i + 1);
                char c3 = lower.charAt(i + 2);
                
                if (c2 == c1 + 1 && c3 == c2 + 1) {
                    return true; // Séquence croissante
                }
                if (c2 == c1 - 1 && c3 == c2 - 1) {
                    return true; // Séquence décroissante
                }
            }
        }
        
        // Vérifier les séquences alphabétiques
        for (int i = 0; i < lower.length() - 2; i++) {
            if (Character.isLetter(lower.charAt(i))) {
                char c1 = lower.charAt(i);
                char c2 = lower.charAt(i + 1);
                char c3 = lower.charAt(i + 2);
                
                if (c2 == c1 + 1 && c3 == c2 + 1) {
                    return true; // Séquence croissante
                }
                if (c2 == c1 - 1 && c3 == c2 - 1) {
                    return true; // Séquence décroissante
                }
            }
        }
        
        return false;
    }
    
    public String getMessageValidation() {
        StringBuilder message = new StringBuilder();
        message.append("Le mot de passe doit contenir :");
        message.append("\n- Entre ").append(longueurMinimale).append(" et ").append(longueurMaximale).append(" caractères");
        
        if (exigerMajuscules) {
            message.append("\n- Au moins une lettre majuscule");
        }
        if (exigerMinuscules) {
            message.append("\n- Au moins une lettre minuscule");
        }
        if (exigerChiffres) {
            message.append("\n- Au moins un chiffre");
        }
        if (exigerCaracteresSpeciaux) {
            message.append("\n- Au moins un caractère spécial (").append(caracteresSpeciauxAutorises).append(")");
        }
        if (interdireRepetitionCaracteres) {
            message.append("\n- Pas plus de ").append(maxRepetitionCaracteres).append(" caractères identiques consécutifs");
        }
        if (interdireSequences) {
            message.append("\n- Pas de séquences (ex: 123, abc)");
        }
        
        return message.toString();
    }
    
    // Constructeur pour politique par défaut
    public static PasswordPolicy createDefaultPolicy() {
        PasswordPolicy policy = new PasswordPolicy();
        policy.setNomPolitique("Politique par défaut");
        policy.setDescription("Politique de mot de passe par défaut du système");
        policy.setPolitiqueParDefaut(true);
        policy.setPolitiqueActive(true);
        return policy;
    }
}