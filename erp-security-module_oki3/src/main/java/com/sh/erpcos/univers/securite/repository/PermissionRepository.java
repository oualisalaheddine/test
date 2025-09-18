package com.sh.erpcos.univers.securite.repository;

import com.sh.erpcos.univers.securite.entity.Permission;
import com.sh.erpcos.univers.securite.entity.Module;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, Integer> {
    
    Optional<Permission> findByNom(String nom);
    
    boolean existsByNom(String nom);
    
    List<Permission> findByPermissionActif(boolean permissionActif);
    
    List<Permission> findByNomModule(String nomModule);
    
    List<Permission> findByNomAction(String nomAction);
    
    List<Permission> findByNomModuleAndNomAction(String nomModule, String nomAction);
    
    List<Permission> findByModule(Module module);
    
    @Query("SELECT p FROM Permission p WHERE p.nomModule = :nomModule AND p.permissionActif = true")
    List<Permission> findPermissionsActivesByModule(@Param("nomModule") String nomModule);
    
    @Query("SELECT DISTINCT p.nomModule FROM Permission p WHERE p.permissionActif = true ORDER BY p.nomModule")
    List<String> findModulesDisponibles();
    
    @Query("SELECT DISTINCT p.nomAction FROM Permission p WHERE p.nomModule = :nomModule AND p.permissionActif = true ORDER BY p.nomAction")
    List<String> findActionsByModule(@Param("nomModule") String nomModule);
    
    @Query("SELECT p FROM Permission p WHERE p.nom LIKE %:recherche% OR p.description LIKE %:recherche% OR p.nomModule LIKE %:recherche%")
    List<Permission> rechercherPermissions(@Param("recherche") String recherche);
    
    @Query("SELECT p FROM Permission p WHERE p.urlPattern IS NOT NULL AND p.urlPattern != ''")
    List<Permission> findPermissionsAvecUrlPattern();
    
    @Query("SELECT p FROM Permission p ORDER BY p.nomModule ASC, p.nomAction ASC")
    List<Permission> findAllOrderByModuleEtAction();
    
    @Query("SELECT COUNT(p) FROM Permission p WHERE p.permissionActif = true")
    long countPermissionsActives();
    
    @Query("SELECT COUNT(p) FROM Permission p WHERE p.nomModule = :nomModule AND p.permissionActif = true")
    long countPermissionsActivesByModule(@Param("nomModule") String nomModule);
        
    @EntityGraph(attributePaths = {"roles"})
    @Query("SELECT p FROM Permission p WHERE p.nom = :nom")
    Optional<Permission> findByNomWithRoles(@Param("nom") String nom);
    
    @Query("SELECT r FROM Role r JOIN r.permissions p WHERE p.nom = :nomPermission")
    List<com.sh.erpcos.univers.securite.entity.Role> findRolesByPermission(@Param("nomPermission") String nomPermission);
}
