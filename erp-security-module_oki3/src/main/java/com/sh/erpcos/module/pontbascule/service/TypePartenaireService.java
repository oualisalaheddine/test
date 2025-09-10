package com.sh.erpcos.module.pontbascule.service;

import com.sh.erpcos.module.pontbascule.entity.Partenaire;
import com.sh.erpcos.module.pontbascule.entity.TypePartenaire;
import com.sh.erpcos.module.pontbascule.repository.TypePartenaireRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class TypePartenaireService {

	private final TypePartenaireRepository repository;

	@Transactional(readOnly = true)
	public List<TypePartenaire> findAll() { return repository.findAll(); }
	
	@Transactional(readOnly = true)
	public Optional<TypePartenaire> findById(Long id) { return repository.findById(id); }
	
	public TypePartenaire save(TypePartenaire v) { return repository.save(v); }

	public void deleteById(Long id) { repository.deleteById(id); }
}


