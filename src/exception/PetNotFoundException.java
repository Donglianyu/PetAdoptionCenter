package exception;

public class PetNotFoundException extends Exception {
    // Constructor accepting a message
    public PetNotFoundException(String message) {
        super(message);
    }
}

