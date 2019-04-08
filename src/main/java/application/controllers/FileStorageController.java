package application.controllers;

import application.entities.DBFile;
import application.entities.LegalAssistance;
import application.repositories.DBFilesRepository;
import application.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@CrossOrigin
@RestController
@RequestMapping({"/api"})
public class FileStorageController {

    private final UserRepository userRepository;
    private final DBFilesRepository dbFilesRepository;

    @Autowired
    public FileStorageController(UserRepository userRepository, DBFilesRepository dbFilesRepository) {
        this.userRepository = userRepository;
        this.dbFilesRepository = dbFilesRepository;
    }

    @PostMapping(value = "/files/{user}/{requestNumber}", params = {"returnFile"})
    @PreAuthorize("#user == authentication.principal.username")
    public ResponseEntity<DBFile> handleFileUpload(@PathVariable String user, @PathVariable long requestNumber,
                                           @RequestPart MultipartFile file, @RequestParam boolean returnFile) {
        final Optional<ResponseEntity<DBFile>> responseEntity = transformRequestFromUser(user, requestNumber, request -> {
            try {
                final DBFile.DBFileID  dbFileID = new DBFile.DBFileID();
                dbFileID.setFilename(file.getOriginalFilename());
                dbFileID.setRequest(request.getId());
                if (!dbFilesRepository.existsById(dbFileID)) {
                    DBFile dbFile = new DBFile();
                    dbFile.setDbFileID(dbFileID);
                    dbFile.setRequest(request);
                    dbFile.setData(file.getBytes());
                    dbFile = dbFilesRepository.save(dbFile);
                    if (!returnFile) {
                        dbFile.setData(null);
                    }
                    return new ResponseEntity<>(dbFile, HttpStatus.OK);
                } else {
                    return new ResponseEntity<>(HttpStatus.CONFLICT);
                }
            } catch (final IOException e) {
                return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
            }
        });
        return responseEntity.orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @GetMapping("/files/{user}/{requestNumber}")
    @PreAuthorize("#user == authentication.principal.username")
    public ResponseEntity<List<String>> getUploadedFilesList(@PathVariable String user, @PathVariable long requestNumber) {
        final Optional<ResponseEntity<List<String>>> responseEntity = transformRequestFromUser(user, requestNumber, request -> {
            final List<String> fileNames = request.getFiles().stream()
                    .map(file -> file.getDbFileID().getFilename())
                    .collect(Collectors.toList());
            return new ResponseEntity<>(fileNames, HttpStatus.OK);
        });
        return responseEntity.orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @DeleteMapping("/files/{user}/{requestNumber}/{fileName}")
    @PreAuthorize("#user == authentication.principal.username")
    public ResponseEntity deleteFile(@PathVariable String user, @PathVariable long requestNumber, @PathVariable String fileName) {
        final Optional<ResponseEntity> responseEntity = transformRequestFromUser(user, requestNumber, request ->
                request.getFiles().stream()
                        .filter(file -> file.getDbFileID().getFilename().equals(fileName))
                        .findAny()
                        .map(foundFile -> {
                            dbFilesRepository.delete(foundFile);
                            return new ResponseEntity(HttpStatus.NO_CONTENT);
                        })
                        .orElse(new ResponseEntity(HttpStatus.NOT_FOUND)));
        return responseEntity.orElse(new ResponseEntity(HttpStatus.NOT_FOUND));
    }

    private <A> Optional<A> transformRequestFromUser(final String user, final long requestNumber,
                                                     final Function<LegalAssistance, A> functionOnRequest) {
        return userRepository.findById(user).flatMap(foundUser -> foundUser.getRequiredLegalAssistance().stream()
                .filter(request -> request.getId() == requestNumber)
                .limit(1) //there should be just one hit, but just in case...
                .map(functionOnRequest)
                .findAny());
    }
}
