package br.com.oriontask.backend.config;

import br.com.oriontask.backend.users.exception.UserNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

  /** Handle validation errors from @Validated annotations */
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<Map<String, Object>> handleValidationErrors(
      MethodArgumentNotValidException ex, HttpServletRequest request) {
    Map<String, String> errors = new HashMap<>();
    ex.getBindingResult()
        .getAllErrors()
        .forEach(
            error -> {
              String fieldName = ((FieldError) error).getField();
              String message = error.getDefaultMessage();
              errors.put(fieldName, message);
            });

    Map<String, Object> response = new HashMap<>();
    response.put("timestamp", Instant.now().toString());
    response.put("status", HttpStatus.BAD_REQUEST.value());
    response.put("error", "Validation Failed");
    response.put("errors", errors);
    response.put("path", request.getRequestURI());

    return ResponseEntity.badRequest().body(response);
  }

  /** Handle business logic errors (e.g., duplicate email, invalid credentials) */
  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<Map<String, Object>> handleIllegalArgumentException(
      IllegalArgumentException ex, HttpServletRequest request) {
    Map<String, Object> response = new HashMap<>();
    response.put("timestamp", Instant.now().toString());
    response.put("status", HttpStatus.BAD_REQUEST.value());
    response.put("error", "Bad Request");
    response.put("message", ex.getMessage());
    response.put("path", request.getRequestURI());

    return ResponseEntity.badRequest().body(response);
  }

  /** Handle illegal state exceptions (e.g., unauthenticated access) */
  @ExceptionHandler(IllegalStateException.class)
  public ResponseEntity<Map<String, Object>> handleIllegalStateException(
      IllegalStateException ex, HttpServletRequest request) {
    Map<String, Object> response = new HashMap<>();
    response.put("timestamp", Instant.now().toString());
    response.put("status", HttpStatus.UNAUTHORIZED.value());
    response.put("error", "Unauthorized");
    response.put("message", ex.getMessage());
    response.put("path", request.getRequestURI());

    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
  }

  /** Generic exception handler for unexpected errors */
  @ExceptionHandler(Exception.class)
  public ResponseEntity<Map<String, Object>> handleGenericException(
      Exception ex, HttpServletRequest request) {
    Map<String, Object> response = new HashMap<>();
    response.put("timestamp", Instant.now().toString());
    response.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
    response.put("error", "Internal Server Error");
    response.put("message", "An unexpected error occurred");
    response.put("path", request.getRequestURI());

    // Log the full exception for debugging
    log.error("Unexpected error occurred", ex);

    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
  }

  @ExceptionHandler(DataIntegrityViolationException.class)
  public ResponseEntity<Map<String, Object>> handleDataIntegrityViolationException(
      DataIntegrityViolationException ex, HttpServletRequest request) {
    String cause = ex.getMostSpecificCause() != null ? ex.getMostSpecificCause().getMessage() : "";

    String message = "Dados Inválidos";
    if (cause.contains("uq_users_email") || cause.contains("tab_users_email_key")) {
      message = "Email unavailable";
    }

    Map<String, Object> response = new HashMap<>();
    response.put("timestamp", Instant.now().toString());
    response.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
    response.put("error", message);
    response.put("path", request.getRequestURI());

    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
  }

  @ExceptionHandler(UserNotFoundException.class)
  public ResponseEntity<Map<String, Object>> handleUserNotFoundException(
      UserNotFoundException ex, HttpServletRequest request) {
    Map<String, Object> response = new HashMap<>();
    response.put("timestamp", Instant.now().toString());
    response.put("status", HttpStatus.NOT_FOUND.value());
    response.put("error", "Not Found");
    response.put("message", ex.getMessage());
    response.put("path", request.getRequestURI());

    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
  }
}
