package components.intervalcoverage;

import components.standard.Standard;

/**
 * Kernel for a mutable set of covered integer positions. A span covers the
 * positions from its inclusive start to its exclusive end. The initial value
 * covers no positions.
 *
 * <p>In addition to the inherited {@link Standard} contract, the source of
 * {@code transferFrom} must be a distinct object.</p>
 *
 * @mathmodel a subset of the integers from 0 through Integer.MAX_VALUE - 1
 */
public interface IntervalCoverageKernel extends Standard<IntervalCoverage> {

    /**
     * Immutable, nonempty half-open span with nonnegative endpoints.
     *
     * @param start
     *            the inclusive start
     * @param end
     *            the exclusive end
     */
    record Span(int start, int end) {

        /**
         * Creates a span with the given endpoints.
         *
         * @param start
         *            the inclusive start
         * @param end
         *            the exclusive end
         * @requires 0 <= start < end
         * @ensures this.start = start and this.end = end
         */
        public Span(int start, int end) {
            assert 0 <= start && start < end : "Requires: 0 <= start < end";
            this.start = start;
            this.end = end;
        }
    }

    /**
     * Covers every position in the given span.
     *
     * @param span
     *            the span to add
     * @requires span is not null
     * @updates this
     * @ensures this is the union of the old covered positions and span
     */
    void cover(Span span);

    /**
     * Uncovers every position in the given span.
     *
     * @param span
     *            the span to subtract
     * @requires span is not null
     * @updates this
     * @ensures this is the old covered positions minus the positions in span
     */
    void uncover(Span span);

    /**
     * Reports the number of maximal contiguous covered spans.
     *
     * @return the number of spans
     * @ensures spanCount is the number of maximal contiguous covered spans
     */
    int spanCount();

    /**
     * Reports a maximal contiguous covered span by ascending start.
     *
     * @param index
     *            the zero-based position in ascending start order
     * @return the immutable span at that position
     * @requires 0 <= index < spanCount
     * @ensures spanAt is the maximal covered span at index
     */
    Span spanAt(int index);
}
