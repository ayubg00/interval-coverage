package components.intervalcoverage;

import java.util.ArrayList;

/**
 * Interval coverage backed by an array list of immutable spans. The list
 * supports constant-time indexed reads and linear scans for updates.
 *
 * @convention rep is nonnull and contains only nonnull spans with
 *             {@code 0 <= start < end}. In list order, each span's end is strictly
 *             less than the next span's start.
 * @correspondence this is the union of the integer positions in the spans
 *                 in rep
 */
public final class IntervalCoverage1 extends IntervalCoverageSecondary {

    /**
     * The maximal covered spans in ascending start order.
     */
    private ArrayList<Span> rep;

    /**
     * Establishes a fresh empty representation.
     *
     * @replaces this.rep
     * @ensures this.rep is a new empty array list
     */
    private void createNewRep() {
        this.rep = new ArrayList<>();
    }

    /**
     * Creates empty coverage.
     *
     * @ensures this covers no positions
     */
    public IntervalCoverage1() {
        this.createNewRep();
    }

    @Override
    public IntervalCoverage newInstance() {
        return new IntervalCoverage1();
    }

    @Override
    public void clear() {
        this.createNewRep();
    }

    @Override
    public void transferFrom(IntervalCoverage source) {
        assert source instanceof IntervalCoverage1
                : "Requires: source has the same runtime type as this";
        assert source != this : "Requires: source is not this";
        IntervalCoverage1 localSource = (IntervalCoverage1) source;
        ArrayList<Span> moved = localSource.rep;
        localSource.createNewRep();
        this.rep = moved;
    }

    @Override
    public void cover(Span span) {
        assert span != null : "Requires: span is not null";
        ArrayList<Span> updated = new ArrayList<>();
        int start = span.start();
        int end = span.end();
        int index = 0;
        int count = this.rep.size();
        while (index < count && this.rep.get(index).end() < start) {
            updated.add(this.rep.get(index));
            index++;
        }
        while (index < count && this.rep.get(index).start() <= end) {
            Span current = this.rep.get(index);
            start = Math.min(start, current.start());
            end = Math.max(end, current.end());
            index++;
        }
        updated.add(new Span(start, end));
        for (int remaining = index; remaining < count; remaining++) {
            updated.add(this.rep.get(remaining));
        }
        this.rep = updated;
    }

    @Override
    public void uncover(Span span) {
        assert span != null : "Requires: span is not null";
        ArrayList<Span> updated = new ArrayList<>();
        for (Span current : this.rep) {
            if (current.end() <= span.start() || span.end() <= current.start()) {
                updated.add(current);
            } else {
                if (current.start() < span.start()) {
                    updated.add(new Span(current.start(), span.start()));
                }
                if (span.end() < current.end()) {
                    updated.add(new Span(span.end(), current.end()));
                }
            }
        }
        this.rep = updated;
    }

    @Override
    public int spanCount() {
        return this.rep.size();
    }

    @Override
    public Span spanAt(int index) {
        assert 0 <= index && index < this.rep.size()
                : "Requires: 0 <= index < spanCount";
        return this.rep.get(index);
    }
}
