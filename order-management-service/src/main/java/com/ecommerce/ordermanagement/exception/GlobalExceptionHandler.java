package com.ecommerce.ordermanagement.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * Global exception handler to provide consistent error responses across the API.
 */
@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    String errormessage = "Error occurred ";
    private String ERROR = "error";

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleResourceNotFoundException(ResourceNotFoundException ex) {
        log.error(errormessage, ex);
        return new ResponseEntity<>(Map.of(ERROR, ex.getMessage()), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(OrderProcessingException.class)
    public ResponseEntity<Map<String, String>> handleOrderProcessingException(OrderProcessingException ex) {
        log.error(errormessage, ex);
        return new ResponseEntity<>(Map.of(ERROR, ex.getMessage()), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ServiceCommunicationException.class)
    public ResponseEntity<Map<String, String>> handleServiceCommunicationException(ServiceCommunicationException ex) {
        // Return 503 Service Unavailable, as the downstream service has issues.
        log.error(errormessage, ex);
        return new ResponseEntity<>(Map.of(ERROR, ex.getMessage()), HttpStatus.SERVICE_UNAVAILABLE);
    }


    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        fieldError -> fieldError.getDefaultMessage() == null ? "":fieldError.getDefaultMessage()
                ));
        log.error(errormessage, ex);
        return new ResponseEntity<>(Map.of(ERROR, "Validation failed", "details", errors), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGenericException(Exception ex) {
        // Log the exception here for debugging purposes

        log.error(errormessage, ex);
        return new ResponseEntity<>(Map.of(ERROR, "An unexpected error occurred"), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}

