package com.sh.erpcos.univers.securite.repository;

import com.sh.erpcos.univers.securite.entity.AuditLog;
import com.sh.erpcos.univers.securite.entity.UserSession;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
	
	

    // Recherche par utilisateur
    List<AuditLog> findByUsernameOrderByDateActionDesc(String username);
    
    Page<AuditLog> findByUsernameOrderByDateActionDesc(String username, Pageable pageable);
    
    List<AuditLog> findByUtilisateurIdOrderByDateActionDesc(Long utilisateurId);
    
    // Recherche par action
    List<AuditLog> findByActionContainingIgnoreCaseOrderByDateActionDesc(String action);
    
    // Recherche par ressource
    List<AuditLog> findByRessourceContainingIgnoreCaseOrderByDateActionDesc(String ressource);
    
    // Recherche par niveau
    List<AuditLog> findByNiveauOrderByDateActionDesc(AuditLog.NiveauAudit niveau);
    
    // Recherche par catégorie
    List<AuditLog> findByCategorieOrderByDateActionDesc(AuditLog.CategorieAudit categorie);
    
    // Recherche par période
    List<AuditLog> findByDateActionBetweenOrderByDateActionDesc(LocalDateTime dateDebut, LocalDateTime dateFin);
    
    Page<AuditLog> findByDateActionBetweenOrderByDateActionDesc(LocalDateTime dateDebut, LocalDateTime dateFin, Pageable pageable);
    
    // Recherche par succès/échec
    List<AuditLog> findBySuccesOrderByDateActionDesc(boolean succes);
    
    // Recherche par adresse IP
    List<AuditLog> findByIpAddressOrderByDateActionDesc(String ipAddress);
    
    // Recherche par session
    List<AuditLog> findBySessionIdOrderByDateActionDesc(String sessionId);
    
    // Recherche combinée avec JPQL
    @Query("SELECT a FROM AuditLog a WHERE " +
           "(:username IS NULL OR a.username = :username) AND " +
           "(:action IS NULL OR LOWER(a.action) LIKE LOWER(CONCAT('%', :action, '%'))) AND " +
           "(:ressource IS NULL OR LOWER(a.ressource) LIKE LOWER(CONCAT('%', :ressource, '%'))) AND " +
           "(:niveau IS NULL OR a.niveau = :niveau) AND " +
           "(:categorie IS NULL OR a.categorie = :categorie) AND " +
           "(:dateDebut IS NULL OR a.dateAction >= :dateDebut) AND " +
           "(:dateFin IS NULL OR a.dateAction <= :dateFin) AND " +
           "(:succes IS NULL OR a.succes = :succes) " +
           "ORDER BY a.dateAction DESC")
    Page<AuditLog> rechercheAvancee(@Param("username") String username,
                                   @Param("action") String action,
                                   @Param("ressource") String ressource,
                                   @Param("niveau") AuditLog.NiveauAudit niveau,
                                   @Param("categorie") AuditLog.CategorieAudit categorie,
                                   @Param("dateDebut") LocalDateTime dateDebut,
                                   @Param("dateFin") LocalDateTime dateFin,
                                   @Param("succes") Boolean succes,
                                   Pageable pageable);
    
    // Statistiques avec JPQL
    long countByDateActionBetween(LocalDateTime dateDebut, LocalDateTime dateFin);
    
    @Query("SELECT a.niveau, COUNT(a) FROM AuditLog a WHERE a.dateAction BETWEEN :dateDebut AND :dateFin GROUP BY a.niveau")
    List<Object[]> getStatistiquesParNiveau(@Param("dateDebut") LocalDateTime dateDebut, @Param("dateFin") LocalDateTime dateFin);
    
    @Query("SELECT a.categorie, COUNT(a) FROM AuditLog a WHERE a.dateAction BETWEEN :dateDebut AND :dateFin GROUP BY a.categorie")
    List<Object[]> getStatistiquesParCategorie(@Param("dateDebut") LocalDateTime dateDebut, @Param("dateFin") LocalDateTime dateFin);
    
    @Query("SELECT a.username, COUNT(a) FROM AuditLog a WHERE a.dateAction BETWEEN :dateDebut AND :dateFin GROUP BY a.username ORDER BY COUNT(a) DESC")
    List<Object[]> getStatistiquesParUtilisateur(@Param("dateDebut") LocalDateTime dateDebut, @Param("dateFin") LocalDateTime dateFin);
    
    @Query("SELECT a.action, COUNT(a) FROM AuditLog a WHERE a.dateAction BETWEEN :dateDebut AND :dateFin GROUP BY a.action ORDER BY COUNT(a) DESC")
    List<Object[]> getStatistiquesParAction(@Param("dateDebut") LocalDateTime dateDebut, @Param("dateFin") LocalDateTime dateFin);
    
    // Logs récents
    Page<AuditLog> findAllByOrderByDateActionDesc(Pageable pageable);
    
    // Logs d'erreur récents
    Page<AuditLog> findBySuccesFalseOrderByDateActionDesc(Pageable pageable);
    
    // Logs critiques récents
    Page<AuditLog> findByNiveauOrderByDateActionDesc(AuditLog.NiveauAudit niveau, Pageable pageable);
    
    // Activité par utilisateur dans une période
    List<AuditLog> findByUsernameAndDateActionBetweenOrderByDateActionDesc(String username, LocalDateTime dateDebut, LocalDateTime dateFin);
    
    // Tentatives de connexion échouées
    List<AuditLog> findByCategorieAndSuccesFalseOrderByDateActionDesc(AuditLog.CategorieAudit categorie);
    
    // Activités suspectes avec JPQL
    @Query("SELECT a.username, a.ipAddress, COUNT(a) FROM AuditLog a " +
           "WHERE a.categorie = :categorie AND a.succes = false " +
           "AND a.dateAction >= :dateDebut " +
           "GROUP BY a.username, a.ipAddress " +
           "HAVING COUNT(a) >= :seuilTentatives " +
           "ORDER BY COUNT(a) DESC")
    List<Object[]> findActivitesSuspectes(@Param("categorie") AuditLog.CategorieAudit categorie,
                                         @Param("dateDebut") LocalDateTime dateDebut, 
                                         @Param("seuilTentatives") long seuilTentatives);
    
    // Nettoyage des anciens logs
    void deleteByDateActionBefore(LocalDateTime dateLimit);
    
    //added
    long countByDateActionAfter(LocalDateTime dateAction);
    long countBySuccesFalseAndDateActionAfter(LocalDateTime dateAction);
    
 // Query pour récupérer les top utilisateurs actifs depuis une certaine date
    @Query("SELECT a.username, COUNT(a) as actionsCount " +
           "FROM AuditLog a " +
           "WHERE a.dateAction >= :since " +
           "GROUP BY a.username " +
           "ORDER BY actionsCount DESC")
    Page<Object[]> findTopUsersByActivity(@Param("since") LocalDateTime since, Pageable pageable);

 // Query pour récupérer les sessions suspectes (par exemple : sessions ayant plus de :threshold événements)
    @Query("SELECT a.sessionId, COUNT(a) as sessionCount " +
           "FROM AuditLog a " +
           "WHERE a.dateAction >= :since AND a.sessionId IS NOT NULL " +
           "GROUP BY a.sessionId " +
           "HAVING COUNT(a) >= :threshold")
    List<String> findSuspiciousSessionIds(@Param("since") LocalDateTime since, @Param("threshold") long threshold);
    
}