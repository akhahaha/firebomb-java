package firebomb.definition;

public class DefinitionException extends RuntimeException {
    public DefinitionException() {
    }

    public DefinitionException(String message) {
        super(message);
    }

    public DefinitionException(String message, Throwable cause) {
        super(message, cause);
    }

    public DefinitionException(Throwable cause) {
        super(cause);
    }
}
