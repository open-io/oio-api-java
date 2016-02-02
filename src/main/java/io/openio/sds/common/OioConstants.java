package io.openio.sds.common;

public class OioConstants {

    /* -- proxyd common headers -- */

    public static final String ACCOUNT_HEADER = "X-oio-container-meta-sys-account";
    public static final String M2_CTIME_HEADER = "X-oio-container-meta-sys-m2-ctime";
    public static final String M2_INIT_HEADER = "X-oio-container-meta-sys-m2-init";
    public static final String M2_USAGE_HEADER = "X-oio-container-meta-sys-m2-usage";
    public static final String M2_VERSION_HEADER = "X-oio-container-meta-sys-m2-version";
    public static final String CONTAINER_SYS_NAME_HEADER = "X-oio-container-meta-sys-name";
    public static final String NS_HEADER = "X-oio-container-meta-sys-ns";
    public static final String TYPE_HEADER = "X-oio-container-meta-sys-type";
    public static final String USER_NAME_HEADER = "X-oio-container-meta-sys-user-name";
    public static final String SCHEMA_VERSION_HEADER = "X-oio-container-meta-x-schema-version";
    public static final String VERSION_MAIN_ADMIN_HEADER = "X-oio-container-meta-x-version-main-admin";
    public static final String VERSION_MAIN_ALIASES_HEADER = "X-oio-container-meta-x-version-main-aliases";
    public static final String VERSION_MAIN_CHUNKS_HEADER = "X-oio-container-meta-x-version-main-chunks";
    public static final String VERSION_MAIN_CONTENTS_HEADER = "X-oio-container-meta-x-version-main-contents";
    public static final String VERSION_MAIN_PROPERTIES_HEADER = "X-oio-container-meta-x-version-main-properties";

    public static final String CONTENT_META_ID_HEADER = "X-oio-content-meta-id";
    public static final String CONTENT_META_CHUNK_METHOD_HEADER = "X-oio-content-meta-chunk-method";
    public static final String CONTENT_META_CTIME_HEADER = "X-oio-content-meta-ctime";
    public static final String CONTENT_META_DELETED_HEADER = "X-oio-content-meta-deleted";
    public static final String CONTENT_META_HASH_HEADER = "X-oio-content-meta-hash";
    public static final String CONTENT_META_HASH_METHOD_HEADER = "X-oio-content-meta-hash-method";
    public static final String CONTENT_META_LENGTH_HEADER = "X-oio-content-meta-length";
    public static final String CONTENT_META_MIME_TYPE_HEADER = "X-oio-content-meta-mime-type";
    public static final String CONTENT_META_NAME_HEADER = "X-oio-content-meta-name";
    public static final String CONTENT_META_POLICY_HEADER = "X-oio-content-meta-policy";
    public static final String CONTENT_META_VERSION_HEADER = "X-oio-content-meta-version";

    public static final String NS_CHUNK_SIZE_HEADER = "X-oio-ns-chunk-size";
    public static final String LIST_TRUNCATED_HEADER = "X-oio-list-truncated";

    public static final String OIO_ACTION_MODE_HEADER = "X-oio-action-mode";

    /* -- RAWX common headers -- */
    public static final String CHUNK_META_CONTENT_ID = "X-oio-chunk-meta-content-id";
    public static final String CHUNK_META_CONTAINER_ID = "X-oio-chunk-meta-container-id";
    public static final String CHUNK_META_CONTENT_CHUNKSNB = "x-oio-chunk-meta-content-chunksnb";
    public static final String CHUNK_META_CONTENT_SIZE = "X-oio-chunk-meta-content-size";
    public static final String CHUNK_META_CONTENT_PATH = "X-oio-chunk-meta-content-path";
    public static final String CHUNK_META_CONTENT_POLICY = "X-oio-chunk-meta-content-storage-policy";
    public static final String CHUNK_META_CONTENT_CHUNK_METHOD = "X-oio-chunk-meta-content-chunk-method";
    public static final String CHUNK_META_CONTENT_MIME_TYPE = "X-oio-chunk-meta-content-mime-type";
    public static final String CHUNK_META_CHUNK_ID = "X-oio-chunk-meta-chunk-id";
    public static final String CHUNK_META_CHUNK_POS = "X-oio-chunk-meta-chunk-pos";
    public static final String CHUNK_META_CHUNK_HASH = "X-oio-chunk-meta-chunk-hash";
}
