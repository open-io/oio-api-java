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
     * 
     * @return
     */
    public List<String> prefixes() {
        return prefixes;
    }

    /**
     * 
     * @return
     */
    public ListOptions listOptions() {
        return listOptions;
    }

    /**
     * 
     * @param prefixes
     */
    public void prefixes(List<String> prefixes) {
        this.prefixes = prefixes;
    }

    /**
     * 
     * @param listOptions
     */
    public void listOptions(ListOptions listOptions) {
        this.listOptions = listOptions;
    }

    /**
     * 
     * @param truncated
     */
    public void truncated(boolean truncated) {
        this.truncated = truncated;
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
         * 
         * @return
         */
        public String name() {
            return name;
        }

        /**
         * 
         * @param name
         * @return
         */
        public ObjectView name(String name) {
            this.name = name;
            return this;
        }

        /**
         * 
         * @return
         */
        public String version() {
            return ver;
        }

        /**
         * 
         * @param version
         * @return
         */
        public ObjectView version(String version) {
            this.ver = version;
            return this;
        }

        /**
         * 
         * @return
         */
        public String ctime() {
            return ctime;
        }

        /**
         * 
         * @param ctime
         * @return
         */
        public ObjectView ctime(String ctime) {
            this.ctime = ctime;
            return this;
        }

        /**
         * 
         * @return
         */
        public String sysmd() {
            return sysmd;
        }

        /**
         * 
         * @param sysmd
         * @return
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
         * 
         * @param deleted
         * @return
         */
        public ObjectView deleted(Boolean deleted) {
            this.deleted = deleted;
            return this;
        }

        /**
         * 
         * @return
         */
        public String policy() {
            return policy;
        }

        /**
         * 
         * @param policy
         * @return
         */
        public ObjectView policy(String policy) {
            this.policy = policy;
            return this;
        }

        /**
         * 
         * @return
         */
        public String hash() {
            return hash;
        }

        /**
         * 
         * @param hash
         * @return
         */
        public ObjectView hash(String hash) {
            this.hash = hash;
            return this;
        }

        /**
         * 
         * @return
         */
        public Long size() {
            return size;
        }

        /**
         * 
         * @param size
         * @return
         */
        public ObjectView size(Long size) {
            this.size = size;
            return this;
        }

    }
}