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

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Future;
import java.util.function.Function;

@Service
public class Mailer {
    private final Logger logger = LoggerFactory.getLogger(Mailer.class);
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
                props.put("mail.smtps.from", parameters.get("from"));
                props.put("mail.smtp.starttls.enable", parameters.get("starttls"));
                mailSender = localMailSender;
            } else {
                logger.info("Mailer disabled (as specified in config file)");
            }
        } catch (final IOException | ClassCastException e) {
            logger.warn("Cannot load mailer configuration file: " + e.getMessage() + ". Email system won't work.");
        }
    }

    @Async
    public Future<Boolean> sendConfirmationEmail(final String address) {
        return ifPossible(address, innerAddress -> {
            final SimpleMailMessage msg = new SimpleMailMessage();
            msg.setFrom("no-reply@studiolegalebrioli.it");
            msg.setTo(innerAddress);
            msg.setSubject("Conferma registrazione");
            msg.setText("Registrazione di " + address + " completata.");
            try {
                mailSender.send(msg);
                return new AsyncResult<>(true);
            } catch (final MailException e) {
                logger.error("Cannot send email: ", e);
                return new AsyncResult<>(false);
            }
        }, new AsyncResult<>(false));
    }

    @Async
    public Future<Boolean> sendPaymentEmail(final String address, final long request) {
        return ifPossible(address, innerAddress -> {
            final SimpleMailMessage msg = new SimpleMailMessage();
            msg.setFrom("no-reply@studiolegalebrioli.it");
            msg.setTo(innerAddress);
            msg.setSubject("Conferma pagamento");
            msg.setText("Pagamento di " + address + " per la prestazione n. " + request + " completato.");
            try {
                mailSender.send(msg);
                return new AsyncResult<>(true);
            } catch (final Exception e) {
                logger.error("Cannot send email: ", e);
                return new AsyncResult<>(false);
            }
        }, new AsyncResult<>(false));
    }

    private <A, B> B ifPossible(final A parameter, final Function<A, B> function, final B orElse) {
        if (mailSender != null) {
            return function.apply(parameter);
        } else {
            return orElse;
        }
    }
}
