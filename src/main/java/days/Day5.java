package days;

import com.google.common.base.Splitter;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class Day5 implements Day {

  public void part1(List<String> input) {
    int numStacks = getNumOfStacks(input);
    List<Stack<String>> stacks = parseIntoStartingStacks(numStacks, input);
    List<Move> moves = parseMoves(input);

    // 1 at a time
    for (Move move : moves) {
      Stack<String> from = stacks.get(move.getFrom() - 1);
      Stack<String> to = stacks.get(move.getTo() - 1);
      for (int q = 0; q < move.getQuantity(); q++) {
        to.push(from.pop());
      }
    }

    System.out.println("Result: " + getResult(stacks));
  }

  public void part2(List<String> input) {
    int numStacks = getNumOfStacks(input);
    List<Stack<String>> stacks = parseIntoStartingStacks(numStacks, input);
    List<Move> moves = parseMoves(input);

    // quantity at a time
    for (Move move : moves) {
      int quantity = move.getQuantity();
      Stack<String> from = stacks.get(move.getFrom() - 1);
      Stack<String> to = stacks.get(move.getTo() - 1);

      Stack<String> cratesToMoveAtOnce = new Stack<>();
      for (int q = 0; q < quantity; q++) {
        cratesToMoveAtOnce.push(from.pop());
      }

      int amount = cratesToMoveAtOnce.size();
      for (int c = 0; c < amount; c++) {
        to.push(cratesToMoveAtOnce.pop());
      }
    }

    System.out.println("Result: " + getResult(stacks));
  }

  private String getResult(List<Stack<String>> stacks) {
    StringBuilder result = new StringBuilder();
    for (Stack<String> stack : stacks) {
      result.append(stack.peek());
    }
    return result.toString();
  }

  private int getNumOfStacks(List<String> input) {
    // let's pretend we can't look to see how many, since that's fun
    for (String line : input) {
      if (line.startsWith(" 1")) {
        int numStacks = Integer.parseInt(
          line.substring(line.length() - 2, line.length() - 1)
        );
        System.out.println("Num stacks total: " + numStacks);
        return numStacks;
      }
    }

    throw new IllegalArgumentException("input was wonky!");
  }

  private List<Stack<String>> parseIntoStartingStacks(
    int numStacks,
    List<String> input
  ) {
    List<List<String>> stackLists = populateStartingStackLists(
      numStacks,
      input
    );
    System.out.println("DEBUG: starting stackLists: " + stackLists);

    List<Stack<String>> startingStacks = new ArrayList<>(numStacks);
    for (List<String> stackList : stackLists) {
      startingStacks.add(stackListToStack(stackList));
    }

    return startingStacks;
  }

  private List<List<String>> populateStartingStackLists(
    int numStacks,
    List<String> input
  ) {
    List<List<String>> stackLists = new ArrayList<>(numStacks);
    for (int i = 0; i < numStacks; i++) {
      List<String> list = new ArrayList<>();
      stackLists.add(list);
    }

    for (String line : input) {
      if (line.startsWith(" 1")) {
        // we've hit stack #s, we're done
        return stackLists;
      }

      // j: indices in line where items would be in order are: 1, 5, 9, 13, +4, ...+4
      // and indices for which stackLists list to insert are floor(j/4)
      for (int j = 1; j < line.length(); j = j + 4) {
        String crateId = line.substring(j, j + 1);
        if (crateId.equals(" ")) {
          continue;
        }
        int whichStackList = Math.floorDiv(j, 4);
        stackLists.get(whichStackList).add(crateId);
      }
    }

    throw new IllegalArgumentException("input was wonky!");
  }

  private Stack<String> stackListToStack(List<String> stackList) {
    Stack<String> stack = new Stack<>();
    // reverse order creates actual stack that mimics list (could just use a stack instead of the list, but, wanted the flexibility)
    for (int i = stackList.size() - 1; i >= 0; i--) {
      stack.push(stackList.get(i));
    }
    return stack;
  }

  private class Move {

    private final int quantity;
    private final int from;
    private final int to;

    public Move(int quantity, int from, int to) {
      this.quantity = quantity;
      this.from = from;
      this.to = to;
    }

    public int getQuantity() {
      return quantity;
    }

    public int getFrom() {
      return from;
    }

    public int getTo() {
      return to;
    }
  }

  private static final Splitter SPLITTER = Splitter.on(" ");

  private List<Move> parseMoves(List<String> input) {
    List<Move> moves = new ArrayList<>();

    for (String line : input) {
      if (!line.startsWith("m")) { // "m"ove
        continue;
      }
      List<String> parts = SPLITTER.splitToList(line);
      moves.add(
        new Move(
          Integer.parseInt(parts.get(1)),
          Integer.parseInt(parts.get(3)),
          Integer.parseInt(parts.get(5))
        )
      );
    }

    return moves;
  }
}
