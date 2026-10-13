package com.compa.repository;

import com.compa.model.MensajeAcompanamiento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MensajeAcompanamientoRepository extends JpaRepository<MensajeAcompanamiento, Long> {
    List<MensajeAcompanamiento> findByActivoTrue();
}