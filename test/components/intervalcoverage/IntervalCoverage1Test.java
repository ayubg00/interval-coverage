package components.intervalcoverage;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;

import org.junit.Test;

import components.intervalcoverage.IntervalCoverageKernel.Span;

/**
 * Public contract tests for the kernel and Standard methods.
 */
// Test coordinates and expected counts are easier to check as literals.
// SUPPRESS CHECKSTYLE MagicNumber FOR NEXT 240 LINES
public final class IntervalCoverage1Test {

    /**
     * Checks the complete abstract value through the kernel interface.
     *
     * @param actual
     *            the coverage under test
     * @param expected
     *            the expected spans in order
     */
    private static void assertSpans(IntervalCoverageKernel actual,
            Span... expected) {
        assertEquals(expected.length, actual.spanCount());
        for (int index = 0; index < expected.length; index++) {
            assertEquals(expected[index], actual.spanAt(index));
        }
    }

    /** A new instance has no spans. */
    @Test
    public void constructorStartsEmpty() {
        assertSpans(new IntervalCoverage1());
    }

    /** A span exposes its inclusive start and exclusive end unchanged. */
    @Test
    public void spanPreservesEndpointLimits() {
        Span span = new Span(0, Integer.MAX_VALUE);
        assertEquals(0, span.start());
        assertEquals(Integer.MAX_VALUE, span.end());
    }

    /** A span can cover the entire valid domain. */
    @Test
    public void coverOnEmptyAcceptsBothEndpointLimits() {
        IntervalCoverageKernel coverage = new IntervalCoverage1();
        Span whole = new Span(0, Integer.MAX_VALUE);
        coverage.cover(whole);
        assertSpans(coverage, whole);
    }

    /** Disjoint inserts at the front, middle, and end stay ordered. */
    @Test
    public void coverOrdersDisjointSpans() {
        IntervalCoverageKernel coverage = new IntervalCoverage1();
        coverage.cover(new Span(8, 10));
        coverage.cover(new Span(0, 2));
        coverage.cover(new Span(12, 14));
        coverage.cover(new Span(4, 6));
        assertSpans(coverage, new Span(0, 2), new Span(4, 6), new Span(8, 10),
                new Span(12, 14));
    }

    /** One insertion merges spans that overlap or touch on either side. */
    @Test
    public void coverBridgesSeveralSpansAndKeepsNeighbors() {
        IntervalCoverageKernel coverage = new IntervalCoverage1();
        coverage.cover(new Span(0, 1));
        coverage.cover(new Span(2, 4));
        coverage.cover(new Span(5, 7));
        coverage.cover(new Span(9, 11));
        coverage.cover(new Span(12, 13));
        coverage.cover(new Span(4, 9));
        assertSpans(coverage, new Span(0, 1), new Span(2, 11),
                new Span(12, 13));
    }

    /** An insertion may extend either endpoint of an existing span. */
    @Test
    public void coverExtendsOverlappingSpan() {
        IntervalCoverageKernel coverage = new IntervalCoverage1();
        coverage.cover(new Span(4, 8));
        coverage.cover(new Span(2, 6));
        assertSpans(coverage, new Span(2, 8));
        coverage.cover(new Span(6, 10));
        assertSpans(coverage, new Span(2, 10));
    }

    /** Repeated and fully contained additions leave the value unchanged. */
    @Test
    public void coverContainedSpanIsIdempotent() {
        IntervalCoverageKernel coverage = new IntervalCoverage1();
        Span span = new Span(2, 8);
        coverage.cover(span);
        coverage.cover(new Span(3, 7));
        coverage.cover(span);
        assertSpans(coverage, span);
    }

    /** Subtraction from empty coverage leaves it empty. */
    @Test
    public void uncoverFromEmptyIsNoOp() {
        IntervalCoverageKernel coverage = new IntervalCoverage1();
        coverage.uncover(new Span(0, 1));
        assertSpans(coverage);
    }

    /** Touching endpoints do not overlap in the half-open model. */
    @Test
    public void uncoverDisjointAndTouchingSpansIsNoOp() {
        IntervalCoverageKernel coverage = new IntervalCoverage1();
        Span span = new Span(2, 8);
        coverage.cover(span);
        coverage.uncover(new Span(0, 2));
        coverage.uncover(new Span(8, 10));
        coverage.uncover(new Span(11, 12));
        assertSpans(coverage, span);
    }

    /** Removing a strict interior interval splits one span in two. */
    @Test
    public void uncoverSplitsSpan() {
        IntervalCoverageKernel coverage = new IntervalCoverage1();
        coverage.cover(new Span(0, 10));
        coverage.uncover(new Span(3, 7));
        assertSpans(coverage, new Span(0, 3), new Span(7, 10));
    }

    /**
     * Subtraction across several spans trims both ends and removes the middle.
     */
    @Test
    public void uncoverAcrossSeveralSpans() {
        IntervalCoverageKernel coverage = new IntervalCoverage1();
        coverage.cover(new Span(0, 3));
        coverage.cover(new Span(4, 6));
        coverage.cover(new Span(7, 10));
        coverage.uncover(new Span(2, 8));
        assertSpans(coverage, new Span(0, 2), new Span(8, 10));
    }

    /** Exact subtraction removes a span and leaves the object reusable. */
    @Test
    public void uncoverExactSpanThenCoverAgain() {
        IntervalCoverageKernel coverage = new IntervalCoverage1();
        Span lastPosition = new Span(Integer.MAX_VALUE - 1, Integer.MAX_VALUE);
        coverage.cover(lastPosition);
        coverage.uncover(lastPosition);
        assertSpans(coverage);
        coverage.cover(lastPosition);
        assertSpans(coverage, lastPosition);
    }

    /** Count preserves the value in empty, singleton, and multiple states. */
    @Test
    public void spanCountPreservesState() {
        IntervalCoverageKernel coverage = new IntervalCoverage1();
        assertEquals(0, coverage.spanCount());
        assertSpans(coverage);
        coverage.cover(new Span(0, 1));
        assertEquals(1, coverage.spanCount());
        assertSpans(coverage, new Span(0, 1));
        coverage.cover(new Span(3, 4));
        assertEquals(2, coverage.spanCount());
        assertSpans(coverage, new Span(0, 1), new Span(3, 4));
    }

    /**
     * Indexed reads return the first, middle, and last values without removal.
     */
    @Test
    public void spanAtPreservesState() {
        IntervalCoverageKernel coverage = new IntervalCoverage1();
        Span first = new Span(0, 1);
        Span middle = new Span(3, 4);
        Span last = new Span(6, 7);
        coverage.cover(last);
        coverage.cover(first);
        coverage.cover(middle);
        assertEquals(first, coverage.spanAt(0));
        assertEquals(middle, coverage.spanAt(1));
        assertEquals(last, coverage.spanAt(2));
        assertSpans(coverage, first, middle, last);
    }

    /** A new instance has the same runtime type and independent state. */
    @Test
    public void newInstanceIsEmptyAndIndependent() {
        IntervalCoverageKernel original = new IntervalCoverage1();
        Span oldSpan = new Span(0, 2);
        Span newSpan = new Span(3, 6);
        original.cover(oldSpan);
        IntervalCoverage fresh = original.newInstance();
        assertNotSame(original, fresh);
        assertEquals(original.getClass(), fresh.getClass());
        assertSpans(fresh);
        assertSpans(original, oldSpan);
        fresh.cover(newSpan);
        assertSpans(fresh, newSpan);
        assertSpans(original, oldSpan);
    }

    /** An empty instance produces a distinct empty instance. */
    @Test
    public void newInstanceFromEmpty() {
        IntervalCoverageKernel original = new IntervalCoverage1();
        IntervalCoverage fresh = original.newInstance();
        assertNotSame(original, fresh);
        assertEquals(original.getClass(), fresh.getClass());
        assertSpans(fresh);
        assertSpans(original);
    }

    /** Clearing empty or populated coverage resets it for reuse. */
    @Test
    public void clearResetsValueAndIsIdempotent() {
        IntervalCoverageKernel coverage = new IntervalCoverage1();
        coverage.clear();
        assertSpans(coverage);
        coverage.cover(new Span(0, 2));
        coverage.cover(new Span(4, 6));
        coverage.clear();
        assertSpans(coverage);
        coverage.clear();
        assertSpans(coverage);
        coverage.cover(new Span(8, 10));
        assertSpans(coverage, new Span(8, 10));
    }

    /**
     * Transfer replaces the receiver and clears the source without aliasing.
     */
    @Test
    public void transferFromPopulatedOverwritesAndSeparatesObjects() {
        IntervalCoverage source = new IntervalCoverage1();
        IntervalCoverage receiver = new IntervalCoverage1();
        Span first = new Span(1, 3);
        Span second = new Span(5, 7);
        source.cover(first);
        source.cover(second);
        receiver.cover(new Span(10, 12));
        receiver.transferFrom(source);
        assertSpans(receiver, first, second);
        assertSpans(source);
        source.cover(new Span(12, 14));
        receiver.uncover(first);
        assertSpans(receiver, second);
        assertSpans(source, new Span(12, 14));
    }

    /** Moving the empty value clears a populated receiver as well. */
    @Test
    public void transferFromEmptyReplacesReceiver() {
        IntervalCoverage source = new IntervalCoverage1();
        IntervalCoverage receiver = new IntervalCoverage1();
        receiver.cover(new Span(0, 1));
        receiver.transferFrom(source);
        assertSpans(receiver);
        assertSpans(source);
    }
}
