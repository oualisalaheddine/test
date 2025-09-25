
package com.sh.erpcos.module.pontbascule.service;

import com.sh.erpcos.module.pontbascule.entity.OperationEnCoursInterne;
import com.sh.erpcos.module.pontbascule.repository.OperationEncoursInterneRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class OperationEncoursInterneService {


    private final OperationEncoursInterneRepository repository;

    @Transactional(readOnly = true)
    public List<OperationEnCoursInterne> findAll() {
        return repository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<OperationEnCoursInterne> findById(int id) {
        return repository.findById(id);
    }

   
    public OperationEnCoursInterne save(OperationEnCoursInterne operation) {
    	
    	
        return repository.save(operation);
    }

    public void deleteById(int id) {
        repository.deleteById(id);
    }
}
