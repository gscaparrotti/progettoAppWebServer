package application.repositories;

import application.entities.DBFile;
import org.springframework.data.repository.CrudRepository;

public interface DBFilesRepository extends CrudRepository<DBFile, DBFile.DBFileID> {
}
