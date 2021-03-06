package application.controllers;

import application.entities.DrunkDriving;
import application.entities.LegalAssistance;
import application.repositories.DrunkDrivingRepository;
import application.repositories.UserRepository;
import application.utils.Mailer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.Set;

@CrossOrigin
@RestController
@RequestMapping({"/api"})
public class LegalRequestsController {

    private final UserRepository userRepository;
    private final DrunkDrivingRepository drunkDrivingRepository;
    private final Mailer mailer;

    @Autowired
    public LegalRequestsController(UserRepository userRepository, DrunkDrivingRepository drunkDrivingRepository,
                                   Mailer mailer) {
        this.userRepository = userRepository;
        this.drunkDrivingRepository = drunkDrivingRepository;
        this.mailer = mailer;
    }

    @PostMapping("/drunkDriving/{user}")
    @PreAuthorize("#user == authentication.principal.username or hasRole('ROLE_ADMIN')")
    public ResponseEntity<DrunkDriving> newDrunkDriving(@PathVariable String user, @RequestBody DrunkDriving drunkDriving) {
        return userRepository.findById(user).map(foundUser -> {
            DrunkDriving localDrunkDriving = drunkDriving;
            localDrunkDriving.setRequestDate(new Date());
            localDrunkDriving.setUser(foundUser);
            localDrunkDriving = drunkDrivingRepository.save(localDrunkDriving);
            mailer.sendNewRequestEmail(foundUser.getEmail(), localDrunkDriving.getId());
            return new ResponseEntity<>(localDrunkDriving, HttpStatus.OK);
        }).orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @GetMapping("/legalAssistance/{user}")
    @PreAuthorize("#user == authentication.principal.username or hasRole('ROLE_ADMIN')")
    public ResponseEntity<Set<LegalAssistance>> getLegalAssistance(@PathVariable String user) {
        return userRepository.findById(user)
                .map(foundUser -> {
                    final HttpStatus status = foundUser.getRequiredLegalAssistance().size() > 0 ? HttpStatus.OK : HttpStatus.NOT_FOUND;
                    return new ResponseEntity<>(foundUser.getRequiredLegalAssistance(), status);
                })
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }
}
