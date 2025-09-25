package com.sh.erpcos.module.pontbascule.repository;

import com.sh.erpcos.module.pontbascule.entity.Partenaire;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PartenaireRepository extends JpaRepository<Partenaire, Long> {
	
	List<Partenaire> findAllByOrderByRaisonSocialeAsc();
}


