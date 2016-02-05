package io.openio.sds.models;

public class BeansRequest {

    private long size;

    public BeansRequest() {

    }

    public long size() {
        return size;
    }

    public BeansRequest size(long size) {
        this.size = size;
        return this;
    }
}
