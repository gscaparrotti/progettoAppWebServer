package application.controllers;

import application.model.User;
import application.model.UserRepository;
import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@CrossOrigin
@RestController
@RequestMapping({"/api"})
public class MainController {

    @Autowired
    UserRepository userRepository;

    @PostMapping(path = {"/newUser"})
    public @ResponseBody boolean newUser(@RequestBody Map<String, String> JSONUser) {
        final User newUser = new User();
        newUser.setCodicefiscale(JSONUser.get("codicefiscale"));
        newUser.setNome(JSONUser.get("nome"));
        newUser.setCognome(JSONUser.get("cognome"));
        newUser.setEmail(JSONUser.get("email"));
        newUser.setPassword(JSONUser.get("password"));
        userRepository.save(newUser);
        return userRepository.existsById(JSONUser.get("codicefiscale"));
    }

    @PostMapping("/login")
    public boolean login(@RequestBody Map<String, String> JSONLogin) {
        return true;
    }

    @GetMapping("/test")
    public String test() {
        return new Gson().toJson("test successful");
    }
}
