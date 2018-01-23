package io.openio.sds;

import static io.openio.sds.common.Check.checkArgument;
import io.openio.sds.common.DeadlineManager;
import io.openio.sds.common.IdGen;

/**
 * Generic parameters and context for all OpenIO SDS requests,
 * including a request ID, a timeout (or deadline), etc.
 *
 * @author Florent Vennetier
 *
 */
public class RequestContext {

    private DeadlineManager dm;

    private String reqId = null;
    private int reqStart = -1;
    private int rawTimeout = -1;
    private int deadline = -1;

    /**
     * Build a new {@link RequestContext} with a default 30s timeout.
     */
    public RequestContext() {
        this.dm = DeadlineManager.instance();
    }

    /**
     * Copy constructor. Build a new {@link RequestContext} from another one.
     * This will keep the request ID, the timeout and the deadline (if there is one).
     *
     * @param src The {@link RequestContext} to copy.
     */
    public RequestContext(RequestContext src) {
        this.withRequestId(src.requestId());
        this.deadline = src.deadline;
        this.rawTimeout = src.rawTimeout;
    }

    /* -- Request IDs ----------------------------------------------------- */

    /**
     * Ensure this request has an ID and it is at least 8 characters.
     *
     * @return this
     */
    public RequestContext ensureRequestId() {
        if (this.reqId == null)
            this.reqId = IdGen.requestId();
        else if (this.reqId.length() < 8)
            this.reqId += IdGen.requestId().substring(8 - this.reqId.length());
        return this;
    }

    /**
     * Get the request ID.
     *
     * @return the request ID (auto-generated if not set)
     */
    public String requestId() {
        return this.ensureRequestId().reqId;
    }

    /**
     * Set a request ID.
     *
     * @param requestId
     *            a request ID string. If is null, it will be auto-generated. If
     *            it is less than 8 characters, it will be suffixed.
     * @return this
     */
    public RequestContext withRequestId(String requestId) {
        this.reqId = requestId;
        return this;
    }

    /* -- Deadlines and timeouts ------------------------------------------ */

    /**
     * Compute a deadline from the {@link #timeout()}. This also means that the
     * timeout will be recomputed for each unique sub-request resulting from an
     * API call. If this request has started, compute the timeout from the start,
     * otherwise compute it from now.
     *
     * @return {@code this}
     */
    public RequestContext computeDeadline() {
        if (!this.hasTimeout())
            throw new IllegalStateException("No timeout has been set, cannot compute deadline");
        if (this.hasStarted())
            this.deadline = dm.timeoutToDeadline(this.rawTimeout, this.reqStart);
        else
            this.deadline = dm.timeoutToDeadline(this.rawTimeout);
        return this;
    }

    /**
     * Get the deadline for this request.
     *
     * If a timeout has been set ({@link #withTimeout(int)}) but no
     * deadline, {@link #computeDeadline()} will be called.
     *
     * @return the overall deadline for this request, in milliseconds
     */
    public int deadline() {
        if (!hasDeadline()) {
            startTiming();
            computeDeadline();
        }
        return this.deadline;
    }

    /**
     * Tell the time elapsed since this request has started.
     *
     * @return the duration since this request has started, in milliseconds
     */
    public int elapsed() {
        if (!this.hasStarted())
            return 0;
        return this.dm.now() - this.reqStart;
    }

    /**
     * Tell whether this request has a deadline or not. This will always return
     * {@code true} after {@link #computeDeadline()} or {@link #deadline()} has been
     * called.
     *
     * @return {@code true} if this request has a deadline.
     */
    public boolean hasDeadline() {
        return this.deadline >= 0;
    }

    /**
     * Tell if this request has started.
     *
     * @return {@code true} if this request has started.
     */
    public boolean hasStarted() {
        return this.reqStart >= 0;
    }

    /**
     * Tell whether this request has a timeout or not.
     *
     * @return {@code true} if this request has timeout.
     */
    public boolean hasTimeout() {
        return this.rawTimeout >= 0;
    }

    /**
     * Reset the deadline set on this request. This must be called when reusing
     * a context only for its timeout and request ID.
     *
     * @return {@code this}
     */
    public RequestContext resetDeadline() {
        this.deadline = -1;
        return this;
    }

    /**
     * Start timing this request.
     *
     * @return this
     */
    public RequestContext startTiming() {
        this.reqStart = dm.now();
        return this;
    }

    /**
     * Get the timeout for the request.
     *
     * If {@link #hasDeadline()} returns {@code true}, successive calls to this
     * method will return decreasing values, and negative values when the
     * deadline has been exceeded.
     *
     * @return the timeout for this request, in milliseconds
     */
    public int timeout() {
        if (this.hasDeadline())
            return this.dm.deadlineToTimeout(this.deadline);
        return this.rawTimeout;
    }

    /**
     * Set a deadline on the whole request.
     *
     * This will reset any previous timeout set with {@link #withTimeout(int)} to the duration
     * from now to the deadline.
     *
     * @param deadline the deadline in milliseconds
     * @return this
     */
    public RequestContext withDeadline(int deadline) {
        checkArgument(deadline >= 0, "deadline cannot be negative");
        this.deadline = deadline;
        this.rawTimeout = this.dm.deadlineToTimeout(deadline);
        return this;
    }

    /**
     * Set a timeout for each unique sub-request resulting from an API call.
     *
     * This will reset any previous deadline set with {@link #withDeadline(int)}.
     *
     * The timeout will be used to compute the deadline when
     * {@link #startTiming()} or {@link #deadline()} is called.
     *
     * @param timeout
     *            a timeout in milliseconds
     * @return this
     */
    public RequestContext withTimeout(int timeout) {
        checkArgument(timeout > 0, "timeout cannot be negative");
        this.rawTimeout = timeout;
        this.resetDeadline();
        return this;
    }
}
