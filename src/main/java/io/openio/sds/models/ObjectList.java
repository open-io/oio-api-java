package io.openio.sds.models;

import java.util.List;

/**
 * View of object list inside a container. The listing is partial if
 * {@code truncated} field is set to {@code true}.
 *
 *
 */
public class ObjectList {

    private List<ObjectView> objects;
    private List<String> prefixes;
    private ListOptions listOptions;
    private boolean truncated;

    /**
     * 
     */
    public ObjectList() {
    }

    /**
     * List of {@link ObjectView} found in the container
     * 
     * @return a list of {@link ObjectView}
     */
    public List<ObjectView> objects() {
        return objects;
    }

    /**
     * Returns {@code true} if this listing does not contains all objects
     * matching the specified {@code ListOptions}, {@code false} otherwise
     * 
     * @return {@code true} if this listing does not contains all objects
     *         matching the specified {@code ListOptions}, {@code false}
     *         otherwise
     */
    public boolean truncated() {
        return truncated;
    }

    /**
     * Returns the prefixes
     * 
     * @return the prefixes
     */
    public List<String> prefixes() {
        return prefixes;
    }

    /**
     * Returns the options
     * 
     * @return the options
     */
    public ListOptions listOptions() {
        return listOptions;
    }

    /**
     * Specifies the listing prefixes
     * 
     * @param prefixes
     *            the value to set
     * @return this
     */
    public ObjectList prefixes(List<String> prefixes) {
        this.prefixes = prefixes;
        return this;
    }

    /**
     * Specifies the listing options
     * 
     * @param listOptions
     *            the value to set
     * @return this
     */
    public ObjectList listOptions(ListOptions listOptions) {
        this.listOptions = listOptions;
        return this;
    }

    /**
     * Specifies if the listing is truncated
     * 
     * @param truncated
     *            the value to set
     * @return this
     */
    public ObjectList truncated(boolean truncated) {
        this.truncated = truncated;
        return this;
    }

    /**
     * Representation of an object inside a container.
     * 
     * Here is a description of all fields meaning.
     * <ul>
     * <li>Name: the name of the object</li>
     * <li>Version: the current object version</li>
     * <li>Creation time: Timestamp where the object was created</li>
     * <li>System metadata: Meta informations about the object</li>
     * <li>deleted: {@code true} if the object is deleted, {@code false}
     * otherwise</li>
     * <li>policy: the storage policy used to store the object</li>
     * <li>hash: the object data md5sum</li>
     * <li>size: the total size of the object</li>
     * </ul>
     */
    public static class ObjectView {

        private String name;
        private String ver;
        private String ctime;
        // @SerializedName("system_metadata")
        private String sysmd;
        private Boolean deleted;
        private String policy;
        private String hash;
        private Long size;

        public ObjectView() {
        }

        /**
         * Returns the name of the object
         * 
         * @return the name of the object
         */
        public String name() {
            return name;
        }

        /**
         * Specifies the name of the object
         * 
         * @param name
         *            the value to set
         * @return this
         */
        public ObjectView name(String name) {
            this.name = name;
            return this;
        }

        /**
         * Returns the object version
         * 
         * @return the object version
         */
        public String version() {
            return ver;
        }

        /**
         * Specifies the object version
         * 
         * @param version
         *            the version to set
         * @return this
         */
        public ObjectView version(String version) {
            this.ver = version;
            return this;
        }

        /**
         * Returns the object creation time
         * 
         * @return the object creation time
         */
        public String ctime() {
            return ctime;
        }

        /**
         * Specifies the object creation time
         * 
         * @param ctime
         *            the value to set
         * @return this
         */
        public ObjectView ctime(String ctime) {
            this.ctime = ctime;
            return this;
        }

        /**
         * Returns the system metadata
         * 
         * @return the system metadata
         */
        public String sysmd() {
            return sysmd;
        }

        /**
         * Specifies the object system metadata
         * 
         * @param sysmd
         *            the metadata to set
         * @return this
         */
        public ObjectView sysmd(String sysmd) {
            this.sysmd = sysmd;
            return this;
        }

        /**
         * Returns {@code true} if the object is deleted, {@code false}
         * otherwise
         * 
         * @return {@code true} if the object is deleted, {@code false}
         *         otherwise
         */
        public Boolean deleted() {
            return deleted;
        }

        /**
         * Specifies if content is deleted or not
         * 
         * @param deleted
         *            the value to set
         * @return this
         */
        public ObjectView deleted(Boolean deleted) {
            this.deleted = deleted;
            return this;
        }

        /**
         * Returns the policy
         * 
         * @return the policy
         */
        public String policy() {
            return policy;
        }

        /**
         * Specifies the policy
         * 
         * @param policy
         *            the policy to set
         * @return this
         */
        public ObjectView policy(String policy) {
            this.policy = policy;
            return this;
        }

        /**
         * Returns the hash
         * 
         * @return the hash
         */
        public String hash() {
            return hash;
        }

        /**
         * Specifies the hash
         * 
         * @param hash
         *            the hash to set
         * @return this
         */
        public ObjectView hash(String hash) {
            this.hash = hash;
            return this;
        }

        /**
         * Returns the size
         * 
         * @return the size
         */
        public Long size() {
            return size;
        }

        /**
         * Specifies the size
         * 
         * @param size
         *            the size to set
         * @return this
         */
        public ObjectView size(Long size) {
            this.size = size;
            return this;
        }

    }
}