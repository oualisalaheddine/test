package com.sh.erpcos.module.pontbascule.service;

import com.sh.erpcos.module.pontbascule.entity.VehiculeTarer;
import com.sh.erpcos.module.pontbascule.repository.VehiculeTarerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class VehiculeTarerService {

	private final VehiculeTarerRepository repository;

	@Transactional(readOnly = true)
	public List<VehiculeTarer> findAll() { return repository.findAll(); }

	@Transactional(readOnly = true)
	public Optional<VehiculeTarer> findById(String id) { return repository.findById(id); }

	public VehiculeTarer save(VehiculeTarer v) { return repository.save(v); }

	public void deleteById(String id) { repository.deleteById(id); }
}


