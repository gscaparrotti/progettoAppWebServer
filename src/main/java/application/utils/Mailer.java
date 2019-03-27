package application.utils;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

public class Mailer {
    private static Mailer ourInstance = new Mailer();
    private final JavaMailSenderImpl mailSender;

    public static Mailer getInstance() {
        return ourInstance;
    }

    private Mailer() {
        this.mailSender = new JavaMailSenderImpl();
        mailSender.setHost("smtps.aruba.it");
        mailSender.setPort(465);
        mailSender.setUsername("no-reply@studiolegalebrioli.it");
        mailSender.setPassword("a755c358d1");
        final Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtps");
        props.put("mail.smtps.auth", "true");
        props.put("mail.smtps.from", "no-reply@studiolegalebrioli.it");
        props.put("mail.smtp.starttls.enable", "true");
    }

    public void sendConfirmationEmail(final String address) {
        final SimpleMailMessage msg = new SimpleMailMessage();
        msg.setFrom("no-reply@studiolegalebrioli.it");
        msg.setTo(address);
        msg.setSubject("Conferma registrazione");
        msg.setText("Registrazione di " + address + " completata.");
        mailSender.send(msg);
    }
}
