package com.sh.erpcos.module.pontbascule.repository;

import com.sh.erpcos.module.pontbascule.entity.TypeOperation;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TypeOperationRepository extends JpaRepository<TypeOperation, Integer> {

	List<TypeOperation> findAllByOrderByLibelleAsc();

}
