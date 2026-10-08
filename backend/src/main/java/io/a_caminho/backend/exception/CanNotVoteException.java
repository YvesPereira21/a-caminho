package io.a_caminho.backend.exception;

public class CanNotVoteException extends RuntimeException {
    public CanNotVoteException(String message) {
        super(message);
    }
}
