package io.openio.sds.storage;

/**
 * 
 * @author Christopher Dedeurwaerder
 *
 */
public class ObjectOffset {
	
	 private int pos;
     private int offset;

     public ObjectOffset() {
     }

     public int pos() {
         return pos;
     }

     public ObjectOffset pos(int pos) {
         this.pos = pos;
         return this;
     }

     public int offset() {
         return offset;
     }

     public ObjectOffset offset(int offset) {
         this.offset = offset;
         return this;
     }
}
