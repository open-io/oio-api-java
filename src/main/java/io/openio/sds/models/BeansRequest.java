package io.openio.sds.models;

public class BeansRequest {

    private long size;
    private String policy;

    public BeansRequest() {

    }

    public long size() {
        return size;
    }

    public BeansRequest size(long size) {
        this.size = size;
        return this;
    }

    public String policy() {
        return policy;
    }

    public BeansRequest policy(String policy) {
        this.policy = policy;
        return this;
    }

}
