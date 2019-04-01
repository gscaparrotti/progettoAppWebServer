package application.controllers;

import application.entities.DrunkDriving;
import application.repositories.DrunkDrivingRepository;
import application.repositories.UserRepository;
import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.concurrent.atomic.AtomicBoolean;

@CrossOrigin
@RestController
@RequestMapping({"/api"})
public class LegalRequestsController {

    private final UserRepository userRepository;
    private final DrunkDrivingRepository drunkDrivingRepository;
    private final Gson gson = new Gson();

    @Autowired
    public LegalRequestsController(UserRepository userRepository, DrunkDrivingRepository drunkDrivingRepository) {
        this.userRepository = userRepository;
        this.drunkDrivingRepository = drunkDrivingRepository;
    }

    @PostMapping("/drunkDriving/{user}")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("#user == authentication.principal.username")
    public boolean drunkDriving(@PathVariable String user, @RequestBody String jsonData) {
        final AtomicBoolean success = new AtomicBoolean(false);
        userRepository.findById(user).ifPresent(foundUser -> {
            final DrunkDriving drunkDriving = gson.fromJson(jsonData, DrunkDriving.class);
            drunkDriving.setRequestDate(new Date());
            drunkDriving.setUser(foundUser);
            drunkDrivingRepository.save(drunkDriving);
            success.set(true);
        });
        return success.get();
    }
}
