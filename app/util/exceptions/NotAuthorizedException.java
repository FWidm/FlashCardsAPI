package util.exceptions;

/**
 * Created by fabianwidmann on 30/01/17.
 * This should be thrown when the user that called the method does not own the rights to the object.
 */
public class NotAuthorizedException extends Throwable {
    private long objectId;

    public NotAuthorizedException() {
    }


    public NotAuthorizedException(String message, long id) {
        super(message);
        objectId = id;
    }

    public NotAuthorizedException(String message) {
        super(message);
    }

    public NotAuthorizedException(Throwable cause) {
        super(cause);
    }

    public NotAuthorizedException(String message, Throwable cause) {
        super(message, cause);
    }

    public NotAuthorizedException(String message, Throwable cause,
                                  boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public long getObjectId() {
        return objectId;
    }
}
