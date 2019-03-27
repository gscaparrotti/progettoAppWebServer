package application.controllers;

import application.authentication.AuthData;
import application.entities.User;
import application.repositories.UserRepository;
import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@CrossOrigin
@RestController
@RequestMapping({"/api"})
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final Gson gson = new Gson();

    @Autowired
    public AuthController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/login")
    public boolean login(@RequestBody String login) {
        final AuthData authData = gson.fromJson(login, AuthData.class);
        final Optional<User> user = userRepository.findById(authData.getCodicefiscale());
        return user.isPresent() && passwordEncoder.matches(authData.getPassword(), user.get().getPassword());
    }
}
