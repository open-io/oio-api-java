package io.openio.sds.common;

public class Hex {

    private static final char[] hexArray = "0123456789ABCDEF".toCharArray();

    private byte[] wrapped;
    private String str;

    private Hex(byte[] wrapped, String str) {
        this.wrapped = wrapped;
        this.str = str;
    }

    public static Hex fromBytes(byte[] bytes) {
        Check.checkArgument(null != bytes);
        return new Hex(bytes, toHex(bytes));
    }

    private static String toHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    public byte[] bytes() {
        return wrapped;
    }

    public String hexString(boolean upperCase) {
        return (upperCase) ? str
                : str.toLowerCase();
    }

    @Override
    public String toString() {
        return this.str;
    }

}
