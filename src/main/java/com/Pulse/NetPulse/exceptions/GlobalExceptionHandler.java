package com.Pulse.NetPulse.exceptions;

import com.Pulse.NetPulse.exceptions.DuplicateDeviceException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    //If a service launch DuplicateDeviceException, spring return 422 code
    @ExceptionHandler(DuplicateDeviceException.class)
    public ResponseEntity<String> handleDuplicateDevice(DuplicateDeviceException ex) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(ex.getMessage());
    }

    // If a service launc a generic RunTimeException, return 500
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handleGenericRuntimeException(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ex.getMessage());
    }
}