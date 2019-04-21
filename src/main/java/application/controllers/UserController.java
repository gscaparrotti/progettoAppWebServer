package application.controllers;

import application.entities.User;
import application.repositories.UserRepository;
import application.utils.Mailer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@CrossOrigin
@RestController
@RequestMapping({"/api"})
public class UserController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final Mailer mailer;

    @Autowired
    public UserController(UserRepository userRepository, PasswordEncoder passwordEncoder, Mailer mailer) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.mailer = mailer;
    }

    @PostMapping("/users")
    public ResponseEntity<User> newUser(@RequestBody User newUser) {
        newUser.setPassword(passwordEncoder.encode(newUser.getPassword()));
        newUser.setAdmin(false);
        if (userRepository.existsById(newUser.getCodicefiscale())) {
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }
        newUser = userRepository.save(newUser);
        mailer.sendConfirmationEmail(newUser.getEmail());
        return new ResponseEntity<>(newUser, HttpStatus.OK);
    }

    @GetMapping("/users/{user}")
    @PreAuthorize("#user == authentication.principal.username or hasRole('ROLE_ADMIN')")
    public ResponseEntity<User> getUserInfo(@PathVariable String user) {
        return userRepository.findById(user)
                .map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }
}
