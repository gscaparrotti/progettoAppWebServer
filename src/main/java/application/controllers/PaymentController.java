package application.controllers;

import application.entities.LegalAssistance;
import application.repositories.LegalAssistanceRepository;
import application.repositories.UserRepository;
import application.utils.Mailer;
import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Charge;
import com.stripe.net.RequestOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@CrossOrigin
@RestController
@RequestMapping({"/api"})
public class PaymentController {

    private final UserRepository userRepository;
    private final LegalAssistanceRepository legalAssistanceRepository;
    private final Mailer mailer;

    @Autowired
    public PaymentController(UserRepository userRepository, LegalAssistanceRepository legalAssistanceRepository,
                             Mailer mailer) {
        this.userRepository = userRepository;
        this.legalAssistanceRepository = legalAssistanceRepository;
        this.mailer = mailer;
        final String path = Paths.get("", "src/main/resources/stripe.parameters").toAbsolutePath().toString();
        try (final JsonReader jsonReader = new JsonReader(new FileReader(path))) {
            final Map<String, String> parameters = new Gson().fromJson(jsonReader, Map.class);
            Stripe.apiKey = parameters.get("private_key");
        } catch (final IOException | ClassCastException e) {
            final Logger logger = LoggerFactory.getLogger(PaymentController.class);
            logger.warn("Cannot load Stripe configuration file: " + e.getMessage() + ". CC payments won't work.");
        }
    }

    @PostMapping("/payWithCC")
    @PreAuthorize("#user == authentication.principal.username")
    public ResponseEntity payWithCC(@RequestParam String user, @RequestParam long request, @RequestParam String token) {
        if (Stripe.apiKey == null) {
            return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return userRepository.findById(user).flatMap(foundUser ->
            foundUser.getRequiredLegalAssistance().stream()
                    .filter(legalAssistance -> legalAssistance.getId() == request)
                    .filter(legalAssistance -> legalAssistance.getPaymentDate() == null)
                    .findAny()
                    .map(foundRequest -> {
                        final Map<String, Object> params = new HashMap<>();
                        params.put("amount", 100);
                        params.put("currency", "eur");
                        params.put("source", token == null ? "tok_visa" : token);
                        params.put("receipt_email", foundUser.getEmail());
                        params.put("description", "Addebito per richiesta assistenza n. " + request);
                        final RequestOptions requestOptions = RequestOptions
                                .builder()
                                //.setIdempotencyKey(foundUser.getCodicefiscale() + foundRequest.getId()) Must be sent with the same token
                                .build();
                        try {
                            final Charge charge = Charge.create(params, requestOptions);
                            if (charge.getPaid()) {
                                foundRequest.setPaymentDate(new Date());
                                foundRequest.setPaymentType(LegalAssistance.PaymentType.CREDIT_CARD);
                                legalAssistanceRepository.save(foundRequest);
                                mailer.sendPaymentEmail(foundUser.getEmail(), foundRequest.getId());
                                return new ResponseEntity(HttpStatus.NO_CONTENT);
                            } else {
                                return new ResponseEntity(HttpStatus.UNPROCESSABLE_ENTITY);
                            }
                        } catch (StripeException e) {
                            return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
                        }
                    })
        ).orElse(new ResponseEntity(HttpStatus.NOT_FOUND));
    }
}
