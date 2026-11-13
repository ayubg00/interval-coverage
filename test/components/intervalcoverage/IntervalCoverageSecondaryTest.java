package components.intervalcoverage;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Optional;

import org.junit.Test;

import components.intervalcoverage.IntervalCoverageKernel.Span;

/**
 * Public contract tests for the secondary and common methods.
 */
// Test coordinates and expected counts are easier to check as literals.
// SUPPRESS CHECKSTYLE MagicNumber FOR NEXT 260 LINES
public final class IntervalCoverageSecondaryTest {

    /**
     * Creates coverage from input spans through the kernel.
     *
     * @param spans
     *            spans to cover
     * @return coverage of all the supplied spans
     */
    private static IntervalCoverage coverageOf(Span... spans) {
        IntervalCoverage coverage = new IntervalCoverage1();
        for (Span span : spans) {
            coverage.cover(span);
        }
        return coverage;
    }

    /**
     * Checks the complete value independently of the common methods.
     *
     * @param actual
     *            the coverage under test
     * @param expected
     *            the expected spans in order
     */
    private static void assertSpans(IntervalCoverage actual, Span... expected) {
        assertEquals(expected.length, actual.spanCount());
        for (int index = 0; index < expected.length; index++) {
            assertEquals(expected[index], actual.spanAt(index));
        }
    }

    /** Empty coverage cannot cover a nonempty span. */
    @Test
    public void coversOnEmpty() {
        IntervalCoverage coverage = coverageOf();
        assertFalse(coverage.covers(new Span(0, 1)));
        assertSpans(coverage);
    }

    /** Exact endpoints and proper subsets qualify. */
    @Test
    public void coversExactAndContainedSpans() {
        Span first = new Span(1, 7);
        Span last = new Span(9, 12);
        IntervalCoverage coverage = coverageOf(first, last);
        assertTrue(coverage.covers(last));
        assertSpans(coverage, first, last);
        assertTrue(coverage.covers(new Span(2, 6)));
        assertSpans(coverage, first, last);
    }

    /** Any uncovered position makes a query false. */
    @Test
    public void coversRejectsPartialOverlapAndInternalGap() {
        Span first = new Span(1, 4);
        Span last = new Span(5, 8);
        IntervalCoverage coverage = coverageOf(first, last);
        assertFalse(coverage.covers(new Span(0, 2)));
        assertSpans(coverage, first, last);
        assertFalse(coverage.covers(new Span(3, 6)));
        assertSpans(coverage, first, last);
        assertFalse(coverage.covers(new Span(4, 5)));
        assertSpans(coverage, first, last);
    }

    /** With no coverage, a qualifying window is itself the first gap. */
    @Test
    public void firstGapOnEmptyAcceptsExactLength() {
        Span window = new Span(2, 5);
        IntervalCoverage coverage = coverageOf();
        assertEquals(Optional.of(window), coverage.firstGap(window, 3));
        assertSpans(coverage);
        assertEquals(Optional.empty(), coverage.firstGap(window, 4));
        assertSpans(coverage);
    }

    /** The leading gap is clipped and returned in full. */
    @Test
    public void firstGapClipsLeadingGapToWindow() {
        Span earlier = new Span(0, 2);
        Span later = new Span(7, 10);
        IntervalCoverage coverage = coverageOf(earlier, later);
        assertEquals(Optional.of(new Span(3, 7)),
                coverage.firstGap(new Span(3, 9), 2));
        assertSpans(coverage, earlier, later);
    }

    /** The first qualifying interior gap wins over a later, larger one. */
    @Test
    public void firstGapSkipsShortLeadingGapAndChoosesFirstMatch() {
        Span first = new Span(1, 3);
        Span second = new Span(5, 7);
        IntervalCoverage coverage = coverageOf(first, second);
        assertEquals(Optional.of(new Span(3, 5)),
                coverage.firstGap(new Span(0, 12), 2));
        assertSpans(coverage, first, second);
    }

    /** Covered spans beyond the window cannot extend the reported gap. */
    @Test
    public void firstGapClipsTrailingGapToWindow() {
        Span first = new Span(0, 4);
        Span later = new Span(10, 14);
        IntervalCoverage coverage = coverageOf(first, later);
        assertEquals(Optional.of(new Span(4, 8)),
                coverage.firstGap(new Span(2, 8), 4));
        assertSpans(coverage, first, later);
        assertEquals(Optional.of(new Span(14, 16)),
                coverage.firstGap(new Span(12, 16), 2));
        assertSpans(coverage, first, later);
    }

    /** A covered window has no gap even at the minimum length of one. */
    @Test
    public void firstGapWithFullyCoveredWindow() {
        Span full = new Span(0, Integer.MAX_VALUE);
        IntervalCoverage coverage = coverageOf(full);
        assertEquals(Optional.empty(),
                coverage.firstGap(new Span(1, Integer.MAX_VALUE), 1));
        assertSpans(coverage, full);
    }

    /** Several smaller gaps do not add up to a contiguous qualifying gap. */
    @Test
    public void firstGapWithOnlyShortGaps() {
        Span first = new Span(1, 3);
        Span second = new Span(4, 6);
        IntervalCoverage coverage = coverageOf(first, second);
        assertEquals(Optional.empty(), coverage.firstGap(new Span(0, 7), 2));
        assertSpans(coverage, first, second);
    }

    /** Empty coverage has zero length. */
    @Test
    public void coveredLengthOnEmpty() {
        IntervalCoverage coverage = coverageOf();
        assertEquals(0, coverage.coveredLength());
        assertSpans(coverage);
    }

    /** The entire domain fits in the return type. */
    @Test
    public void coveredLengthAtMaximum() {
        Span full = new Span(0, Integer.MAX_VALUE);
        IntervalCoverage coverage = coverageOf(full);
        assertEquals(Integer.MAX_VALUE, coverage.coveredLength());
        assertSpans(coverage, full);
    }

    /** Overlap counts once and a removed position is excluded. */
    @Test
    public void coveredLengthAcrossOverlapsAndRemoval() {
        IntervalCoverage coverage = coverageOf(new Span(0, 5), new Span(3, 8),
                new Span(10, 11));
        coverage.uncover(new Span(2, 4));
        assertEquals(7, coverage.coveredLength());
        assertSpans(coverage, new Span(0, 2), new Span(4, 8), new Span(10, 11));
    }

    /** Equal values have equal hashes regardless of how they were built. */
    @Test
    public void equalsAndHashCodeAgreeAcrossHistories() {
        Span first = new Span(0, 4);
        Span second = new Span(6, 8);
        IntervalCoverage left = coverageOf(first, second);
        IntervalCoverage right = coverageOf(second, new Span(0, 2),
                new Span(2, 4));
        assertTrue(left.equals(right));
        assertTrue(right.equals(left));
        assertEquals(left.hashCode(), right.hashCode());
        assertSpans(left, first, second);
        assertSpans(right, first, second);
    }

    /** Matching span count and total length do not imply equal coverage. */
    @Test
    public void equalsDistinguishesPositionAndSpanCount() {
        Span first = new Span(1, 3);
        Span shifted = new Span(2, 4);
        IntervalCoverage left = coverageOf(first);
        IntervalCoverage right = coverageOf(shifted);
        IntervalCoverage empty = coverageOf();
        assertFalse(left.equals(right));
        assertFalse(right.equals(left));
        assertFalse(left.equals(empty));
        assertSpans(left, first);
        assertSpans(right, shifted);
        assertSpans(empty);
    }

    /** Equality is reflexive and rejects null and unrelated objects. */
    @Test
    public void equalsHandlesSelfNullAndOtherType() {
        IntervalCoverage empty = coverageOf();
        IntervalCoverage other = coverageOf();
        assertTrue(empty.equals(empty));
        assertTrue(empty.equals(other));
        assertFalse(empty.equals(null));
        assertFalse(empty.equals("{}"));
        assertEquals(empty.hashCode(), other.hashCode());
        assertSpans(empty);
        assertSpans(other);
    }

    /** Repeated hash queries agree and preserve the covered positions. */
    @Test
    public void hashCodeIsStableForUnchangedValue() {
        Span span = new Span(1, 3);
        IntervalCoverage coverage = coverageOf(span);
        int originalHash = coverage.hashCode();
        assertSpans(coverage, span);
        assertEquals(originalHash, coverage.hashCode());
        assertSpans(coverage, span);
    }

    /** String output shows the empty value with no spans. */
    @Test
    public void toStringOnEmpty() {
        IntervalCoverage coverage = coverageOf();
        assertEquals("{}", coverage.toString());
        assertSpans(coverage);
    }

    /** String output uses ascending start order and half-open notation. */
    @Test
    public void toStringWithSeveralSpans() {
        Span first = new Span(0, 1);
        Span second = new Span(2, 4);
        IntervalCoverage coverage = coverageOf(second, first);
        assertEquals("{[0, 1), [2, 4)}", coverage.toString());
        assertSpans(coverage, first, second);
    }
}
