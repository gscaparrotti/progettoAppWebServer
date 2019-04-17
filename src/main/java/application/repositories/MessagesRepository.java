package application.repositories;

import application.entities.Message;
import org.springframework.data.repository.CrudRepository;

public interface MessagesRepository extends CrudRepository<Message, String> {
}
