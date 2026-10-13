package com.compa.service;

import com.compa.enums.MotivoConsulta;
import com.compa.enums.PrioridadMensaje;
import com.compa.model.MensajeAcompanamiento;
import com.compa.repository.MensajeAcompanamientoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static com.compa.enums.MotivoConsulta.*;
import static com.compa.enums.PrioridadMensaje.ALTA;
import static com.compa.enums.PrioridadMensaje.BAJA;

// Carga el banco inicial de mensajes la primera vez que arranca la app (solo si la tabla está vacía).
@Slf4j
@Component
@RequiredArgsConstructor
public class MensajeAcompanamientoSeeder implements ApplicationRunner {

    private final MensajeAcompanamientoRepository repository;

    @Override
    public void run(ApplicationArguments args) {
        if (repository.count() > 0) {
            return;
        }
        List<MensajeAcompanamiento> banco = new ArrayList<>();

        // Mensajes generales (para estudiantes sin motivos de consulta registrados)
        banco.add(m(null, BAJA, "Queremos recordarte que no estás solo: tu psicoorientador y COMPA te acompañan. Elige una meta pequeña para estos días y cúmplela paso a paso."));
        banco.add(m(null, BAJA, "Cuidarte también cuenta: duerme lo suficiente, come bien y haz pausas. Cada pequeño hábito suma."));
        banco.add(m(null, ALTA, "Recuerda llenar tu check-in diario y marcar las actividades de tu plan. Ese registro ayuda a tu psicoorientador a acompañarte mejor entre sesiones."));

        // Mensajes por motivo de consulta: uno de prioridad alta y uno de prioridad baja
        banco.add(m(ESTRES_ACADEMICO, ALTA, "Antes de empezar la semana, escribe todo lo que tienes pendiente y elige las tres cosas que sí o sí debes hacer. Lo demás puede esperar. Si sientes que se te acumula demasiado, cuéntaselo a tu psicoorientador en tu próxima sesión."));
        banco.add(m(ESTRES_ACADEMICO, BAJA, "Estudiar con pausas ayuda más que estudiar sin parar. Prueba 45 minutos de estudio y 10 de descanso, y felicítate por cada bloque que completes."));

        banco.add(m(ANSIEDAD, ALTA, "Si esta semana sientes que la ansiedad te desborda, haz una pausa y respira lento: inhala 4 segundos y exhala 6, varias veces. No esperes a la próxima sesión si lo necesitas: busca a tu psicoorientador."));
        banco.add(m(ANSIEDAD, BAJA, "Notar lo que sientes ya es un paso. Esta semana intenta ponerle nombre a lo que te preocupa y anota una cosa pequeña que sí puedes hacer hoy."));

        banco.add(m(ESTADO_DE_ANIMO, ALTA, "Si en los últimos días te has sentido más decaído de lo normal, cuéntaselo a alguien de confianza y a tu psicoorientador. No tienes que pasarlo solo."));
        banco.add(m(ESTADO_DE_ANIMO, BAJA, "Los días buenos y los malos pasan. Esta semana busca un momento corto para algo que te guste, aunque sean diez minutos."));

        banco.add(m(MANEJO_DEL_TIEMPO, ALTA, "Dedica hoy diez minutos a armar tu semana: pon en tu calendario tus clases, tus entregas y dos bloques para estudiar. Lo que no está en el calendario es fácil que se quede por fuera."));
        banco.add(m(MANEJO_DEL_TIEMPO, BAJA, "Empieza cada día eligiendo una tarea principal. Si la terminas, el día ya fue productivo."));

        banco.add(m(PROCRASTINACION, ALTA, "Escoge hoy la tarea que más has aplazado y trabaja en ella solo 5 minutos. Empezar es lo más difícil; después suele ser más fácil seguir."));
        banco.add(m(PROCRASTINACION, BAJA, "Dividir una tarea grande en pasos pequeños la hace menos pesada. Esta semana prueba con una."));

        banco.add(m(SUENO, ALTA, "Esta semana intenta acostarte y levantarte a la misma hora todos los días y dejar el celular un rato antes de dormir. Si sigues durmiendo mal, coméntaselo a tu psicoorientador."));
        banco.add(m(SUENO, BAJA, "Dormir bien también es parte de estudiar bien. Esta noche prueba apagar las pantallas 30 minutos antes de acostarte."));

        banco.add(m(MOTIVACION, ALTA, "Escribe hoy una razón por la que elegiste tu carrera y ponla donde la veas. Si sientes que no tienes ganas de nada, habla de eso en tu próxima sesión."));
        banco.add(m(MOTIVACION, BAJA, "No siempre se necesita motivación para empezar; a veces llega después de empezar. Elige una acción pequeña y hazla hoy."));

        banco.add(m(ADAPTACION_UNIVERSITARIA, ALTA, "Adaptarse a la universidad toma tiempo. Esta semana da un paso concreto: habla con un compañero, pregúntale una duda a un profesor o únete a un grupo de la universidad."));
        banco.add(m(ADAPTACION_UNIVERSITARIA, BAJA, "Es normal sentirse nuevo o fuera de lugar al principio. Muchos de tus compañeros también lo sienten."));

        banco.add(m(RELACIONES_INTERPERSONALES, ALTA, "Si hay una conversación pendiente con alguien importante para ti, busca esta semana un momento tranquilo para tenerla. Si no sabes cómo empezar, puedes practicarlo con tu psicoorientador."));
        banco.add(m(RELACIONES_INTERPERSONALES, BAJA, "Escuchar con atención y decir con calma lo que sientes mejora cualquier relación. Esta semana prueba con una conversación."));

        banco.add(m(ORIENTACION_VOCACIONAL, ALTA, "Esta semana habla con alguien que ya trabaje en lo que te interesa y pregúntale cómo es su día a día. Anota qué te llamó la atención y llévalo a tu próxima sesión."));
        banco.add(m(ORIENTACION_VOCACIONAL, BAJA, "No tienes que tener todo claro ahora. Fíjate en qué actividades te hacen perder la noción del tiempo: ahí hay pistas."));

        repository.saveAll(banco);
        log.info("Banco de mensajes de acompañamiento cargado: {} mensajes", banco.size());
    }

    private MensajeAcompanamiento m(MotivoConsulta motivo, PrioridadMensaje prioridad, String texto) {
        return new MensajeAcompanamiento(motivo, prioridad, texto);
    }
}