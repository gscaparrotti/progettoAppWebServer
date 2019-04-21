package application.controllers;

import application.entities.DBFile;
import application.repositories.DBFilesRepository;
import application.utils.UserRepositoryHelper;
import org.apache.tika.Tika;
import org.apache.tika.io.TikaInputStream;
import org.apache.tika.metadata.Metadata;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@CrossOrigin
@RestController
@RequestMapping({"/api"})
public class FileStorageController {

    private static final String[] ALLOWED_FORMATS = {"image/jpeg", "image/png", "image/tiff"};
    private final Tika tika = new Tika();
    private final UserRepositoryHelper helper;
    private final DBFilesRepository dbFilesRepository;

    @Autowired
    public FileStorageController(UserRepositoryHelper userRepositoryHelper, DBFilesRepository dbFilesRepository) {
        this.helper = userRepositoryHelper;
        this.dbFilesRepository = dbFilesRepository;
    }

    @PostMapping("/files/{user}/{requestNumber}")
    @PreAuthorize("#user == authentication.principal.username")
    public ResponseEntity<DBFile> handleFileUpload(@PathVariable String user, @PathVariable long requestNumber,
                                                   @RequestPart MultipartFile file, @RequestParam boolean returnFile) {
        final Optional<ResponseEntity<DBFile>> responseEntity = helper.transformRequestFromUser(user, requestNumber, request -> {
            try {
                final DBFile.DBFileID dbFileID = new DBFile.DBFileID();
                dbFileID.setFilename(file.getOriginalFilename());
                dbFileID.setRequest(request.getId());
                if (!dbFilesRepository.existsById(dbFileID)) {
                    if (!isAllowedFileType(file.getOriginalFilename(), file.getBytes())) {
                        return new ResponseEntity<>(HttpStatus.I_AM_A_TEAPOT);
                    }
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
    public ResponseEntity<List<DBFile>> getUploadedFilesList(@PathVariable String user, @PathVariable long requestNumber,
                                                             @RequestParam boolean keepContent) {
        final Optional<ResponseEntity<List<DBFile>>> responseEntity = helper.transformRequestFromUser(user, requestNumber, request -> {
            final List<DBFile> fileNames = request.getFiles().stream()
                    .peek(file -> {
                        if (!keepContent) {
                            file.setData(null);
                        }
                    })
                    .collect(Collectors.toList());
            return new ResponseEntity<>(fileNames, HttpStatus.OK);
        });
        return responseEntity.orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @GetMapping("/files/{user}/{requestNumber}/{fileName}")
    @PreAuthorize("#user == authentication.principal.username")
    public ResponseEntity<DBFile> getFile(@PathVariable String user, @PathVariable long requestNumber, @PathVariable String fileName) {
        final Optional<ResponseEntity<DBFile>> responseEntity = helper.transformRequestFromUser(user, requestNumber, request ->
                request.getFiles().stream()
                        .filter(file -> file.getDbFileID().getFilename().equals(fileName))
                        .findAny()
                        .map(foundFile -> new ResponseEntity<>(foundFile, HttpStatus.OK))
                        .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND)));
        return responseEntity.orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @DeleteMapping("/files/{user}/{requestNumber}/{fileName}")
    @PreAuthorize("#user == authentication.principal.username")
    public ResponseEntity deleteFile(@PathVariable String user, @PathVariable long requestNumber, @PathVariable String fileName) {
        final Optional<ResponseEntity> responseEntity = helper.transformRequestFromUser(user, requestNumber, request ->
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

    private boolean isAllowedFileType(final String fileName, final byte[] fileBytes) throws IOException {
        final Metadata metadata = new Metadata();
        metadata.set(Metadata.RESOURCE_NAME_KEY, fileName);
        final String mimeType = tika.getDetector().detect(TikaInputStream.get(fileBytes), metadata).toString();
        return Arrays.asList(ALLOWED_FORMATS).contains(mimeType);
    }
}
