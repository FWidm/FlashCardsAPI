package util.exceptions;

/**
 * @author Jonas Kraus
 * @author Fabian Widmann
 * Should be used if the user specifies information we cannot use such as
 */
public class PartiallyUpdatedException extends Exception {

    public PartiallyUpdatedException()
    {
    }

    public PartiallyUpdatedException(String message)
    {
        super(message);
    }

    public PartiallyUpdatedException(Throwable cause)
    {
        super(cause);
    }

    public PartiallyUpdatedException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public PartiallyUpdatedException(String message, Throwable cause,
                                     boolean enableSuppression, boolean writableStackTrace)
    {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
