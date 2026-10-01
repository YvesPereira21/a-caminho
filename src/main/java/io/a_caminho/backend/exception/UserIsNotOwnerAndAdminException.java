package io.a_caminho.backend.exception;

public class UserIsNotOwnerAndAdminException extends RuntimeException {
    public UserIsNotOwnerAndAdminException(String message) {
        super(message);
    }
}
