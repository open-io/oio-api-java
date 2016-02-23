package io.openio.sds.models;

import java.util.List;

import io.openio.sds.common.MoreObjects;

/**
 * 
 * @author Christopher Dedeurwaerder
 *
 */
public class ReferenceInfo {

    private List<LinkedServiceInfo> dir;
    private List<LinkedServiceInfo> srv;

    public ReferenceInfo() {
    }

    public List<LinkedServiceInfo> dir() {
        return dir;
    }

    public ReferenceInfo dir(List<LinkedServiceInfo> dir) {
        this.dir = dir;
        return this;
    }

    public List<LinkedServiceInfo> srv() {
        return srv;
    }

    public ReferenceInfo srv(List<LinkedServiceInfo> srv) {
        this.srv = srv;
        return this;
    }
    
    @Override
    public String toString(){
        return MoreObjects.toStringHelper(this)
                .add("dir", dir)
                .add("srv", srv)
                .toString();
    }

}
