package application.testing;

import application.utils.Mailer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@CrossOrigin
@RestController
@RequestMapping({"/api/test"})
public class MailerTestController {

    private final Logger logger = LoggerFactory.getLogger(MailerTestController.class);
    private final Mailer mailer;

    @Autowired
    public MailerTestController(Mailer mailer) {
        this.mailer = mailer;
    }

    @PostMapping("/testMail")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity getUserInfo(@RequestParam String to, @RequestParam long request) {
        logger.debug("Received request in thread: " + Thread.currentThread());
        mailer.sendNotificationEmail(to, request);
        logger.debug("Terminating request in thread: " + Thread.currentThread());
        return new ResponseEntity(HttpStatus.OK);
    }
}
