package util;

/**
 * @author Jonas Kraus
 * @author Fabian Widmann
 */
public class ParameterNotSupportedException extends Exception {

    public ParameterNotSupportedException()
    {
    }

    public ParameterNotSupportedException(String message)
    {
        super(message);
    }

    public ParameterNotSupportedException(Throwable cause)
    {
        super(cause);
    }

    public ParameterNotSupportedException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public ParameterNotSupportedException(String message, Throwable cause,
                           boolean enableSuppression, boolean writableStackTrace)
    {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
