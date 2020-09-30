package org.astropeci.urmwstats.auth;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class NotStaffException extends ResponseStatusException {

    public NotStaffException() {
        super(HttpStatus.FORBIDDEN);
    }
}
