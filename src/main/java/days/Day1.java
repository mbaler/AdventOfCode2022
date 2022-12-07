package days;

import com.google.common.base.Strings;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

// input: # calories each elf is carrying
// e.g.
// 1000
// 2000
// 3000
//
// 4000
//
// 5000
// 6000
//
// 7000
// 8000
// 9000
//
// 10000

// equals:
//  The first Elf is carrying food with 1000, 2000, and 3000 Calories, a total of 6000 Calories.
//  The second Elf is carrying one food item with 4000 Calories.
//  The third Elf is carrying food with 5000 and 6000 Calories, a total of 11000 Calories.
//  The fourth Elf is carrying food with 7000, 8000, and 9000 Calories, a total of 24000 Calories.
//  The fifth Elf is carrying one food item with 10000 Calories.

// TODO: Find the Elf carrying the most Calories. How many total Calories is that Elf carrying?
// In the example above, this is 24000 (carried by the fourth Elf).

// Notes: welp, thought I might need to know about which dwarf was which, hence the map

public class Day1 implements Day {

  public void part1(List<String> input) {
    List<Entry<Integer, Integer>> entriesByMostCalories = getCaloriesByElfEntriesOrderedByMostCalories(
      input
    );

    int fattiestElf = entriesByMostCalories.get(0).getKey();
    int fattiestCalories = entriesByMostCalories.get(0).getValue();

    System.out.println("entriesByMostCalories: " + entriesByMostCalories);
    System.out.println(
      "Fattiest elf is index #" +
      fattiestElf +
      " with ~~~" +
      fattiestCalories +
      "~~~ calories!"
    );
  }

  public void part2(List<String> input) {
    List<Entry<Integer, Integer>> entriesByMostCalories = getCaloriesByElfEntriesOrderedByMostCalories(
      input
    );

    int topThreeFattiestCaloriesSum = entriesByMostCalories
      .stream()
      .limit(3)
      .mapToInt(Entry::getValue)
      .sum();

    System.out.println(
      "Top 3 fattiest are index #s: " +
      entriesByMostCalories.get(0).getKey() +
      ", " +
      entriesByMostCalories.get(1).getKey() +
      ", " +
      entriesByMostCalories.get(2).getKey() +
      " with a total of ~~~" +
      topThreeFattiestCaloriesSum +
      "~~~ calories among them!"
    );
  }

  private List<Entry<Integer, Integer>> getCaloriesByElfEntriesOrderedByMostCalories(
    List<String> input
  ) {
    Map<Integer, Integer> caloriesByElf = getCaloriesByElf(input);
    Comparator<Map.Entry<Integer, Integer>> byCaloriesAscending = Map.Entry.comparingByValue();

    return caloriesByElf
      .entrySet()
      .stream()
      .sorted(byCaloriesAscending.reversed())
      .collect(Collectors.toList());
  }

  private static Map<Integer, Integer> getCaloriesByElf(List<String> input) {
    Map<Integer, Integer> caloriesByElf = new HashMap<>();

    int elfNum = 0;

    for (String calories : input) {
      if (Strings.isNullOrEmpty(calories)) {
        elfNum++;
        continue;
      }

      int currentCalories = caloriesByElf.computeIfAbsent(elfNum, ignored -> 0);
      caloriesByElf.put(elfNum, currentCalories + Integer.parseInt(calories));
    }

    return caloriesByElf;
  }
}
