package days;

import com.google.common.base.Splitter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class Day14 implements Day {

  private static final char ROCK = '#';
  private static final char SAND = 'o';
  private static final String SAND_SOURCE_COORD = "500,0";

  public void part1(List<String> input) {
    Map<Coord, Character> grid = createGridFromScan(input);
    Map<Coord, Character> afterSandRest = fallSandToAbyss(grid);
    long atRest = afterSandRest
      .values()
      .stream()
      .filter(c -> c == SAND)
      .count();
    System.out.println("Num at rest: " + atRest);
  }

  public void part2(List<String> input) {
    Map<Coord, Character> grid = createGridFromScan(input);
    Map<Coord, Character> afterSandRest = fallSandToFloorToCoverSource(grid);
    long atRest = afterSandRest
      .values()
      .stream()
      .filter(c -> c == SAND)
      .count();
    System.out.println("Num at rest: " + atRest);
  }

  private static final Splitter PATH_SPLITTER = Splitter.on(" -> ");
  private static final Splitter COORD_SPLITTER = Splitter.on(",");

  private Map<Coord, Character> createGridFromScan(List<String> input) {
    Map<Coord, Character> grid = new HashMap<>();

    for (String rockScan : input) {
      List<Path> paths = new ArrayList<>();
      List<String> coords = PATH_SPLITTER.splitToList(rockScan);
      for (int c = 0; c < coords.size() - 1; c++) {
        paths.add(
          new Path(
            Coord.toCoord(coords.get(c)),
            Coord.toCoord(coords.get(c + 1))
          )
        );
      }

      for (Path path : paths) {
        Map<Coord, Character> drawnPath = drawPath(path);
        grid.putAll(drawnPath);
      }
    }

    return grid;
  }

  private Map<Coord, Character> drawPath(Path path) {
    Map<Coord, Character> pathPoints = new HashMap<>();

    if (path.isHorizontal()) {
      int y = path.getStart().getY();
      if (path.getStart().getX() <= path.getEnd().getX()) {
        for (int x = path.getStart().getX(); x <= path.getEnd().getX(); x++) {
          pathPoints.put(new Coord(x, y), ROCK);
        }
      } else {
        for (int x = path.getEnd().getX(); x <= path.getStart().getX(); x++) {
          pathPoints.put(new Coord(x, y), ROCK);
        }
      }
    } else { // is vertical
      int x = path.getStart().getX();
      if (path.getStart().getY() <= path.getEnd().getY()) {
        for (int y = path.getStart().getY(); y <= path.getEnd().getY(); y++) {
          pathPoints.put(new Coord(x, y), ROCK);
        }
      } else {
        for (int y = path.getEnd().getY(); y <= path.getStart().getY(); y++) {
          pathPoints.put(new Coord(x, y), ROCK);
        }
      }
    }

    return pathPoints;
  }

  private Map<Coord, Character> fallSandToAbyss(Map<Coord, Character> grid) {
    int abyssThreshold = findDeepestY(grid);

    boolean inTheAbyss = false;
    while (!inTheAbyss) {
      Coord end = dropPieceOfSand(
        grid,
        Optional.of(abyssThreshold),
        Optional.empty(),
        Coord.toCoord(SAND_SOURCE_COORD)
      );

      if (end.getY() >= abyssThreshold) {
        inTheAbyss = true;
      } else {
        // came to rest
        grid.put(end, SAND);
      }
    }

    return grid;
  }

  private Map<Coord, Character> fallSandToFloorToCoverSource(
    Map<Coord, Character> grid
  ) {
    int floorDepth = findDeepestY(grid) + 2;
    Coord sourceToPlug = Coord.toCoord(SAND_SOURCE_COORD);

    boolean pluggedSource = false;
    while (!pluggedSource) {
      Coord end = dropPieceOfSand(
        grid,
        Optional.empty(),
        Optional.of(floorDepth),
        Coord.toCoord(SAND_SOURCE_COORD)
      );

      grid.put(end, SAND);
      if (end.equals(sourceToPlug)) {
        pluggedSource = true;
      }
    }

    return grid;
  }

  private int findDeepestY(Map<Coord, Character> grid) {
    return grid
      .keySet()
      .stream()
      .map(Coord::getY)
      .max(Integer::compareTo)
      .get();
  }

  private Coord dropPieceOfSand(
    Map<Coord, Character> grid,
    Optional<Integer> abyssThreshold,
    Optional<Integer> floorDepth,
    Coord current
  ) {
    // first tries to fall down 1
    Coord downOne = new Coord(current.getX(), current.getY() + 1);
    if (shouldGoToCoord(grid, downOne, abyssThreshold, floorDepth)) {
      return dropPieceOfSand(grid, abyssThreshold, floorDepth, downOne);
    }

    // if down is blocked (by rock or sand), try to move diagonally 1 step down & to the left (at once)
    Coord downDiagLeft = new Coord(current.getX() - 1, current.getY() + 1);
    if (shouldGoToCoord(grid, downDiagLeft, abyssThreshold, floorDepth)) {
      return dropPieceOfSand(grid, abyssThreshold, floorDepth, downDiagLeft);
    }

    // if that is blocked, tries the other direction, 1 step down & to the right
    Coord downDiagRight = new Coord(current.getX() + 1, current.getY() + 1);
    if (shouldGoToCoord(grid, downDiagRight, abyssThreshold, floorDepth)) {
      return dropPieceOfSand(grid, abyssThreshold, floorDepth, downDiagRight);
    }

    // if still blocked, comes to rest
    return current;
  }

  private boolean shouldGoToCoord(
    Map<Coord, Character> grid,
    Coord target,
    Optional<Integer> abyssThreshold,
    Optional<Integer> floorDepth
  ) {
    // would just keep falling, done
    if (abyssThreshold.isPresent() && target.getY() > abyssThreshold.get()) {
      return false;
    }

    // currently resting at floor, done
    if (floorDepth.isPresent() && target.getY() == floorDepth.get()) {
      return false;
    }

    return !grid.containsKey(target);
  }

  private static class Coord {

    private final int x;
    private final int y;

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

    public static Coord toCoord(String inputCoord) {
      List<String> coordParts = COORD_SPLITTER.splitToList(inputCoord);
      int y = Integer.parseInt(coordParts.get(1));
      int x = Integer.parseInt(coordParts.get(0)) - 500; // have x centered on sand source
      return new Coord(x, y);
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

  private static final class Path {

    private final Coord start;
    private final Coord end;

    Path(Coord start, Coord end) {
      this.start = start;
      this.end = end;
    }

    public Coord getStart() {
      return start;
    }

    public Coord getEnd() {
      return end;
    }

    public boolean isHorizontal() {
      return start.getY() == end.getY();
    }
  }
}
