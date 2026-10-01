package io.a_caminho.backend.exception;

public class UserDoesNotHavePermissionException extends RuntimeException {
    public UserDoesNotHavePermissionException(String message) {
        super(message);
    }
}
