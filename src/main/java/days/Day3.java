package days;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Day3 implements Day {

  // Each rucksack has 2 large compartments
  // All items of a given type are meant to go into exactly 1 of the 2 compartments
  // The Elf that did the packing failed to follow this rule for exactly 1 item type per rucksack

  // puzzle input: a list of all of the items currently in each rucksack

  // Every item type is identified by a single lowercase or uppercase letter (that is, a and A refer to different types of items).

  // The list of items for each rucksack is given as characters all on a single line.
  // A given rucksack always has the same number of items in each of its two compartments,
  // so the first half of the characters represent items in the 1st compartment,
  // while the second half of the characters represent items in the 2nd compartment.

  // elf's failed packing for each sack means -> one letter, upper or lower, is in both the former and latter half of each sack (can be in each half multiple times)

  // To help prioritize item rearrangement, every item type can be converted to a priority:
  //    - Lowercase item types a through z have priorities 1 through 26.
  //    - Uppercase item types A through Z have priorities 27 through 52.

  // TODO: Find the item type that appears in both compartments of each rucksack. What is the /sum of the priorities/ of those item types?

  // strategy: for each type, identify item in both former and latter half compartments, then save its priority, then sum those all up

  // p2
  // Elves are divided into groups of 3
  // all 3 elves carry their badge item type in their sack, and that's the only item type shared by all 3
  // so, need to find the common item type between all three sacks to know that group's badge type
  // -- but, now, this thing doesn't need to be in both former and latter component of sack, just there at all

  // TODO: Find the item type that corresponds to the badges of each three-Elf group. What is the sum of the priorities of those item types?

  public void part1(List<String> input) {
    List<Integer> priorities = new ArrayList<>();

    for (String rucksack : input) {
      char errorItem = findErrorItem(rucksack);
      priorities.add(toPriority(errorItem));
    }

    System.out.println("Priorities: " + priorities);
    int sum = priorities.stream().mapToInt(Integer::intValue).sum();
    System.out.println("Sum: " + sum);
  }

  public void part2(List<String> input) {
    List<Integer> groupPriorities = new ArrayList<>();

    List<List<String>> groupList = splitIntoGroups(input);

    for (List<String> group : groupList) {
      char groupBadgeItem = findGroupBadgeItem(group);
      groupPriorities.add(toPriority(groupBadgeItem));
    }

    System.out.println("Group Priorities: " + groupPriorities);
    int sum = groupPriorities.stream().mapToInt(Integer::intValue).sum();
    System.out.println("Sum: " + sum);
  }

  private char findErrorItem(String rucksack) {
    int len = rucksack.length();
    int compartmentSize = len / 2;

    String formerCompartment = rucksack.substring(0, compartmentSize);
    String latterCompartment = rucksack.substring(compartmentSize, len);

    // find char that is in both
    Map<Character, Integer> formerExistence = new HashMap<>();

    for (char itemFormer : formerCompartment.toCharArray()) {
      formerExistence.put(itemFormer, 0);
    }

    for (char itemLatter : latterCompartment.toCharArray()) {
      if (formerExistence.containsKey(itemLatter)) {
        return itemLatter;
      }
    }

    throw new IllegalArgumentException(
      "impossible! no item in both compartments!"
    );
  }

  private List<List<String>> splitIntoGroups(List<String> input) {
    int sizeOfGroups = 3;
    List<List<String>> groupList = new ArrayList<>();

    List<String> group = new ArrayList<>();
    int numElf = 0;
    for (String elf : input) {
      if (numElf == sizeOfGroups) {
        groupList.add(group);
        group = new ArrayList<>();
        numElf = 0;
      }
      group.add(elf);
      numElf++;
    }
    groupList.add(group);

    return groupList;
  }

  private char findGroupBadgeItem(List<String> rucksacks) {
    if (rucksacks.size() != 3) {
      throw new IllegalArgumentException("Groups are always in 3s!");
    }
    Map<Character, Integer> numSacksPresentByChar = new HashMap<>();

    // initial sack population
    for (char item : rucksacks.get(0).toCharArray()) {
      if (!numSacksPresentByChar.containsKey(item)) {
        numSacksPresentByChar.put(item, 1);
      }
    }

    for (int i = 1; i < rucksacks.size(); i++) {
      for (char item : rucksacks.get(i).toCharArray()) {
        // only if item has been seen i times before do we care about it; and once we increment once, repeats in same sack get ignored
        Integer numSacksIn = numSacksPresentByChar.get(item);
        if (numSacksIn != null && numSacksIn == i) {
          numSacksPresentByChar.put(item, i + 1);
        }
      }
    }

    for (Map.Entry<Character, Integer> entry : numSacksPresentByChar.entrySet()) {
      if (entry.getValue() == rucksacks.size()) {
        return entry.getKey();
      }
    }
    throw new IllegalArgumentException("No badge found!");
  }

  private static final char LOWER_CASE_BASE = 'a';
  private static final int LOWER_CASE_PRIORITY_START = 1;
  private static final char UPPER_CASE_BASE = 'A';
  private static final int UPPER_CASE_PRIORITY_START = 27;

  private int toPriority(char item) {
    if (Character.isLowerCase(item)) {
      int offsetFromStartForLowerCase = (int) item - (int) LOWER_CASE_BASE;
      return LOWER_CASE_PRIORITY_START + offsetFromStartForLowerCase;
    } else {
      int offsetFromStartForUpperCase = (int) item - (int) UPPER_CASE_BASE;
      return UPPER_CASE_PRIORITY_START + offsetFromStartForUpperCase;
    }
  }
}
