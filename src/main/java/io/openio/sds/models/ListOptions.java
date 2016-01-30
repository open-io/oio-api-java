package io.openio.sds.models;

import io.openio.sds.common.MoreObjects;

public class ListOptions {

    private int limit = 0;
    private String delimiter;
    private String prefix;
    private String marker;

    public ListOptions() {
    }

    public Integer limit() {
        return limit;
    }

    public ListOptions limit(int limit) {
        this.limit = limit;
        return this;
    }

    public String delimiter() {
        return delimiter;
    }

    public ListOptions delimiter(String delimiter) {
        this.delimiter = delimiter;
        return this;
    }

    public String prefix() {
        return prefix;
    }

    public ListOptions prefix(String prefix) {
        this.prefix = prefix;
        return this;
    }

    public String marker() {
        return marker;
    }

    public ListOptions marker(String marker) {
        this.marker = marker;
        return this;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("prefix", prefix)
                .add("delimiter", delimiter)
                .add("marker", marker)
                .add("limit", limit)
                .toString();
    }
}