package com.sh.erpcos.univers.securite.repository;

import com.sh.erpcos.univers.securite.entity.UserSession;
import com.sh.erpcos.univers.securite.entity.Utilisateur;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
public interface UserSessionRepository extends JpaRepository<UserSession, Long> {

    // Recherche par session ID
    Optional<UserSession> findBySessionId(String sessionId);

    

    // Sessions par utilisateur
    List<UserSession> findByUtilisateurOrderByDateCreationDesc(Utilisateur utilisateur);
    List<UserSession> findByUtilisateurIdOrderByDateCreationDesc(Long utilisateurId);

    // Sessions actives - méthodes dérivées
    List<UserSession> findBySessionActiveTrueAndDateExpirationAfterOrderByDateCreationDesc(LocalDateTime maintenant);
    Page<UserSession> findBySessionActiveTrueAndDateExpirationAfter(LocalDateTime maintenant, Pageable pageable);

    // Sessions actives par utilisateur
    List<UserSession> findByUtilisateurIdAndSessionActiveTrueAndDateExpirationAfterOrderByDateCreationDesc(
            Long utilisateurId, LocalDateTime maintenant);

    // Sessions expirées (corrigé: LessThanEqual au lieu de BeforeOrEqual)
    List<UserSession> findBySessionActiveTrueAndDateExpirationLessThanEqual(LocalDateTime maintenant);

    // Sessions par adresse IP
    List<UserSession> findByIpAddressOrderByDateCreationDesc(String ipAddress);

    // Sessions par période
    List<UserSession> findByDateCreationBetweenOrderByDateCreationDesc(LocalDateTime dateDebut, LocalDateTime dateFin);
    Page<UserSession> findByDateCreationBetween(LocalDateTime dateDebut, LocalDateTime dateFin, Pageable pageable);

    // Statistiques - comptages simples
    long countBySessionActiveTrueAndDateExpirationAfter(LocalDateTime maintenant);
    long countByDateCreationBetween(LocalDateTime dateDebut, LocalDateTime dateFin);

    // Sessions par utilisateur et période
    List<UserSession> findByUtilisateurIdAndDateCreationBetweenOrderByDateCreationDesc(
            Long utilisateurId, LocalDateTime dateDebut, LocalDateTime dateFin);

    // Sessions par IP et période
    List<UserSession> findByIpAddressAndDateCreationBetweenOrderByDateCreationDesc(
            String ipAddress, LocalDateTime dateDebut, LocalDateTime dateFin);

    // Sessions par navigateur et période
    List<UserSession> findByNavigateurAndDateCreationBetweenOrderByDateCreationDesc(
            String navigateur, LocalDateTime dateDebut, LocalDateTime dateFin);

    // Sessions avec durée non nulle
    Page<UserSession> findByDureeSessionIsNotNullOrderByDureeSessionDesc(Pageable pageable);

    // Sessions actives par utilisateur et IP
    List<UserSession> findByUtilisateurIdAndIpAddressAndSessionActiveTrueAndDateExpirationAfter(
            Long utilisateurId, String ipAddress, LocalDateTime maintenant);

    // Sessions inactives anciennes
    List<UserSession> findBySessionActiveFalseAndDateCreationBefore(LocalDateTime dateLimit);

    // Recherche par critères multiples
    List<UserSession> findByUtilisateurIdAndIpAddressAndSessionActiveOrderByDateCreationDesc(
            Long utilisateurId, String ipAddress, Boolean sessionActive);

    // Sessions par période et statut
    List<UserSession> findByDateCreationBetweenAndSessionActiveOrderByDateCreationDesc(
            LocalDateTime dateDebut, LocalDateTime dateFin, Boolean sessionActive);

    Page<UserSession> findByDateCreationBetweenAndSessionActive(
            LocalDateTime dateDebut, LocalDateTime dateFin, Boolean sessionActive, Pageable pageable);

    // Sessions par utilisateur et statut
    List<UserSession> findByUtilisateurIdAndSessionActiveOrderByDateCreationDesc(Long utilisateurId, Boolean sessionActive);

    // Sessions par IP et statut
    List<UserSession> findByIpAddressAndSessionActiveOrderByDateCreationDesc(String ipAddress, Boolean sessionActive);

    // Requêtes JPQL personnalisées

    // Comptage des utilisateurs actifs distincts
    @Query("SELECT COUNT(DISTINCT s.utilisateur.id) FROM UserSession s WHERE s.sessionActive = true AND s.dateExpiration > :maintenant")
    long countActiveUsers(@Param("maintenant") LocalDateTime maintenant);

    // Statistiques par utilisateur
    @Query("SELECT s.utilisateur.username, COUNT(s) FROM UserSession s WHERE s.dateCreation >= :dateDebut AND s.dateCreation <= :dateFin GROUP BY s.utilisateur.username ORDER BY COUNT(s) DESC")
    List<Object[]> getSessionStatsByUser(@Param("dateDebut") LocalDateTime dateDebut, @Param("dateFin") LocalDateTime dateFin);

    // Statistiques par IP
    @Query("SELECT s.ipAddress, COUNT(s) FROM UserSession s WHERE s.dateCreation >= :dateDebut AND s.dateCreation <= :dateFin GROUP BY s.ipAddress ORDER BY COUNT(s) DESC")
    List<Object[]> getSessionStatsByIP(@Param("dateDebut") LocalDateTime dateDebut, @Param("dateFin") LocalDateTime dateFin);

    // Statistiques par navigateur
    @Query("SELECT s.navigateur, COUNT(s) FROM UserSession s WHERE s.dateCreation >= :dateDebut AND s.dateCreation <= :dateFin AND s.navigateur IS NOT NULL GROUP BY s.navigateur ORDER BY COUNT(s) DESC")
    List<Object[]> getSessionStatsByBrowser(@Param("dateDebut") LocalDateTime dateDebut, @Param("dateFin") LocalDateTime dateFin);

    // Durée moyenne des sessions
    @Query("SELECT AVG(s.dureeSession) FROM UserSession s WHERE s.dureeSession IS NOT NULL AND s.dateCreation >= :dateDebut AND s.dateCreation <= :dateFin")
    Double getAverageSessionDuration(@Param("dateDebut") LocalDateTime dateDebut, @Param("dateFin") LocalDateTime dateFin);

    // Recherche avancée avec critères optionnels
    @Query("SELECT s FROM UserSession s WHERE " +
           "(:utilisateurId IS NULL OR s.utilisateur.id = :utilisateurId) AND " +
           "(:ipAddress IS NULL OR s.ipAddress = :ipAddress) AND " +
           "(:sessionActive IS NULL OR s.sessionActive = :sessionActive) AND " +
           "(:dateDebut IS NULL OR s.dateCreation >= :dateDebut) AND " +
           "(:dateFin IS NULL OR s.dateCreation <= :dateFin) " +
           "ORDER BY s.dateCreation DESC")
    Page<UserSession> rechercheAvancee(@Param("utilisateurId") Long utilisateurId,
                                       @Param("ipAddress") String ipAddress,
                                       @Param("sessionActive") Boolean sessionActive,
                                       @Param("dateDebut") LocalDateTime dateDebut,
                                       @Param("dateFin") LocalDateTime dateFin,
                                       Pageable pageable);

    // Sessions suspectes - attention: requête dépendante du dialecte (EXTRACT/EPOCH)
      @Query(value = """
            SELECT u.username,
                   s1.ip_address,
                   s2.ip_address,
                   s1.date_creation,
                   s2.date_creation
            FROM user_sessions s1
            JOIN user_sessions s2
                  ON s1.utilisateur_id = s2.utilisateur_id
            JOIN utilisateurs u
                  ON s1.utilisateur_id = u.id
            WHERE s1.id <> s2.id
              AND s1.ip_address <> s2.ip_address
              AND s1.session_active = true
              AND s2.session_active = true
              AND s1.date_creation >= :dateDebut
              AND ABS(
                    EXTRACT(EPOCH FROM (s1.date_creation - s2.date_creation))
                  ) < (:intervalleMinutes * 60)
            """, nativeQuery = true)
        List<Object[]> findSuspiciousSessions(@Param("dateDebut") LocalDateTime dateDebut,
                                              @Param("intervalleMinutes") long intervalleMinutes);
    // Nettoyage des anciennes sessions
    @Modifying
    @Transactional
    @Query("DELETE FROM UserSession s WHERE s.sessionActive = false AND s.dateCreation < :dateLimit")
    int deleteOldInactiveSessions(@Param("dateLimit") LocalDateTime dateLimit);


    //added

    @Query("SELECT s FROM UserSession s WHERE s.sessionId IN :sessionIds")
    List<UserSession> findBySessionIds(@Param("sessionIds") List<String> sessionIds);


}
