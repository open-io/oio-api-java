package io.openio.sds.models;

import java.util.Map;

public class ObjectCreationOptions {

    private Long version;
    private Map<String, String> properties;
    private String policy;
    private String mimeType;
    private boolean simulateVersioning = false;

    public Long version() {
        return version;
    }

    public ObjectCreationOptions version(Long version) {
        this.version = version;
        return this;
    }

    public Map<String, String> properties() {
        return properties;
    }

    public ObjectCreationOptions properties(Map<String, String> properties) {
        this.properties = properties;
        return this;
    }

    public String policy() {
        return policy;
    }

    public ObjectCreationOptions policy(String policy) {
        this.policy = policy;
        return this;
    }

    public String mimeType() {
        return mimeType;
    }

    public ObjectCreationOptions mimeType(String mimeType) {
        this.mimeType = mimeType;
        return this;
    }

    public boolean simulateVersioning() {
        return simulateVersioning;
    }

    public ObjectCreationOptions simulateVersioning(boolean simulateVersioning) {
        this.simulateVersioning = simulateVersioning;
        return this;
    }

}
