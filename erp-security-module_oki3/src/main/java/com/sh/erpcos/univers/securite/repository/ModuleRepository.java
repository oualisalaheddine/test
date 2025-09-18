package com.sh.erpcos.univers.securite.repository;

import java.util.List;
import java.util.Optional;
import com.sh.erpcos.univers.securite.entity.Module;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ModuleRepository extends JpaRepository<Module, Integer> {
    Optional<Module> findByCode(String code);
    List<Module> findByActifTrue();
    boolean existsByCode(String code);
}