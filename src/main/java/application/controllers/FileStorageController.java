package application.controllers;

import application.entities.DBFile;
import application.entities.LegalAssistance;
import application.repositories.DBFilesRepository;
import application.repositories.UserRepository;
import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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

    @PostMapping("/files/{user}/{requestNumber}")
    @PreAuthorize("#user == authentication.principal.username")
    public boolean handleFileUpload(@PathVariable String user, @PathVariable long requestNumber, @RequestParam("file") MultipartFile file) {
        final AtomicBoolean success = new AtomicBoolean(false);
        forEachRequestFromUser(user, requestNumber, request -> {
            try {
                final DBFile.DBFileID  dbFileID = new DBFile.DBFileID();
                dbFileID.setFilename(file.getOriginalFilename());
                dbFileID.setRequest(request.getId());
                if (!dbFilesRepository.existsById(dbFileID)) {
                    final DBFile dbFile = new DBFile();
                    dbFile.setDbFileID(dbFileID);
                    dbFile.setRequest(request);
                    dbFile.setData(file.getBytes());
                    dbFilesRepository.save(dbFile);
                    success.set(true);
                }
            } catch (final IOException e) {
                success.set(false);
            }
        });
        return success.get();
    }

    @GetMapping("/files/{user}/{requestNumber}")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("#user == authentication.principal.username")
    public String getUploadedFilesList(@PathVariable String user, @PathVariable long requestNumber) {
        final List<String> fileNames = new LinkedList<>();
        forEachRequestFromUser(user, requestNumber, request -> request.getFiles().forEach(file -> fileNames.add(file.getDbFileID().getFilename())));
        return gson.toJson(fileNames);
    }

    @GetMapping("/files/{user}/{requestNumber}/{fileName}")
    @PreAuthorize("#user == authentication.principal.username")
    public boolean deleteFile(@PathVariable String user, @PathVariable long requestNumber, @PathVariable String fileName) {
        final AtomicBoolean success = new AtomicBoolean(false);
        forEachRequestFromUser(user, requestNumber, request ->
            request.getFiles().stream().filter(file -> file.getDbFileID().getFilename().equals(fileName)).findAny().ifPresent(file -> {
                dbFilesRepository.delete(file);
                success.set(true);
            }));
        return success.get();
    }

    private void forEachRequestFromUser(final String user, final long requestNumber, final Consumer<LegalAssistance> action) {
        userRepository.findById(user).ifPresent(foundUser -> foundUser.getRequiredLegalAssistance().parallelStream()
                .filter(request -> request.getId() == requestNumber)
                .forEach(action));
    }
}
