package io.openio.sds.common;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.openio.sds.logging.SdsLogger;
import io.openio.sds.logging.SdsLoggerFactory;

public class Strings {

    private static final SdsLogger LOGGER = SdsLoggerFactory.getLogger(Strings.class);

    public static boolean nullOrEmpty(String string) {
        return null == string || 0 == string.length();
    }

    public static HashMap<String, String> toMap(String src, String sep,
            String kvsep) {
        HashMap<String, String> res = new HashMap<String, String>();
        if (nullOrEmpty(src) || nullOrEmpty(sep) || nullOrEmpty(kvsep))
            return res;
        String[] pairs = src.split(sep);
        for (String pair : pairs) {
            if (nullOrEmpty(pair))
                continue;
            String[] tok = pair.split(kvsep, 2);
            if (tok.length != 2 || nullOrEmpty(tok[0]) || nullOrEmpty(tok[1]))
                continue;
            res.put(tok[0], tok[1]);
        }
        return res;
    }

    public static List<String> toList(String src, String sep) {
        ArrayList<String> res = new ArrayList<String>();
        if (nullOrEmpty(src) || nullOrEmpty(sep))
            return res;
        String[] toks = src.split(sep);
        for (String tok : toks)
            if (!nullOrEmpty(tok))
                res.add(tok);
        return res;
    }

    public static String urlEncode(String src) {
        try {
            return URLEncoder.encode(src, OioConstants.OIO_CHARSET.name());
        } catch (UnsupportedEncodingException e) {
            LOGGER.warn("Url encoding fail, may have some problems", e);
            return src;
        }
    }

    public static String quote(String src) {
        return Strings.urlEncode(src).replace("+", "%20");
    }

    public static String quote(Long src) {
        return Strings.quote(String.valueOf(src));
    }
}
