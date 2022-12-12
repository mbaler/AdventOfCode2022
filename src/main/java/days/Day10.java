package days;

import com.google.common.base.Splitter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Day10 implements Day {

  // screen and simple CPU  are both driven by a precise clock circuit which ticks at a constant rate; each tick is called a cycle
  // The CPU has a single register, X, which starts with the value 1. It supports only two instructions:
  //  - addx V takes two cycles to complete. After two cycles, the X register is increased by the value V. (V can be negative.)
  //  - noop takes one cycle to complete. It has no other effect.
  // puzzle input is these instructions

  // consider the signal strength (the cycle number multiplied by the value of the X register) during the 20th cycle and every 40 cycles after that
  // (that is, during the 20th, 60th, 100th, 140th, 180th, and 220th cycles)
  // TODO: Find the signal strength during the 20th, 60th, 100th, 140th, 180th, and 220th cycles.
  //  What is the sum of these six signal strengths?

  // p2
  // X register controls the horizontal position of a sprite
  // - the sprite is 3 pixels wide, and the X register sets the horizontal position of the MIDDLE of that sprite's 3 pixels
  // pixels on the CRT: 40 wide and 6 high
  // CRT screen draws the top row of pixels left-to-right, then the row below that, and so on.
  // The left-most pixel in each row is in position 0, and the right-most pixel in each row is in position 39
  // the CRT draws a single pixel during each cycle
  // here are the cycles during which the first and last pixel in each row are drawn:
  // Cycle   1 -> ######################################## <- Cycle  40
  // Cycle  41 -> ######################################## <- Cycle  80
  // Cycle  81 -> ######################################## <- Cycle 120
  // Cycle 121 -> ######################################## <- Cycle 160
  // Cycle 161 -> ######################################## <- Cycle 200
  // Cycle 201 -> ######################################## <- Cycle 240

  // If the sprite is positioned such that one of its three pixels is the pixel currently being drawn:
  //  - the screen produces a lit pixel (#)
  //  - otherwise, the screen leaves the pixel dark (.)
  // TODO: Render the image given by your program. What eight capital letters appear on your CRT?

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
