package com.sh.erpcos.module.pontbascule.service;

import com.sh.erpcos.module.pontbascule.entity.VehiculeExterne;
import com.sh.erpcos.module.pontbascule.repository.VehiculeExterneRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class VehiculeExterneService {

	private final VehiculeExterneRepository repository;

	public List<VehiculeExterne> findAll() { return repository.findAll(); }

	public Optional<VehiculeExterne> findById(String id) { return repository.findById(id); }

	public VehiculeExterne save(VehiculeExterne v) { return repository.save(v); }

	public void deleteById(String id) { repository.deleteById(id); }
}


