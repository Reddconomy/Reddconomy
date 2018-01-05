package webbit_lite.handler.exceptions;

/**
 * Exception handler that does nothing. Exceptions are silently discarded.
 *
 * @see webbit_lite.WebServer#connectionExceptionHandler(java.lang.Thread.UncaughtExceptionHandler)
 * @see webbit_lite.WebServer#uncaughtExceptionHandler(java.lang.Thread.UncaughtExceptionHandler)
 */
public class SilentExceptionHandler implements Thread.UncaughtExceptionHandler {

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        // Do nothing.
    }

}

