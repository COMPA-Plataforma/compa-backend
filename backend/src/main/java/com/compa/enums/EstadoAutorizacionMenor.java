package com.compa.enums;
 
// Estado de la autorización del representante legal de un estudiante menor de edad.
// Mientras no esté APROBADA, el estudiante solo ve la pantalla para descargar y subir el documento.
public enum EstadoAutorizacionMenor {
    PENDIENTE,    // el estudiante aún no ha subido el documento
    EN_REVISION,  // subió el documento y espera la revisión del psicoorientador
    APROBADA,     // el psicoorientador la aprobó: el estudiante accede a la plataforma
    RECHAZADA     // el psicoorientador la rechazó con un motivo: debe subir otro documento
}
 