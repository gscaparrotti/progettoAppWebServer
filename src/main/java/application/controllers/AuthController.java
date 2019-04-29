package application.controllers;

import application.entities.User;
import application.repositories.UserRepository;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@CrossOrigin
@RestController
@RequestMapping({"/api"})
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public AuthController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResult> login(@RequestBody User user) {
        final Optional<User> optUser = userRepository.findById(user.getCodicefiscale());
        if (optUser.isPresent() && passwordEncoder.matches(user.getPassword(), optUser.get().getPassword())) {
            if (optUser.get().isAdmin()) {
                return new ResponseEntity<>(new AuthResult(true), HttpStatus.OK);
            }
            return new ResponseEntity<>(new AuthResult(false), HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
    }

    private static class AuthResult {

        private boolean isAdmin;

        AuthResult(boolean isAdmin) {
            this.isAdmin = isAdmin;
        }

        @JsonProperty("isAdmin")
        public boolean isAdmin() {
            return isAdmin;
        }

        public void setAdmin(boolean admin) {
            isAdmin = admin;
        }
    }
}
