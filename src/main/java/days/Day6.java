package days;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Day6 implements Day {

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
