package io.openio.sds.models;

import io.openio.sds.common.MoreObjects;

public class ChunkInfo {

    public ChunkInfo() {
    }

    private String url;
    private String real_url;
    private Long size;
    private String hash;
    private Position pos;

    public String url() {
        return url;
    }

    public String hash() {
        return hash;
    }

    public Position pos() {
        return pos;
    }

    public Long size() {
        return size;
    }

    public ChunkInfo url(String url) {
        this.url = url;
        return this;
    }

    public ChunkInfo real_url(String real_url) {
        this.real_url = real_url;
        return this;
    }

    public ChunkInfo size(Long size) {
        this.size = size;
        return this;
    }

    public ChunkInfo hash(String hash) {
        this.hash = hash;
        return this;
    }

    public ChunkInfo pos(Position pos) {
        this.pos = pos;
        return this;
    }

    public String id(){
        return url.substring(url.lastIndexOf("/") + 1);
    }

    public String finalUrl() {
        if (real_url != null && real_url.length() > 0) {
            return real_url;
        }
        return url;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .omitNullValues()
                .add("url", url)
                .add("real_url", real_url)
                .add("size", size)
                .add("hash", hash)
                .add("pos", pos)
                .toString();
    }
}
