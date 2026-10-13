# Mensaje de acompañamiento semanal

Cada semana, cada estudiante activo recibe por correo un mensaje de acompañamiento según sus
motivos de consulta (o uno general si no tiene). El envío está **apagado por defecto**.

## Cómo funciona

- Una tarea programada corre cada hora (hora de Colombia). Los lunes empieza a las 8:00 a.m.
- A cada estudiante activo, con la cuenta activada, que aún no recibió el mensaje de la semana, se le envía uno.
  Si la app estaba apagada el lunes, el mensaje sale cuando vuelve a estar activa.
- El mensaje se elige del banco (`mensajes_acompanamiento`). El estudiante no recibe un mensaje que ya
  recibió hasta haber recibido todos los de sus motivos de consulta.
- Cada envío queda en `envios_mensaje_semanal` (uno por estudiante y semana).

## Cómo habilitarlo

1. En Render, abrir el servicio del backend, **Environment**.
2. Agregar la variable `MENSAJE_SEMANAL_HABILITADO` con el valor `true`.
3. Guardar. Render reinicia el servicio.
4. A la siguiente hora en punto, todos los estudiantes activos que aún no recibieron el mensaje de la
   semana lo reciben. Para que el primer envío sea un lunes, activarlo el domingo en la noche o el lunes
   antes de las 8:00 a.m.
   En local, agregar `MENSAJE_SEMANAL_HABILITADO=true` al archivo `.env` (junto al `pom.xml`).

## Cómo apagarlo

Cambiar la variable a `false` (o borrarla) y guardar. No se envía nada más. Los registros de envíos
anteriores se conservan.

## Antes de encenderlo, revisar

- Que los correos de los estudiantes en la base sean reales y que los estudiantes deban recibir mensajes.
  En una base de pruebas con correos reales, esto les escribe de verdad.
- Que `brevo.api-key` esté configurada en el despliegue.

## Probar sin esperar al lunes

Con el envío habilitado, borrar las filas del estudiante en `envios_mensaje_semanal`; en la siguiente
hora en punto se le envía otro mensaje.

## Banco de mensajes

Los 23 mensajes iniciales se cargan solos la primera vez, solo si la tabla `mensajes_acompanamiento` está
vacía (`MensajeAcompanamientoSeeder`). Para agregar o desactivar mensajes después, editar esa tabla
(`activo = false` deja de enviar un mensaje sin borrarlo).
