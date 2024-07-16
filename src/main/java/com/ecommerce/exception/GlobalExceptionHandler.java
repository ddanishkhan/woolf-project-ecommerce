package com.ecommerce.exception;

import com.ecommerce.dto.response.ErrorResponseDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.Optional;

@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Provides handling for exceptions throughout this service.
     */
    @ExceptionHandler(value = ProductNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleProductNotFoundException(Exception ex) {
        ErrorResponseDTO errorResponse = new ErrorResponseDTO();
        errorResponse.setMessage(Optional.ofNullable(ex.getMessage()).orElse("Requested product not found."));
        errorResponse.setMessageCode(404);
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(value = UnsupportedOperationException.class)
    public ResponseEntity<ErrorResponseDTO> handleUnsupportedOperationException(UnsupportedOperationException ex) {
        ErrorResponseDTO errorResponse = new ErrorResponseDTO();
        errorResponse.setMessage(Optional.ofNullable(ex.getMessage()).orElse("Requested operation is not supported."));
        errorResponse.setMessageCode(HttpStatus.NOT_IMPLEMENTED.value());
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_IMPLEMENTED);
    }

}
