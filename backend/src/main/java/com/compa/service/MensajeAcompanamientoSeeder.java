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

// Carga el banco de mensajes por motivo la primera vez que arranca la app (si no hay mensajes activos por motivo).
@Slf4j
@Component
@RequiredArgsConstructor
public class MensajeAcompanamientoSeeder implements ApplicationRunner {

    private final MensajeAcompanamientoRepository repository;

    @Override
    public void run(ApplicationArguments args) {
        List<MensajeAcompanamiento> existentes = repository.findByActivoTrue();
        boolean hayPorMotivo = existentes.stream().anyMatch(x -> x.getMotivo() != null);
        if (hayPorMotivo) {
            return;
        }
        boolean hayGenerales = existentes.stream().anyMatch(x -> x.getMotivo() == null);
        List<MensajeAcompanamiento> banco = new ArrayList<>();

        // Mensajes generales (para estudiantes sin motivos de consulta registrados)
        if (!hayGenerales) {
            banco.add(m(null, BAJA, "Queremos recordarte que no estás solo: tu psicoorientador y COMPA te acompañan. Elige una meta pequeña para estos días y cúmplela paso a paso."));
            banco.add(m(null, BAJA, "Cuidarte también cuenta: duerme lo suficiente, come bien y haz pausas. Cada pequeño hábito suma."));
            banco.add(m(null, ALTA, "Recuerda llenar tu check-in diario y marcar las actividades de tu plan. Ese registro ayuda a tu psicoorientador a acompañarte mejor entre sesiones."));
        }

        // Mensajes por motivo de consulta: uno de prioridad alta y uno de prioridad baja
        banco.add(m(BIENESTAR_EMOCIONAL, ALTA, "Esta semana dedica unos minutos cada día a notar cómo te sientes y ponle nombre a lo que sientes. Si hay emociones que se te hacen pesadas, cuéntaselo a tu psicoorientador en tu próxima sesión."));
        banco.add(m(BIENESTAR_EMOCIONAL, BAJA, "Los días buenos y los difíciles pasan. Busca hoy un momento corto para algo que te guste, aunque sean diez minutos."));

        banco.add(m(DIFICULTADES_ACADEMICAS, ALTA, "Antes de empezar la semana, escribe lo que tienes pendiente y elige las tres cosas más importantes. Arma tu horario con dos bloques de estudio y llévalo a tu próxima sesión si necesitas ajustarlo."));
        banco.add(m(DIFICULTADES_ACADEMICAS, BAJA, "Estudiar con pausas funciona mejor que estudiar sin parar. Prueba 45 minutos de estudio y 10 de descanso, y felicítate por cada bloque que completes."));

        banco.add(m(RELACIONES_INTERPERSONALES, ALTA, "Si hay una conversación pendiente con alguien importante para ti, busca esta semana un momento tranquilo para tenerla. Si no sabes cómo empezar, puedes practicarlo con tu psicoorientador."));
        banco.add(m(RELACIONES_INTERPERSONALES, BAJA, "Escuchar con atención y decir con calma lo que sientes mejora cualquier relación. Esta semana prueba con una conversación."));

        banco.add(m(SITUACIONES_FAMILIARES, ALTA, "Las situaciones en casa pueden afectar tu estudio y tu ánimo, y es válido sentirlo. Si esta semana se te hace difícil, habla con tu psicoorientador para pensar juntos cómo cuidarte."));
        banco.add(m(SITUACIONES_FAMILIARES, BAJA, "Aunque no puedas cambiar todo lo que pasa en casa, sí puedes elegir un espacio y un rato al día solo para ti. Esta semana intenta apartarlo."));

        banco.add(m(ADAPTACION_PERMANENCIA, ALTA, "Adaptarse a la universidad y mantenerse en ella toma tiempo. Esta semana da un paso concreto: habla con un compañero, pregúntale una duda a un profesor o busca un grupo o servicio de la universidad que te apoye."));
        banco.add(m(ADAPTACION_PERMANENCIA, BAJA, "Es normal sentirse nuevo o fuera de lugar, o dudar si seguir. Muchos de tus compañeros también lo han sentido; hablarlo ayuda."));

        banco.add(m(PROYECTO_VIDA_VOCACIONAL, ALTA, "Esta semana habla con alguien que ya trabaje en lo que te interesa y pregúntale cómo es su día a día. Anota qué te llamó la atención y llévalo a tu próxima sesión."));
        banco.add(m(PROYECTO_VIDA_VOCACIONAL, BAJA, "No tienes que tener todo claro ahora. Fíjate en qué actividades te hacen perder la noción del tiempo: ahí hay pistas sobre lo que quieres."));

        banco.add(m(SALUD_MENTAL_SEGUIMIENTO, ALTA, "Sigue con las actividades que acordaste con tu psicoorientador y registra tu check-in cada día. Si sientes que lo que vives te sobrepasa, no esperes a la sesión: puedes llamar a la Línea ALMA al 018000 423874 (opción 1, salud mental) o escribir por WhatsApp al 305 433 6317."));
        banco.add(m(SALUD_MENTAL_SEGUIMIENTO, BAJA, "Cuidar tu salud mental es un proceso de pasos pequeños. Hoy revisa tu plan y elige la actividad que más fácil te quede para empezar."));

        banco.add(m(VIOLENCIAS_GENERO_SEXUALES, ALTA, "Lo que viviste no es tu culpa y mereces apoyo y respeto. Puedes hablar con tu psicoorientador cuando lo necesites. También puedes comunicarte con la Línea ALMA al 018000 423874 (opción 2, violencias basadas en género) o por WhatsApp al 305 433 6317."));
        banco.add(m(VIOLENCIAS_GENERO_SEXUALES, BAJA, "Tú decides a tu ritmo qué contar y cuándo. Cuidarte también es rodearte de personas y espacios donde te sientas seguro."));

        banco.add(m(OTROS_MOTIVOS, ALTA, "Esta semana elige una meta pequeña relacionada con lo que hablaste en tu sesión y registra cómo te fue. Si algo no te queda claro, pregúntaselo a tu psicoorientador."));
        banco.add(m(OTROS_MOTIVOS, BAJA, "Cada paso cuenta, por pequeño que sea. Celebra hoy algo que hiciste por ti."));

        repository.saveAll(banco);
        log.info("Banco de mensajes de acompañamiento cargado: {} mensajes", banco.size());
    }

    private MensajeAcompanamiento m(MotivoConsulta motivo, PrioridadMensaje prioridad, String texto) {
        return new MensajeAcompanamiento(motivo, prioridad, texto);
    }
}