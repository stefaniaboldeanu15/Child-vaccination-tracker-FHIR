package org.prt.prtvaccinationtracking_fhir.exception;

import ca.uhn.fhir.rest.server.exceptions.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, Object> notFound(Exception ex) {
        return Map.of("error", "NOT_FOUND", "message", ex.getMessage());
    }

    @ExceptionHandler({InvalidRequestException.class, IllegalArgumentException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, Object> badRequest(Exception ex) {
        return Map.of("error", "BAD_REQUEST", "message", ex.getMessage());
    }

    @ExceptionHandler(UnprocessableEntityException.class)
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    public Map<String, Object> unprocessable(Exception ex) {
        return Map.of("error", "UNPROCESSABLE_ENTITY", "message", ex.getMessage());
    }
}