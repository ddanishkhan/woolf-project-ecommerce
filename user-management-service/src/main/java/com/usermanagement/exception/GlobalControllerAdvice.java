package com.usermanagement.exception;

import com.usermanagement.dto.response.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AccountStatusException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.security.SignatureException;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.List;

@Slf4j
@ControllerAdvice
public class GlobalControllerAdvice extends ResponseEntityExceptionHandler {

    static final String DESCRIPTION = "description";

    @ExceptionHandler(RoleAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleRoleAlreadyExistsException(RoleAlreadyExistsException ex) {
        logException(ex);
        ErrorResponse response = new ErrorResponse("Role already exists: " + ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler({InvalidLoginCredentialsException.class, InvalidTokenException.class})
    public ResponseEntity<ErrorResponse> handleUnauthorizedExceptions(RuntimeException ex) {
        logException(ex);
        var response = new ErrorResponse(ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    @ExceptionHandler({UserAlreadyExistsException.class, UserNotFoundException.class, InvalidRoleException.class})
    public ResponseEntity<ErrorResponse> handleCustomExceptions(RuntimeException ex) {
        logException(ex);
        var response = new ErrorResponse(ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler({NullPointerException.class})
    public ResponseEntity<ErrorResponse> handleNPE(NullPointerException ex) {
        var response = new ErrorResponse("Internal server error.");
        logException(ex);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    private void logException(RuntimeException ex) {
        log.error("Exception {}", ex.getMessage(), ex);
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleSecurityException(Exception exception) {
        ProblemDetail errorDetail = null;

        log.error("SecurityException {}", exception.getMessage(), exception);

        if (exception instanceof BadCredentialsException) {
            errorDetail = ProblemDetail.forStatusAndDetail(HttpStatusCode.valueOf(401), exception.getMessage());
            errorDetail.setProperty(DESCRIPTION, "The username or password is incorrect");

            return errorDetail;
        }

        if (exception instanceof AccountStatusException) {
            errorDetail = ProblemDetail.forStatusAndDetail(HttpStatusCode.valueOf(403), exception.getMessage());
            errorDetail.setProperty(DESCRIPTION, "The account is locked");
        }

        if (exception instanceof AccessDeniedException) {
            errorDetail = ProblemDetail.forStatusAndDetail(HttpStatusCode.valueOf(403), exception.getMessage());
            errorDetail.setProperty(DESCRIPTION, "You are not authorized to access this resource");
        }

        if (exception instanceof SignatureException) {
            errorDetail = ProblemDetail.forStatusAndDetail(HttpStatusCode.valueOf(403), exception.getMessage());
            errorDetail.setProperty(DESCRIPTION, "The JWT signature is invalid");
        }

        if (exception instanceof ExpiredJwtException) {
            errorDetail = ProblemDetail.forStatusAndDetail(HttpStatusCode.valueOf(403), exception.getMessage());
            errorDetail.setProperty(DESCRIPTION, "The JWT token has expired");
        }

        if (errorDetail == null) {
            errorDetail = ProblemDetail.forStatusAndDetail(HttpStatusCode.valueOf(500), exception.getMessage());
            errorDetail.setProperty(DESCRIPTION, "Unknown internal server error.");
        }

        return errorDetail;
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        var problemDetail = ProblemDetail.forStatus(status);

        List<String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(x -> "Field " + x.getField() + " " + x.getDefaultMessage())
                .toList();

        problemDetail.setProperty("errors", errors);
        return this.handleExceptionInternal(ex, problemDetail, headers, status, request);
    }

}
