package days;

import com.google.common.base.Splitter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Day11 implements Day {

  public void part1(List<String> input) {
    List<Monkey> monkeys = parseInputToMonkeys(input);

    Collection<Monkey> monkeysPostRounds = calculateNumInspectionsByMonkeyOverRounds(
      monkeys,
      20,
      item -> item / 3
    );

    List<Monkey> monkeysByMostInspections = monkeysPostRounds
      .stream()
      .sorted(Comparator.comparing(Monkey::getNumInspections).reversed())
      .collect(Collectors.toList());
    System.out.println(monkeysByMostInspections);

    System.out.println(
      "Monkey business = " +
      monkeysByMostInspections.get(0).getNumInspections() *
      monkeysByMostInspections.get(1).getNumInspections()
    );
  }

  public void part2(List<String> input) {
    List<Monkey> monkeys = parseInputToMonkeys(input);

    int modder = monkeys
      .stream()
      .map(Monkey::getDivisor)
      .reduce(1, (a, b) -> a * b);
    System.out.println("modder: " + modder);

    Collection<Monkey> monkeysPostRounds = calculateNumInspectionsByMonkeyOverRounds(
      monkeys,
      10000,
      item -> {
        if (item > modder) { // prevents possible 0 weirdness if == ?
          return item % modder;
        }
        return item;
      }
    );

    List<Monkey> monkeysByMostInspections = monkeysPostRounds
      .stream()
      .sorted(Comparator.comparing(Monkey::getNumInspections).reversed())
      .collect(Collectors.toList());
    System.out.println(monkeysByMostInspections);

    System.out.println(
      "Monkey business = " +
      monkeysByMostInspections.get(0).getNumInspections() *
      monkeysByMostInspections.get(1).getNumInspections()
    );
  }

  private static final Splitter COLON_SPLITTER = Splitter.on(": ");
  private static final Splitter COMMA_SPLITTER = Splitter.on(", ");

  private List<Monkey> parseInputToMonkeys(List<String> input) {
    List<Monkey> monkeys = new ArrayList<>();

    int monkeyNum = -1;
    List<Long> startingItems = null;
    Function<Long, Long> inspectionOperation = null;
    int divisor = -1;
    Function<Long, Boolean> test = null;
    int trueTarget = -1;
    int falseTarget = -1;

    for (String line : input) {
      if (line.startsWith("Monk")) {
        monkeyNum++;
        continue;
      }

      if (line.startsWith("  Starting")) {
        startingItems =
          COMMA_SPLITTER
            .splitToList(COLON_SPLITTER.splitToList(line).get(1))
            .stream()
            .map(Long::parseLong)
            .collect(Collectors.toList());
        continue;
      }

      if (line.startsWith("  Operation")) {
        String operationFunc = COLON_SPLITTER.splitToList(line).get(1); // e.g. "new = old * 19"

        Character operand = operationFunc.charAt(10);
        boolean multiply = operand.equals('*');
        String value = operationFunc.substring(12);
        if (multiply) {
          if (value.startsWith("old")) {
            inspectionOperation = old -> old * old;
          } else {
            inspectionOperation = old -> old * Integer.parseInt(value);
          }
        } else {
          // adding value
          if (value.startsWith("old")) {
            inspectionOperation = old -> old + old;
          } else {
            inspectionOperation = old -> old + Integer.parseInt(value);
          }
        }
        continue;
      }

      if (line.startsWith("  Test")) {
        String testFunc = COLON_SPLITTER.splitToList(line).get(1); // e.g. "divisible by 23"
        String value = testFunc.substring(13);
        divisor = Integer.parseInt(value);
        test = old -> old % Integer.parseInt(value) == 0;
        continue;
      }

      if (line.startsWith("    If true")) {
        String throwText = COLON_SPLITTER.splitToList(line).get(1); // e.g. "throw to monkey 0"
        trueTarget = Integer.parseInt(throwText.substring(16));
        continue;
      }

      if (line.startsWith("    If false")) {
        String throwText = COLON_SPLITTER.splitToList(line).get(1); // e.g. "throw to monkey 1"
        falseTarget = Integer.parseInt(throwText.substring(16));
        continue;
      }

      if (line.isEmpty()) {
        monkeys.add(
          new Monkey(
            monkeyNum,
            startingItems,
            inspectionOperation,
            divisor,
            test,
            trueTarget,
            falseTarget
          )
        );
        continue;
      }
    }

    // last monkey since no empty line after
    monkeys.add(
      new Monkey(
        monkeyNum,
        startingItems,
        inspectionOperation,
        divisor,
        test,
        trueTarget,
        falseTarget
      )
    );

    return monkeys;
  }

  private Collection<Monkey> calculateNumInspectionsByMonkeyOverRounds(
    List<Monkey> monkeys,
    int numRounds,
    Function<Long, Long> reliefOperation
  ) {
    Map<Integer, Monkey> monkeysByNum = monkeys
      .stream()
      .collect(Collectors.toMap(Monkey::getNumber, Function.identity()));

    for (int r = 0; r < numRounds; r++) {
      for (int m = 0; m < monkeys.size(); m++) {
        Monkey monkey = monkeysByNum.get(m);
        for (long itemWorryLevel : monkey.getItems()) {
          long itemWorryLevelPostInspectionAndRelief = reliefOperation.apply(
            monkey.applyInspectionOperation(itemWorryLevel)
          );

          int target = monkey.throwTarget(
            itemWorryLevelPostInspectionAndRelief
          );
          monkeysByNum
            .get(target)
            .addItem(itemWorryLevelPostInspectionAndRelief);
        }
        monkey.clearItems();
      }
    }

    return monkeysByNum.values();
  }

  private static class Monkey {

    private final int number;
    private final List<Long> items;
    private final Function<Long, Long> inspectionOperation;
    private final int divisor;
    private final Function<Long, Boolean> test;
    private final int trueTarget;
    private final int falseTarget;
    private long numInspections = 0L;

    Monkey(
      int number,
      List<Long> items,
      Function<Long, Long> inspectionOperation,
      int divisor,
      Function<Long, Boolean> test,
      int trueTarget,
      int falseTarget
    ) {
      this.number = number;
      this.items = items;
      this.inspectionOperation = inspectionOperation;
      this.divisor = divisor;
      this.test = test;
      this.trueTarget = trueTarget;
      this.falseTarget = falseTarget;
    }

    public int getNumber() {
      return number;
    }

    public List<Long> getItems() {
      return items;
    }

    public void addItem(long item) {
      items.add(item);
    }

    public void clearItems() {
      items.clear();
    }

    public long applyInspectionOperation(long itemWorryLevel) {
      numInspections++;
      return inspectionOperation.apply(itemWorryLevel);
    }

    public int getDivisor() {
      return divisor;
    }

    public int throwTarget(long itemWorryLevel) {
      return applyTest(itemWorryLevel) ? trueTarget : falseTarget;
    }

    private boolean applyTest(long itemWorryLevel) {
      return test.apply(itemWorryLevel);
    }

    public long getNumInspections() {
      return this.numInspections;
    }

    @Override
    public String toString() {
      return (
        "Monkey{" +
        "number=" +
        number +
        ", numInspections=" +
        numInspections +
        '}'
      );
    }
  }
}
