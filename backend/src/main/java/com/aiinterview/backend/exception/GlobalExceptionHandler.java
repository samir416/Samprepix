package com.aiinterview.backend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Validation Exception Handler
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<String> handleValidationException(
            MethodArgumentNotValidException ex) {

        ex.getBindingResult()
                .getFieldErrors()
                .forEach(error -> System.out.println(
                        error.getField() + " : " + error.getDefaultMessage()));

        String errorMessage = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .filter(error -> "NotBlank".equals(error.getCode()))
                .map(error -> error.getDefaultMessage())
                .findFirst()
                .orElse(ex.getBindingResult()
                        .getFieldErrors()
                        .get(0)
                        .getDefaultMessage());

        return ResponseEntity
                .badRequest()
                .body(errorMessage);
    }

    /**
     * Resource Not Found Exception
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<String> handleResourceNotFoundException(
            ResourceNotFoundException ex) {

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ex.getMessage());
    }

    /**
     * Invalid Token Exception
     */
    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<String> handleInvalidTokenException(
            InvalidTokenException ex) {

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ex.getMessage());
    }

    /**
     * Token Already Used Exception
     */
    @ExceptionHandler(TokenAlreadyUsedException.class)
    public ResponseEntity<String> handleTokenAlreadyUsedException(
            TokenAlreadyUsedException ex) {

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(ex.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<java.util.Map<String, String>> handleIllegalArgumentException(IllegalArgumentException ex) {
        return ResponseEntity
                .badRequest()
                .body(java.util.Map.of("error", ex.getMessage() != null ? ex.getMessage() : "Invalid request parameters."));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<java.util.Map<String, Object>> handleIllegalStateException(IllegalStateException ex) {
        boolean isAuth = ex.getMessage() != null && (ex.getMessage().contains("connect") || ex.getMessage().contains("permission") || ex.getMessage().contains("token"));
        return ResponseEntity
                .badRequest()
                .body(java.util.Map.of(
                        "error", ex.getMessage() != null ? ex.getMessage() : "Action cannot be performed in current state.",
                        "requireGitHubAuth", isAuth
                ));
    }

    @ExceptionHandler(org.springframework.http.converter.HttpMessageNotReadableException.class)
    public ResponseEntity<java.util.Map<String, String>> handleMessageNotReadable(org.springframework.http.converter.HttpMessageNotReadableException ex) {
        String detail = ex.getMostSpecificCause() != null ? ex.getMostSpecificCause().getMessage() : ex.getMessage();
        return ResponseEntity
                .badRequest()
                .body(java.util.Map.of("error", "Malformed request body or invalid JSON: " + (detail != null ? detail : "")));
    }

    @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
    public ResponseEntity<java.util.Map<String, String>> handleAccessDeniedException(org.springframework.security.access.AccessDeniedException ex) {
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(java.util.Map.of("error", "Access denied. You do not have permission to perform this action."));
    }

    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<java.util.Map<String, String>> handleSecurityException(SecurityException ex) {
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(java.util.Map.of("error", ex.getMessage() != null ? ex.getMessage() : "Access denied."));
    }

    /**
     * Fallback Exception
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleGenericException(
            Exception ex) {

        ex.printStackTrace();

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Something went wrong. Please try again later.");
    }
}