package io.openio.sds.http;

import static io.openio.sds.common.OioConstants.INTERNAL_ERROR_FORMAT;
import static io.openio.sds.common.OioConstants.UNMANAGED_ERROR_FORMAT;
import static java.lang.String.format;

import io.openio.sds.exceptions.BadRequestException;
import io.openio.sds.exceptions.ContainerNotFoundException;
import io.openio.sds.exceptions.ObjectNotFoundException;
import io.openio.sds.exceptions.OioException;
import io.openio.sds.exceptions.OioSystemException;
import io.openio.sds.exceptions.ReferenceAlreadyExistException;
import io.openio.sds.exceptions.ReferenceNotFoundException;

/**
 * 
 * @author Christopher Dedeurwaerder
 *
 */
public class Verifiers {

    public static final OioHttpResponseVerifier REFERENCE_VERIFIER = new OioHttpResponseVerifier() {

        @Override
        public void verify(OioHttpResponse resp) throws OioException {
            switch (resp.code()) {
            case 200:
            case 201:
            case 204:
                return;
            case 202:
                throw new ReferenceAlreadyExistException(resp.msg());
            case 400:
                throw new BadRequestException(resp.msg());
            case 404:
                throw new ReferenceNotFoundException(resp.msg());
            case 500:
                throw new OioSystemException(
                        format(INTERNAL_ERROR_FORMAT, resp.code(),
                                resp.msg()));
            default:
                throw new OioSystemException(
                        format(UNMANAGED_ERROR_FORMAT, resp.code(),
                                resp.msg()));
            }
        }
    };

    public static final OioHttpResponseVerifier CONTAINER_VERIFIER = new OioHttpResponseVerifier() {

        @Override
        public void verify(OioHttpResponse resp) throws OioException {
            switch (resp.code()) {
            case 200:
            case 201:
            case 204:
                return;
            case 400:
                throw new BadRequestException(resp.msg());
            case 404:
                throw new ContainerNotFoundException(resp.msg());
            case 500:
                throw new OioSystemException(
                        format(INTERNAL_ERROR_FORMAT, resp.code(),
                                resp.msg()));
            default:
                throw new OioSystemException(
                        format(UNMANAGED_ERROR_FORMAT, resp.code(),
                                resp.msg()));
            }
        }
    };

    public static final OioHttpResponseVerifier OBJECT_VERIFIER = new OioHttpResponseVerifier() {

        @Override
        public void verify(OioHttpResponse resp) throws OioException {
            switch (resp.code()) {
            case 200:
            case 201:
            case 204:
                return;
            case 400:
                throw new BadRequestException(resp.msg());
            case 404:
                throw new ObjectNotFoundException(resp.msg());
            case 500:
                throw new OioSystemException(
                        format(INTERNAL_ERROR_FORMAT, resp.code(),
                                resp.msg()));
            default:
                throw new OioSystemException(
                        format(UNMANAGED_ERROR_FORMAT, resp.code(),
                                resp.msg()));
            }
        }
    };

    public static final OioHttpResponseVerifier STANDALONE_VERIFIER = new OioHttpResponseVerifier() {

        @Override
        public void verify(OioHttpResponse resp) throws OioException {
            switch (resp.code()) {
            case 200:
            case 201:
            case 204:
                return;
            case 400:
                throw new BadRequestException(resp.msg());
            case 404:
                throw new OioException(resp.msg());
            case 500:
                throw new OioSystemException(
                        format(INTERNAL_ERROR_FORMAT, resp.code(),
                                resp.msg()));
            default:
                throw new OioSystemException(
                        format(UNMANAGED_ERROR_FORMAT, resp.code(),
                                resp.msg()));
            }
        }
    };
}
