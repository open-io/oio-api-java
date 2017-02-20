package io.openio.sds.storage;

/**
 * 
 * @author Christopher Dedeurwaerder
 *
 */
public class ObjectOffset {
	
	 private int pos;
     private long offset;

     public ObjectOffset() {
     }

     public int pos() {
         return pos;
     }

     public ObjectOffset pos(int pos) {
         this.pos = pos;
         return this;
     }

     public long offset() {
         return offset;
     }

     public ObjectOffset offset(long offset) {
         this.offset = offset;
         return this;
     }
}
