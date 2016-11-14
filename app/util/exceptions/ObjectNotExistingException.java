package util.exceptions;

/**
 * @author Jonas Kraus
 * @author Fabian Widmann
 * This exception can and should be thrown if an incoming json body demands access to a specific object with an id and the acess fails as it does not exist at the moment.
 */
public class ObjectNotExistingException extends Exception {

    public long getObjectId() {
        return objectId;
    }

    private long objectId;

    public ObjectNotExistingException()
    {
    }

    public ObjectNotExistingException(String message, long id)
    {
        super(message);
        objectId=id;
    }

    public ObjectNotExistingException(String message)
    {
        super(message);
    }

    public ObjectNotExistingException(Throwable cause)
    {
        super(cause);
    }

    public ObjectNotExistingException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public ObjectNotExistingException(String message, Throwable cause,
                                      boolean enableSuppression, boolean writableStackTrace)
    {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
