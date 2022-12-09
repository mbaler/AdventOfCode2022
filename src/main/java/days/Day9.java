package days;

import com.google.common.base.Splitter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class Day9 implements Day {

  // Consider a rope with a knot at each end; these knots mark the head and the tail of the rope.
  // If the head moves far enough away from the tail, the tail is pulled toward the head.
  // model knot positions on 2d grid

  // input: series of motions for the Head

  // H and T must always be touching (diagonally adjacent and even overlapping both count as touching)
  // If the head is ever two steps directly up, down, left, or right from the tail, the tail must also move one step in that direction so it remains close enough
  // Otherwise, if the head and tail aren't touching and aren't in the same row or column, the tail always moves one step diagonally to keep up

  // You just need to work out where the tail goes as the head follows a series of motions.
  // Assume the head and the tail both start at the same position, overlapping

  // TODO: How many positions does the tail of the rope visit at least once?
  //  (where the starting pos is included)

  // p2
  // Rather than two knots, you now must simulate a rope consisting of ten knots.
  // One knot is still the head of the rope and moves according to the series of motions.
  // Each knot further down the rope follows the knot in front of it using the same rules as before.
  // knots now thought of as H, 1, 2, ..., 9
  // TODO: how many positions does the new tail, 9, visit?

  public void part1(List<String> input) {
    int numTailTouched = calculateNumLongTailTouched(input, 2);
    System.out.println("Num positions Tail touched: " + numTailTouched);
  }

  public void part2(List<String> input) {
    int numLongTailTouched = calculateNumLongTailTouched(input, 10);
    System.out.println(
      "Num positions long Tail touched: " + numLongTailTouched
    );
  }

  private static final Splitter SPLITTER = Splitter.on(" ");
  private static final int STARTING_X = 0;
  private static final int STARTING_Y = 0;

  private int calculateNumLongTailTouched(List<String> input, int numKnots) {
    Map<Coord, Boolean> tailTouchedCoords = new HashMap<>();

    int tailIndex = numKnots - 1;
    Coord[] tailKnots = new Coord[numKnots];
    for (int i = 0; i < numKnots; i++) {
      tailKnots[i] = new Coord(STARTING_X, STARTING_Y);
    }
    tailTouchedCoords.put(tailKnots[tailIndex], true);

    for (String motion : input) {
      List<String> parts = SPLITTER.splitToList(motion);
      String direction = parts.get(0);
      int dist = Integer.parseInt(parts.get(1));

      for (int d = 0; d < dist; d++) {
        tailKnots[0] = tailKnots[0].move(direction);
        for (int t = 0; t < tailIndex; t++) {
          tailKnots[t + 1] = Coord.newTail(tailKnots[t], tailKnots[t + 1]);
        }
        tailTouchedCoords.put(tailKnots[tailIndex], true);
      }
    }

    return tailTouchedCoords.size();
  }

  private static class Coord {

    private int x;
    private int y;

    Coord(int x, int y) {
      this.x = x;
      this.y = y;
    }

    public int getX() {
      return x;
    }

    public int getY() {
      return y;
    }

    public Coord move(String dir) {
      int x = this.x;
      int y = this.y;
      switch (dir) {
        case "U":
          y += 1;
          break;
        case "D":
          y -= 1;
          break;
        case "L":
          x -= 1;
          break;
        case "R":
          x += 1;
          break;
      }
      return new Coord(x, y);
    }

    public Coord add(int x, int y) {
      return new Coord(this.x + x, this.y + y);
    }

    public static Coord newTail(Coord H, Coord T) {
      int xDiff = H.getX() - T.getX();
      int yDiff = H.getY() - T.getY();

      if (yDiff == 2) {
        // top 3 pos
        return T.add(delta(xDiff), 1);
      }

      if (yDiff == -2) {
        // bot 3 pos
        return T.add(delta(xDiff), -1);
      }

      if (xDiff == 2) {
        // right 3 pos
        return T.add(1, delta(yDiff));
      }

      if (xDiff == -2) {
        // left 3 pos
        return T.add(-1, delta(yDiff));
      }

      return new Coord(T.getX(), T.getY());
    }

    private static int delta(int diff) {
      if (diff == 0) {
        return 0;
      }
      if (diff >= 1) {
        return 1;
      }
      // diff <= -1
      return -1;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      Coord coord = (Coord) o;
      return x == coord.x && y == coord.y;
    }

    @Override
    public int hashCode() {
      return Objects.hash(x, y);
    }
  }
}
