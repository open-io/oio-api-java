package io.openio.sds.http;

public class HttpSettings {

    private Integer sendBufferSize = 8192;
    private Integer receiveBufferSize = 8192;
    private Integer connectTimeout = 30000;
    private Integer readTimeout = 60000;

    public HttpSettings() {
    }

    public Integer sendBufferSize() {
        return sendBufferSize;
    }

    public HttpSettings sendBufferSize(Integer sendBufferSize) {
        this.sendBufferSize = sendBufferSize;
        return this;
    }

    public Integer receiveBufferSize() {
        return receiveBufferSize;
    }

    public HttpSettings receiveBufferSize(Integer receiveBufferSize) {
        this.receiveBufferSize = receiveBufferSize;
        return this;
    }

    public Integer connectTimeout() {
        return connectTimeout;
    }

    public HttpSettings connectTimeout(Integer connectTimeout) {
        this.connectTimeout = connectTimeout;
        return this;
    }

    public Integer readTimeout() {
        return readTimeout;
    }

    public HttpSettings readTimeout(Integer readTimeout) {
        this.readTimeout = readTimeout;
        return this;
    }
}
