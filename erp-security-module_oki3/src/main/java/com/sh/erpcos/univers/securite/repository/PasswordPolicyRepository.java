package com.sh.erpcos.univers.securite.repository;

import com.sh.erpcos.univers.securite.entity.PasswordPolicy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PasswordPolicyRepository extends JpaRepository<PasswordPolicy, Long> {
    
    // Recherche par nom
    Optional<PasswordPolicy> findByNomPolitique(String nomPolitique);
    
    // Politique par défaut
    Optional<PasswordPolicy> findByPolitiqueParDefautTrue();
    
    // Politiques actives
    List<PasswordPolicy> findByPolitiqueActiveTrueOrderByNomPolitique();
    
    // Politiques inactives
    List<PasswordPolicy> findByPolitiqueActiveFalseOrderByNomPolitique();
    
    // Vérifier l'existence d'une politique par nom
    boolean existsByNomPolitique(String nomPolitique);
    
    // Compter les politiques actives
    @Query("SELECT COUNT(p) FROM PasswordPolicy p WHERE p.politiqueActive = true")
    long countActivePolicies();
    
    // Recherche par critères
    @Query("SELECT p FROM PasswordPolicy p WHERE " +
           "(:nom IS NULL OR p.nomPolitique LIKE %:nom%) AND " +
           "(:actif IS NULL OR p.politiqueActive = :actif) AND " +
           "(:longueurMin IS NULL OR p.longueurMinimale >= :longueurMin) AND " +
           "(:longueurMax IS NULL OR p.longueurMaximale <= :longueurMax) " +
           "ORDER BY p.nomPolitique")
    List<PasswordPolicy> rechercheAvancee(@Param("nom") String nom,
                                         @Param("actif") Boolean actif,
                                         @Param("longueurMin") Integer longueurMin,
                                         @Param("longueurMax") Integer longueurMax);
    
    // Politiques avec exigences spécifiques
    List<PasswordPolicy> findByExigerMajusculesTrue();
    List<PasswordPolicy> findByExigerMinusculesTrue();
    List<PasswordPolicy> findByExigerChiffresTrue();
    List<PasswordPolicy> findByExigerCaracteresSpeciauxTrue();
    
    // Politiques avec historique
    @Query("SELECT p FROM PasswordPolicy p WHERE p.historiqueMotesPasse > 0 ORDER BY p.historiqueMotesPasse DESC")
    List<PasswordPolicy> findPoliciesWithHistory();
    
    // Politiques avec expiration
    @Query("SELECT p FROM PasswordPolicy p WHERE p.dureeValiditeJours > 0 ORDER BY p.dureeValiditeJours ASC")
    List<PasswordPolicy> findPoliciesWithExpiration();
    
    // Politiques les plus strictes
    @Query("SELECT p FROM PasswordPolicy p WHERE " +
           "p.longueurMinimale >= :minLength AND " +
           "p.exigerMajuscules = true AND " +
           "p.exigerMinuscules = true AND " +
           "p.exigerChiffres = true AND " +
           "p.exigerCaracteresSpeciaux = true " +
           "ORDER BY p.longueurMinimale DESC")
    List<PasswordPolicy> findStrictPolicies(@Param("minLength") int minLength);
}