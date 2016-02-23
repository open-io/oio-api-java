package io.openio.sds.models;

import io.openio.sds.common.MoreObjects;

public class LinkedServiceInfo {

    private Integer seq;
    private String type;
    private String host;
    private String args;

    public LinkedServiceInfo() {
    }

    public Integer seq() {
        return seq;
    }

    public LinkedServiceInfo seq(Integer seq) {
        this.seq = seq;
        return this;
    }

    public String type() {
        return type;
    }

    public LinkedServiceInfo type(String type) {
        this.type = type;
        return this;
    }

    public String host() {
        return host;
    }

    public LinkedServiceInfo host(String host) {
        this.host = host;
        return this;
    }

    public String args() {
        return args;
    }

    public LinkedServiceInfo args(String args) {
        this.args = args;
        return this;
    }
    
    @Override
    public String toString(){
        return MoreObjects.toStringHelper(this)
                .add("seq", seq)
                .add("type", type)
                .add("host", host)
                .add("args", args)
                .toString();
    }

}
