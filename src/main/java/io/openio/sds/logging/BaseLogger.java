package io.openio.sds.logging;

import static java.util.logging.Level.FINE;
import static java.util.logging.Level.FINEST;

import java.util.logging.Level;
import java.util.logging.Logger;

public class BaseLogger implements SdsLogger {

    private Logger logger;

    BaseLogger(Class<?> c) {
        logger = Logger.getLogger(c.getName());
    }

    @Override
    public void trace(String message) {
        logger.finest(message);
    }

    @Override
    public void trace(Throwable thrown) {
        logger.log(FINEST, thrown.getMessage(), thrown);
    }

    @Override
    public void trace(String message, Throwable thrown) {
        logger.log(FINEST, message, thrown);
    }

    @Override
    public boolean isTraceEnabled() {
        return logger.isLoggable(FINEST);
    }

    @Override
    public void debug(String message) {
        logger.fine(message);
    }

    @Override
    public boolean isDebugEnabled() {
        return logger.isLoggable(FINE);
    }

    @Override
    public void info(String message) {
        logger.info(message);
    }

    @Override
    public void warn(String message) {
        logger.warning(message);
    }

    @Override
    public void warn(Throwable thrown) {
        logger.log(Level.WARNING, thrown.getMessage(), thrown);
    }

    @Override
    public void warn(String message, Throwable thrown) {
        logger.log(Level.WARNING, message, thrown);
    }

    @Override
    public void error(String message) {
        logger.severe(message);
    }

    @Override
    public void error(Throwable thrown) {
        logger.log(Level.SEVERE, thrown.getMessage(), thrown);
    }

    @Override
    public void error(String message, Throwable thrown) {
        logger.log(Level.SEVERE, message, thrown);
    }

}
