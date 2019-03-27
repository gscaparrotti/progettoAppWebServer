package application.controllers;

import application.entities.DBFile;
import application.entities.LegalAssistance;
import application.repositories.DBFilesRepository;
import application.repositories.UserRepository;
import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

@CrossOrigin
@RestController
@RequestMapping({"/api"})
public class FileStorageController {

    private final UserRepository userRepository;
    private final DBFilesRepository dbFilesRepository;
    private final Gson gson = new Gson();

    @Autowired
    public FileStorageController(UserRepository userRepository, DBFilesRepository dbFilesRepository) {
        this.userRepository = userRepository;
        this.dbFilesRepository = dbFilesRepository;
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
}
