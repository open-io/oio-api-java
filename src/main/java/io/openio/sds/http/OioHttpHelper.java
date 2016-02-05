package io.openio.sds.http;

public class OioHttpHelper {

    public static Long longHeader(OioHttpResponse r, String header) {
        try {
            return Long.parseLong(r.header(header));
        } catch (NumberFormatException e) {
            return -1L;
        }
    }

}
