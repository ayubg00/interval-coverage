# Interval Coverage

A Java component that keeps track of which parts of a range are covered.
You can add or remove ranges, count covered positions, and find the first
gap that fits. Overlapping or touching ranges are stored as one span.

A span like `[2, 5)` covers 2, 3, and 4. See the
[design notes](doc/INTERVAL-COVERAGE.md) for how the classes fit together.

## Examples

- [RoomScheduler](src/examples/RoomScheduler.java) finds time for a
  45-minute meeting after a cancellation.
- [ChunkReceipt](src/examples/ChunkReceipt.java) tracks bytes received and
  requests the missing part of a file.

## Run it

Use JDK 17 or newer. Download
[components.jar](https://cse22x1.engineering.osu.edu/common/components.jar),
[JUnit 4.13.2](https://repo.maven.apache.org/maven2/junit/junit/4.13.2/junit-4.13.2.jar),
and [Hamcrest Core 1.3](https://repo.maven.apache.org/maven2/org/hamcrest/hamcrest-core/1.3/hamcrest-core-1.3.jar)
and put them in `lib/`.

On macOS or Linux, run these commands from the project folder.

```sh
mkdir -p bin
javac --release 17 -cp 'lib/*' -d bin $(find src test -name '*.java')
java -ea -cp 'bin:lib/*' examples.RoomScheduler
java -ea -cp 'bin:lib/*' examples.ChunkReceipt
```

To run the tests:

```sh
java -ea -cp 'bin:lib/*' org.junit.runner.JUnitCore \
  components.intervalcoverage.IntervalCoverage1Test \
  components.intervalcoverage.IntervalCoverageSecondaryTest
```
