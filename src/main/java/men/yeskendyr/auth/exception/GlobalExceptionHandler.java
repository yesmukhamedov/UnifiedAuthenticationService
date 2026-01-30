package men.yeskendyr.auth.exception;

import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.Map;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(ApiException.class)
    public ResponseEntity<Map<String, Object>> handleApi(ApiException ex, HttpServletRequest request) {
        return response(ex.getStatus(), ex.getErrorCode(), ex.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    public ResponseEntity<Map<String, Object>> handleValidation(Exception ex, HttpServletRequest request) {
        return response(HttpStatus.BAD_REQUEST, ErrorCode.INVALID_REQUEST,
                "Request validation failed", request.getRequestURI());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> handleIntegrity(DataIntegrityViolationException ex,
                                                               HttpServletRequest request) {
        return response(HttpStatus.CONFLICT, ErrorCode.IDENTIFIER_IN_USE,
                "Identifier already in use", request.getRequestURI());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneric(Exception ex, HttpServletRequest request) {
        return response(HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.INVALID_REQUEST,
                "Unexpected error", request.getRequestURI());
    }

    private ResponseEntity<Map<String, Object>> response(HttpStatus status, ErrorCode errorCode,
                                                         String message, String path) {
        Map<String, Object> body = Map.of(
                "timestamp", Instant.now().toString(),
                "path", path,
                "errorCode", errorCode.name(),
                "message", message
        );
        return ResponseEntity.status(status).body(body);
    }
}
