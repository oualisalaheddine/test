
package com.sh.erpcos.module.pontbascule.service;

import com.sh.erpcos.module.pontbascule.entity.OperationEnCoursExterne;
import com.sh.erpcos.module.pontbascule.repository.OperationEncoursExterneRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class OperationEncoursExterneService {


    private final OperationEncoursExterneRepository repository;
    @Transactional(readOnly = true)
    public List<OperationEnCoursExterne> findAll() {
        return repository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<OperationEnCoursExterne> findById(Integer id) {
        return repository.findById(id);
    }
    
 

    public OperationEnCoursExterne save(OperationEnCoursExterne operation) {
    	
        return repository.save(operation);
    }

    public void deleteById(Integer id) {
        repository.deleteById(id);
    }
}
