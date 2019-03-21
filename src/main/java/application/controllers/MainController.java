package application.controllers;

import application.authentication.AuthData;
import application.entities.DrunkDriving;
import application.entities.User;
import application.repositories.DrunkDrivingRepository;
import application.repositories.UserRepository;
import application.storage.StorageService;
import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

@CrossOrigin
@RestController
@RequestMapping({"/api"})
public class MainController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final DrunkDrivingRepository drunkDrivingRepository;
    private final StorageService storageService;
    private final Gson gson = new Gson();

    @Autowired
    public MainController(UserRepository userRepository, DrunkDrivingRepository drunkDrivingRepository,
                          PasswordEncoder passwordEncoder, StorageService storageService) {
        this.userRepository = userRepository;
        this.drunkDrivingRepository = drunkDrivingRepository;
        this.passwordEncoder = passwordEncoder;
        this.storageService = storageService;
    }

    @PostMapping("/newUser")
    public @ResponseBody boolean newUser(@RequestBody String user) {
        final User newUser = gson.fromJson(user, User.class);
        newUser.setPassword(passwordEncoder.encode(newUser.getPassword()));
        if (userRepository.existsById(newUser.getCodicefiscale())) {
            return false;
        }
        userRepository.save(newUser);
        Mailer.getInstance().sendConfirmationEmail(newUser.getEmail());
        return true;
    }

    @GetMapping("/getUserInfo")
    @PreAuthorize("#user == authentication.principal.username")
    public @ResponseBody String getUserInfo(@RequestParam String user) {
        final Optional<User> searchedUser = userRepository.findById(user);
        if (searchedUser.isPresent()) {
            final User localUser = searchedUser.get();
            //this is necessary to avoid circular references when serializing
            localUser.getRequiredLegalAssistance().parallelStream().forEach(request -> request.setUser(null));
            return gson.toJson(localUser);
        } else {
            return gson.toJson(null);
        }
    }

    @PostMapping("/login")
    public boolean login(@RequestBody String login) {
        final AuthData authData = gson.fromJson(login, AuthData.class);
        final Optional<User> user = userRepository.findById(authData.getCodicefiscale());
        return user.isPresent() && passwordEncoder.matches(authData.getPassword(), user.get().getPassword());
    }

    @PostMapping("/drunkDriving/{user}")
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

    @PostMapping("/fileUpload/{user}/{requestNumber}")
    @PreAuthorize("#user == authentication.principal.username")
    public boolean handleFileUpload(@PathVariable String user, @PathVariable long requestNumber, @RequestParam("file") MultipartFile file) {
        try {
            storageService.store(file, user, requestNumber);
            return true;
        } catch (final IOException | IllegalArgumentException e) {
            return false;
        }
    }

    @GetMapping("/uploadedFilesList")
    @PreAuthorize("#user == authentication.principal.username")
    public String getUploadedFilesList(@RequestParam String user, @RequestParam long requestNumber) {
        try {
            final List<String> files = new LinkedList<>();
            storageService.loadAllFilenames(user, requestNumber).map(file -> file.getFileName().toString()).forEach(files::add);
            return gson.toJson(files);
        } catch (final IOException e) {
            return gson.toJson(null);
        }
    }

    @GetMapping("/test")
    @PreAuthorize("#codicefiscale == authentication.principal.username")
    public String test(@RequestParam String codicefiscale) {
        return gson.toJson("test successful for user " + codicefiscale);
    }
}
