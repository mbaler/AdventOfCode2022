package days;

import com.google.common.base.Splitter;
import java.util.List;

public class Day4 implements Day {

  // each section has unique int ID #
  // elves paired up, each pair given a range of section IDs, e.g. 3-5 (3, 4, 5) or 12-12 (12)
  // input: list of pair section assignments, e.g.
  // 2-4,6-8
  // 2-3,4-5
  // 5-7,7-9
  // 2-8,3-7
  // 6-6,4-6
  // 2-6,4-8
  // some assignments fully contain others -- e.g. 2-8 fully contains 3-7, and 6-6 is fully contained by 4-6

  // TODO: in how many assignment pairs (lines) does one range fully contain the other?

  // p2
  // instead of caring only about full containments, we now want the # of assignment pairs that have /any overlap at all/

  public void part1(List<String> input) {
    int numContainments = 0;
    for (String assignmentPairLine : input) {
      RangePair rangePair = toRangePair(assignmentPairLine);
      if (
        doesOneRangeContainOther(rangePair.getFirst(), rangePair.getSecond())
      ) {
        numContainments++;
      }
    }

    System.out.println(
      "Number of assignment pairs that have a containment: " + numContainments
    );
  }

  public void part2(List<String> input) {
    int numOverlaps = 0;
    for (String assignmentPairLine : input) {
      RangePair rangePair = toRangePair(assignmentPairLine);
      if (
        doesOneRangeOverlapOther(rangePair.getFirst(), rangePair.getSecond())
      ) {
        numOverlaps++;
      }
    }

    System.out.println(
      "Number of assignment pairs that have a containment: " + numOverlaps
    );
  }

  private class Range {

    private final int start;
    private final int end;

    Range(List<String> bounds) {
      this(Integer.parseInt(bounds.get(0)), Integer.parseInt(bounds.get(1)));
    }

    Range(int start, int end) {
      this.start = start;
      this.end = end;
    }

    public int getStart() {
      return start;
    }

    public int getEnd() {
      return end;
    }
  }

  private class RangePair {

    private final Range first;
    private final Range second;

    RangePair(Range first, Range second) {
      this.first = first;
      this.second = second;
    }

    public Range getFirst() {
      return first;
    }

    public Range getSecond() {
      return second;
    }
  }

  private static final Splitter PAIR_SPLITTER = Splitter.on(",");
  private static final Splitter RANGE_SPLITTER = Splitter.on("-");

  private RangePair toRangePair(String assignmentPairLine) {
    List<String> assignments = PAIR_SPLITTER.splitToList(assignmentPairLine);

    String firstAssignmentRange = assignments.get(0);
    String secondAssignmentRange = assignments.get(1);

    List<String> firstRangeBounds = RANGE_SPLITTER.splitToList(
      firstAssignmentRange
    );
    List<String> secondRangeBounds = RANGE_SPLITTER.splitToList(
      secondAssignmentRange
    );

    Range firstRange = new Range(firstRangeBounds);
    Range secondRange = new Range(secondRangeBounds);

    return new RangePair(firstRange, secondRange);
  }

  private boolean doesOneRangeContainOther(Range range1, Range range2) {
    // 1 contained by 2
    if (
      range1.getStart() >= range2.getStart() &&
      range1.getEnd() <= range2.getEnd()
    ) {
      return true;
    }
    // 2 contained by 1
    if (
      range1.getStart() <= range2.getStart() &&
      range1.getEnd() >= range2.getEnd()
    ) {
      return true;
    }
    return false;
  }

  private boolean doesOneRangeOverlapOther(Range range1, Range range2) {
    // 1 begins within 2
    if (
      range1.getStart() >= range2.getStart() &&
      range1.getStart() <= range2.getEnd()
    ) {
      return true;
    }
    // 2 begins within 1
    if (
      range2.getStart() >= range1.getStart() &&
      range2.getStart() <= range1.getEnd()
    ) {
      return true;
    }
    return false;
  }
}
