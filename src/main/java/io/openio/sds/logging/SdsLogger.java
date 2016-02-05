package io.openio.sds.logging;

import java.util.logging.Level;

public interface SdsLogger {

    /**
     * Logs a message at {@link Level#FINEST}.
     *
     * @param message
     *            the message to log.
     */
    public void trace(String message);

    /**
     * Logs a throwable at {@link Level#FINEST}. The message of the Throwable
     * will be the message.
     *
     * @param thrown
     *            the Throwable to log.
     */
    public void trace(Throwable thrown);

    /**
     * Logs message with associated throwable information at
     * {@link Level#FINEST}.
     *
     * @param message
     *            the message to log
     * @param thrown
     *            the Throwable associated to the message.
     */
    public void trace(String message, Throwable thrown);

    /**
     * Checks if the {@link Level#FINEST} is enabled.
     *
     * @return true if enabled, false otherwise.
     */
    public boolean isTraceEnabled();

    /**
     * Logs a message at {@link Level#FINE}.
     *
     * @param message
     *            the message to log.
     */
    public void debug(String message);

    /**
     * Checks if the {@link Level#FINE} is enabled.
     *
     * @return true if enabled, false otherwise.
     */
    public boolean isDebugEnabled();

    /**
     * Logs a message at {@link Level#INFO}.
     *
     * @param message
     *            the message to log.
     */
    public void info(String message);

    /**
     * Logs a message at {@link Level#WARNING}.
     *
     * @param message
     *            the message to log.
     */
    public void warn(String message);

    /**
     * Logs a throwable at {@link Level#WARNING}. The message of the Throwable
     * will be the message.
     *
     * @param thrown
     *            the Throwable to log.
     */
    public void warn(Throwable thrown);

    /**
     * Logs message with associated throwable information at
     * {@link Level#WARNING}.
     *
     * @param message
     *            the message to log
     * @param thrown
     *            the Throwable associated to the message.
     */
    public void warn(String message, Throwable thrown);

    /**
     * Logs a message at {@link Level#SEVERE}.
     *
     * @param message
     *            the message to log.
     */
    public void error(String message);

    /**
     * Logs a throwable at {@link Level#SEVERE}. The message of the Throwable
     * will be the message.
     *
     * @param thrown
     *            the Throwable to log.
     */
    public void error(Throwable thrown);

    /**
     * Logs message with associated throwable information at
     * {@link Level#SEVERE}.
     *
     * @param message
     *            the message to log
     * @param thrown
     *            the Throwable associated to the message.
     */
    public void error(String message, Throwable thrown);
}
