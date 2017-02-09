package util.exceptions;

import java.util.List;

/**
 * @author Fabian Widmann
 *         This exception can and should be thrown if an incoming json body would contain an object that already exists in the database.
 */
public class DuplicateKeyException extends Exception {


    private long objectId;


    private List<Object> objects;

    public DuplicateKeyException(String message, List<Object> objects) {
        super(message);
        this.objects = objects;
    }

    public DuplicateKeyException() {
    }

    public DuplicateKeyException(String message, long id) {
        super(message);
        objectId = id;
    }

    public DuplicateKeyException(String message) {
        super(message);
    }

    public DuplicateKeyException(Throwable cause) {
        super(cause);
    }

    public DuplicateKeyException(String message, Throwable cause) {
        super(message, cause);
    }

    public DuplicateKeyException(String message, Throwable cause,
                                 boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public long getObjectId() {
        return objectId;
    }

    public List<Object> getObjects() {
        return objects;
    }

}
