package examples;

import java.util.Optional;

import components.intervalcoverage.IntervalCoverage;
import components.intervalcoverage.IntervalCoverage1;
import components.intervalcoverage.IntervalCoverageKernel.Span;
import components.simplewriter.SimpleWriter;
import components.simplewriter.SimpleWriter1L;

/**
 * Tracks unique received bytes and requests the first missing range.
 */
public final class ChunkReceipt {

    /** Prevents instantiation. */
    private ChunkReceipt() {
    }

    /**
     * Simulates receiving chunks that have already been verified and stored.
     *
     * @param args
     *            command-line arguments (unused)
     */
    // Offsets below are the example's input data.
    // SUPPRESS CHECKSTYLE MagicNumber FOR NEXT 33 LINES
    public static void main(String[] args) {
        SimpleWriter out = new SimpleWriter1L();
        Span file = new Span(0, 12);
        IntervalCoverage received = new IntervalCoverage1();
        received.cover(new Span(8, 12));
        received.cover(new Span(0, 4));
        received.cover(new Span(2, 6));
        received.cover(new Span(0, 4));

        out.println("Unique bytes received: " + received.coveredLength()
                + "/" + file.end());
        Optional<Span> missing = received.firstGap(file, 1);
        if (missing.isPresent()) {
            Span requested = missing.get();
            out.println("Requesting bytes [" + requested.start() + ", "
                    + requested.end() + ")");
            received.cover(requested);
        }
        out.println("Complete file: " + received.covers(file));
        out.close();
    }
}
