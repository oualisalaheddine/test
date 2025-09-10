package com.sh.erpcos.module.pontbascule.repository;

import com.sh.erpcos.module.pontbascule.entity.Produit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProduitRepository extends JpaRepository<Produit, Long> {
	Optional<Produit> findByDesignationProduit(String designationProduit);
}


