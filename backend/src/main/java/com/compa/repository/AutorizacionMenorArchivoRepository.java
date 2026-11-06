package com.compa.repository;

import com.compa.model.AutorizacionMenorArchivo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AutorizacionMenorArchivoRepository extends JpaRepository<AutorizacionMenorArchivo, Long> {

    Optional<AutorizacionMenorArchivo> findByAutorizacionId(Long autorizacionId);
}