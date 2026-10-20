package com.compa.repository;

import com.compa.model.Orientador;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrientadorRepository extends JpaRepository<Orientador, Long>
{
    Optional<Orientador> findByEmail(String email);

    Optional<Orientador> findByVerificationToken(String token);

    boolean existsByEmail(String email);

    boolean existsByEmailIgnoreCase(String email);


}
