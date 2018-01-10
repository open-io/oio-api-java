/**
 * 
 */
package io.openio.sds.exceptions;

/**
 * Exception raised when a request deadline has been reached, and we detected it client-side.
 * 
 * @author Florent Vennetier
 *
 */
public class DeadlineReachedException extends OioException {

    private static final long serialVersionUID = -3825904433261832246L;

    /**
     * @param message A message for the exception
     */
    public DeadlineReachedException(String message) {
        super(message);
    }

    /**
     * 
     * @param excess by how much time the deadline has been exceeded (milliseconds) 
     */
    public DeadlineReachedException(int excess) {
        super("Request deadline exceeded by " + excess + " milliseconds");
    }

    public DeadlineReachedException() {
        super("Request deadline reached");
    }
}