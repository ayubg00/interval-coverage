package components.intervalcoverage;

import java.util.Optional;

/**
 * Interval coverage with queries for full coverage, gaps, and total length.
 */
public interface IntervalCoverage extends IntervalCoverageKernel {

    /**
     * Reports whether every position in a span is covered.
     *
     * @param span
     *            the span to check
     * @return whether the entire span is covered
     * @requires span is not null
     * @ensures covers is true exactly when every position in span is covered
     */
    boolean covers(Span span);

    /**
     * Finds the first maximal uncovered span of sufficient length in a window.
     *
     * @param window
     *            the span within which to search
     * @param minimumLength
     *            the required number of consecutive uncovered positions
     * @return the entire first qualifying gap within window, or empty
     * @requires window is not null and minimumLength > 0
     * @ensures firstGap contains the leftmost maximal uncovered span within
     *          window whose length is at least minimumLength, or is empty
     *          if none qualifies
     */
    Optional<Span> firstGap(Span window, int minimumLength);

    /**
     * Counts covered positions, counting overlaps only once.
     *
     * @return the total length of the covered spans
     * @ensures coveredLength is the number of positions in this
     */
    int coveredLength();
}
