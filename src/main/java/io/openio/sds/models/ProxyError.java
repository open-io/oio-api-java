package io.openio.sds.models;

import static io.openio.sds.common.OioConstants.PROXY_ERROR_FORMAT;
import static java.lang.String.format;

/**
 * 
 * @author Christopher Dedeurwaerder
 *
 */
public class ProxyError {

    private Integer status;
    private String message;

    public ProxyError() {
    }

    public Integer status() {
        return status;
    }

    public ProxyError status(Integer status) {
        this.status = status;
        return this;
    }

    public String message() {
        return message;
    }

    public ProxyError message(String message) {
        this.message = message;
        return this;
    }

    @Override
    public String toString() {
        return format(PROXY_ERROR_FORMAT, status, message);
    }
}
