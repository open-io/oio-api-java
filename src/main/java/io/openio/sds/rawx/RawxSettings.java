package io.openio.sds.rawx;

import io.openio.sds.http.OioHttpSettings;

/**
 * 
 * @author Christopher Dedeurwaerder
 *
 */
public class RawxSettings {

    private OioHttpSettings http;

    public RawxSettings() {
    }

    public OioHttpSettings http() {
        return http;
    }

    public RawxSettings http(OioHttpSettings http) {
        this.http = http;
        return this;
    }
}