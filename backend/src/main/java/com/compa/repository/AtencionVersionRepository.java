package com.compa.repository;

import com.compa.model.AtencionVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AtencionVersionRepository extends JpaRepository<AtencionVersion, Long> {
    List<AtencionVersion> findByAtencionIdOrderByNumeroDesc(Long atencionId);

    int countByAtencionId(Long atencionId);
}