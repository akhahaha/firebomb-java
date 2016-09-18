package firebomb;

public class FirebombException extends RuntimeException {
    public FirebombException() {
    }

    public FirebombException(String message) {
        super(message);
    }

    public FirebombException(String message, Throwable cause) {
        super(message, cause);
    }

    public FirebombException(Throwable cause) {
        super(cause);
    }

    public FirebombException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
