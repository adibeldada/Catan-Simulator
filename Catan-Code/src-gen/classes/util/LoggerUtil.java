package classes.util;

import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.util.logging.ConsoleHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * Utility class to provide consistent logging configuration across the application.
 */
public class LoggerUtil {

    // Private constructor to prevent instantiation of utility class
    private LoggerUtil() {}

    /**
     * Custom handler that redirects standard output to the console.
     */
    private static class WhiteTextHandler extends ConsoleHandler {
        WhiteTextHandler() {
            super();
            setOutputStream(new FileOutputStream(FileDescriptor.out));
        }
    }

    /**
     * Configures the root logger to use a clean formatting style and remove default handlers.
     */
    public static void setupLogging() {
        Logger rootLogger = Logger.getLogger("");
        
        // Remove existing handlers to avoid duplicate output
        for (Handler handler : rootLogger.getHandlers()) {
            rootLogger.removeHandler(handler);
        }

        // Initialize the custom handler
        ConsoleHandler whiteHandler = new WhiteTextHandler();

        // Apply custom formatting that only returns the message text
        whiteHandler.setFormatter(new Formatter() {
            @Override
            public String format(LogRecord logRecord) {
                return logRecord.getMessage() + System.lineSeparator();
            }
        });

        rootLogger.addHandler(whiteHandler);
    }
}