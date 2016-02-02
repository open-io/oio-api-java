package io.openio.sds.models;

import java.util.Map;

import io.openio.sds.common.MoreObjects;

/**
 * 
 * @author Christopher Dedeurwaerder
 *
 */
public class ServiceInfo {

    private String addr;
    private Integer score;
    private Map<String, String> tags;
    
    public ServiceInfo() {
    }

    public String addr() {
        return addr;
    }

    public ServiceInfo addr(String addr) {
        this.addr = addr;
        return this;
    }

    public Integer score() {
        return score;
    }

    public ServiceInfo score(Integer score) {
        this.score = score;
        return this;
    }

    public Map<String, String> tags() {
        return tags;
    }

    public ServiceInfo tags(Map<String, String> tags) {
        this.tags = tags;
        return this;
    }
    
    @Override
    public String toString(){
        return MoreObjects.toStringHelper(this)
                .add("addr", addr)
                .add("score", score)
                .add("tags", tags)
                .toString();
    }
    
}
