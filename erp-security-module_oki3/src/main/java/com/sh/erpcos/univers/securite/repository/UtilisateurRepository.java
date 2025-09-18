package com.sh.erpcos.univers.securite.repository;

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
public interface UtilisateurRepository extends JpaRepository<Utilisateur, Long> {
	

    
    Optional<Utilisateur> findByUsername(String username);
    
    @Query("SELECT u FROM Utilisateur u " +
            "JOIN FETCH u.roles r " +
            "JOIN FETCH r.permissions p " +
            "WHERE u.username = :username")
     Optional<Utilisateur> findByUsernameWithPermissions(@Param("username") String username);
    
    Optional<Utilisateur> findByEmail(String email);
    
    boolean existsByUsername(String username);
    
    boolean existsByEmail(String email);
    
    List<Utilisateur> findByCompteActif(boolean compteActif);
    
    @Query("SELECT u FROM Utilisateur u JOIN u.roles r WHERE r.nom = :nomRole")
    List<Utilisateur> findByRoleNom(@Param("nomRole") String nomRole);
    
    @Query("SELECT u FROM Utilisateur u JOIN u.roles r JOIN r.permissions p WHERE p.nom = :nomPermission")
    List<Utilisateur> findByPermissionNom(@Param("nomPermission") String nomPermission);
    
    @Query("SELECT u FROM Utilisateur u JOIN u.roles r JOIN r.permissions p WHERE p.nomModule = :nomModule")
    List<Utilisateur> findByModule(@Param("nomModule") String nomModule);
    
    @Query("SELECT u FROM Utilisateur u WHERE u.nom LIKE %:recherche% OR u.prenom LIKE %:recherche% OR u.email LIKE %:recherche% OR u.username LIKE %:recherche%")
    List<Utilisateur> rechercherUtilisateurs(@Param("recherche") String recherche);
    
    @Query("SELECT u FROM Utilisateur u WHERE u.nom LIKE %:recherche% OR u.prenom LIKE %:recherche% OR u.email LIKE %:recherche% OR u.username LIKE %:recherche%")
    Page<Utilisateur> rechercherUtilisateursPage(@Param("recherche") String recherche, Pageable pageable);
    
    @Query("SELECT COUNT(u) FROM Utilisateur u WHERE u.compteActif = true")
    long countUtilisateursActifs();
    
    @Query("SELECT COUNT(u) FROM Utilisateur u WHERE u.compteActif = false")
    long countUtilisateursInactifs();
    
    @Query("SELECT u FROM Utilisateur u WHERE u.derniereConnexion IS NOT NULL ORDER BY u.derniereConnexion DESC")
    List<Utilisateur> findUtilisateursRecents();
    
    @Query("SELECT u FROM Utilisateur u WHERE u.derniereConnexion IS NOT NULL ORDER BY u.derniereConnexion DESC")
    Page<Utilisateur> findUtilisateursRecentsPage(Pageable pageable);
    
    @Query("SELECT u FROM Utilisateur u WHERE u.dateCreation >= :dateDebut ORDER BY u.dateCreation DESC")
    List<Utilisateur> findUtilisateursCreesDepuis(@Param("dateDebut") LocalDateTime dateDebut);
    
    @Query("SELECT u FROM Utilisateur u WHERE u.derniereConnexion IS NULL OR u.derniereConnexion < :dateLimite")
    List<Utilisateur> findUtilisateursInactifs(@Param("dateLimite") LocalDateTime dateLimite);
    
    @Query("SELECT u FROM Utilisateur u WHERE u.compteNonVerrouille = false")
    List<Utilisateur> findUtilisateursVerrouilles();
    
    @Query("SELECT u FROM Utilisateur u WHERE u.credentialsNonExpire = false")
    List<Utilisateur> findUtilisateursMotDePasseExpire();
    
    @Modifying
    @Transactional
    @Query("UPDATE Utilisateur u SET u.derniereConnexion = :dateConnexion WHERE u.username = :username")
    void updateDerniereConnexion(@Param("username") String username, @Param("dateConnexion") LocalDateTime dateConnexion);
    
    @Modifying
    @Transactional
    @Query("UPDATE Utilisateur u SET u.compteNonVerrouille = :statut WHERE u.id = :id")
    void updateStatutVerrouillage(@Param("id") Long id, @Param("statut") boolean statut);
    
    @Query("SELECT COUNT(u) FROM Utilisateur u WHERE u.dateCreation >= :dateDebut AND u.dateCreation <= :dateFin")
    long countUtilisateursCreesEntreDates(@Param("dateDebut") LocalDateTime dateDebut, @Param("dateFin") LocalDateTime dateFin);
    
    @Query("SELECT COUNT(u) FROM Utilisateur u WHERE u.derniereConnexion >= :dateDebut AND u.derniereConnexion <= :dateFin")
    long countUtilisateursConnectesEntreDates(@Param("dateDebut") LocalDateTime dateDebut, @Param("dateFin") LocalDateTime dateFin);
    
    // Statistiques par rôle
    @Query("SELECT r.nom, COUNT(u) FROM Utilisateur u JOIN u.roles r GROUP BY r.nom ORDER BY COUNT(u) DESC")
    List<Object[]> getStatistiquesParRole();
    
    // Utilisateurs avec permissions spécifiques
    @Query("SELECT DISTINCT u FROM Utilisateur u JOIN u.roles r JOIN r.permissions p WHERE p.nomModule = :module AND p.nomAction = :action")
    List<Utilisateur> findUtilisateursAvecPermission(@Param("module") String module, @Param("action") String action);
    
    // Recherche avancée
    @Query("SELECT u FROM Utilisateur u WHERE " +
           "(:nom IS NULL OR u.nom LIKE %:nom%) AND " +
           "(:prenom IS NULL OR u.prenom LIKE %:prenom%) AND " +
           "(:email IS NULL OR u.email LIKE %:email%) AND " +
           "(:actif IS NULL OR u.compteActif = :actif)")
    Page<Utilisateur> rechercheAvancee(@Param("nom") String nom, 
                                      @Param("prenom") String prenom, 
                                      @Param("email") String email, 
                                      @Param("actif") Boolean actif, 
                                      Pageable pageable);
}
