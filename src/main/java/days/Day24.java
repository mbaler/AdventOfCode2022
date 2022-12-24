package days;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

public class Day24 implements Day {

  private static final char WALL = '#';

  private Coord start;
  private Coord exit;

  private Set<Coord> walls = new HashSet<>();
  private Set<Blizzard> blizzards = new HashSet<>();
  private SetMultimap<Integer, Coord> blizzardsByTime = HashMultimap.create();

  public void part1(List<String> input) {
    parseInput(input);

    initializeBlizzardsByTime();

    long minMinutesToExit = calcMinMinutesToGoal(0, start);
    System.out.println("Min minutes to exit: " + minMinutesToExit);
  }

  public void part2(List<String> input) {
    parseInput(input);

    initializeBlizzardsByTime();

    int minMinutesToExit = calcMinMinutesToGoal(0, start);
    int minMinutesToExitAndGetBackToStart = calcMinMinutesToGoal(
      minMinutesToExit,
      exit
    );
    int minMinutesToExitAndGoBackAndExitAgain = calcMinMinutesToGoal(
      minMinutesToExitAndGetBackToStart,
      start
    );
    System.out.println(
      "Min minutes to exit, then grab snacks at start, then exit again: " +
      minMinutesToExitAndGoBackAndExitAgain
    );
  }

  private void parseInput(List<String> input) {
    for (int y = 0; y < input.size(); y++) {
      String line = input.get(y);
      for (int x = 0; x < line.length(); x++) {
        char c = line.charAt(x);
        Coord current = new Coord(x, y);

        if (y == 0 && c == '.') {
          start = current;
        }
        if (y == input.size() - 1 && c == '.') {
          exit = current;
        }

        if (c == WALL) {
          walls.add(current);
        }

        if (c == Direction.UP.getChar()) {
          blizzards.add(new Blizzard(Direction.UP, current));
        }
        if (c == Direction.DOWN.getChar()) {
          blizzards.add(new Blizzard(Direction.DOWN, current));
        }
        if (c == Direction.LEFT.getChar()) {
          blizzards.add(new Blizzard(Direction.LEFT, current));
        }
        if (c == Direction.RIGHT.getChar()) {
          blizzards.add(new Blizzard(Direction.RIGHT, current));
        }
      }
    }
  }

  private void initializeBlizzardsByTime() {
    for (int t = 0; t < 5_000; t++) { // arbitrarily large, enough to not run out
      blizzardsByTime.putAll(t, calcBlizardCoordsWhenMoveAtTime(t));
    }
  }

  private int calcMinMinutesToGoal(int startingMinute, Coord startingCoord) {
    int minMinutesToExit = Integer.MAX_VALUE;

    Coord goal = startingCoord.equals(start) ? exit : start;

    MazeState initial = new MazeState(startingMinute, startingCoord);

    Queue<MazeState> q = new LinkedList<>();
    q.add(initial);

    Set<MazeState> seen = new HashSet<>();

    while (!q.isEmpty()) {
      MazeState state = q.poll();
      int currMinute = state.minute();
      Coord currPos = state.pos();

      if (currPos.equals(goal)) {
        // theoretically first of these we see is the smallest cause order of queue, but do min anyways?
        if (currMinute < minMinutesToExit) {
          minMinutesToExit = currMinute;
        }
        continue;
      }

      if (currMinute >= minMinutesToExit) {
        continue;
      }

      if (seen.contains(state)) {
        continue;
      }
      seen.add(state);

      Set<Coord> newBlizzards = blizzardsByTime.get(currMinute);

      if (!inBlizzard(currPos, newBlizzards)) { // wait
        q.add(new MazeState(currMinute + 1, currPos));
      }

      Coord up = getNextInDirection(currPos, Direction.UP);
      if (canIMoveHere(up, newBlizzards)) {
        q.add(new MazeState(currMinute + 1, up));
      }

      Coord down = getNextInDirection(currPos, Direction.DOWN);
      if (canIMoveHere(down, newBlizzards)) {
        q.add(new MazeState(currMinute + 1, down));
      }

      Coord left = getNextInDirection(currPos, Direction.LEFT);
      if (canIMoveHere(left, newBlizzards)) {
        q.add(new MazeState(currMinute + 1, left));
      }

      Coord right = getNextInDirection(currPos, Direction.RIGHT);
      if (canIMoveHere(right, newBlizzards)) {
        q.add(new MazeState(currMinute + 1, right));
      }
    }

    return minMinutesToExit;
  }

  private Set<Coord> calcBlizardCoordsWhenMoveAtTime(int currentMinute) {
    int numMoves = currentMinute + 1;

    int height = exit.y() - 1;
    int width = exit.x();

    Set<Coord> blizzardCoords = new HashSet<>();

    for (Blizzard b : blizzards) {
      Coord currCoord = b.coord();
      Coord newCoord =
        switch (b.dir()) {
          case UP -> {
            int val = currCoord.y() - numMoves;
            int y = val > 0 ? val : height - (Math.abs(val) % height);
            yield new Coord(currCoord.x(), y);
          }
          case DOWN -> {
            int val = (currCoord.y() + numMoves) % height;
            int y = val == 0 ? height : val;
            yield new Coord(currCoord.x(), y);
          }
          case LEFT -> {
            int val = currCoord.x() - numMoves;
            int x = val > 0 ? val : width - (Math.abs(val) % width);
            yield new Coord(x, currCoord.y());
          }
          case RIGHT -> {
            int val = (currCoord.x() + numMoves) % width;
            int x = val == 0 ? width : val;
            yield new Coord(x, currCoord.y());
          }
        };
      blizzardCoords.add(newCoord);
    }

    return blizzardCoords;
  }

  private Coord getNextInDirection(Coord current, Direction dir) {
    return switch (dir) {
      case UP -> new Coord(current.x(), current.y() - 1);
      case DOWN -> new Coord(current.x(), current.y() + 1);
      case LEFT -> new Coord(current.x() - 1, current.y());
      case RIGHT -> new Coord(current.x() + 1, current.y());
    };
  }

  private boolean canIMoveHere(Coord proposed, Set<Coord> blizzardCoords) {
    // can't go off map
    if (proposed.y() < start.y() || proposed.y() > exit.y()) {
      return false;
    }

    // can't go into wall
    if (walls.contains(proposed)) {
      return false;
    }

    // can't enter blizzard
    if (inBlizzard(proposed, blizzardCoords)) {
      return false;
    }

    return true;
  }

  private boolean inBlizzard(Coord c, Set<Coord> blizzardCoords) {
    return blizzardCoords.contains(c);
  }

  private enum Direction {
    UP('^'),
    DOWN('v'),
    LEFT('<'),
    RIGHT('>');

    private final char c;

    Direction(char c) {
      this.c = c;
    }

    public char getChar() {
      return c;
    }
  }

  private record Coord(int x, int y) {}

  private record Blizzard(Direction dir, Coord coord) {}

  private record MazeState(int minute, Coord pos) {}
}
