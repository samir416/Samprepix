package com.aiinterview.backend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(
        MethodArgumentNotValidException.class
)
public ResponseEntity<String> handleValidationException(
        MethodArgumentNotValidException ex) {

    ex.getBindingResult()
            .getFieldErrors()
            .forEach(error -> {

                System.out.println(
                        error.getField()
                                + " : "
                                + error.getDefaultMessage()
                );
            });

    String errorMessage =
            ex.getBindingResult()
                    .getFieldErrors()
                    .stream()
                    .filter(error ->
                            error.getCode()
                                    .equals("NotBlank"))
                    .map(error ->
                            error.getDefaultMessage())
                    .findFirst()
                    .orElse(
                            ex.getBindingResult()
                                    .getFieldErrors()
                                    .get(0)
                                    .getDefaultMessage()
                    );

    return ResponseEntity
            .badRequest()
            .body(errorMessage);
}

}