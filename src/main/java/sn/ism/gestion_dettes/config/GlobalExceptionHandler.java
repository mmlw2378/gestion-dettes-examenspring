package sn.ism.gestion_dettes.config;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import jakarta.validation.ConstraintViolationException;

@RestControllerAdvice
public class GlobalExceptionHandler {
    
    /**
     * Gestion des erreurs de validation des champs
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidationErrors(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        
        return new ResponseEntity<>(Map.of(
                "success", false,
                "message", "Erreurs de validation",
                "errors", errors
        ), HttpStatus.BAD_REQUEST);
    }
    
    /**
     * Gestion des violations de contraintes
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<?> handleConstraintViolationException(ConstraintViolationException ex) {
        Map<String, String> errors = new HashMap<>();
        
        ex.getConstraintViolations().forEach(violation -> {
            String propertyPath = violation.getPropertyPath().toString();
            String message = violation.getMessage();
            errors.put(propertyPath, message);
        });
        
        return new ResponseEntity<>(Map.of(
                "success", false,
                "message", "Violation de contraintes",
                "errors", errors
        ), HttpStatus.BAD_REQUEST);
    }
    
    /**
     * Gestion des erreurs runtime personnalisées
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<?> handleRuntimeException(RuntimeException ex, WebRequest request) {
        return new ResponseEntity<>(Map.of(
                "success", false,
                "message", ex.getMessage(),
                "timestamp", System.currentTimeMillis(),
                "path", request.getDescription(false)
        ), HttpStatus.BAD_REQUEST);
    }
    
    /**
     * Gestion des erreurs IllegalArgument
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> handleIllegalArgumentException(IllegalArgumentException ex) {
        return new ResponseEntity<>(Map.of(
                "success", false,
                "message", "Argument invalide: " + ex.getMessage()
        ), HttpStatus.BAD_REQUEST);
    }
    
    /**
     * Gestion des erreurs générales
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleGeneralException(Exception ex, WebRequest request) {
        return new ResponseEntity<>(Map.of(
                "success", false,
                "message", "Une erreur interne s'est produite",
                "details", ex.getMessage(),
                "timestamp", System.currentTimeMillis(),
                "path", request.getDescription(false)
        ), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}