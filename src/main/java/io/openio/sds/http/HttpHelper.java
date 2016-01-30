package io.openio.sds.http;

import io.openio.sds.log.SdsLogger;

public class HttpHelper {
    
    private static final SdsLogger logger = SdsLogger
            .getLogger(HttpHelper.class);

    public static Long longHeader(OioHttpResponse r, String header) {
        try {
            return Long.parseLong(r.header(header));
        } catch (NumberFormatException e) {
            return -1L;
        }
    }

}
