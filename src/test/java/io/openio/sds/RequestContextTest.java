package io.openio.sds;

import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import io.openio.sds.common.DeadlineManager;

public class RequestContextTest {

    class PilotedClockSource implements DeadlineManager.ClockSource {

        int now = 0;

        @Override
        public int now() {
            return this.now;
        }

        public void tick() {
            this.now++;
        }
    }

    PilotedClockSource clock;

    public RequestContextTest() {
        this.clock = new PilotedClockSource();
    }

    /* --- Request IDs ----------------------------------------------- */
    @Test
    public void ensureRequestId_unset() {
        RequestContext ctx = Mockito.spy(new RequestContext());
        String reqId = ctx.requestId();
        Mockito.verify(ctx).ensureRequestId();
        Assert.assertNotNull(reqId);
        Assert.assertTrue(reqId.length() >= 8);
    }

    @Test
    public void ensureRequestId_valid() {
        RequestContext ctx = Mockito.spy(new RequestContext());
        String reqIdI = "longenoughrequestID";
        ctx.withRequestId(reqIdI);
        String reqIdO = ctx.requestId();
        Mockito.verify(ctx).ensureRequestId();
        Assert.assertNotNull(reqIdO);
        Assert.assertEquals(reqIdI, reqIdO);
    }

    @Test
    public void ensureRequestId_short() {
        RequestContext ctx = Mockito.spy(new RequestContext());
        String reqIdI = "short";
        ctx.withRequestId(reqIdI);
        String reqIdO = ctx.requestId();
        Mockito.verify(ctx).ensureRequestId();
        Assert.assertNotNull(reqIdO);
        Assert.assertTrue(reqIdO.length() >= 8);
        Assert.assertThat(reqIdO, CoreMatchers.startsWith(reqIdI));
        Assert.assertTrue(reqIdO.startsWith(reqIdI));
    }

    /* --- Deadlines and timeouts ------------------------------------ */
    @Test
    public void withDeadline_0() {
        RequestContext ctx = new RequestContext().withDeadline(0);
        Assert.assertEquals(0, ctx.deadline());
    }

    @Test(expected = IllegalArgumentException.class)
    public void withDeadline_negative() {
        new RequestContext().withDeadline(-1);
        Assert.fail("Should have raised exception!");
    }

    @Test
    public void timeout_after_withDeadline() {
        DeadlineManager.instance().useMockedClockSource(this.clock);
        RequestContext ctx = new RequestContext().withDeadline(1);

        Assert.assertTrue(ctx.hasTimeout());  // computed from deadline
        Assert.assertEquals(1, ctx.deadline());
        Assert.assertEquals(1, ctx.timeout());
    }

    @Test
    public void timeout_after_withTimeout() {
        DeadlineManager.instance().useMockedClockSource(this.clock);
        RequestContext ctx = new RequestContext().withTimeout(1);

        Assert.assertFalse(ctx.hasDeadline());
        Assert.assertEquals(1, ctx.timeout());
        Assert.assertEquals(1, ctx.deadline());
        Assert.assertTrue(ctx.hasDeadline());
    }

    @Test
    public void resetDeadline() {
        DeadlineManager.instance().useMockedClockSource(this.clock);
        RequestContext ctx = new RequestContext().withDeadline(10);

        Assert.assertTrue(ctx.hasDeadline());
        Assert.assertTrue(ctx.hasTimeout());  // computed from deadline

        ctx.resetDeadline();
        Assert.assertFalse(ctx.hasDeadline());
        Assert.assertTrue(ctx.hasTimeout());  // not reset
        Assert.assertEquals(10, ctx.timeout());

        this.clock.tick();
        ctx.computeDeadline();
        Assert.assertTrue(ctx.hasDeadline());
        Assert.assertEquals(10, ctx.timeout());
        Assert.assertEquals(11, ctx.deadline());
    }

    @Test
    public void elapsed() {
        DeadlineManager.instance().useMockedClockSource(this.clock);
        RequestContext ctx = new RequestContext();

        Assert.assertFalse(ctx.hasStarted());
        Assert.assertEquals(0, ctx.elapsed());

        ctx.startTiming();
        Assert.assertTrue(ctx.hasStarted());
        Assert.assertEquals(0, ctx.elapsed());
        this.clock.tick();
        Assert.assertEquals(1, ctx.elapsed());
    }

    @Test(expected = IllegalStateException.class)
    public void computeDeadline_no_timeout() {
        new RequestContext().computeDeadline();
    }
    
    @Test
    public void copy_constructor() {
        RequestContext ctx = new RequestContext().withTimeout(10).ensureRequestId();
        RequestContext ctx2 = new RequestContext(ctx);
        Assert.assertTrue(ctx2.hasTimeout());
        Assert.assertFalse(ctx2.hasDeadline());
        Assert.assertEquals(ctx.requestId(), ctx2.requestId());
        Assert.assertThat(ctx2, CoreMatchers.not(CoreMatchers.is(ctx)));
    }
}
