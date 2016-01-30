package io.openio.sds.models;

import java.util.List;

/**
 *
 *
 *
 */
public class ObjectList {
    private List<ObjectView> objects;
    private List<String> prefixes;
    private ListOptions listOptions;
    private boolean truncated;

    public ObjectList() {
    }

    public List<ObjectView> objects() {
        return objects;
    }

    public boolean isTruncated() {
        return truncated;
    }

    public List<String> prefixes() {
        return prefixes;
    }

    public ListOptions listOptions() {
        return listOptions;
    }

    public void setPrefixes(List<String> prefixes) {
        this.prefixes = prefixes;
    }

    public void setListOptions(ListOptions listOptions) {
        this.listOptions = listOptions;
    }

    public void setTruncated(boolean truncated) {
        this.truncated = truncated;
    }

    /**
     * 
     *
     *
     */
    public static class ObjectView {
        private String name;
        private String ver;
        private String ctime;
        //@SerializedName("system_metadata")
        private String sysmd;
        private Boolean deleted;
        private String policy;
        private String hash;
        private Long size;

        public String name() {
            return name;
        }

        public ObjectView name(String name) {
            this.name = name;
            return this;
        }

        public String version() {
            return ver;
        }

        public ObjectView version(String version) {
            this.ver = version;
            return this;
        }

        public String ctime() {
            return ctime;
        }

        public ObjectView ctime(String ctime) {
            this.ctime = ctime;
            return this;
        }

        public String sysmd() {
            return sysmd;
        }

        public ObjectView sysmd(String sysmd) {
            this.sysmd = sysmd;
            return this;
        }

        public Boolean deleted() {
            return deleted;
        }

        public ObjectView deleted(Boolean deleted) {
            this.deleted = deleted;
            return this;
        }

        public String policy() {
            return policy;
        }

        public ObjectView policy(String policy) {
            this.policy = policy;
            return this;
        }

        public String hash() {
            return hash;
        }

        public ObjectView hash(String hash) {
            this.hash = hash;
            return this;
        }

        public Long size() {
            return size;
        }

        public ObjectView size(Long size) {
            this.size = size;
            return this;
        }

    }
}