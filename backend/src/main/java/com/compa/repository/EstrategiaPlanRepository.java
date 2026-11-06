package com.compa.repository;

import com.compa.enums.EstadoEstrategia;
import com.compa.model.EstrategiaPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
 
import java.util.List;
 
@Repository
public interface EstrategiaPlanRepository extends JpaRepository<EstrategiaPlan, Long> {
 
    // Historial completo del estudiante (incluye anuladas), en orden cronológico
    List<EstrategiaPlan> findByEstudianteIdOrderByFechaSesionAscIdAsc(Long estudianteId);
 
    // Las vigentes de un plan, en orden cronológico
    List<EstrategiaPlan> findByPlanIdAndEstadoOrderByFechaSesionAscIdAsc(Long planId, EstadoEstrategia estado);
}
 