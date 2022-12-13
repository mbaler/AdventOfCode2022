package days;

import com.google.common.base.Splitter;
import java.util.List;

public class Day4 implements Day {

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
