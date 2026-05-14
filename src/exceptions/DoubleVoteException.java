package exceptions;

public class DoubleVoteException extends Exception {
    public DoubleVoteException(String message) {
        super(message);
    }
}
