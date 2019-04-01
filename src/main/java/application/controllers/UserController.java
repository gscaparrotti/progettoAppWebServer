package application.controllers;

import application.entities.User;
import application.repositories.UserRepository;
import application.utils.Mailer;
import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@CrossOrigin
@RestController
@RequestMapping({"/api"})
public class UserController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final Gson gson = new Gson();

    @Autowired
    public UserController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/users")
    @ResponseStatus(HttpStatus.CREATED)
    public boolean newUser(@RequestBody String user) {
        final User newUser = gson.fromJson(user, User.class);
        newUser.setPassword(passwordEncoder.encode(newUser.getPassword()));
        if (userRepository.existsById(newUser.getCodicefiscale())) {
            return false;
        }
        userRepository.save(newUser);
        Mailer.getInstance().sendConfirmationEmail(newUser.getEmail());
        return true;
    }

    @GetMapping("/users/{user}")
    @PreAuthorize("#user == authentication.principal.username")
    public String getUserInfo(@PathVariable String user) {
        final Optional<User> searchedUser = userRepository.findById(user);
        if (searchedUser.isPresent()) {
            final User localUser = searchedUser.get();
            //this is necessary to avoid circular references when serializing
            //also, this method is not intended to provide information about uploaded files
            localUser.getRequiredLegalAssistance().parallelStream().forEach(request -> {
                request.setUser(null);
                request.setFiles(null);
            });
            return gson.toJson(localUser);
        } else {
            return gson.toJson(null);
        }
    }
}
