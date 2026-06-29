package unpsjb.labprog.backend.exception;

public class SchedulingException extends BusinessException {

    private final int schedulableQuantity;

    public SchedulingException(String message) {
        super(message);
        this.schedulableQuantity = 0;
    }

    public SchedulingException(String message, int schedulableQuantity) {
        super(message);
        this.schedulableQuantity = schedulableQuantity;
    }

    public int getSchedulableQuantity() {
        return schedulableQuantity;
    }
}