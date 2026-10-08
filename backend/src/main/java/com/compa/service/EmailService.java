package com.compa.service;

import sendinblue.ApiException;
import sendinblue.Configuration;
import sibApi.TransactionalEmailsApi;
import sibModel.SendSmtpEmail;
import sibModel.SendSmtpEmailTo;
import sibModel.SendSmtpEmailSender;
import sendinblue.ApiClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Slf4j
@Service
public class EmailService {

    @Value("${app.frontend-url}")
    private String frontendUrl;

    @Value("${brevo.api-key}")
    private String brevoApiKey;

    private static final String FROM_EMAIL = "lifetrackwellness@gmail.com";
    private static final String FROM_NAME = "COMPA - Acompañamiento estudiantil";

    @Async
    public void sendVerificationEmail(String toEmail, String name, String verificationToken) {
        String verificationLink = frontendUrl + "/verify-email?token=" + verificationToken;
        String body =
                "Hola " + name + ",\n\n" +
                        "Gracias por registrarte en COMPA.\n\n" +
                        "Confirma tu cuenta haciendo clic aquí:\n" +
                        verificationLink + "\n\n" +
                        "Este enlace vence en 24 horas.\n\n" +
                        "Si no creaste esta cuenta, ignora este mensaje.\n\n" +
                        "Equipo COMPA";
        send(toEmail, name, "Confirma tu cuenta en COMPA", body);
    }

    @Async
    public void sendEstudianteCredentials(String toEmail, String name,
                                          String tempPassword, String activationToken) {
        String activationLink = frontendUrl + "/activate?token=" + activationToken;
        String body =
                "Hola " + name + ",\n\n" +
                        "Tu orientador te ha registrado en COMPA.\n\n" +
                        "Tus credenciales temporales son:\n" +
                        "- Email: " + toEmail + "\n" +
                        "- Contraseña temporal: " + tempPassword + "\n\n" +
                        "Por favor activa tu cuenta haciendo clic aquí:\n" +
                        activationLink + "\n\n" +
                        "Este enlace vence en 48 horas.\n\n" +
                        "Una vez activada tu cuenta podrás cambiar tu contraseña.\n\n" +
                        "Si no esperabas este mensaje, ignóralo.\n\n" +
                        "Equipo COMPA";
        send(toEmail, name, "Bienvenido a COMPA - Tus credenciales de acceso", body);
    }

    @Async
    public void sendRiskAlertEmail(String toEmail, String orientadorName,
                                   String estudianteName, String description) {
        String body =
                "Hola " + orientadorName + ",\n\n" +
                        "Tu estudiante " + estudianteName + " ha sido identificado en nivel de riesgo ALTO.\n\n" +
                        "Descripción: " + description + "\n\n" +
                        "Te recomendamos revisar su estado lo antes posible en la plataforma.\n\n" +
                        "Equipo COMPA";
        send(toEmail, orientadorName, "⚠️ Alerta de riesgo alto — " + estudianteName, body);
    }

    // HU-01: recordatorio de tareas pendientes (lista de tareas + seguimiento)
    @Async
    public void sendTaskReminderEmail(String toEmail, String name, List<String> pendingTaskNames,
                                      boolean isFollowUp) {
        StringBuilder taskList = new StringBuilder();
        for (String taskName : pendingTaskNames) {
            taskList.append("- ").append(taskName).append("\n");
        }

        String subject = isFollowUp
                ? "Recordatorio: aún tienes tareas pendientes en COMPA"
                : "Tienes tareas pendientes hoy en COMPA";

        String intro = isFollowUp
                ? "Vimos que todavía no has marcado algunas tareas de hoy:\n\n"
                : "Este es tu recordatorio de tareas pendientes por hoy:\n\n";

        String body = "Hola " + name + ",\n\n" +
                intro +
                taskList +
                "\nPuedes registrarlas desde tu cuenta en COMPA.\n\n" +
                "Equipo COMPA";

        send(toEmail, name, subject, body);
    }

    // HU-03: recordatorio con enlace para marcar la tarea como cumplida desde el correo
    @Async
    public void sendTaskReminderEmail(String toEmail, String name, String title,
                                      String message, String actionToken) {
        String actionUrl = frontendUrl + "/notifications/complete-task?token=" + actionToken;
        String body =
                "Hola " + name + ",\n\n" +
                        message + "\n\n" +
                        "Puedes marcarla como cumplida directamente desde aquí, sin abrir la app:\n" +
                        actionUrl + "\n\n" +
                        "Este enlace vence en 48 horas.\n\n" +
                        "Equipo COMPA";
        send(toEmail, name, title, body);
    }

    @Async
    public void sendNotificationEmail(String toEmail, String name, String title, String message) {
        String body =
                "Hola " + name + ",\n\n" +
                        message + "\n\n" +
                        "Equipo COMPA";
        send(toEmail, name, title, body);
    }

    private void send(String toEmail, String toName, String subject, String body) {
        try {
            ApiClient defaultClient = Configuration.getDefaultApiClient();
            defaultClient.setApiKey(brevoApiKey);

            TransactionalEmailsApi apiInstance = new TransactionalEmailsApi();

            SendSmtpEmailSender sender = new SendSmtpEmailSender();
            sender.setEmail(FROM_EMAIL);
            sender.setName(FROM_NAME);

            SendSmtpEmailTo recipient = new SendSmtpEmailTo();
            recipient.setEmail(toEmail);
            recipient.setName(toName);

            SendSmtpEmail email = new SendSmtpEmail();
            email.setSender(sender);
            email.setTo(Collections.singletonList(recipient));
            email.setSubject(subject);
            email.setTextContent(body);

            apiInstance.sendTransacEmail(email);
            log.info(">>> Email enviado a: {}", toEmail);

        } catch (ApiException e) {
            log.error(">>> Error enviando email a {}: {}", toEmail, e.getMessage());
        }
    }
}