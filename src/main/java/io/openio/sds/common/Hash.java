package io.openio.sds.common;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Hashing utility class wrapping a {@linkplain MessageDigest} instance for
 * cleaner code
 *
 */
public class Hash {

	private MessageDigest md;

	private Hash(MessageDigest md) {
		this.md = md;
	}

	/**
	 * Returns an {@link Hash} instance ready to compute a md5 hash
	 * 
	 * @return an {@link Hash} instance ready to compute a md5 hash
	 */
	public static Hash md5() {
		try {
			return new Hash(MessageDigest.getInstance("MD5"));
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Returns an {@link Hash} instance ready to compute a md5 hash
	 * 
	 * @return an {@link Hash} instance ready to compute a md5 hash
	 */
	public static Hash sha256() {
		try {
			return new Hash(MessageDigest.getInstance("SHA-256"));
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Updates the current hash with the specified bytes
	 * 
	 * @param bytes
	 *            the bytes to add
	 * @return this
	 */
	public Hash putBytes(byte[] bytes) {
		this.md.update(bytes);
		return this;
	}

	/**
	 * Completes the current hash computation
	 * 
	 * @return an {@link Hex} instance handling the computed hash
	 */
	public Hex hash() {
		return Hex.fromBytes(this.md.digest());
	}

	/**
	 * Add a final update to the current hash and completes it
	 * 
	 * @param bytes
	 *            the final bytes to add
	 * @return an {@link Hex} instance handling the computed hash
	 */
	public Hex hashBytes(byte[] bytes) {
		return Hex.fromBytes(this.md.digest(bytes));
	}
}
