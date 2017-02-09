package util.exceptions;

/**
 * @author Fabian Widmann
 *         This exception can and should be thrown if an incoming json body demands access to a specific object with an id and the acess fails as it does not exist at the moment.
 */
public class ObjectNotFoundException extends Exception {
    private long objectId;

    public ObjectNotFoundException() {
    }


    public ObjectNotFoundException(String message, long id) {
        super(message);
        objectId = id;
    }

    public ObjectNotFoundException(String message) {
        super(message);
    }

    public ObjectNotFoundException(Throwable cause) {
        super(cause);
    }

    public ObjectNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public ObjectNotFoundException(String message, Throwable cause,
                                   boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public long getObjectId() {
        return objectId;
    }

}
