package io.openio.sds.models;

import static io.openio.sds.common.Check.checkArgument;
import static java.lang.Integer.parseInt;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.openio.sds.common.MoreObjects;

/**
 * 
 *
 *
 */
public class Range {

    private static final Pattern RANGE_PATTERN = Pattern
            .compile("^([\\d]+)?-([\\d]+)?$");

    private int from = 0;
    private int to = -1;

    private Range(int from, int to) {
        this.from = from;
        this.to = to;
    }

    public static Range upTo(int to) {
        return new Range(0, to);
    }

    public static Range from(int from) {
        return new Range(from, -1);
    }

    public static Range between(int from, int to) {
        from = Math.max(from, 0);
        if (to > 0 && to <= from)
            throw new IllegalArgumentException("Invalid range");
        return new Range(from, to);
    }

    public int from() {
        return from;
    }

    public int to() {
        return to;
    }

    public static Range parse(String str) {
        Matcher m = RANGE_PATTERN.matcher(str);
        checkArgument(m.matches());
        if (null == m.group(1)) {
            checkArgument(null != m.group(2), "useless range");
            return upTo(parseInt(m.group(2)));
        }
        return (null == m.group(2)) ? from(parseInt(m.group(1)))
                : between(parseInt(m.group(1)), parseInt(m.group(2)));
    }

    public String headerValue() {
        return (to < 0)
                ? String.format("bytes=%d-", from)
                : String.format("bytes=%d-%d", from, to);
    }

    public String rangeValue() {
        return (to < 0)
                ? String.format("%d-", from)
                : String.format("%d-%d", from, to);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("from", from)
                .add("to", to)
                .toString();
    }
}