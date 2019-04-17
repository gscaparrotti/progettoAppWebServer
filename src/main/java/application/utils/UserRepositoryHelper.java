package application.utils;

import application.entities.LegalAssistance;
import application.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.function.Function;

@Service
public class UserRepositoryHelper {

    private final UserRepository userRepository;

    @Autowired
    public UserRepositoryHelper(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    //useful to find something inside a particular request from a certain user and manipulate it
    public <A> Optional<A> transformRequestFromUser(final String user, final long requestNumber,
                                                    final Function<LegalAssistance, A> functionOnRequest) {
        return userRepository.findById(user).flatMap(foundUser -> foundUser.getRequiredLegalAssistance().stream()
                .filter(request -> request.getId() == requestNumber)
                .limit(1) //there should be just one hit, but just in case...
                .map(functionOnRequest)
                .findAny());
    }
}
