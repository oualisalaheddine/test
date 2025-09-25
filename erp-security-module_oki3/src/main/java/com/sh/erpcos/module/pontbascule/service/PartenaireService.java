package com.sh.erpcos.module.pontbascule.service;

import com.sh.erpcos.module.pontbascule.entity.Partenaire;
import com.sh.erpcos.module.pontbascule.repository.PartenaireRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class PartenaireService {

	private final PartenaireRepository partenaireRepository;

	@Transactional(readOnly = true)
	public List<Partenaire> findAll() { return partenaireRepository.findAll(); }
	
	@Transactional(readOnly = true)
	public Optional<Partenaire> findById(Long id) { return partenaireRepository.findById(id); }

	public Partenaire save(Partenaire partenaire) { return partenaireRepository.save(partenaire); }

	public void deleteById(Long id) { partenaireRepository.deleteById(id); }
	
	public List<Partenaire> findAllOrderByRaisonSociale() {
	    return partenaireRepository.findAllByOrderByRaisonSocialeAsc();
	}
}


