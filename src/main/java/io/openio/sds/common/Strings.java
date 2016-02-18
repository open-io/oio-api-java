package io.openio.sds.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Strings {

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

}
