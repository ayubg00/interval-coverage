package components.intervalcoverage;

import java.util.Optional;
import java.util.StringJoiner;

/**
 * Representation-independent implementations of the enhanced operations.
 */
public abstract class IntervalCoverageSecondary implements IntervalCoverage {

    /**
     * Multiplier used to combine the hashes of consecutive spans.
     */
    private static final int HASH_MULTIPLIER = 31;

    /**
     * Allows subclasses to initialize their own representation.
     */
    protected IntervalCoverageSecondary() {
    }

    @Override
    public final boolean covers(Span span) {
        assert span != null : "Requires: span is not null";
        boolean covered = false;
        int count = this.spanCount();
        for (int index = 0; index < count && !covered; index++) {
            Span current = this.spanAt(index);
            covered = current.start() <= span.start()
                    && span.end() <= current.end();
        }
        return covered;
    }

    @Override
    public final Optional<Span> firstGap(Span window, int minimumLength) {
        assert window != null : "Requires: window is not null";
        assert minimumLength > 0 : "Requires: minimumLength > 0";
        Optional<Span> gap = Optional.empty();
        int cursor = window.start();
        int count = this.spanCount();
        for (int index = 0; index < count && cursor < window.end()
                && gap.isEmpty(); index++) {
            Span covered = this.spanAt(index);
            int gapEnd = Math.min(covered.start(), window.end());
            if (gapEnd - cursor >= minimumLength) {
                gap = Optional.of(new Span(cursor, gapEnd));
            } else {
                cursor = Math.max(cursor, covered.end());
            }
        }
        if (gap.isEmpty() && window.end() - cursor >= minimumLength) {
            gap = Optional.of(new Span(cursor, window.end()));
        }
        return gap;
    }

    @Override
    public final int coveredLength() {
        int length = 0;
        int count = this.spanCount();
        for (int index = 0; index < count; index++) {
            Span current = this.spanAt(index);
            length += current.end() - current.start();
        }
        return length;
    }

    /**
     * Compares covered positions with another member of this class family.
     *
     * @param object
     *            the object to compare
     * @return whether the objects cover the same positions
     */
    @Override
    public final boolean equals(Object object) {
        boolean equal = this == object;
        if (!equal && object instanceof IntervalCoverageSecondary) {
            IntervalCoverage other = (IntervalCoverageSecondary) object;
            int count = this.spanCount();
            equal = count == other.spanCount();
            for (int index = 0; index < count && equal; index++) {
                equal = this.spanAt(index).equals(other.spanAt(index));
            }
        }
        return equal;
    }

    /**
     * Computes a hash from the ordered maximal covered spans.
     *
     * @return the hash code of this coverage
     */
    @Override
    public final int hashCode() {
        int hash = 1;
        int count = this.spanCount();
        for (int index = 0; index < count; index++) {
            hash = HASH_MULTIPLIER * hash + this.spanAt(index).hashCode();
        }
        return hash;
    }

    /**
     * Formats the maximal covered spans as a brace-delimited list.
     *
     * @return the covered spans in start order, written as half-open spans
     */
    @Override
    public final String toString() {
        StringJoiner result = new StringJoiner(", ", "{", "}");
        int count = this.spanCount();
        for (int index = 0; index < count; index++) {
            Span span = this.spanAt(index);
            result.add("[" + span.start() + ", " + span.end() + ")");
        }
        return result.toString();
    }
}
