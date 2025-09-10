package com.sh.erpcos.univers.securite.repository;

import com.sh.erpcos.univers.securite.entity.TwoFactorAuth;
import com.sh.erpcos.univers.securite.entity.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TwoFactorAuthRepository extends JpaRepository<TwoFactorAuth, Long> {
    
    // Recherche par utilisateur
    Optional<TwoFactorAuth> findByUtilisateur(Utilisateur utilisateur);
    
    Optional<TwoFactorAuth> findByUtilisateurId(Long utilisateurId);
    
    Optional<TwoFactorAuth> findByUtilisateurUsername(String username);
    
    // 2FA activé
    List<TwoFactorAuth> findByEnabledTrueOrderByDateActivationDesc();
    
    // 2FA désactivé
    List<TwoFactorAuth> findByEnabledFalseOrderByDateCreationDesc();
    
    // 2FA vérifié
    List<TwoFactorAuth> findByVerifiedTrueOrderByDateActivationDesc();
    
    // 2FA non vérifié
    List<TwoFactorAuth> findByVerifiedFalseOrderByDateCreationDesc();
    
    // Par type de méthode
    List<TwoFactorAuth> findByMethodTypeOrderByDateCreationDesc(TwoFactorAuth.MethodType methodType);
    
    // 2FA verrouillé
    @Query("SELECT t FROM TwoFactorAuth t WHERE t.verrouilleJusqu IS NOT NULL AND t.verrouilleJusqu > :maintenant")
    List<TwoFactorAuth> findLockedAccounts(@Param("maintenant") LocalDateTime maintenant);
    
    // 2FA avec tentatives d'échec
    @Query("SELECT t FROM TwoFactorAuth t WHERE t.tentativesEchec > 0 ORDER BY t.tentativesEchec DESC, t.dateDernierEchec DESC")
    List<TwoFactorAuth> findAccountsWithFailedAttempts();
    
    // 2FA avec tentatives d'échec élevées
    @Query("SELECT t FROM TwoFactorAuth t WHERE t.tentativesEchec >= :seuil ORDER BY t.tentativesEchec DESC")
    List<TwoFactorAuth> findAccountsWithHighFailedAttempts(@Param("seuil") int seuil);
    
    // Mise à jour des tentatives d'échec
    @Modifying
    @Transactional
    @Query("UPDATE TwoFactorAuth t SET t.tentativesEchec = t.tentativesEchec + 1, t.dateDernierEchec = :maintenant WHERE t.id = :id")
    void incrementFailedAttempts(@Param("id") Long id, @Param("maintenant") LocalDateTime maintenant);
    
    // Réinitialiser les tentatives d'échec
    @Modifying
    @Transactional
    @Query("UPDATE TwoFactorAuth t SET t.tentativesEchec = 0, t.dateDernierEchec = NULL, t.verrouilleJusqu = NULL WHERE t.id = :id")
    void resetFailedAttempts(@Param("id") Long id);
    
    // Verrouiller un compte
    @Modifying
    @Transactional
    @Query("UPDATE TwoFactorAuth t SET t.verrouilleJusqu = :verrouilleJusqu WHERE t.id = :id")
    void lockAccount(@Param("id") Long id, @Param("verrouilleJusqu") LocalDateTime verrouilleJusqu);
    
    // Déverrouiller un compte
    @Modifying
    @Transactional
    @Query("UPDATE TwoFactorAuth t SET t.verrouilleJusqu = NULL, t.tentativesEchec = 0 WHERE t.id = :id")
    void unlockAccount(@Param("id") Long id);
    
    // Mettre à jour la dernière utilisation
    @Modifying
    @Transactional
    @Query("UPDATE TwoFactorAuth t SET t.dateDerniereUtilisation = :maintenant WHERE t.id = :id")
    void updateLastUsed(@Param("id") Long id, @Param("maintenant") LocalDateTime maintenant);
    
    // Activer 2FA
    @Modifying
    @Transactional
    @Query("UPDATE TwoFactorAuth t SET t.enabled = true, t.verified = true, t.dateActivation = :maintenant WHERE t.id = :id")
    void enable2FA(@Param("id") Long id, @Param("maintenant") LocalDateTime maintenant);
    
    // Désactiver 2FA
    @Modifying
    @Transactional
    @Query("UPDATE TwoFactorAuth t SET t.enabled = false, t.verified = false, t.dateActivation = NULL WHERE t.id = :id")
    void disable2FA(@Param("id") Long id);
    
    // Statistiques
    @Query("SELECT COUNT(t) FROM TwoFactorAuth t WHERE t.enabled = true")
    long countEnabled2FA();
    
    @Query("SELECT COUNT(t) FROM TwoFactorAuth t WHERE t.enabled = false")
    long countDisabled2FA();
    
    @Query("SELECT COUNT(t) FROM TwoFactorAuth t WHERE t.verified = true")
    long countVerified2FA();
    
    @Query("SELECT t.methodType, COUNT(t) FROM TwoFactorAuth t GROUP BY t.methodType ORDER BY COUNT(t) DESC")
    List<Object[]> getStatisticsByMethodType();
    
    // 2FA récemment activé
    @Query("SELECT t FROM TwoFactorAuth t WHERE t.dateActivation >= :dateDebut ORDER BY t.dateActivation DESC")
    List<TwoFactorAuth> findRecentlyEnabled(@Param("dateDebut") LocalDateTime dateDebut);
    
    // 2FA récemment utilisé
    @Query("SELECT t FROM TwoFactorAuth t WHERE t.dateDerniereUtilisation >= :dateDebut ORDER BY t.dateDerniereUtilisation DESC")
    List<TwoFactorAuth> findRecentlyUsed(@Param("dateDebut") LocalDateTime dateDebut);
    
    // 2FA non utilisé depuis longtemps
    @Query("SELECT t FROM TwoFactorAuth t WHERE t.enabled = true AND (t.dateDerniereUtilisation IS NULL OR t.dateDerniereUtilisation < :dateLimite) ORDER BY t.dateDerniereUtilisation ASC")
    List<TwoFactorAuth> findUnusedFor(@Param("dateLimite") LocalDateTime dateLimite);
    
    // Recherche par numéro de téléphone
    Optional<TwoFactorAuth> findByPhoneNumber(String phoneNumber);
    
    List<TwoFactorAuth> findByPhoneNumberContaining(String phoneNumber);
    
    // Recherche par email de secours
    Optional<TwoFactorAuth> findByEmailBackup(String emailBackup);
    
    List<TwoFactorAuth> findByEmailBackupContaining(String emailBackup);
    
    // Comptes avec codes de récupération épuisés
    @Query("SELECT t FROM TwoFactorAuth t WHERE t.enabled = true AND " +
           "(LENGTH(t.backupCodes) - LENGTH(REPLACE(t.backupCodes, ',', '')) + 1) - " +
           "(CASE WHEN t.codesUtilises IS NULL THEN 0 ELSE LENGTH(t.codesUtilises) - LENGTH(REPLACE(t.codesUtilises, ',', '')) + 1 END) <= :seuilRestant")
    List<TwoFactorAuth> findAccountsWithLowBackupCodes(@Param("seuilRestant") int seuilRestant);
    
    // Déverrouiller automatiquement les comptes expirés
    @Modifying
    @Transactional
    @Query("UPDATE TwoFactorAuth t SET t.verrouilleJusqu = NULL WHERE t.verrouilleJusqu IS NOT NULL AND t.verrouilleJusqu <= :maintenant")
    int unlockExpiredAccounts(@Param("maintenant") LocalDateTime maintenant);

//added
    @Query("SELECT COUNT(t) FROM TwoFactorAuth t WHERE t.methodType = :methodType AND t.enabled = true")
    long countEnabled2FAByMethodType(@Param("methodType") TwoFactorAuth.MethodType methodType);

}