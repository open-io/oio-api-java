package io.openio.sds.http;

import io.openio.sds.exceptions.OioException;

public interface OioHttpResponseVerifier {

    public void verify(OioHttpResponse response) throws OioException;
}
