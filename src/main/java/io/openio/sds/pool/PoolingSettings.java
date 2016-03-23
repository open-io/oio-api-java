package io.openio.sds.pool;

/**
 * 
 * @author Christopher Dedeurwaerder
 *
 */
public class PoolingSettings {

    private Boolean enabled = true;
    private Long cleanDelay = 1L;
    private Long cleanRate = 1L;
    private Integer maxForEach = 1024;
    private Integer maxTotal = 8192;
    private Integer maxWait = 5000;
    private Integer idleTimeout = 3000;

    public PoolingSettings() {
    }

    /**
     * Returns {@code true} if the socket pooling is enabled, {@code false}
     * otherwise
     * 
     * @return {@code true} if the socket pooling is enabled, {@code false}
     *         otherwise
     */
    public Boolean enabled() {
        return enabled;
    }

    /**
     * Specifies if the socket pooling is enabled
     * 
     * @param enabled
     *            the value to set
     * @return this
     */
    public PoolingSettings enabled(Boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    /**
     * Returns the idle socket first cleanup delay in seconds
     * 
     * @return the idle socket first cleanup delay in seconds
     */
    public Long cleanDelay() {
        return cleanDelay;
    }

    /**
     * Specifies the idle socket first cleanup delay in seconds
     * 
     * @param cleanDelay
     *            the value to set
     * @return this
     */
    public PoolingSettings cleanDelay(Long cleanDelay) {
        this.cleanDelay = cleanDelay;
        return this;
    }

    /**
     * Returns the idle socket cleanup frequency is seconds
     * 
     * @return the idle socket cleanup frequency is seconds
     */
    public Long cleanRate() {
        return cleanRate;
    }

    /**
     * Specifies the idle socket cleanup frequency is seconds
     * 
     * @param cleanRate
     *            the value to set
     * @return this
     */
    public PoolingSettings cleanRate(Long cleanRate) {
        this.cleanRate = cleanRate;
        return this;
    }

    /**
     * Returns the socket idle timeout in milliseconds
     * 
     * @return the socket idle timeout in milliseconds
     */
    public Integer idleTimeout() {
        return idleTimeout;
    }

    /**
     * Specifies the socket idle timeout in milliseconds
     * 
     * @param socketIdleTimeout
     *            the socket idle timeout in milliseconds
     * @return this
     */
    public PoolingSettings idleTimeout(Integer idleTimeout) {
        this.idleTimeout = idleTimeout;
        return this;
    }

    /**
     * Returns the max leased socket allowed per route
     * 
     * @return the max leased item allowed by pool
     */
    public Integer maxForEach() {
        return maxForEach;
    }

    /**
     * Specifies the max leased items in each pool in a pool group
     * 
     * @param maxForEach
     *            the value to set
     * @return this
     */
    public PoolingSettings maxForEach(Integer maxForEach) {
        this.maxForEach = maxForEach;
        return this;
    }

    /**
     * Returns the max leased socket allowed in total
     * 
     * @return the max leased socket allowed in total
     */
    public Integer maxTotal() {
        return maxTotal;
    }

    /**
     * Specifies the max leased socket allowed in total
     * 
     * @param maxTotal the value to set
     * @return this
     */
    public PoolingSettings maxTotal(Integer maxTotal) {
        this.maxTotal = maxTotal;
        return this;
    }

    /**
     * Returns the max time allowed to wait for a connection to become available
     * (in milliseconds)
     * 
     * @return the max time allowed to wait for a connection to become available
     *         (in milliseconds)
     */
    public Integer maxWait() {
        return maxWait;
    }

    /**
     * Specifies the max time allowed to wait for a connection to become
     * available
     * 
     * @param maxWait
     *            the value to set
     * @return this
     */
    public PoolingSettings maxWait(Integer maxWait) {
        this.maxWait = maxWait;
        return this;
    }

}
