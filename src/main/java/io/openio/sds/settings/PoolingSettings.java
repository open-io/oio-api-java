package io.openio.sds.settings;

/**
 * 
 * @author Christopher Dedeurwaerder
 *
 */
public class PoolingSettings {

    private Boolean enabled = true;
    private Long cleanDelay = 1L;
    private Long cleanRate = 1L;
    private Integer maxConnPerTarget = 1024;
    private Integer maxConnTotal = 8192;
    private Integer maxConnWait = 5000;
    private Integer socketIdleTimeout = 3000;
    

    public PoolingSettings() {
    }

    public Boolean enabled() {
        return enabled;
    }

    public PoolingSettings enabled(Boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    public Long cleanDelay() {
        return cleanDelay;
    }

    public PoolingSettings cleanDelay(Long cleanDelay) {
        this.cleanDelay = cleanDelay;
        return this;
    }

    public Long cleanRate() {
        return cleanRate;
    }

    public PoolingSettings cleanRate(Long cleanRate) {
        this.cleanRate = cleanRate;
        return this;
    }

    public Integer socketIdleTimeout() {
        return socketIdleTimeout;
    }

    public PoolingSettings socketIdleTimeout(Integer socketIdleTimeout) {
        this.socketIdleTimeout = socketIdleTimeout;
        return this;
    }

    public Integer maxConnPerTarget() {
        return maxConnPerTarget;
    }

    public PoolingSettings maxConnPerTarget(Integer maxConnPerTarget) {
        this.maxConnPerTarget = maxConnPerTarget;
        return this;
    }

    public Integer maxConnTotal() {
        return maxConnTotal;
    }

    public PoolingSettings maxConnTotal(Integer maxConnTotal) {
        this.maxConnTotal = maxConnTotal;
        return this;
    }

    public Integer maxConnWait() {
        return maxConnWait;
    }

    public PoolingSettings maxConnWait(Integer maxConnWait) {
        this.maxConnWait = maxConnWait;
        return this;
    }

}
