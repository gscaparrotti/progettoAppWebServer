package application.controllers;

import application.entities.Message;
import application.repositories.MessagesRepository;
import application.utils.UserRepositoryHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.Set;

@CrossOrigin
@RestController
@RequestMapping({"/api"})
public class MessagesController {

    private final UserRepositoryHelper helper;
    private final MessagesRepository messagesRepository;

    @Autowired
    public MessagesController(UserRepositoryHelper userRepositoryHelper, MessagesRepository messagesRepository) {
        this.helper = userRepositoryHelper;
        this.messagesRepository = messagesRepository;
    }

    @GetMapping("/messages/{user}/{requestNumber}")
    @PreAuthorize("#user == authentication.principal.username or hasRole('ROLE_ADMIN')")
    public ResponseEntity<Set<Message>> getMessages(@PathVariable String user, @PathVariable long requestNumber) {
        return helper.transformRequestFromUser(user, requestNumber,
                request -> new ResponseEntity<>(request.getMessages(), HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @PostMapping("/messages/{user}/{requestNumber}")
    @PreAuthorize("#user == authentication.principal.username or hasRole('ROLE_ADMIN')")
    public ResponseEntity<Message> addMessage(@PathVariable String user, @PathVariable long requestNumber,
                                              @RequestBody Message message) {
        return helper.transformRequestFromUser(user, requestNumber, request -> {
            if (message.getMessage() == null || message.getMessage().length() == 0) {
                return new ResponseEntity<Message>(HttpStatus.BAD_REQUEST);
            }
            message.setRequest(request);
            message.setDate(new Date());
            final Message newMessage = messagesRepository.save(message);
            return new ResponseEntity<>(newMessage, HttpStatus.OK);
        }).orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }
}
