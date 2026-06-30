package com.employeesphere.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.ModelAndView;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    // For REST API ResourceNotFoundException
    @ExceptionHandler(ResourceNotFoundException.class)
    public Object handleResourceNotFound(ResourceNotFoundException ex, WebRequest request) {
        if (isApiRequest(request)) {
            Map<String, Object> body = new HashMap<>();
            body.put("timestamp", LocalDateTime.now());
            body.put("status", HttpStatus.NOT_FOUND.value());
            body.put("error", "Not Found");
            body.put("message", ex.getMessage());
            return new ResponseEntity<>(body, HttpStatus.NOT_FOUND);
        } else {
            ModelAndView modelAndView = new ModelAndView("error");
            modelAndView.addObject("message", ex.getMessage());
            modelAndView.addObject("status", 404);
            return modelAndView;
        }
    }

    // For REST API BadRequestException
    @ExceptionHandler(BadRequestException.class)
    public Object handleBadRequest(BadRequestException ex, WebRequest request) {
        if (isApiRequest(request)) {
            Map<String, Object> body = new HashMap<>();
            body.put("timestamp", LocalDateTime.now());
            body.put("status", HttpStatus.BAD_REQUEST.value());
            body.put("error", "Bad Request");
            body.put("message", ex.getMessage());
            return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
        } else {
            ModelAndView modelAndView = new ModelAndView("error");
            modelAndView.addObject("message", ex.getMessage());
            modelAndView.addObject("status", 400);
            return modelAndView;
        }
    }

    // Validation Exceptions
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }

    // General Exceptions fallback
    @ExceptionHandler(Exception.class)
    public Object handleGlobalException(Exception ex, WebRequest request) {
        ex.printStackTrace();
        if (isApiRequest(request)) {
            Map<String, Object> body = new HashMap<>();
            body.put("timestamp", LocalDateTime.now());
            body.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
            body.put("error", "Internal Server Error");
            body.put("message", ex.getMessage());
            return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
        } else {
            ModelAndView modelAndView = new ModelAndView("error");
            modelAndView.addObject("message", "An unexpected error occurred: " + ex.getMessage());
            modelAndView.addObject("status", 500);
            return modelAndView;
        }
    }

    private boolean isApiRequest(WebRequest request) {
        String path = request.getDescription(false);
        return path != null && path.contains("/api/");
    }
}
