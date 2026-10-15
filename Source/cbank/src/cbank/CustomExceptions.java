package cbank;

public class CustomExceptions {
    public static class InsufficientFundsException extends Exception {
        public InsufficientFundsException(String message) { super(message); }
    }

    public static class VaultDepletedException extends Exception {
        public VaultDepletedException(String message) { super(message); }
    }

    public static class AuthenticationException extends Exception {
        public AuthenticationException(String message) { super(message); }
    }

    public static class InvalidPinException extends Exception {
        public InvalidPinException(String message) { super(message); }
    }
}