package com.app.tastefrancesinhasbackend.exception;

// Se lanza cuando el usuario intenta hacer algo que no le pertenece (403)
// Ej: borrar una review de otro usuario
public class UnauthorizedException extends RuntimeException {
    public UnauthorizedException(String message) {
        super(message);
    }
}
