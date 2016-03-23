package io.openio.sds.pool;

/**
 * Interface to define item manageable by a {@link Pool}
 * @author Christopher Dedeurwaerder
 *
 */
public interface Poolable {

    /**
     * Returns {@code true} if the item is reusable and so could be set in pool,
     * {@code false} otherwise
     * 
     * @return {@code true} if the item is reusable and so could be set in pool,
     *         {@code false} otherwise
     */
    public boolean reusable();

    /**
     * Returns the timestamp matching this item last usage
     * @return the timestamp matching this item last usage
     */
    public long lastUsage();

    /**
     *  Specifies the last usage timestamp
     * @param lastUsage the value to set
     */
    public void lastUsage(long lastUsage);
    
    /**
     * 
     */
    public void markUnpooled();

}
