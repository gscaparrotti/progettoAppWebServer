package application.controllers;

import application.model.AuthData;
import application.model.User;
import application.model.UserRepository;
import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@CrossOrigin
@RestController
@RequestMapping({"/api"})
public class MainController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public MainController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping(path = {"/newUser"})
    public @ResponseBody boolean newUser(@RequestBody String user) {
        final User newUser = new Gson().fromJson(user, User.class);
        newUser.setPassword(passwordEncoder.encode(newUser.getPassword()));
        if (userRepository.existsById(newUser.getCodicefiscale())) {
            return false;
        }
        userRepository.save(newUser);
        return userRepository.existsById(newUser.getCodicefiscale());
    }

    @PostMapping("/login")
    public boolean login(@RequestBody String login) {
        AuthData authData = new Gson().fromJson(login, AuthData.class);
        Optional<User> user = userRepository.findById(authData.getCodicefiscale());
        return user.isPresent() && passwordEncoder.matches(authData.getPassword(), user.get().getPassword());
    }

    @GetMapping("/test")
    @PreAuthorize("#codicefiscale == authentication.principal.username")
    public String test(@RequestParam String codicefiscale) {
        return new Gson().toJson("test successful for user " + codicefiscale);
    }
}
