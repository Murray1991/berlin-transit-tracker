package dev.leonardo.bvgtransit.commute;

import dev.leonardo.bvgtransit.commute.dto.ErrorDto;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(value = {ResponseStatusException.class})
    public ResponseEntity<ErrorDto> handleResponseStatusException(ResponseStatusException e) {
        var status = e.getStatusCode();
        var error =
                new ErrorDto(
                        LocalDateTime.now(),
                        status.value(),
                        HttpStatus.valueOf(status.value()).name(),
                        e.getReason());
        return ResponseEntity.status(status).body(error);
    }
}
