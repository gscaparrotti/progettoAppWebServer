package application.repositories;

import application.entities.LegalAssistance;
import org.springframework.data.repository.CrudRepository;

public interface LegalAssistanceRepository extends CrudRepository<LegalAssistance, Long> {
}
