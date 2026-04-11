package com.app.tastefrancesinhasbackend.exception;

// Se lanza cuando un recurso no existe en BD (404)
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
