package days;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Day6 implements ADay {

  // To be able to communicate with the Elves, the device needs to lock on to their signal.
  // The signal is a series of seemingly-random characters that the device receives one at a time.

  // To fix the communication system, you need to add a subroutine to the device that detects a start-of-packet marker in the datastream.
  // In the protocol being used by the Elves, the start of a packet is indicated by a sequence of four characters that are all different.

  // The device will send your subroutine a datastream buffer (your puzzle input); your subroutine needs to identify the first position where the four most recently received characters were all different.
  // TODO: Specifically, it needs to report the number of characters (1-indexed) from the beginning of the buffer to the end of the first such four-character marker (all 4 chars are different).

  // p2
  // same, but 14-char distinct sequence instead of 4

  public void part1(List<String> input) {
    String buffer = input.get(0);
    int zeroIndexedAnswer = findZeroIndexedPosition(buffer, 4);
    int oneIndexedAnswer = zeroIndexedAnswer + 1;
    System.out.println("Num characters = " + oneIndexedAnswer);
  }

  public void part2(List<String> input) {
    String buffer = input.get(0);
    int zeroIndexedAnswer = findZeroIndexedPosition(buffer, 14);
    int oneIndexedAnswer = zeroIndexedAnswer + 1;
    System.out.println("Num characters = " + oneIndexedAnswer);
  }

  private int findZeroIndexedPosition(String buffer, int sizeOfUniqueSequence) {
    for (int i = 0; i <= buffer.length() - sizeOfUniqueSequence; i++) {
      boolean isUnique = isSequenceFromStartUnique(
        buffer,
        sizeOfUniqueSequence,
        i
      );
      if (isUnique) {
        return i + sizeOfUniqueSequence - 1;
      }
    }
    throw new IllegalArgumentException(
      "Never found unique " + sizeOfUniqueSequence + "sequence!"
    );
  }

  private boolean isSequenceFromStartUnique(
    String buffer,
    int sizeOfUniqueSequence,
    int start
  ) {
    Map<Character, Boolean> seen = new HashMap<>(sizeOfUniqueSequence);
    for (int c = start; c < start + sizeOfUniqueSequence; c++) {
      char current = buffer.charAt(c);
      if (seen.containsKey(current)) {
        return false;
      }
      seen.put(current, true);
    }
    return true;
  }
}
