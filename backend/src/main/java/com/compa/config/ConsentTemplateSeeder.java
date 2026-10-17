package com.compa.config;

import com.compa.enums.ConsentType;
import com.compa.model.ConsentTemplate;
import com.compa.repository.ConsentTemplateRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * Deja cargadas las plantillas vigentes de los consentimientos informados.
 * Para publicar una nueva versión: cambiar VERSION y el texto, y desplegar.
 * Los estudiantes deberán aceptarla de nuevo.
 * Si se cambia el texto después de que alguien ya aceptó, hay que subir VERSION
 * (si no, no se les vuelve a pedir).
 * VERSION de datos debe coincidir con POLITICA_VIGENCIA.version del frontend (lib/politicaDatos.ts).
 */
@Component
public class ConsentTemplateSeeder implements ApplicationRunner {

    static final String VERSION = "1.0";

    // ---------- Consentimiento de acompañamiento de psicoorientación ----------
    static final String TITULO_ACOMPANAMIENTO = "Consentimiento informado para el acompañamiento de psicoorientación";

    static final String TEXTO_ACOMPANAMIENTO = """
            Yo, estudiante usuario de COMPA, declaro que he sido informado de manera clara y comprensible sobre el proceso de acompañamiento de psicoorientación en el que voy a participar y sobre el uso de la plataforma COMPA como apoyo entre sesiones.

            QUÉ ES COMPA Y PARA QUÉ SIRVE
            COMPA es una aplicación web que apoya el acompañamiento y seguimiento entre sesiones de psicoorientación. No reemplaza las sesiones con mi psicoorientador: las complementa.

            QUÉ IMPLICA MI PARTICIPACIÓN
            Entiendo que el proceso incluye:
            - Sesiones con mi psicoorientador, en las que él registra la atención (motivos de consulta e información de la sesión) y los compromisos acordados. Este registro de la atención solo lo consulta el psicoorientador.
            - Un plan de acompañamiento con las actividades que acordamos en cada sesión, que puedo consultar en la plataforma.
            - El reporte del cumplimiento de mis actividades, indicando el motivo cuando no pude realizarlas y, si quiero, un comentario, además del registro de mi estado de ánimo.
            - Recordatorios de mis actividades pendientes y mensajes de acompañamiento según mis motivos de consulta.
            - El seguimiento de mi adherencia al plan por parte de mi psicoorientador, para conversar conmigo en la sesión sobre mi avance y las dificultades que encuentro.

            ALCANCE
            La psicoorientación es un servicio de orientación y acompañamiento. Comprendo que no incluye diagnóstico ni tratamiento clínico, y que COMPA no reemplaza la atención médica, psicológica clínica, psiquiátrica ni de urgencias. Si estoy en una situación de riesgo o emergencia, debo buscar ayuda inmediata en los servicios de salud o en las líneas de atención de mi ciudad.

            ALERTAS
            Entiendo que, si el sistema detecta una baja adherencia a mi plan, mi psicoorientador podrá recibir una alerta para acercarse a mí y apoyarme.

            DERIVACIÓN
            Entiendo que, si mi psicoorientador considera que necesito otro tipo de atención, podrá orientarme o derivarme hacia una ruta institucional o externa (por ejemplo, mi EPS o el Hospital Alma Mater), y que la plataforma registrará el avance de ese proceso.

            CONFIDENCIALIDAD Y SUS LÍMITES
            Mi psicoorientador mantendrá la confidencialidad de lo que comparta con él. Comprendo que esta confidencialidad puede tener límites cuando exista un riesgo para mi vida o integridad, o para las de otras personas, o cuando una autoridad competente lo exija conforme a la ley. Para apoyar la planificación de talleres y actividades de psicoorientación, el área de Bienestar Institucional solo conocerá información agregada y anonimizada, a partir de la cual no es posible identificarme.

            PARTICIPACIÓN VOLUNTARIA
            Mi participación es voluntaria. He sido informado de que puedo retirar este consentimiento en cualquier momento, solicitándolo al equipo de COMPA o a mi psicoorientador, sin que esto afecte la atención que recibo de mi psicoorientador.

            DATOS PERSONALES
            El tratamiento de mis datos personales se rige por el consentimiento de tratamiento de datos y la política de tratamiento de datos de COMPA, que también debo leer y aceptar.

            Al aceptar, declaro que leí este consentimiento, que lo entendí y que participo de forma libre y voluntaria. Quedará registrada la fecha, la hora y la versión que acepto.
            """.strip();

    // ---------- Consentimiento de tratamiento de datos personales ----------
    static final String TITULO_DATOS = "Consentimiento informado para el tratamiento de datos personales";

    static final String TEXTO_DATOS = """
            Antes de usar COMPA, lee con atención este consentimiento. Aquí te explicamos cómo trataremos tus datos personales, de acuerdo con la Ley Estatutaria 1581 de 2012 y sus decretos reglamentarios.

            RESPONSABLE DEL TRATAMIENTO
            Equipo de desarrollo de COMPA, plataforma de acompañamiento psicoorientador desarrollada en el marco del curso Proyecto Integrador I de la Universidad de Antioquia (Medellín, Antioquia, Colombia). Puedes escribirnos a mariana.carvajalr@udea.edu.co o a andrea.marind.1@gmail.com.

            DATOS QUE TRATAREMOS
            - Datos de identificación y contacto: nombre, documento de identidad, correo electrónico y teléfono.
            - Datos de acompañamiento: registros de atención, seguimientos de sesión, plan de hábitos y su cumplimiento.
            - Datos de bienestar y salud: check-ins diarios, nivel de riesgo, motivos de consulta y observaciones del psicoorientador, incluido el aseguramiento en salud.
            - Datos de uso de la plataforma: alertas generadas, derivaciones a rutas de atención y notificaciones enviadas.

            CARÁCTER SENSIBLE DE LOS DATOS
            Parte de estos datos se refiere a tu salud y a tu estado emocional, por lo que son datos sensibles. Por eso no estás obligado a autorizar su tratamiento y puedes negarte a responder cualquier pregunta sobre ellos. Si no aceptas este consentimiento, no podrás usar la plataforma.

            PARA QUÉ USAREMOS TUS DATOS
            - Brindarte acompañamiento psicoorientador y llevar el historial de tu proceso.
            - Registrar y consultar tus check-ins, tu plan de hábitos y tu avance entre sesiones.
            - Generar alertas de riesgo para que el psicoorientador asignado pueda actuar oportunamente.
            - Derivarte hacia una ruta de atención institucional o externa (por ejemplo, la EPS o el Hospital Alma Mater) cuando el psicoorientador lo considere necesario.
            - Enviarte notificaciones, recordatorios y mensajes de acompañamiento por correo electrónico.
            - Generar informes agregados y anonimizados para que Bienestar Institucional planifique talleres y actividades de psicoorientación.
            - Garantizar la seguridad de tu cuenta y el correcto funcionamiento de la plataforma.

            QUIÉN VE TU INFORMACIÓN
            Tu información de acompañamiento solo la consultan tú y el psicoorientador que te tiene asignado. Para funcionar, COMPA usa proveedores tecnológicos de alojamiento, base de datos y envío de correos, que tratan los datos por cuenta nuestra y solo para prestar ese servicio.

            TUS DERECHOS COMO TITULAR
            - Conocer, actualizar y rectificar tus datos personales.
            - Solicitar prueba de la autorización que diste para el tratamiento de tus datos.
            - Ser informado sobre el uso que se ha dado a tus datos, si lo pides.
            - Presentar quejas ante la Superintendencia de Industria y Comercio por infracciones a la ley.
            - Revocar la autorización y solicitar la supresión de tus datos cuando no se respeten los principios, derechos y garantías legales.
            - Acceder de forma gratuita a tus datos personales.

            VIGENCIA
            Tus datos se conservarán mientras dure tu proceso de acompañamiento y por el tiempo adicional que exijan las obligaciones legales. Después se suprimen.

            Al aceptar, declaras que leíste este consentimiento y autorizas el tratamiento de tus datos en los términos descritos. Quedará registrada la fecha, la hora y la versión que aceptas. Puedes consultar la política completa en la página «Política de tratamiento de datos».
            """.strip();

    private final ConsentTemplateRepository consentTemplateRepository;

    public ConsentTemplateSeeder(ConsentTemplateRepository consentTemplateRepository) {
        this.consentTemplateRepository = consentTemplateRepository;
    }

    @Override
    public void run(ApplicationArguments args) {
        upsert(ConsentType.MEDICO, TITULO_ACOMPANAMIENTO, TEXTO_ACOMPANAMIENTO);
        upsert(ConsentType.DATOS_PERSONALES, TITULO_DATOS, TEXTO_DATOS);
    }

    // Crea la plantilla del tipo indicado, o la actualiza si el texto o la versión cambiaron
    private void upsert(ConsentType tipo, String titulo, String texto) {
        ConsentTemplate template = consentTemplateRepository.findAll().stream()
                .filter(t -> t.getTipo() == tipo)
                .findFirst()
                .orElseGet(ConsentTemplate::new);

        boolean cambio = template.getId() == null
                || !VERSION.equals(template.getVersion())
                || !titulo.equals(template.getTitulo())
                || !texto.equals(template.getContenido());

        if (cambio) {
            template.setTipo(tipo);
            template.setTitulo(titulo);
            template.setContenido(texto);
            template.setVersion(VERSION);
            consentTemplateRepository.save(template);
        }
    }
}