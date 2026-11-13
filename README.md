# Interval Coverage

One instance represents the integer positions currently covered on a single
axis. Client programs can add or subtract coverage, check whether a range is
fully covered, and find the first gap of a requested length. Positions can
represent minutes, byte offsets, or any other unit chosen by the client.

A new instance is empty. A span `[start, end)` includes its start and excludes
its end, with `0 <= start < end <= Integer.MAX_VALUE`. Adding overlapping or
adjacent spans combines them. Subtracting an interior span can split coverage
in two. The component stores coverage only. Clients own booking records,
time zones, file contents, and decisions about which spans to add or remove.

## API design

See the [hierarchy and design notes](doc/INTERVAL-COVERAGE.md). Use
`IntervalCoverage` as the variable type and `IntervalCoverage1` to construct an
instance. The immutable `Span` record supplies the endpoints.

| Operation | Result |
| --- | --- |
| `cover(span)` | Adds the span's positions to the covered set. |
| `uncover(span)` | Removes the span's positions from the covered set. |
| `spanCount()`, `spanAt(index)` | Enumerate the maximal covered spans in ascending start order. |
| `covers(span)` | Reports whether the entire span is covered. |
| `firstGap(window, minimumLength)` | Finds the full first uncovered span of at least the requested length, clipped to the window. |
| `coveredLength()` | Counts covered positions once. |
| `newInstance()`, `clear()`, `transferFrom(source)` | Create, reset, or move coverage using OSU's `Standard` contract. |

Coverage has value equality across implementations that extend
`IntervalCoverageSecondary`. Its hash code can change when its value changes,
so use a stable value as a hash-based collection key.

## Build and test

Use JDK 17 or newer. Put the official
[OSU components.jar](https://cse22x1.engineering.osu.edu/common/components.jar),
[JUnit 4.13.2](https://repo.maven.apache.org/maven2/junit/junit/4.13.2/junit-4.13.2.jar),
and [Hamcrest Core 1.3](https://repo.maven.apache.org/maven2/org/hamcrest/hamcrest-core/1.3/hamcrest-core-1.3.jar)
in `lib/`. The template ignores dependency JARs and compiled classes. VS Code
uses the template's recommended extensions, formatter, and Checkstyle settings.

Run these commands from the repository root on macOS or Linux:

```sh
mkdir -p bin
javac --release 17 -Xlint:all -Werror -cp 'lib/*' -d bin $(find src test -name '*.java')
java -ea -cp 'bin:lib/*' org.junit.runner.JUnitCore \
  components.intervalcoverage.IntervalCoverage1Test \
  components.intervalcoverage.IntervalCoverageSecondaryTest
```

Use `-ea` to enable the OSU contract assertions. With a local Checkstyle 9.3
distribution, run the template rules on both source trees:

```sh
java -jar /path/to/checkstyle-9.3-all.jar -c .vscode/osu-cse-checkstyle-config.xml src test
```

## Run the examples

```sh
java -ea -cp 'bin:lib/*' examples.RoomScheduler
java -ea -cp 'bin:lib/*' examples.ChunkReceipt
```

- [RoomScheduler](src/examples/RoomScheduler.java) removes a cancelled booking,
  finds the first gap that fits a 45-minute meeting, and books part of it.
  The new booking runs from 10:00 to 10:45.
- [ChunkReceipt](src/examples/ChunkReceipt.java) records out-of-order,
  overlapping, and duplicate arrivals. It counts 10 of 12 distinct bytes,
  requests exactly `[6, 8)`, and confirms completion after receipt.
