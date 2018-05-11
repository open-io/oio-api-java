package io.openio.sds.common;

import io.openio.sds.exceptions.DeadlineReachedException;

/**
 * Various utility methods to manage deadlines and timeouts.
 *
 * @author Florent Vennetier
 *
 */
public class DeadlineManager {

    public interface ClockSource {
        /**
         *
         * @return the current time in milliseconds
         */
        int now();
    }

    private class SystemClockSource implements ClockSource {

        public SystemClockSource() {
        }

        @Override
        public int now() {
            return (int) (System.nanoTime() / 1000000);
        }
    }


    private static volatile DeadlineManager instance = null;

    private ClockSource clock;

    private DeadlineManager() {
        useSystemClockSource();
    }

    /**
     * Get the single instance of {@link DeadlineManager}.
     * @return the single instance of {@link DeadlineManager}
     */
    public static DeadlineManager instance() {
        if (instance == null) {
            synchronized (DeadlineManager.class) {
                if (instance == null) {
                    instance = new DeadlineManager();
                }
            }
        }
        return instance;
    }

    /**
     * Force the DeadlineManager to use a mocked {@link ClockSource}
     * @param clockSource The new clock source to use
     */
    public void useMockedClockSource(ClockSource clockSource) {
        this.clock = clockSource;
    }

    /**
     * Force the DeadlineManager to use the system monotonic clock source.
     */
    public void useSystemClockSource() {
        this.clock = this.new SystemClockSource();
    }

    /**
     * Raise an exception when a deadline has been reached.
     * 
     * @param deadline the deadline, in milliseconds
     * @throws DeadlineReachedException when the deadline has been reached
     */
    public void checkDeadline(int deadline) throws DeadlineReachedException {
        checkDeadline(deadline, now());
    }

    /**
     * Raise an exception when a deadline has been reached.
     * 
     * @param deadline the deadline, in milliseconds
     * @param refTime the reference time for the deadline, in milliseconds
     * @throws DeadlineReachedException when the deadline has been reached
     */
    public void checkDeadline(int deadline, int refTime) throws DeadlineReachedException {
        int diff = deadline - refTime;
        if (diff <= 0) {
            throw new DeadlineReachedException(-diff);
        }
    }

    /**
     * Convert a deadline to a timeout.
     *
     * @param deadline the deadline to convert, in milliseconds
     * @return a timeout in milliseconds
     */
    public int deadlineToTimeout(int deadline) {
        return deadline - now();
    }

    /**
     * Convert a deadline to a timeout.
     *
     * @param deadline the deadline to convert, in milliseconds
     * @param refTime the reference time for the deadline, in monotonic milliseconds
     * @return a timeout in milliseconds
     */
    public int deadlineToTimeout(int deadline, int refTime) {
        return deadline - refTime;
    }

    /**
     * Get the current monotonic time in milliseconds.
     *
     * @return The current monotonic time in milliseconds
     */
    public int now() {
        return clock.now();
    }

    /**
     * Convert a timeout to a deadline.
     *
     * @param timeout the timeout in milliseconds
     * @return a deadline in monotonic milliseconds
     */
    public int timeoutToDeadline(int timeout) {
        return timeoutToDeadline(timeout, now());
    }

    /**
     * Convert a timeout to a deadline.
     *
     * @param timeout the timeout in milliseconds
     * @param refTime the reference time for the deadline, in milliseconds
     * @return a deadline in monotonic milliseconds
     */
    public int timeoutToDeadline(int timeout, int refTime) {
        return refTime + timeout;
    }
}
