package com.app.tastefrancesinhasbackend.exception;

// Se lanza cuando la petición es semánticamente incorrecta (400)
// Ej: transición de estado no permitida
public class BadRequestException extends RuntimeException {
    public BadRequestException(String message) {
        super(message);
    }
}
