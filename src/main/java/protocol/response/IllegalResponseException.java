package protocol.response;

public class IllegalResponseException extends Exception {

    public IllegalResponseException() {
    }

    public IllegalResponseException(String msg) {
        super(msg);
    }

    public IllegalResponseException(Throwable cause) {
        super(cause);
    }

    public IllegalResponseException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
