package io.openio.sds.common;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Hash {

    private MessageDigest md;

    public Hash(MessageDigest md) {
        this.md = md;        
    }

    public static Hash md5() {
        try {
            return new Hash(MessageDigest.getInstance("MD5"));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
    
    public static Hash sha256() {
        try {
            return new Hash(MessageDigest.getInstance("SHA-256"));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
    
    public Hash putBytes(byte[] bytes){
        this.md.update(bytes);
        return this;
    }
    
    public Hex hash(){
        return Hex.fromBytes(this.md.digest());
    }
    
    public Hex hashBytes(byte[] bytes){
        return Hex.fromBytes(this.md.digest(bytes));
    }
}
