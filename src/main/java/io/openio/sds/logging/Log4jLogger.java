package io.openio.sds.logging;

import org.apache.log4j.Logger;

public class Log4jLogger implements SdsLogger {

    private Logger logger;

    Log4jLogger(Class<?> c) {
        logger = Logger.getLogger(c);
    }

    @Override
    public void trace(String message) {
        logger.trace(message);
    }

    @Override
    public void trace(Throwable thrown) {
        logger.trace(thrown);
    }

    @Override
    public void trace(String message, Throwable thrown) {
        logger.trace(message, thrown);
    }

    @Override
    public boolean isTraceEnabled() {
        return logger.isTraceEnabled();
    }

    @Override
    public void debug(String message) {
        logger.debug(message);
    }
    
    @Override
    public void debug(String message, Throwable thrown) {
        logger.debug(message, thrown);
    }

    @Override
    public boolean isDebugEnabled() {
        return logger.isDebugEnabled();
    }

    @Override
    public void info(String message) {
        logger.info(message);
    }

    @Override
    public void warn(String message) {
        logger.warn(message);
    }

    @Override
    public void warn(Throwable thrown) {
        logger.warn(thrown);
    }

    @Override
    public void warn(String message, Throwable thrown) {
        logger.warn(message, thrown);
    }

    @Override
    public void error(String message) {
        logger.error(message);
    }

    @Override
    public void error(Throwable thrown) {
        logger.error(thrown);
    }

    @Override
    public void error(String message, Throwable thrown) {
        logger.error(message, thrown);
    }
}
