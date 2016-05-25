package io.openio.sds.models;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Assert;
import org.junit.Test;

public class RangeTest {

    @Test
    public void upTo() {
        int up = 1 + ((int) Math.random() * 1000);
        Range r = Range.upTo(up);
        assertNotNull(r);
        assertEquals(0, r.from());
        assertEquals(up, r.to());
    }

    @Test
    public void from() {
        int from = (int) (Math.random() * 1000);
        Range r = Range.from(from);
        assertNotNull(r);
        assertEquals(from, r.from());
        assertEquals(-1, r.to());
    }

    @Test
    public void between() {
        int from = (int) (Math.random() * 1000);
        int up = from + (int) (Math.random() * 1000);
        Range r = Range.between(from, up);
        assertNotNull(r);
        assertEquals(from, r.from());
        assertEquals(up, r.to());
    }

    @Test(expected = IllegalArgumentException.class)
    public void badFrom() {
        Range.from(-1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void badTo() {
        Range.upTo(0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void betweenBadFrom() {
        Range.between(-1, 10);
    }

    @Test(expected = IllegalArgumentException.class)
    public void betweenBadTo() {
        Range.between(0, 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void betweenBadBoth() {
        Range.between(10, 1);
    }
    
    @Test
    public void parseOkWithFromAndTo(){
        Range r = Range.parse("10-20");
        Assert.assertNotNull(r);
        Assert.assertEquals(10, r.from());
        Assert.assertEquals(20, r.to());
    }
    
    @Test
    public void parseOkWithFrom(){
        Range r = Range.parse("10-");
        Assert.assertNotNull(r);
        Assert.assertEquals(10, r.from());
        Assert.assertEquals(-1, r.to());
    }
    
    @Test
    public void parseOkWithTo(){
        Range r = Range.parse("-20");
        Assert.assertNotNull(r);
        Assert.assertEquals(0, r.from());
        Assert.assertEquals(20, r.to());
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void parseInvalid1(){
        Range.parse("azuhepoaizeuh");
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void parseInvalid2(){
        Range.parse("-");
    }
    
    @Test
    public void stringify(){
        Range r = Range.between(10, 20);
        Assert.assertEquals("bytes=10-20", r.headerValue());
        Assert.assertEquals("10-20", r.rangeValue());
        Assert.assertNotNull(r.toString());
        
        r = Range.from(10);
        Assert.assertEquals("bytes=10-", r.headerValue());
        Assert.assertEquals("10-", r.rangeValue());
        
        r = Range.upTo(20);
        Assert.assertEquals("bytes=0-20", r.headerValue());
        Assert.assertEquals("0-20", r.rangeValue());
    }

}
