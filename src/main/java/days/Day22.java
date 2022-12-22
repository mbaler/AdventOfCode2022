package days;

import com.google.common.primitives.Ints;
import java.util.ArrayList;
import java.util.List;

public class Day22 implements Day {

  private static final char EMPTY = ' ';
  private static final char WALL = '#';

  private static final int FACE_SIZE = 50;

  private List<String> instructions;
  private char[][] map;
  private Coord current;
  private Facing facing;

  public void part1(List<String> input) {
    map = parseInput(input);
    followInstructions(false);

    int sum =
      (1000 * (current.y() + 1)) + (4 * (current.x() + 1)) + facing.getVal();
    System.out.println("Password sum: " + sum);
  }

  public void part2(List<String> input) {
    map = parseInput(input);
    followInstructions(true);

    int sum =
      (1000 * (current.y() + 1)) + (4 * (current.x() + 1)) + facing.getVal();
    System.out.println("Password sum with cube: " + sum);
  }

  private char[][] parseInput(List<String> input) {
    instructions = parseInstructionLine(input.get(input.size() - 1));

    int maxLen = 0;
    for (int r = 0; r < input.size() - 2; r++) {
      String line = input.get(r);
      if (line.length() > maxLen) {
        maxLen = line.length();
      }
    }

    // [y][x] -- from top, from left
    char[][] map = new char[input.size() - 2][maxLen];

    for (int y = 0; y < input.size() - 2; y++) {
      String line = input.get(y);
      int len = line.length();
      int numSpacesToAddAtEnd = maxLen - len;
      for (int x = 0; x < len; x++) {
        map[y][x] = line.charAt(x);
      }
      int after = len - 1;
      for (int r = 0; r < numSpacesToAddAtEnd; r++) {
        map[y][++after] = EMPTY;
      }
    }

    return map;
  }

  private List<String> parseInstructionLine(String instructionLine) {
    List<String> instructions = new ArrayList<>();

    String num = "";
    for (char c : instructionLine.toCharArray()) {
      if (!Character.isDigit(c)) {
        if (!num.isEmpty()) {
          instructions.add(num);
          num = "";
        }
        instructions.add(Character.toString(c));
      } else {
        num += Character.toString(c);
      }
    }
    if (!num.isEmpty()) {
      instructions.add(num);
    }

    return instructions;
  }

  private void followInstructions(boolean part2) {
    current = new Coord(getIndexOfFirstNonEmptyXInRow(0), 0); // leftmost open tile of first row
    facing = Facing.RIGHT;

    for (String instruction : instructions) {
      Integer numSteps = Ints.tryParse(instruction);
      if (numSteps != null) {
        for (int i = 0; i < numSteps; i++) {
          current = getCoordOfNextMove(part2);
        }
      } else {
        facing = turn(instruction);
      }
    }
  }

  private int getIndexOfFirstNonEmptyXInRow(int y) {
    char[] row = map[y];
    for (int x = 0; x < row.length; x++) {
      if (row[x] != EMPTY) {
        return x;
      }
    }
    throw new IllegalArgumentException("nope");
  }

  private int getIndexOfLastNonEmptyXInRow(int y) {
    char[] row = map[y];
    for (int x = row.length - 1; x >= 0; x--) {
      if (row[x] != EMPTY) {
        return x;
      }
    }
    throw new IllegalArgumentException("nope");
  }

  private int getIndexOfFirstNonEmptyYInColumn(int x) {
    for (int y = 0; y < map.length; y++) {
      if (map[y][x] != EMPTY) {
        return y;
      }
    }
    throw new IllegalArgumentException("nope");
  }

  private int getIndexOfLastNonEmptyYInColumn(int x) {
    for (int y = map.length - 1; y >= 0; y--) {
      if (map[y][x] != EMPTY) {
        return y;
      }
    }
    throw new IllegalArgumentException("nope");
  }

  private Coord getCoordOfNextMove(boolean part2) {
    int cX = current.x();
    int cY = current.y();

    return switch (facing) {
      case UP -> {
        if (cY == 0 || map[cY - 1][cX] == EMPTY) { // can't go any higher || next space empty
          if (!part2) {
            Coord newCoord = new Coord(cX, getIndexOfLastNonEmptyYInColumn(cX));
            if (isWall(newCoord)) {
              yield current;
            } else {
              yield newCoord;
            }
          } else {
            List<Object> newCoordAndFacing = nextCoordWhenMovingUpOnCube(
              cX,
              cY
            );
            if (isWall((Coord) newCoordAndFacing.get(0))) {
              yield current;
            } else {
              facing = (Facing) newCoordAndFacing.get(1);
              yield (Coord) newCoordAndFacing.get(0);
            }
          }
        } else {
          Coord newCoord = new Coord(cX, cY - 1);
          if (isWall(newCoord)) {
            yield current;
          } else {
            yield newCoord;
          }
        }
      }
      case DOWN -> {
        if (cY == map.length - 1 || map[cY + 1][cX] == EMPTY) { // can't go any lower || next space empty
          if (!part2) {
            Coord newCoord = new Coord(
              cX,
              getIndexOfFirstNonEmptyYInColumn(cX)
            );
            if (isWall(newCoord)) {
              yield current;
            } else {
              yield newCoord;
            }
          } else {
            List<Object> newCoordAndFacing = nextCoordWhenMovingDownOnCube(
              cX,
              cY
            );
            if (isWall((Coord) newCoordAndFacing.get(0))) {
              yield current;
            } else {
              facing = (Facing) newCoordAndFacing.get(1);
              yield (Coord) newCoordAndFacing.get(0);
            }
          }
        } else {
          Coord newCoord = new Coord(cX, cY + 1);
          if (isWall(newCoord)) {
            yield current;
          } else {
            yield newCoord;
          }
        }
      }
      case LEFT -> {
        if (cX == 0 || map[cY][cX - 1] == EMPTY) { // can't go any more left || next space empty
          if (!part2) {
            Coord newCoord = new Coord(getIndexOfLastNonEmptyXInRow(cY), cY);
            if (isWall(newCoord)) {
              yield current;
            } else {
              yield newCoord;
            }
          } else {
            List<Object> newCoordAndFacing = nextCoordWhenMovingLeftOnCube(
              cX,
              cY
            );
            if (isWall((Coord) newCoordAndFacing.get(0))) {
              yield current;
            } else {
              facing = (Facing) newCoordAndFacing.get(1);
              yield (Coord) newCoordAndFacing.get(0);
            }
          }
        } else {
          Coord newCoord = new Coord(cX - 1, cY);
          if (isWall(newCoord)) {
            yield current;
          } else {
            yield newCoord;
          }
        }
      }
      case RIGHT -> {
        if (cX == map[cY].length - 1 || map[cY][cX + 1] == EMPTY) { // can't go any more right || next space empty
          if (!part2) {
            Coord newCoord = new Coord(getIndexOfFirstNonEmptyXInRow(cY), cY);
            if (isWall(newCoord)) {
              yield current;
            } else {
              yield newCoord;
            }
          } else {
            List<Object> newCoordAndFacing = nextCoordWhenMovingRightOnCube(
              cX,
              cY
            );
            if (isWall((Coord) newCoordAndFacing.get(0))) {
              yield current;
            } else {
              facing = (Facing) newCoordAndFacing.get(1);
              yield (Coord) newCoordAndFacing.get(0);
            }
          }
        } else {
          Coord newCoord = new Coord(cX + 1, cY);
          if (isWall(newCoord)) {
            yield current;
          } else {
            yield newCoord;
          }
        }
      }
    };
  }

  private List<Object> nextCoordWhenMovingUpOnCube(int x, int y) { // UP
    if (onA(x, y)) {
      // fine, moving into B
      return List.of(new Coord(x, y - 1), Facing.UP);
    }
    if (onB(x, y)) {
      // enter left of D; low x is low y
      return List.of(new Coord(FACE_SIZE, FACE_SIZE + (x - 0)), Facing.RIGHT);
    }
    if (onC(x, y)) {
      // fine, moving into D
      return List.of(new Coord(x, y - 1), Facing.UP);
    }
    if (onD(x, y)) {
      // fine, moving into E
      return List.of(new Coord(x, y - 1), Facing.UP);
    }
    if (onE(x, y)) {
      // enter left of A; low x is low y
      return List.of(
        new Coord(0, (FACE_SIZE * 3) + (x - FACE_SIZE)),
        Facing.RIGHT
      );
    }
    // on F
    // enter bott of A; low x is low x
    return List.of(
      new Coord(0 + (x - (FACE_SIZE * 2)), ((FACE_SIZE * 4) - 1)),
      Facing.UP
    );
  }

  private List<Object> nextCoordWhenMovingDownOnCube(int x, int y) { // DOWN
    if (onA(x, y)) {
      // enter top of F; low x is low x
      return List.of(new Coord((FACE_SIZE * 2) + x, 0), Facing.DOWN);
    }
    if (onB(x, y)) {
      // fine; moving into A
      return List.of(new Coord(x, y + 1), Facing.DOWN);
    }
    if (onC(x, y)) {
      // right of A; low x is low y
      return List.of(
        new Coord((FACE_SIZE - 1), (FACE_SIZE * 3) + (x - FACE_SIZE)),
        Facing.LEFT
      );
    }
    if (onD(x, y)) {
      // fine; moving into C
      return List.of(new Coord(x, y + 1), Facing.DOWN);
    }
    if (onE(x, y)) {
      // fine; moving into D
      return List.of(new Coord(x, y + 1), Facing.DOWN);
    }
    // on F
    // enter right of D; low x is low y
    return List.of(
      new Coord(((FACE_SIZE * 2) - 1), FACE_SIZE + (x - (FACE_SIZE * 2))),
      Facing.LEFT
    );
  }

  private List<Object> nextCoordWhenMovingLeftOnCube(int x, int y) { // LEFT
    if (onA(x, y)) {
      // enter top of E; low y is low x
      return List.of(new Coord(FACE_SIZE + (y - 150), 0), Facing.DOWN);
    }
    if (onB(x, y)) {
      // enter left of E; low y is high y
      return List.of(
        new Coord(FACE_SIZE, (FACE_SIZE - 1) - (y - (FACE_SIZE * 2))),
        Facing.RIGHT
      );
    }
    if (onC(x, y)) {
      // fine; moving into B
      return List.of(new Coord(x - 1, y), Facing.LEFT);
    }
    if (onD(x, y)) {
      // enter top of B; low y is low x
      return List.of(
        new Coord(0 + (y - FACE_SIZE), (FACE_SIZE * 2)),
        Facing.DOWN
      );
    }
    if (onE(x, y)) {
      // enter left of B; low y is high y
      return List.of(
        new Coord(0, ((FACE_SIZE * 3) - 1) - (y - 0)),
        Facing.RIGHT
      );
    }
    // on F
    // fine; moving into E
    return List.of(new Coord(x - 1, y), Facing.LEFT);
  }

  private List<Object> nextCoordWhenMovingRightOnCube(int x, int y) { // RIGHT
    if (onA(x, y)) {
      // entering bott of C; low y is low x
      return List.of(
        new Coord(FACE_SIZE + (y - (FACE_SIZE * 3)), ((FACE_SIZE * 3) - 1)),
        Facing.UP
      );
    }
    if (onB(x, y)) {
      // fine, moving into C
      return List.of(new Coord(x + 1, y), Facing.RIGHT);
    }
    if (onC(x, y)) {
      // entering right of F; low y is high y
      return List.of(
        new Coord(
          ((FACE_SIZE * 3) - 1),
          (FACE_SIZE - 1) - (y - (FACE_SIZE * 2))
        ),
        Facing.LEFT
      );
    }
    if (onD(x, y)) {
      // entering bott of F; low y is low x
      return List.of(
        new Coord((FACE_SIZE * 2) + (y - FACE_SIZE), (FACE_SIZE - 1)),
        Facing.UP
      );
    }
    if (onE(x, y)) {
      // fine, moving into F
      return List.of(new Coord(x + 1, y), Facing.RIGHT);
    }
    // on F
    // entering right of C; low y is high y
    return List.of(
      new Coord(((FACE_SIZE * 2) - 1), ((FACE_SIZE * 3) - 1) - (y - 0)),
      Facing.LEFT
    );
  }

  private boolean onA(int x, int y) {
    return (
      (x >= 0 && x <= (FACE_SIZE - 1)) &&
      (y >= (FACE_SIZE * 3) && y <= ((FACE_SIZE * 4) - 1))
    );
  }

  private boolean onB(int x, int y) {
    return (
      (x >= 0 && x <= (FACE_SIZE - 1)) &&
      (y >= (FACE_SIZE * 2) && y <= ((FACE_SIZE * 3) - 1))
    );
  }

  private boolean onC(int x, int y) {
    return (
      (x >= FACE_SIZE && x <= ((FACE_SIZE * 2) - 1)) &&
      (y >= (FACE_SIZE * 2) && y <= ((FACE_SIZE * 3) - 1))
    );
  }

  private boolean onD(int x, int y) {
    return (
      (x >= FACE_SIZE && x <= ((FACE_SIZE * 2) - 1)) &&
      (y >= FACE_SIZE && y <= ((FACE_SIZE * 2) - 1))
    );
  }

  private boolean onE(int x, int y) {
    return (
      (x >= FACE_SIZE && x <= ((FACE_SIZE * 2) - 1)) &&
      (y >= 0 && y <= (FACE_SIZE - 1))
    );
  }

  private boolean onF(int x, int y) {
    return (
      (x >= (FACE_SIZE * 2) && x <= ((FACE_SIZE * 3) - 1)) &&
      (y >= 0 && y <= (FACE_SIZE - 1))
    );
  }

  private Facing turn(String instruction) {
    if (instruction.equals("R")) {
      return switch (facing) {
        case UP -> Facing.RIGHT;
        case DOWN -> Facing.LEFT;
        case LEFT -> Facing.UP;
        case RIGHT -> Facing.DOWN;
      };
    }

    // L
    return switch (facing) {
      case UP -> Facing.LEFT;
      case DOWN -> Facing.RIGHT;
      case LEFT -> Facing.DOWN;
      case RIGHT -> Facing.UP;
    };
  }

  private boolean isWall(Coord coord) {
    return map[coord.y()][coord.x()] == WALL;
  }

  private record Coord(int x, int y) {}

  private enum Facing {
    RIGHT(0),
    DOWN(1),
    LEFT(2),
    UP(3);

    private final int val;

    Facing(int val) {
      this.val = val;
    }

    public int getVal() {
      return val;
    }
  }
}
