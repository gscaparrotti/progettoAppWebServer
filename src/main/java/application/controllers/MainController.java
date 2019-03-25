package application.controllers;

import application.authentication.AuthData;
import application.entities.DBFile;
import application.entities.DrunkDriving;
import application.entities.LegalAssistance;
import application.entities.User;
import application.repositories.DBFilesRepository;
import application.repositories.DrunkDrivingRepository;
import application.repositories.UserRepository;
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
import java.util.function.Consumer;

@CrossOrigin
@RestController
@RequestMapping({"/api"})
public class MainController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final DrunkDrivingRepository drunkDrivingRepository;
    private final DBFilesRepository dbFilesRepository;
    private final Gson gson = new Gson();

    @Autowired
    public MainController(UserRepository userRepository, DrunkDrivingRepository drunkDrivingRepository,
                          PasswordEncoder passwordEncoder, DBFilesRepository dbFilesRepository) {
        this.userRepository = userRepository;
        this.drunkDrivingRepository = drunkDrivingRepository;
        this.passwordEncoder = passwordEncoder;
        this.dbFilesRepository = dbFilesRepository;
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
        final AtomicBoolean success = new AtomicBoolean(false);
        forEachRequestFromUser(user, requestNumber, request -> {
            try {
                final DBFile dbFile = new DBFile();
                dbFile.setRequest(request);
                dbFile.setFileName(file.getOriginalFilename());
                dbFile.setData(file.getBytes());
                dbFilesRepository.save(dbFile);
                success.set(true);
            } catch (final IOException e) {
                success.set(false);
            }
        });
        return success.get();
    }

    @GetMapping("/uploadedFilesList")
    @PreAuthorize("#user == authentication.principal.username")
    public String getUploadedFilesList(@RequestParam String user, @RequestParam long requestNumber) {
        final List<String> fileNames = new LinkedList<>();
        forEachRequestFromUser(user, requestNumber, request -> request.getFiles().forEach(file -> fileNames.add(file.getFileName())));
        return gson.toJson(fileNames);
    }

    private void forEachRequestFromUser(final String user, final long requestNumber, final Consumer<LegalAssistance> action) {
        userRepository.findById(user).ifPresent(foundUser -> foundUser.getRequiredLegalAssistance().parallelStream()
                .filter(request -> request.getId() == requestNumber)
                .forEach(action));
    }

    @GetMapping("/test")
    @PreAuthorize("#codicefiscale == authentication.principal.username")
    public String test(@RequestParam String codicefiscale) {
        return gson.toJson("test successful for user " + codicefiscale);
    }
}
