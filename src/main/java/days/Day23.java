package days;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.SetMultimap;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class Day23 implements Day {

  private static final char ELF = '#';

  private Map<Integer, Elf> elvesById = new HashMap<>();
  private SetMultimap<Coord, Elf> elvesByProposedCoord = HashMultimap.create();
  private List<Direction> directions = Arrays.asList(
    Direction.N,
    Direction.S,
    Direction.W,
    Direction.E
  );

  private static final int AXIS_SIZE = 1000;
  private static final int OFFSET = AXIS_SIZE / 2; // give em some room to move
  private char[][] map; // [y][x]

  private static final int NUM_ROUNDS = 10;

  public void part1(List<String> input) {
    parseInput(input);

    simulate(false);

    int numEmpties = calcNumEmptySpacesInEnclosingRectangle();
    System.out.println("Empty spaces: " + numEmpties);
  }

  public void part2(List<String> input) {
    parseInput(input);

    simulate(true);
  }

  private void parseInput(List<String> input) {
    char[][] grid = new char[AXIS_SIZE][AXIS_SIZE];

    int elfId = 0;
    for (int y = 0; y < input.size(); y++) {
      String line = input.get(y);
      for (int x = 0; x < line.length(); x++) {
        if (line.charAt(x) == ELF) {
          grid[y + OFFSET][x + OFFSET] = ELF;
          elvesById.put(
            elfId,
            new Elf(elfId, new Coord(x + OFFSET, y + OFFSET))
          );
          elfId++;
        }
      }
    }
    map = grid;
  }

  private void simulate(boolean partTwo) {
    int numRounds = partTwo ? Integer.MAX_VALUE : NUM_ROUNDS;

    for (int r = 1; r <= numRounds; r++) {
      elvesByProposedCoord = HashMultimap.create();
      Set<Elf> elves = new HashSet<>(elvesById.values());

      for (Elf elf : elves) {
        if (noOneElseAround(elf)) {
          continue;
        }
        Coord currentPos = elf.getCurrent();
        for (Direction d : directions) {
          if (isDirectionEmpty(d, currentPos)) {
            elvesByProposedCoord.put(newCoordInDirection(d, currentPos), elf);
            break;
          }
        }
      }

      if (elvesByProposedCoord.isEmpty()) {
        System.out.println("First round where no elves move: " + r);
        return;
      }

      moveIfPossible();
      Collections.rotate(directions, -1);
    }
  }

  private void moveIfPossible() {
    Set<Coord> toClear = new HashSet<>();
    Set<Coord> toMark = new HashSet<>();

    for (Coord proposed : elvesByProposedCoord.keySet()) {
      Set<Elf> elvesProposingHere = elvesByProposedCoord.get(proposed);
      if (elvesProposingHere.size() == 1) {
        Elf moving = Iterables.getOnlyElement(elvesProposingHere);
        elvesById.put(moving.getId(), moving.withCurrent(proposed));

        toClear.add(moving.getCurrent());
        toMark.add(proposed);
      }
    }

    for (Coord clear : toClear) {
      map[clear.y()][clear.x()] = '.';
    }

    for (Coord mark : toMark) {
      map[mark.y()][mark.x()] = ELF;
    }
  }

  private boolean noOneElseAround(Elf elf) {
    Coord current = elf.getCurrent();

    Coord NW = getNW(current);
    if (map[NW.y()][NW.x()] == ELF) {
      return false;
    }

    Coord N = getN(current);
    if (map[N.y()][N.x()] == ELF) {
      return false;
    }

    Coord NE = getNE(current);
    if (map[NE.y()][NE.x()] == ELF) {
      return false;
    }

    Coord W = getW(current);
    if (map[W.y()][W.x()] == ELF) {
      return false;
    }

    Coord E = getE(current);
    if (map[E.y()][E.x()] == ELF) {
      return false;
    }

    Coord SW = getSW(current);
    if (map[SW.y()][SW.x()] == ELF) {
      return false;
    }

    Coord S = getS(current);
    if (map[S.y()][S.x()] == ELF) {
      return false;
    }

    Coord SE = getSE(current);
    if (map[SE.y()][SE.x()] == ELF) {
      return false;
    }

    return true;
  }

  private boolean isDirectionEmpty(Direction d, Coord current) {
    return switch (d) {
      case N -> isTopEmpty(current);
      case S -> isBotEmpty(current);
      case W -> isLeftEmpty(current);
      case E -> isRightEmpty(current);
    };
  }

  private boolean isTopEmpty(Coord current) {
    Coord NW = getNW(current);
    if (map[NW.y()][NW.x()] == ELF) {
      return false;
    }

    Coord N = getN(current);
    if (map[N.y()][N.x()] == ELF) {
      return false;
    }

    Coord NE = getNE(current);
    if (map[NE.y()][NE.x()] == ELF) {
      return false;
    }

    return true;
  }

  private boolean isBotEmpty(Coord current) {
    Coord SW = getSW(current);
    if (map[SW.y()][SW.x()] == ELF) {
      return false;
    }

    Coord S = getS(current);
    if (map[S.y()][S.x()] == ELF) {
      return false;
    }

    Coord SE = getSE(current);
    if (map[SE.y()][SE.x()] == ELF) {
      return false;
    }

    return true;
  }

  private boolean isLeftEmpty(Coord current) {
    Coord NW = getNW(current);
    if (map[NW.y()][NW.x()] == ELF) {
      return false;
    }

    Coord W = getW(current);
    if (map[W.y()][W.x()] == ELF) {
      return false;
    }

    Coord SW = getSW(current);
    if (map[SW.y()][SW.x()] == ELF) {
      return false;
    }

    return true;
  }

  private boolean isRightEmpty(Coord current) {
    Coord NE = getNE(current);
    if (map[NE.y()][NE.x()] == ELF) {
      return false;
    }

    Coord E = getE(current);
    if (map[E.y()][E.x()] == ELF) {
      return false;
    }

    Coord SE = getSE(current);
    if (map[SE.y()][SE.x()] == ELF) {
      return false;
    }

    return true;
  }

  private Coord newCoordInDirection(Direction d, Coord current) {
    return switch (d) {
      case N -> getN(current);
      case S -> getS(current);
      case W -> getW(current);
      case E -> getE(current);
    };
  }

  private Coord getNW(Coord current) {
    return new Coord(current.x() - 1, current.y() - 1);
  }

  private Coord getN(Coord current) {
    return new Coord(current.x(), current.y() - 1);
  }

  private Coord getNE(Coord current) {
    return new Coord(current.x() + 1, current.y() - 1);
  }

  private Coord getW(Coord current) {
    return new Coord(current.x() - 1, current.y());
  }

  private Coord getE(Coord current) {
    return new Coord(current.x() + 1, current.y());
  }

  private Coord getSW(Coord current) {
    return new Coord(current.x() - 1, current.y() + 1);
  }

  private Coord getS(Coord current) {
    return new Coord(current.x(), current.y() + 1);
  }

  private Coord getSE(Coord current) {
    return new Coord(current.x() + 1, current.y() + 1);
  }

  private int calcNumEmptySpacesInEnclosingRectangle() {
    int leftX = Integer.MAX_VALUE;
    int rightX = Integer.MIN_VALUE;
    int topY = Integer.MAX_VALUE;
    int botY = Integer.MIN_VALUE;

    for (Elf elf : elvesById.values()) {
      Coord pos = elf.getCurrent();
      if (pos.x() < leftX) {
        leftX = pos.x();
      }
      if (pos.x() > rightX) {
        rightX = pos.x();
      }
      if (pos.y() < topY) {
        topY = pos.y();
      }
      if (pos.y() > botY) {
        botY = pos.y();
      }
    }

    int numEmpty = 0;
    for (int y = topY; y <= botY; y++) {
      for (int x = leftX; x <= rightX; x++) {
        if (map[y][x] != ELF) {
          numEmpty++;
        }
      }
    }
    return numEmpty;
  }

  private enum Direction {
    N,
    S,
    W,
    E,
  }

  private record Coord(int x, int y) {}

  private class Elf {

    private final int id;
    private final Coord current;

    private Elf(int id, Coord current) {
      this.id = id;
      this.current = current;
    }

    public int getId() {
      return id;
    }

    public Coord getCurrent() {
      return current;
    }

    public Elf withCurrent(Coord newCurrent) {
      return new Elf(this.id, newCurrent);
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }
      Elf elf = (Elf) o;
      return id == elf.id && Objects.equals(current, elf.current);
    }

    @Override
    public int hashCode() {
      return Objects.hash(id, current);
    }
  }
}
