package util.exceptions;

/**
 * @author Jonas Kraus
 * @author Fabian Widmann
 * Should be used if the user specifies information we cannot use such as
 */
public class PartiallyModifiedException extends Exception {
    private long objectId;

    public long getObjectId() {
        return objectId;
    }

    public PartiallyModifiedException()
    {
    }

    public PartiallyModifiedException(String message, long objectId)
    {
        super(message);
        this.objectId=objectId;
    }

    public PartiallyModifiedException(String message)
    {
        super(message);
    }

    public PartiallyModifiedException(Throwable cause)
    {
        super(cause);
    }

    public PartiallyModifiedException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public PartiallyModifiedException(String message, Throwable cause,
                                      boolean enableSuppression, boolean writableStackTrace)
    {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
