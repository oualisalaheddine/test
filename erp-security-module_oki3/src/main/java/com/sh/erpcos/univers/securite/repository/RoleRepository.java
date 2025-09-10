package com.sh.erpcos.univers.securite.repository;

import com.sh.erpcos.univers.securite.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    
    Optional<Role> findByNom(String nom);
    
    boolean existsByNom(String nom);
    
    List<Role> findByRoleActif(boolean roleActif);
    
    List<Role> findByRoleParentIsNull();
    
    List<Role> findByRoleParent(Role roleParent);
    
    @Query("SELECT r FROM Role r WHERE r.niveauHierarchie = :niveau")
    List<Role> findByNiveauHierarchie(@Param("niveau") Integer niveau);
    
    @Query("SELECT r FROM Role r WHERE r.niveauHierarchie <= :niveauMax")
    List<Role> findByNiveauHierarchieMax(@Param("niveauMax") Integer niveauMax);
    
    @Query("SELECT r FROM Role r JOIN r.permissions p WHERE p.nom = :nomPermission")
    List<Role> findByPermissionNom(@Param("nomPermission") String nomPermission);
    
    @Query("SELECT r FROM Role r JOIN r.permissions p WHERE p.nomModule = :nomModule")
    List<Role> findByModule(@Param("nomModule") String nomModule);
    
    @Query("SELECT r FROM Role r WHERE r.nom LIKE %:recherche% OR r.description LIKE %:recherche%")
    List<Role> rechercherRoles(@Param("recherche") String recherche);
    
    @Query("SELECT r FROM Role r ORDER BY r.niveauHierarchie ASC, r.nom ASC")
    List<Role> findAllOrderByHierarchie();
    
    @Query("SELECT COUNT(r) FROM Role r WHERE r.roleActif = true")
    long countRolesActifs();
    
    @Query("SELECT u FROM Utilisateur u JOIN u.roles r WHERE r.nom = :nomRole")
    List<com.sh.erpcos.univers.securite.entity.Utilisateur> findUtilisateursByRole(@Param("nomRole") String nomRole);
}
