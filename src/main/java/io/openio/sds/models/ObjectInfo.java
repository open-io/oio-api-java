package io.openio.sds.models;

import static io.openio.sds.common.OioConstants.OIO_CHARSET;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.openio.sds.RequestContext;
import io.openio.sds.common.Hash;
import io.openio.sds.common.MoreObjects;
import io.openio.sds.common.OioConstants;

public class ObjectInfo {

    private OioUrl url;
    private String oid;
    private Long ctime;
    private Boolean deleted = false;
    private String policy;
    private String hash;
    private String hashMethod;
    private String chunkMethod;
    private Long size;
    private Long version;
    private String mtype;
    private Map<String, String> properties;
    private List<ChunkInfo> chunks;
    private ECInfo ecinfo;
    private RequestContext reqCtx;

    private transient Map<Integer, List<ChunkInfo>> sortedChunks;

    private static final Comparator<ChunkInfo> comparator = new Comparator<ChunkInfo>() {

        @Override
        public int compare(ChunkInfo c1, ChunkInfo c2) {
            return c1.pos()
                    .compare(c2.pos());
        }
    };

    public ObjectInfo() {
        // TODO: content hash
        this.properties = new HashMap<String, String>();
        this.hash = Hash.md5()
                .hashBytes("".getBytes(OIO_CHARSET))
                .toString();
    }

    public List<ChunkInfo> chunks() {
        return chunks;
    }

    public Map<Integer, List<ChunkInfo>> sortedChunks() {
        return sortedChunks;
    }

    public ObjectInfo chunks(List<ChunkInfo> chunks) {
        this.sortedChunks = sortChunks(chunks);
        this.chunks = chunks;
        return this;
    }

    public ObjectInfo size(Long size) {
        this.size = size;
        return this;
    }

    public ObjectInfo hash(String hash) {
        this.hash = hash;
        return this;
    }

    public ObjectInfo url(OioUrl url) {
        this.url = url;
        return this;
    }

    public OioUrl url() {
        return url;
    }

    public String oid() {
        return oid;
    }

    public ObjectInfo oid(String oid) {
        this.oid = oid;
        return this;
    }

    public String hash() {
        return hash;
    }

    public Long size() {
        return size;
    }

    public String policy() {
        return policy;
    }

    public ObjectInfo policy(String policy) {
        this.policy = policy;
        return this;
    }

    public boolean deleted() {
        return deleted;
    }

    public Long ctime() {
        return ctime;
    }

    public ObjectInfo ctime(Long ctime) {
        this.ctime = ctime;
        return this;
    }

    public String hashMethod() {
        return hashMethod;
    }

    public ObjectInfo hashMethod(String hashMethod) {
        this.hashMethod = hashMethod;
        return this;
    }

    public String chunkMethod() {
        return chunkMethod;
    }

    public ObjectInfo chunkMethod(String chunkMethod) {
        this.chunkMethod = chunkMethod;
        this.ecinfo = ECInfo.fromString(chunkMethod);
        return this;
    }

    public Long version() {
        return version;
    }

    public ObjectInfo version(Long version) {
        this.version = version;
        return this;
    }

    public String mtype() {
        return mtype;
    }

    public ObjectInfo mtype(String mtype) {
        this.mtype = mtype;
        return this;
    }

    public Integer nbchunks() {
        return isEC() ? chunks.size() : sortedChunks.size();
    }

    public Long chunksize(Integer pos) {
        return sortedChunks.get(pos).get(0).size();
    }

    public Map<String, String> properties() {
        return properties;
    }

    public ObjectInfo properties(Map<String, String> properties) {
        this.properties = properties;
        return this;
    }

    public ECInfo ecinfo(){
    	return ecinfo;
    }

    public boolean isEC(){
    	return null != ecinfo;
    }

    public int metachunksize(int pos) {
        if (!isEC())
            return -1;
        int maxmcsize = (int) (ecinfo.k() * sortedChunks.get(pos).get(0).size());
        int remaining = (int) (size - (pos * maxmcsize));
        return Math.min(maxmcsize, remaining);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .omitNullValues()
                .add("url", url)
                .add("ctime", ctime)
                .add("mime-type", mtype)
                .add("deleted", deleted)
                .add("policy", policy)
                .add("hash", hash)
                .add("hash-method", hashMethod)
                .add("chunk-method", chunkMethod)
                .add("size", size)
                .add("version", version)
                .add("props", properties)
                .add("chunks", chunks)
                .toString();
    }

    public RequestContext requestContext() {
        return this.reqCtx;
    }

    /**
     * Set the context of the request that lead to this object's creation.
     *
     * @param reqCtx
     *            the request context
     * @return {@code this}
     */
    public ObjectInfo withRequestContext(RequestContext reqCtx) {
        this.reqCtx = reqCtx;
        return this;
    }

    /* -- INTERNAL -- */

    private Map<Integer, List<ChunkInfo>> sortChunks(
            List<ChunkInfo> chunks) {

        Map<Integer, List<ChunkInfo>> res = new HashMap<Integer, List<ChunkInfo>>();
        for (ChunkInfo ci : chunks) {
            List<ChunkInfo> l = res.get(ci.pos().meta());
            if (null == l) {
                l = new ArrayList<ChunkInfo>();
                res.put(ci.pos().meta(), l);
            }
            l.add(ci);
        }
        for (List<ChunkInfo> l : res.values())
            Collections.sort(l, comparator);

        return res;
    }

}