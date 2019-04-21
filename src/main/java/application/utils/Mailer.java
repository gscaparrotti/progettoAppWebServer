package application.utils;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Future;
import java.util.function.Function;
import java.util.function.Supplier;

@Service
public class Mailer {
    private final Logger logger = LoggerFactory.getLogger(Mailer.class);
    private String adminAddress = null;
    private String from = null;
    private JavaMailSenderImpl mailSender = null;


    public Mailer() {
        final String path = Paths.get("", "src/main/resources/mailer.parameters").toAbsolutePath().toString();
        try (final JsonReader jsonReader = new JsonReader(new FileReader(path))) {
            final Map<String, Object> parameters = new Gson().fromJson(jsonReader, Map.class);
            if ((Boolean) parameters.get("enabled")) {
                final JavaMailSenderImpl localMailSender = new JavaMailSenderImpl();
                localMailSender.setHost((String) parameters.get("host"));
                localMailSender.setPort(((Double) parameters.get("port")).intValue());
                localMailSender.setUsername((String) parameters.get("username"));
                localMailSender.setPassword((String) parameters.get("password"));
                final Properties props = localMailSender.getJavaMailProperties();
                props.put("mail.transport.protocol", parameters.get("protocol"));
                props.put("mail.smtps.auth", parameters.get("auth"));
                props.put("mail.smtps.from", validateEmail((String) parameters.get("from")));
                props.put("mail.smtp.starttls.enable", parameters.get("starttls"));
                from = validateEmail((String) parameters.get("from"));
                adminAddress = validateEmail((String) parameters.get("adminAddress"));
                mailSender = localMailSender;
            } else {
                logger.info("Mailer disabled (as specified in config file)");
            }
        } catch (final IOException | ClassCastException | AddressException | NullPointerException e) {
            logger.warn("Cannot load mailer configuration file: " + e.getMessage() + ". Email system won't work.");
        }
    }

    @Async
    public Future<Boolean> sendNewUserEmail(final String address) {
        return ifPossible(address, innerAddress -> {
            final String subject = "Conferma registrazione";
            final String text = "Registrazione di " + address + " completata.";
            final boolean toUserResult = sendEmail(innerAddress, subject, text);
            final boolean toAdminResult = sendEmail(adminAddress, subject, text);
            return new AsyncResult<>(toUserResult && toAdminResult);
        }, () -> new AsyncResult<>(false));
    }

    @Async
    public Future<Boolean> sendNewRequestEmail(final String address, final long request) {
        return ifPossible(address, innerAddress -> {
            final String subject = "Nuova richiesta di assistenza";
            final String text = "L'utente " + address + " ha effettuato la richiesta di assistenza con ID " + request + ".";
            final boolean toUserResult = sendEmail(innerAddress, subject, text);
            final boolean toAdminResult = sendEmail(adminAddress, subject, text);
            return new AsyncResult<>(toUserResult && toAdminResult);
        }, () -> new AsyncResult<>(false));
    }

    @Async
    public Future<Boolean> sendPaymentEmail(final String address, final long request) {
        return ifPossible(address, innerAddress -> {
            final String subject = "Conferma pagamento";
            final String text = "Pagamento di " + address + " per la prestazione n. " + request + " completato.";
            final boolean toUserResult = sendEmail(innerAddress, subject, text);
            final boolean toAdminResult = sendEmail(adminAddress, subject, text);
            return new AsyncResult<>(toUserResult && toAdminResult);
        }, () -> new AsyncResult<>(false));
    }

    @Async
    public Future<Boolean> sendNotificationEmail(final String address, final long request) {
        return ifPossible(address, innerAddress -> {
            logger.debug("Sending mails in thread: " + Thread.currentThread());
            final String subject = "Nuova notifica da Smart Legal Services";
            final String text = "È presente una nuova notifica da " + address + " per la prestazione n. " + request;
            final boolean toUserResult = sendEmail(innerAddress, subject, text);
            final boolean toAdminResult = sendEmail(adminAddress, subject, text);
            return new AsyncResult<>(toUserResult && toAdminResult);
        }, () -> new AsyncResult<>(false));
    }

    private boolean sendEmail(final String recipient, final String subject, final String text) {
        final SimpleMailMessage msg = new SimpleMailMessage();
        msg.setFrom(from);
        msg.setTo(recipient);
        msg.setSubject(subject);
        msg.setText(text);
        try {
            mailSender.send(msg);
            return true;
        } catch (final MailException e) {
            logger.error("Cannot send email: ", e);
            return false;
        }
    }

    private <A, B> B ifPossible(final A parameter, final Function<A, B> function, final Supplier<B> orElse) {
        if (mailSender != null) {
            return function.apply(parameter);
        } else {
            return orElse.get();
        }
    }

    private static String validateEmail(final String email) throws AddressException {
            InternetAddress emailAddr = new InternetAddress(email);
            emailAddr.validate();
            return email;
    }
}
