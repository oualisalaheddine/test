package com.sh.erpcos.module.pontbascule.service;

import com.sh.erpcos.module.pontbascule.entity.Produit;
import com.sh.erpcos.module.pontbascule.repository.ProduitRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class ProduitService {

	private final ProduitRepository produitRepository;

	@Transactional(readOnly = true)
	public List<Produit> findAll() { return produitRepository.findAll(); }

	@Transactional(readOnly = true)
	public Optional<Produit> findById(Long id) { return produitRepository.findById(id); }

	public Produit save(Produit produit) { return produitRepository.save(produit); }

	public void deleteById(Long id) { produitRepository.deleteById(id); }
}


