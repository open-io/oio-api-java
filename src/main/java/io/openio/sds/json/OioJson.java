package io.openio.sds.json;

import java.util.HashMap;
import java.util.regex.Pattern;

import io.openio.sds.common.Check;

public class OioJson {

    private static final Pattern OBJECT_PATTERN = Pattern
            .compile("^\\{(\"([^\"]+)\":(.+))?\\}$");
    
    private static final Pattern STRING_PATTERN = Pattern.compile("^\"([[^\\\"]|\\(?=[\"\\/bfnrt])]*)\"$");

    private HashMap<String, String> elements;

    private OioJson() {

    }

    public static OioJson parseString(String json) {
        return new OioJson().parseObject(json);
    }

    private OioJson parseObject(String json) {
        return this;
    }

    public static void main(String[] args) {
        System.out.println("{\"key\":\"value\"}");
        System.out.println(
                OBJECT_PATTERN.matcher("{\"key\":\"value\"}").matches());
        System.out.println(
                STRING_PATTERN.matcher("\"key\"").matches());
        System.out.println(
                STRING_PATTERN.matcher("\"k\\\"e\\\"y\"").matches());
    }
}
