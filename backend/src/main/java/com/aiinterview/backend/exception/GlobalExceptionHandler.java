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