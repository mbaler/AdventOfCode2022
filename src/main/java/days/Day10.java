package days;

import com.google.common.base.Splitter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Day10 implements Day {

  public void part1(List<String> input) {
    Map<Integer, Integer> valuesAtCycles = computeValuesAtCycles(input, 220);
    System.out.println(valuesAtCycles);

    Set<Integer> CYCLES_TO_MEASURE = Set.of(20, 60, 100, 140, 180, 220);

    int signalStrengthSum = 0;
    for (Map.Entry<Integer, Integer> entry : valuesAtCycles.entrySet()) {
      int signalStrength = entry.getKey() * entry.getValue();
      System.out.println(
        "Strength at cycle " + entry.getKey() + ": " + signalStrength
      );
      if (CYCLES_TO_MEASURE.contains(entry.getKey())) {
        signalStrengthSum += signalStrength;
      }
    }
    System.out.println(
      "Sum of signal strengths at particular cycles: " + signalStrengthSum
    );
  }

  public void part2(List<String> input) {
    Map<Integer, Integer> valuesAtCycles = computeValuesAtCycles(input, 240);
    System.out.println(valuesAtCycles);
    drawCrt(valuesAtCycles);
  }

  private static final Splitter SPLITTER = Splitter.on(" ");

  private Map<Integer, Integer> computeValuesAtCycles(
    List<String> input,
    int totalCycles
  ) {
    Map<Integer, Integer> valuesAtCycles = new HashMap<>();

    int x = 1;
    Map<Integer, Integer> valueToAddByCycleToAddIt = new HashMap<>();

    int instructionToGet = 0;
    boolean midAdd = false;

    for (int currCycle = 1; currCycle <= totalCycles; currCycle++) {
      valuesAtCycles.put(currCycle, x);

      if (!midAdd && instructionToGet < input.size()) {
        String instruction = input.get(instructionToGet++);
        if (instruction.equals("noop")) {
          // nothing
        } else {
          // addx V
          valueToAddByCycleToAddIt.put(
            currCycle + 1, // add happens at end of next cycle
            Integer.parseInt(SPLITTER.splitToList(instruction).get(1))
          );
          midAdd = true;
        }
      }

      // end-of-cycle add check
      if (valueToAddByCycleToAddIt.containsKey(currCycle)) {
        x += valueToAddByCycleToAddIt.get(currCycle);
        valueToAddByCycleToAddIt.remove(currCycle);
        midAdd = false;
      }
    }

    return valuesAtCycles;
  }

  private static final int ROW_LEN = 40;
  private static final int NUM_ROWS = 6;

  private void drawCrt(Map<Integer, Integer> valuesAtCycles) {
    for (int cycle = 1; cycle <= ROW_LEN * NUM_ROWS; cycle++) {
      int p;
      if (cycle % ROW_LEN == 0) {
        // last pos
        p = ROW_LEN - 1;
      } else {
        p = (cycle % ROW_LEN) - 1;
      }

      System.out.print(draw(p, valuesAtCycles.get(cycle)));

      if (p == ROW_LEN - 1) {
        // new line after last pos
        System.out.print("\n");
      }
    }
  }

  private String draw(int crtPosition, int spriteMiddlePosition) {
    if (
      crtPosition == spriteMiddlePosition - 1 ||
      crtPosition == spriteMiddlePosition ||
      crtPosition == spriteMiddlePosition + 1
    ) {
      return "#";
    }
    return ".";
  }
}
