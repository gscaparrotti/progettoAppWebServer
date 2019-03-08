package application.controllers;

import application.authentication.AuthData;
import application.entities.*;
import application.repositories.DrunkDrivingRepository;
import application.repositories.UserRepository;
import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

@CrossOrigin
@RestController
@RequestMapping({"/api"})
public class MainController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final DrunkDrivingRepository drunkDrivingRepository;

    @Autowired
    public MainController(UserRepository userRepository, DrunkDrivingRepository drunkDrivingRepository,
                          PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.drunkDrivingRepository = drunkDrivingRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/newUser")
    public @ResponseBody boolean newUser(@RequestBody String user) {
        final User newUser = new Gson().fromJson(user, User.class);
        newUser.setPassword(passwordEncoder.encode(newUser.getPassword()));
        if (userRepository.existsById(newUser.getCodicefiscale())) {
            return false;
        }
        userRepository.save(newUser);
        return userRepository.existsById(newUser.getCodicefiscale());
    }

    @GetMapping("/getUserInfo")
    public @ResponseBody String getUserInfo(@RequestParam String codicefiscale) {
        return new Gson().toJson(userRepository.findById(codicefiscale).orElse(null));
    }

    @PostMapping("/login")
    public boolean login(@RequestBody String login) {
        final AuthData authData = new Gson().fromJson(login, AuthData.class);
        final Optional<User> user = userRepository.findById(authData.getCodicefiscale());
        return user.isPresent() && passwordEncoder.matches(authData.getPassword(), user.get().getPassword());
    }

    @PostMapping("/drunkDriving/{user}")
    @PreAuthorize("#user == authentication.principal.username")
    public boolean drunkDriving(@PathVariable String user, @RequestBody String jsonData) {
        final AtomicBoolean success = new AtomicBoolean(false);
        userRepository.findById(user).ifPresent(foundUser -> {
            final DrunkDriving drunkDriving = new Gson().fromJson(jsonData, DrunkDriving.class);
            drunkDriving.setRequestDate(new Date());
            drunkDriving.setUser(foundUser);
            drunkDrivingRepository.save(drunkDriving);
            success.set(true);
        });
        return success.get();
    }

    @GetMapping("/test")
    @PreAuthorize("#codicefiscale == authentication.principal.username")
    public String test(@RequestParam String codicefiscale) {
        return new Gson().toJson("test successful for user " + codicefiscale);
    }
}
