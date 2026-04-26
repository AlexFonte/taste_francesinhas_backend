package com.app.tastefrancesinhasbackend.exception;

// Se lanza cuando hay un conflicto de datos (409)
//email ya registrado, usuario ya tiene review para esa francesinha
public class ConflictException extends RuntimeException {
    public ConflictException(String message) {
        super(message);
    }
}
