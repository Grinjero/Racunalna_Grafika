package math.vector;

public class ReadOnlyException extends RuntimeException {
    public ReadOnlyException() {
        super("This vector is read only");
    }
}
