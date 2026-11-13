package examples;

import java.util.Optional;

import components.intervalcoverage.IntervalCoverage;
import components.intervalcoverage.IntervalCoverage1;
import components.intervalcoverage.IntervalCoverageKernel.Span;
import components.simplewriter.SimpleWriter;
import components.simplewriter.SimpleWriter1L;

/**
 * Finds and books the earliest available slot after a room cancellation.
 */
public final class RoomScheduler {

    /** Minutes per hour, for converting clock times to positions. */
    private static final int MINUTES_PER_HOUR = 60;

    /** Minutes requested by the next group. */
    private static final int REQUESTED_MINUTES = 45;

    /** Prevents instantiation. */
    private RoomScheduler() {
    }

    /**
     * Converts an hour and minute to minutes since midnight.
     *
     * @param hour
     *            the hour of the day
     * @param minute
     *            the minute of the hour
     * @return the minute of the day
     */
    private static int at(int hour, int minute) {
        return hour * MINUTES_PER_HOUR + minute;
    }

    /**
     * Formats a minute of the day as a clock time.
     *
     * @param position
     *            minutes since midnight
     * @return the clock time
     */
    private static String clockTime(int position) {
        return String.format("%02d:%02d", position / MINUTES_PER_HOUR,
                position % MINUTES_PER_HOUR);
    }

    /**
     * Runs a booking example using one room's occupancy.
     *
     * @param args
     *            command-line arguments (unused)
     */
    // Times below are the example's input data.
    // SUPPRESS CHECKSTYLE MagicNumber FOR NEXT 30 LINES
    public static void main(String[] args) {
        SimpleWriter out = new SimpleWriter1L();
        Span openHours = new Span(at(9, 0), at(12, 0));
        IntervalCoverage occupied = new IntervalCoverage1();
        occupied.cover(new Span(at(9, 0), at(10, 30)));
        occupied.cover(new Span(at(11, 0), at(12, 0)));

        out.println("45-minute slot before cancellation: "
                + occupied.firstGap(openHours, REQUESTED_MINUTES).isPresent());
        occupied.uncover(new Span(at(10, 0), at(10, 30)));
        Optional<Span> available = occupied.firstGap(openHours, REQUESTED_MINUTES);
        if (available.isPresent()) {
            int start = available.get().start();
            Span booking = new Span(start, start + REQUESTED_MINUTES);
            occupied.cover(booking);
            out.println("Booked " + clockTime(booking.start()) + " to "
                    + clockTime(booking.end()));
            out.println("Busy minutes: " + occupied.coveredLength());
        }
        out.close();
    }
}
