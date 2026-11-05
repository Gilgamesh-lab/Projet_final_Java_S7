package altn72.tpfilrouge.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class RessourceIntrouvableException extends ResponseStatusException {

    public RessourceIntrouvableException(String message) {
        super(HttpStatus.NOT_FOUND, message);
    }
}
