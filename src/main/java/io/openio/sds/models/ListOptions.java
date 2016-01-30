package io.openio.sds.models;

import io.openio.sds.common.MoreObjects;

public class ListOptions {
    private int limit;
    private String delimiter;
    private String prefix;
    private String marker;

    public int getLimit() {
        return limit;
    }

    public String getDelimiter() {
        return delimiter;
    }

    public String getPrefix() {
        return prefix;
    }

    public String getMarker() {
        return marker;
    }

    private ListOptions(int limit, String prefix, String delimiter,
            String marker) {
        this.limit = limit;
        this.prefix = prefix;
        this.delimiter = delimiter;
        this.marker = marker;
    }

    public static class ListOptionsBuilder {

        private int limit;
        private String delimiter;
        private String prefix;
        private String marker;

        public ListOptionsBuilder limit(int limit) {
            this.limit = limit;
            return this;
        }

        public ListOptionsBuilder marker(String marker) {
            this.marker = marker;
            return this;
        }

        public ListOptionsBuilder prefix(String prefix) {
            this.prefix = prefix;
            return this;
        }

        public ListOptionsBuilder delimiter(String delimiter) {
            this.delimiter = delimiter;
            return this;
        }

        public ListOptions build() {
            return new ListOptions(limit, prefix, delimiter, marker);
        }
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