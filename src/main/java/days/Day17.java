package days;

import com.google.common.collect.ImmutableMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class Day17 implements Day {

  private static final long LEFT_WALL_X = 0L;
  private static final long RIGHT_WALL_X = 8L;
  // valid area -- x=1-7
  private static final long FLOOR_Y = 0;

  private long finalHighestRockY;
  private long heightViaCycles;

  public void part1(List<String> input) {
    List<Direction> jets = parseInput(input);

    finalHighestRockY = 0;
    heightViaCycles = 0;

    fallRocks(jets, 2022L);

    System.out.println("Height: " + finalHighestRockY);
  }

  public void part2(List<String> input) {
    List<Direction> jets = parseInput(input);

    finalHighestRockY = 0;
    heightViaCycles = 0;

    fallRocks(jets, 1000000000000L);

    System.out.println("Height: " + (heightViaCycles + finalHighestRockY));
  }

  private List<Direction> parseInput(List<String> input) {
    List<Direction> jets = new ArrayList<>();
    for (char c : input.get(0).toCharArray()) {
      jets.add(Direction.fromChar(c));
    }
    return jets;
  }

  private Set<Coord> fallRocks(List<Direction> jets, long maxRocks) {
    Map<RockMemo, RockState> memo = new HashMap<>();

    Set<Coord> grid = new HashSet<>();

    long highestRockY = FLOOR_Y;

    for (long x = 1L; x <= 7L; x++) {
      grid.add(new Coord(x, FLOOR_Y));
    }

    long round = 0L;

    boolean newRock = true;

    Shape currShape = null;
    Coord currShapeBotLeftCoord = null;

    long numRocksSettled = 0L;

    while (numRocksSettled < maxRocks) {
      if (round % 2 == 0) { //// jet
        if (newRock) {
          currShape = Shape.getShape(numRocksSettled);
          long startYBottomEdge = highestRockY + 4; // 3 in between up from highest
          currShapeBotLeftCoord =
            new Coord(currShape.getBotLeftX(), startYBottomEdge);
          newRock = false;
        }

        //// jet
        Direction jet = getJet(jets, round);
        boolean canBeMoved = canBeMoved(
          currShape,
          currShapeBotLeftCoord,
          jet,
          grid
        );
        if (canBeMoved) {
          currShapeBotLeftCoord =
            jet == Direction.LEFT
              ? Coord.oneLeft(currShapeBotLeftCoord)
              : Coord.oneRight(currShapeBotLeftCoord);
        }
      } else { //// fall
        boolean canFallDown = canFallDown(
          currShape,
          currShapeBotLeftCoord,
          grid
        );
        if (!canFallDown) {
          numRocksSettled++;
          newRock = true;
          long topYSettled =
            currShapeBotLeftCoord.getY() + currShape.getTopYOffsetFromBot();
          if (topYSettled > highestRockY) {
            highestRockY = topYSettled;
          }
          grid = fillInGrid(currShape, currShapeBotLeftCoord, grid);

          RockMemo currMemoKey = new RockMemo(
            getCurrentJetIndex(jets, round),
            currShape,
            calcRecentRockLayout(grid, highestRockY)
          );
          long currentNumRocksSettledToPutInMemo = numRocksSettled; // theoretically not necessary
          if (
            numRocksSettled > 2022 && // theoretically not necessary
            memo.containsKey(currMemoKey) &&
            heightViaCycles == 0 // theoretically not necessary
          ) {
            // then we've seen this same jet-shape-snapshot match before -- cycle detected!
            RockState sawBefore = memo.get(currMemoKey);
            long highestRockYBefore = sawBefore.getMaxHeight();
            long numRocksSettledBefore = sawBefore.getNumRocksSettled();

            long changeInHeightPerCycle = highestRockY - highestRockYBefore;
            long changeInRocksSettledPerCycle =
              numRocksSettled - numRocksSettledBefore;

            long numRocksToGo = maxRocks - numRocksSettled;
            long numFullCyclesPossibleFromHere = Math.floorDiv(
              numRocksToGo,
              changeInRocksSettledPerCycle
            );

            heightViaCycles =
              changeInHeightPerCycle * numFullCyclesPossibleFromHere;
            numRocksSettled +=
              changeInRocksSettledPerCycle * numFullCyclesPossibleFromHere;
          }

          memo.put(
            currMemoKey,
            new RockState(highestRockY, currentNumRocksSettledToPutInMemo)
          );
        } else {
          // else, moved
          currShapeBotLeftCoord = Coord.oneDown(currShapeBotLeftCoord);
        }
      }
      round++;
    }

    finalHighestRockY = highestRockY;

    return grid;
  }

  private boolean canBeMoved(
    Shape currShape,
    Coord botLeftCoord,
    Direction jet,
    Set<Coord> grid
  ) {
    if (jet == Direction.LEFT) {
      long farthestLeftXCoord = getFarthestLeftXCoordOfShape(
        currShape,
        botLeftCoord
      );
      if (farthestLeftXCoord <= LEFT_WALL_X + 1) {
        return false;
      }
      return canSafelyMoveToLeft(currShape, botLeftCoord, grid);
    } else { // RIGHT
      long farthestRightXCoord = getFarthestRightXCoordOfShape(
        currShape,
        botLeftCoord
      );
      if (farthestRightXCoord >= RIGHT_WALL_X - 1) {
        return false;
      }
      return canSafelyMoveToRight(currShape, botLeftCoord, grid);
    }
  }

  // only shape where botLeftCoord's x isn't representative of farthest left is PLUS, where we want to -1
  private long getFarthestLeftXCoordOfShape(
    Shape currShape,
    Coord botLeftCoord
  ) {
    return currShape == Shape.PLUS
      ? botLeftCoord.getX() - 1
      : botLeftCoord.getX();
  }

  private long getFarthestRightXCoordOfShape(
    Shape currShape,
    Coord botLeftCoord
  ) {
    return (
      botLeftCoord.getX() +
      switch (currShape) {
        case HORIZ_LINE -> 3;
        case PLUS -> 1;
        case ELBOW -> 2;
        case VERT_LINE -> 0;
        case SQUARE -> 1;
      }
    );
  }

  // see if any coords that have open left-face (left-most on each y) will collide
  private boolean canSafelyMoveToLeft(
    Shape currShape,
    Coord botLeftCoord,
    Set<Coord> grid
  ) {
    Set<Coord> currents = new HashSet<>();

    if (currShape == Shape.HORIZ_LINE) {
      currents.add(botLeftCoord);
    } else if (currShape == Shape.PLUS) {
      currents.add(botLeftCoord);
      currents.add(new Coord(botLeftCoord.getX() - 1, botLeftCoord.getY() + 1));
      currents.add(new Coord(botLeftCoord.getX(), botLeftCoord.getY() + 2));
    } else if (currShape == Shape.ELBOW) {
      currents.add(botLeftCoord);
      currents.add(new Coord(botLeftCoord.getX() + 2, botLeftCoord.getY() + 1));
      currents.add(new Coord(botLeftCoord.getX() + 2, botLeftCoord.getY() + 2));
    } else if (currShape == Shape.VERT_LINE) {
      currents.add(botLeftCoord);
      currents.add(new Coord(botLeftCoord.getX(), botLeftCoord.getY() + 1));
      currents.add(new Coord(botLeftCoord.getX(), botLeftCoord.getY() + 2));
      currents.add(new Coord(botLeftCoord.getX(), botLeftCoord.getY() + 3));
    } else if (currShape == Shape.SQUARE) {
      currents.add(botLeftCoord);
      currents.add(new Coord(botLeftCoord.getX(), botLeftCoord.getY() + 1));
    }

    Set<Coord> wouldMoveTo = currents
      .stream()
      .map(current -> new Coord(current.getX() - 1, current.getY()))
      .collect(Collectors.toSet());

    boolean willCollide = grid.stream().anyMatch(wouldMoveTo::contains);
    return !willCollide;
  }

  // see if any coords that have open right-face (right-most on each y) will collide
  private boolean canSafelyMoveToRight(
    Shape currShape,
    Coord botLeftCoord,
    Set<Coord> grid
  ) {
    Set<Coord> currents = new HashSet<>();

    if (currShape == Shape.HORIZ_LINE) {
      currents.add(new Coord(botLeftCoord.getX() + 3, botLeftCoord.getY()));
    } else if (currShape == Shape.PLUS) {
      currents.add(botLeftCoord);
      currents.add(new Coord(botLeftCoord.getX() + 1, botLeftCoord.getY() + 1));
      currents.add(new Coord(botLeftCoord.getX(), botLeftCoord.getY() + 2));
    } else if (currShape == Shape.ELBOW) {
      currents.add(new Coord(botLeftCoord.getX() + 2, botLeftCoord.getY()));
      currents.add(new Coord(botLeftCoord.getX() + 2, botLeftCoord.getY() + 1));
      currents.add(new Coord(botLeftCoord.getX() + 2, botLeftCoord.getY() + 2));
    } else if (currShape == Shape.VERT_LINE) {
      currents.add(botLeftCoord);
      currents.add(new Coord(botLeftCoord.getX(), botLeftCoord.getY() + 1));
      currents.add(new Coord(botLeftCoord.getX(), botLeftCoord.getY() + 2));
      currents.add(new Coord(botLeftCoord.getX(), botLeftCoord.getY() + 3));
    } else if (currShape == Shape.SQUARE) {
      currents.add(new Coord(botLeftCoord.getX() + 1, botLeftCoord.getY()));
      currents.add(new Coord(botLeftCoord.getX() + 1, botLeftCoord.getY() + 1));
    }

    Set<Coord> wouldMoveTo = currents
      .stream()
      .map(current -> new Coord(current.getX() + 1, current.getY()))
      .collect(Collectors.toSet());

    boolean willCollide = grid.stream().anyMatch(wouldMoveTo::contains);
    return !willCollide;
  }

  // see if any ones that have open down-face (down-most on each x) will collide
  private boolean canFallDown(
    Shape currShape,
    Coord botLeftCoord,
    Set<Coord> grid
  ) {
    // first check floor
    if (botLeftCoord.getY() - 1 == FLOOR_Y) {
      return false;
    }

    Set<Coord> currents = new HashSet<>();

    if (currShape == Shape.HORIZ_LINE) {
      currents.add(botLeftCoord);
      currents.add(new Coord(botLeftCoord.getX() + 1, botLeftCoord.getY()));
      currents.add(new Coord(botLeftCoord.getX() + 2, botLeftCoord.getY()));
      currents.add(new Coord(botLeftCoord.getX() + 3, botLeftCoord.getY()));
    } else if (currShape == Shape.PLUS) {
      currents.add(new Coord(botLeftCoord.getX() - 1, botLeftCoord.getY() + 1));
      currents.add(botLeftCoord);
      currents.add(new Coord(botLeftCoord.getX() + 1, botLeftCoord.getY() + 1));
    } else if (currShape == Shape.ELBOW) {
      currents.add(botLeftCoord);
      currents.add(new Coord(botLeftCoord.getX() + 1, botLeftCoord.getY()));
      currents.add(new Coord(botLeftCoord.getX() + 2, botLeftCoord.getY()));
    } else if (currShape == Shape.VERT_LINE) {
      currents.add(botLeftCoord);
    } else if (currShape == Shape.SQUARE) {
      currents.add(botLeftCoord);
      currents.add(new Coord(botLeftCoord.getX() + 1, botLeftCoord.getY()));
    }

    Set<Coord> wouldMoveTo = currents
      .stream()
      .map(current -> new Coord(current.getX(), current.getY() - 1))
      .collect(Collectors.toSet());

    boolean willCollide = grid.stream().anyMatch(wouldMoveTo::contains);
    return !willCollide;
  }

  private Set<Coord> fillInGrid(
    Shape currShape,
    Coord botLeftCoord,
    Set<Coord> grid
  ) {
    Set<Coord> postSettle = new HashSet<>(grid);
    postSettle.addAll(getAllShapeCoordsFromBotLeft(currShape, botLeftCoord));
    return postSettle;
  }

  private Set<Coord> getAllShapeCoordsFromBotLeft(
    Shape shape,
    Coord botLeftCoord
  ) {
    return switch (shape) {
      case HORIZ_LINE -> {
        Set<Coord> shapeCoords = new HashSet<>();
        shapeCoords.add(botLeftCoord);
        shapeCoords.add(
          new Coord(botLeftCoord.getX() + 1, botLeftCoord.getY())
        );
        shapeCoords.add(
          new Coord(botLeftCoord.getX() + 2, botLeftCoord.getY())
        );
        shapeCoords.add(
          new Coord(botLeftCoord.getX() + 3, botLeftCoord.getY())
        );
        yield shapeCoords;
      }
      case PLUS -> {
        Set<Coord> shapeCoords = new HashSet<>();
        shapeCoords.add(botLeftCoord);
        shapeCoords.add(
          new Coord(botLeftCoord.getX() - 1, botLeftCoord.getY() + 1)
        );
        shapeCoords.add(
          new Coord(botLeftCoord.getX(), botLeftCoord.getY() + 1)
        );
        shapeCoords.add(
          new Coord(botLeftCoord.getX() + 1, botLeftCoord.getY() + 1)
        );
        shapeCoords.add(
          new Coord(botLeftCoord.getX(), botLeftCoord.getY() + 2)
        );
        yield shapeCoords;
      }
      case ELBOW -> {
        Set<Coord> shapeCoords = new HashSet<>();
        shapeCoords.add(botLeftCoord);
        shapeCoords.add(
          new Coord(botLeftCoord.getX() + 1, botLeftCoord.getY())
        );
        shapeCoords.add(
          new Coord(botLeftCoord.getX() + 2, botLeftCoord.getY())
        );
        shapeCoords.add(
          new Coord(botLeftCoord.getX() + 2, botLeftCoord.getY() + 1)
        );
        shapeCoords.add(
          new Coord(botLeftCoord.getX() + 2, botLeftCoord.getY() + 2)
        );
        yield shapeCoords;
      }
      case VERT_LINE -> {
        Set<Coord> shapeCoords = new HashSet<>();
        shapeCoords.add(botLeftCoord);
        shapeCoords.add(
          new Coord(botLeftCoord.getX(), botLeftCoord.getY() + 1)
        );
        shapeCoords.add(
          new Coord(botLeftCoord.getX(), botLeftCoord.getY() + 2)
        );
        shapeCoords.add(
          new Coord(botLeftCoord.getX(), botLeftCoord.getY() + 3)
        );
        yield shapeCoords;
      }
      case SQUARE -> {
        Set<Coord> shapeCoords = new HashSet<>();
        shapeCoords.add(botLeftCoord);
        shapeCoords.add(
          new Coord(botLeftCoord.getX(), botLeftCoord.getY() + 1)
        );
        shapeCoords.add(
          new Coord(botLeftCoord.getX() + 1, botLeftCoord.getY())
        );
        shapeCoords.add(
          new Coord(botLeftCoord.getX() + 1, botLeftCoord.getY() + 1)
        );
        yield shapeCoords;
      }
    };
  }

  private Direction getJet(List<Direction> jets, long currRound) {
    return jets.get(getCurrentJetIndex(jets, currRound));
  }

  private int getCurrentJetIndex(List<Direction> jets, long currRound) {
    return (int) (currRound / 2 % jets.size());
  }

  private Set<Coord> calcRecentRockLayout(Set<Coord> grid, long highestRockY) {
    Set<Coord> layoutSnapshot = new HashSet<>();
    for (Coord rock : grid) {
      if (highestRockY - rock.getY() <= 30) { // this 30 is arbitrary...
        layoutSnapshot.add(new Coord(rock.getX(), rock.getY() - highestRockY));
      }
    }
    return layoutSnapshot;
  }

  private enum Direction {
    LEFT('<'),
    RIGHT('>');

    private static final Map<Character, Direction> CHAR_TO_DIR = ImmutableMap
      .<Character, Direction>builder()
      .put(RIGHT.getC(), RIGHT)
      .put(LEFT.getC(), LEFT)
      .build();

    private final char c;

    Direction(char c) {
      this.c = c;
    }

    public char getC() {
      return c;
    }

    public static Direction fromChar(char c) {
      return CHAR_TO_DIR.get(c);
    }
  }

  private enum Shape {
    HORIZ_LINE(0, 3, 0),
    PLUS(1, 4, 2),
    ELBOW(2, 3, 2),
    VERT_LINE(3, 3, 3),
    SQUARE(4, 3, 1);

    private static final Map<Integer, Shape> INDEX_TO_SHAPE = ImmutableMap
      .<Integer, Shape>builder()
      .put(HORIZ_LINE.getOrdering(), HORIZ_LINE)
      .put(PLUS.getOrdering(), PLUS)
      .put(ELBOW.getOrdering(), ELBOW)
      .put(VERT_LINE.getOrdering(), VERT_LINE)
      .put(SQUARE.getOrdering(), SQUARE)
      .build();

    private final int ordering;
    private final int botLeftX;
    private final int topYOffsetFromBot;

    Shape(int ordering, int botLeftX, int topYOffsetFromBot) {
      this.ordering = ordering;
      this.botLeftX = botLeftX;
      this.topYOffsetFromBot = topYOffsetFromBot;
    }

    public int getOrdering() {
      return ordering;
    }

    public int getBotLeftX() {
      return botLeftX;
    }

    public int getTopYOffsetFromBot() {
      return topYOffsetFromBot;
    }

    public static Shape getShape(long numRocksSettled) {
      return INDEX_TO_SHAPE.get(
        (int) (numRocksSettled % INDEX_TO_SHAPE.size())
      );
    }
  }

  private static class Coord {

    private final long x;
    private final long y;

    Coord(long x, long y) {
      this.x = x;
      this.y = y;
    }

    public long getX() {
      return x;
    }

    public long getY() {
      return y;
    }

    public static Coord oneLeft(Coord input) {
      return new Coord(input.getX() - 1, input.getY());
    }

    public static Coord oneRight(Coord input) {
      return new Coord(input.getX() + 1, input.getY());
    }

    public static Coord oneDown(Coord input) {
      return new Coord(input.getX(), input.getY() - 1);
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }
      Coord coord = (Coord) o;
      return x == coord.x && y == coord.y;
    }

    @Override
    public int hashCode() {
      return Objects.hash(x, y);
    }
  }

  private static class RockMemo {

    private final int jetIndex;
    private final Shape rockShape;
    private final Set<Coord> highestThirtySettledLayoutCoords;

    RockMemo(
      int jetIndex,
      Shape rockShape,
      Set<Coord> highestThirtySettledLayoutCoords
    ) {
      this.jetIndex = jetIndex;
      this.rockShape = rockShape;
      this.highestThirtySettledLayoutCoords = highestThirtySettledLayoutCoords;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }
      RockMemo rockMemo = (RockMemo) o;
      return (
        jetIndex == rockMemo.jetIndex &&
        rockShape == rockMemo.rockShape &&
        Objects.equals(
          highestThirtySettledLayoutCoords,
          rockMemo.highestThirtySettledLayoutCoords
        )
      );
    }

    @Override
    public int hashCode() {
      return Objects.hash(
        jetIndex,
        rockShape,
        highestThirtySettledLayoutCoords
      );
    }
  }

  private static class RockState {

    private final long maxHeight;
    private final long numRocksSettled;

    RockState(long maxHeight, long numRocksSettled) {
      this.maxHeight = maxHeight;
      this.numRocksSettled = numRocksSettled;
    }

    public long getMaxHeight() {
      return maxHeight;
    }

    public long getNumRocksSettled() {
      return numRocksSettled;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }
      RockState rockState = (RockState) o;
      return (
        maxHeight == rockState.maxHeight &&
        numRocksSettled == rockState.numRocksSettled
      );
    }

    @Override
    public int hashCode() {
      return Objects.hash(maxHeight, numRocksSettled);
    }
  }
}
