package com.sh.erpcos.module.pontbascule.service;

import com.sh.erpcos.module.pontbascule.entity.Partenaire;
import com.sh.erpcos.module.pontbascule.entity.TypeOperation;
import com.sh.erpcos.module.pontbascule.repository.TypeOperationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class TypeOperationService {

	private final TypeOperationRepository repository;

	@Transactional(readOnly = true)
	public List<TypeOperation> findAll() { return repository.findAll(); }
	
	@Transactional(readOnly = true)
	public Optional<TypeOperation> findById(Integer id) { return repository.findById(id); }
	
	public TypeOperation save(TypeOperation v) { return repository.save(v); }

	public void deleteById(Integer id) { repository.deleteById(id); }
	
	public List<TypeOperation> findAllOrderByLibelle() {
	    return repository.findAllByOrderByLibelleAsc();
	}
}


