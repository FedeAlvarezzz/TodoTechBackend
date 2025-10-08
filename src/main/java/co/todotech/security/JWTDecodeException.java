package co.todotech.security;

public class JWTDecodeException extends RuntimeException {
    public JWTDecodeException(String message) {
        super(message);
    }

    public JWTDecodeException(String message, Throwable cause) {
        super(message, cause);
    }
}