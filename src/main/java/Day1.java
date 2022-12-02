import java.io.File;
import java.io.FileNotFoundException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.stream.Collectors;

import com.google.common.base.Strings;

// input: # calories each elf is carrying
// e.g.
/**
 * 1000
 * 2000
 * 3000
 *
 * 4000
 *
 * 5000
 * 6000
 *
 * 7000
 * 8000
 * 9000
 *
 * 10000
 */
// equals:
//  The first Elf is carrying food with 1000, 2000, and 3000 Calories, a total of 6000 Calories.
//  The second Elf is carrying one food item with 4000 Calories.
//  The third Elf is carrying food with 5000 and 6000 Calories, a total of 11000 Calories.
//  The fourth Elf is carrying food with 7000, 8000, and 9000 Calories, a total of 24000 Calories.
//  The fifth Elf is carrying one food item with 10000 Calories.

// TODO: Find the Elf carrying the most Calories. How many total Calories is that Elf carrying?
// In the example above, this is 24000 (carried by the fourth Elf).

// Notes: welp, thought I might need to know about which dwarf was which, hence the map


public class Day1 {

  public static void main(String[] args) throws FileNotFoundException {
    Scanner scanner = new Scanner(new File("src/main/resources/day1input.txt"));
    Map<Integer, Integer> caloriesByElf = getCaloriesByElf(scanner);
    scanner.close();;

    Comparator<Map.Entry<Integer,Integer>> byCaloriesAscending = Map.Entry.comparingByValue();
    List<Entry<Integer, Integer>> entriesByMostCalories = caloriesByElf
        .entrySet()
        .stream()
        .sorted(byCaloriesAscending.reversed())
        .collect(Collectors.toList());

    int fattiestElf = entriesByMostCalories.get(0).getKey();
    int fattiestCalories = entriesByMostCalories.get(0).getValue();

    int topThreeFattiestCaloriesSum = entriesByMostCalories.stream().limit(3).mapToInt(Entry::getValue).sum();

    System.out.println("map: " + caloriesByElf);
    System.out.println("entriesByMostCalories: " + entriesByMostCalories);

    System.out.println("Fattiest elf is index #" + fattiestElf + " with ~~~" + fattiestCalories + "~~~ calories!");
    System.out.println("Top 3 fattiest are index #s: " + entriesByMostCalories.get(0).getKey() + ", " + entriesByMostCalories.get(1).getKey() + ", " + entriesByMostCalories.get(2).getKey() +
        " with a total of ~~~" + topThreeFattiestCaloriesSum + "~~~ calories among them!");
  }

  private static Map<Integer, Integer> getCaloriesByElf(Scanner scanner) {
    Map<Integer, Integer> caloriesByElf = new HashMap<>();

    int elfNum = 0;

    while (scanner.hasNext()) {
      String calories = scanner.nextLine();

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
