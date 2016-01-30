package io.openio.sds.common;

import java.util.HashMap;
import java.util.Map.Entry;

public class MoreObjects {

    public static ToStringHelper toStringHelper(String name) {
        return new ToStringHelper(name);
    }

    public static ToStringHelper toStringHelper(Object o) {
        return new ToStringHelper(o.getClass().getSimpleName());
    }

    public static class ToStringHelper {

        private String name;
        private HashMap<String, Object> fields = new HashMap<String, Object>();
        private boolean skipNulls = false;

        private ToStringHelper(String name) {
            this.name = name;
        }

        public ToStringHelper add(String key, Object value) {
            if (null != key && (null != value || !skipNulls))
                fields.put(key, value);
            return this;
        }
        
        public ToStringHelper omitNullValues(){
            this.skipNulls = true;
            return this;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder(name)
                    .append("={");
            for (Entry<String, Object> e : fields.entrySet()) {
                sb.append("\"")
                .append(e.getKey())
                .append("\":\"")
                .append(e.getValue())
                .append("\",");
            }

            return sb.replace(sb.length() - 1, sb.length(), "}")
                    .toString();
        }
    }
}
