package io.openio.sds.models;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class PositionTest {

    @Test
    public void string() {
        Position p1 = Position.simple(0);
        assertEquals(p1.toString(),"0");

        Position p2 = Position.composed(0,1);
        assertEquals(p2.toString(),"0.1");
    }

    @Test
    public void parse() {
        Position p1 = Position.parse("0");
        assertEquals(p1.meta(),0);

        Position p2 = Position.parse("1.0");
        assertEquals(p2.meta(),1);
        assertEquals(p2.sub(),0);
    }

    @Test
    public void parseInvalid() {
        List<String> positions = Arrays.asList("", "1.2.3", "a", "a.b");

        for (String pos : positions) {
            try {
                Position.parse(pos);
                fail("Expected IllegalArgumentException");
            } catch (IllegalArgumentException e) {
                assertTrue(e.getMessage().contains("Invalid position"));
            }
        }
    }

    @Test
    public void compareSimple() {
        Position p1 = Position.simple(0);
        Position p2 = Position.simple(0);
        Position p3 = Position.simple(1);

        // meta is equal
        assertTrue(p1.compare(p2) == 0);
        // meta is lower
        assertTrue(p1.compare(p3) < 0);
        // meta is higher
        assertTrue(p3.compare(p1) > 0);
    }

    @Test
    public void compareComposed() {
        Position p1 = Position.composed(0,2);
        Position p2 = Position.composed(0,2);
        Position p3 = Position.composed(0,3);
        Position p4 = Position.composed(1,3);

        // meta is equal, sub is equal
        assertTrue(p1.compare(p2) == 0);
        // meta is equal, sub is lower
        assertTrue(p1.compare(p3) < 0);
        // meta is equal, sub is higher
        assertTrue(p3.compare(p1) > 0);
        // meta is lower, sub is equal
        assertTrue(p3.compare(p4) < 0);
        // meta is higher, sub is equal
        assertTrue(p4.compare(p3) > 0);
    }
}
