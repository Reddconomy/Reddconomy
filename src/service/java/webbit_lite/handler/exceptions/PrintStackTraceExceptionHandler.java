package webbit_lite.handler.exceptions;

import java.io.PrintStream;

/**
 * Exception handler that dumps the stack trace.
 *
 * @see webbit_lite.WebServer#connectionExceptionHandler(java.lang.Thread.UncaughtExceptionHandler)
 * @see webbit_lite.WebServer#uncaughtExceptionHandler(java.lang.Thread.UncaughtExceptionHandler)
 */
public class PrintStackTraceExceptionHandler implements Thread.UncaughtExceptionHandler {

    private final PrintStream out;

    public PrintStackTraceExceptionHandler() {
        this(System.err);
    }

    public PrintStackTraceExceptionHandler(PrintStream out) {
        this.out = out;
    }

    @Override
    public void uncaughtException(Thread t, Throwable exception) {
        exception.printStackTrace(out);
    }
}
