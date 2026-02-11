package com.cantina.biometric.exception;

import com.cantina.biometric.config.RequestIdFilter;
import com.cantina.biometric.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        String message = ex.getBindingResult().getAllErrors().stream()
                .map(error -> {
                    if (error instanceof FieldError fieldError) {
                        return fieldError.getField() + ": " + fieldError.getDefaultMessage();
                    }
                    return error.getDefaultMessage();
                })
                .collect(Collectors.joining("; "));

        log.warn("event=validation-error requestId={} method={} path={} message={}",
                MDC.get(RequestIdFilter.REQUEST_ID_MDC_KEY),
                request.getMethod(),
                request.getRequestURI(),
                message);

        return buildError(HttpStatus.BAD_REQUEST, message, request);
    }

    @ExceptionHandler({BadRequestException.class, HttpMessageNotReadableException.class, IllegalArgumentException.class})
    public ResponseEntity<ErrorResponse> handleBadRequest(Exception ex, HttpServletRequest request) {
        log.warn("event=bad-request requestId={} method={} path={} errorClass={} message={}",
                MDC.get(RequestIdFilter.REQUEST_ID_MDC_KEY),
                request.getMethod(),
                request.getRequestURI(),
                ex.getClass().getSimpleName(),
                ex.getMessage());
        return buildError(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    @ExceptionHandler(PayloadTooLargeException.class)
    public ResponseEntity<ErrorResponse> handleTooLarge(PayloadTooLargeException ex, HttpServletRequest request) {
        log.warn("event=payload-too-large requestId={} method={} path={} message={}",
                MDC.get(RequestIdFilter.REQUEST_ID_MDC_KEY),
                request.getMethod(),
                request.getRequestURI(),
                ex.getMessage());
        return buildError(HttpStatus.PAYLOAD_TOO_LARGE, ex.getMessage(), request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex, HttpServletRequest request) {
        log.error("event=unhandled-exception requestId={} method={} path={} message={}",
                MDC.get(RequestIdFilter.REQUEST_ID_MDC_KEY),
                request.getMethod(),
                request.getRequestURI(),
                ex.getMessage(),
                ex);
        return buildError(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected internal error", request);
    }

    private ResponseEntity<ErrorResponse> buildError(HttpStatus status, String message, HttpServletRequest request) {
        ErrorResponse body = new ErrorResponse(
                Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                request.getRequestURI(),
                MDC.get(RequestIdFilter.REQUEST_ID_MDC_KEY)
        );
        return ResponseEntity.status(status).body(body);
    }
}
