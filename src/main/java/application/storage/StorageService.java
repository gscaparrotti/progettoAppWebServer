package application.storage;

import org.apache.tika.Tika;
import org.apache.tika.io.TikaInputStream;
import org.apache.tika.metadata.Metadata;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.stream.Stream;

@Service
public class StorageService {

    private static String[] ALLOWED_FORMATS = {"image/jpeg", "image/png", "image/tiff", "application/pdf"};

    private final Path folder = Paths.get(System.getProperty("user.dir") + System.getProperty("file.separator") + "uploads");
    private Tika tika = new Tika();
    private boolean canStore = true;

    @PostConstruct
    private void init() {
        if (!Files.exists(folder)) {
            try {
                Files.createDirectory(folder);
            } catch (final IOException e) {
                canStore = false;
            }
        }
    }

    public void store(final MultipartFile file, final String user, final long requestNumber) throws IOException, IllegalArgumentException {
        if (!canStore) {
            throw new IOException("Unable to store files");
        }
        Metadata metadata = new Metadata();
        metadata.set(Metadata.RESOURCE_NAME_KEY, file.toString());
        String mimeType = tika.getDetector().detect(TikaInputStream.get(file.getBytes()), metadata).toString();
        if (Arrays.stream(ALLOWED_FORMATS).noneMatch(mimeType::equals)) {
            throw new IllegalArgumentException("Invalid file format");
        }
        final String filename = StringUtils.cleanPath(file.getOriginalFilename());
        if (file.isEmpty() || filename.contains("..")) {
            throw new IOException("Failed to store file " + filename);
        }
        final Path innerFolderPath = folder.resolve(Paths.get(user + '_' + requestNumber));
        if (!Files.exists(innerFolderPath)) {
            Files.createDirectory(innerFolderPath);
        }
        Files.copy(file.getInputStream(), innerFolderPath.resolve(filename), StandardCopyOption.REPLACE_EXISTING);
    }

    public Stream<Path> loadAllFilenames(final String user, final long requestNumber) throws IOException {
        final Path innerFolderPath = folder.resolve(Paths.get(user + '_' + requestNumber));
        if (Files.exists(innerFolderPath)) {
            return Files.walk(innerFolderPath, 1)
                    .filter(path -> !path.equals(innerFolderPath))
                    .map(this.folder::relativize);
        } else {
            return Stream.empty();
        }
    }
}
