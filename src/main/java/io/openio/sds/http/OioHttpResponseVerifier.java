package io.openio.sds.http;

import io.openio.sds.exceptions.SdsException;

public interface OioHttpResponseVerifier {

    public void verify(OioHttpResponse response) throws SdsException;
}
