package io.openio.sds.common;

import java.util.Arrays;

/**
 * Byte array wrapper which provide convenience methods to deal with hexadecimal
 * strings
 */
public class Hex {

	private static final char[] hexArray = "0123456789ABCDEF".toCharArray();

	private byte[] wrapped;
	private String str;

	private Hex(byte[] wrapped, String str) {
		this.wrapped = wrapped;
		this.str = str;
	}

	/**
	 * Wraps the specifies byte array
	 * 
	 * @param bytes
	 *            the array to wrap
	 * @return a new Hex wrapping the array
	 * @throws IllegalArgumentException
	 *             if the specified array is {@code null}
	 */
	public static Hex fromBytes(byte[] bytes) {
		Check.checkArgument(null != bytes);
		return new Hex(bytes, toHex(bytes));
	}

	/**
	 * Converts the specified byte array in its hexadecimal string format (in upper case)
	 * @param bytes the byte array to convert
	 * @return the hexadecimal format
	 */
	public static String toHex(byte[] bytes) {
		char[] hexChars = new char[bytes.length * 2];
		for (int j = 0; j < bytes.length; j++) {
			int v = bytes[j] & 0xFF;
			hexChars[j * 2] = hexArray[v >>> 4];
			hexChars[j * 2 + 1] = hexArray[v & 0x0F];
		}
		return new String(hexChars);
	}

	/**
	 * Returns a copy of the wrapped array
	 * @return a copy of the wrapped array
	 */
	public byte[] bytes() {
		return Arrays.copyOf(wrapped, wrapped.length);
	}

	/**
	 * Returns the hexadecimal string format of the wrapped array
	 * @param upperCase specifies if the result will be in upper case or not
	 * @return the hexadecimal string
	 */
	public String hexString(boolean upperCase) {
		return upperCase ? str : str.toLowerCase();
	}

	@Override
	public String toString() {
		return this.str;
	}

}
