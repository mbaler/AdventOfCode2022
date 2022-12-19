package days;

import com.google.common.base.Splitter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Collectors;

public class Day18 implements Day {

  private int maxX = Integer.MIN_VALUE;
  private int minX = Integer.MAX_VALUE;

  private int maxY = Integer.MIN_VALUE;
  private int minY = Integer.MAX_VALUE;

  private int maxZ = Integer.MIN_VALUE;
  private int minZ = Integer.MAX_VALUE;

  public void part1(List<String> input) {
    List<Cube> cubes = parseInput(input);
    int surfaceArea = sumOpenFaces(cubes);

    System.out.println("Total surface area: " + surfaceArea);
  }

  public void part2(List<String> input) {
    List<Cube> cubes = parseInput(input);
    int surfaceArea = sumOpenFaces(cubes);

    int internalSurfaceArea = calculateInternalSurfaceArea(cubes);
    System.out.println(
      "Total external surface area: " + (surfaceArea - internalSurfaceArea)
    );
  }

  private static final Splitter COMMA = Splitter.on(",");

  private List<Cube> parseInput(List<String> input) {
    List<Cube> cubes = new ArrayList<>(input.size());
    for (String line : input) {
      List<Integer> coordParts = COMMA
        .splitToList(line)
        .stream()
        .map(Integer::parseInt)
        .collect(Collectors.toList());
      int x = coordParts.get(0);
      int y = coordParts.get(1);
      int z = coordParts.get(2);

      Coord coord = new Coord(x, y, z);
      cubes.add(new Cube(coord));

      if (x > maxX) {
        maxX = x;
      }
      if (x < minX) {
        minX = x;
      }

      if (y > maxY) {
        maxY = y;
      }
      if (y < minY) {
        minY = y;
      }

      if (z > maxZ) {
        maxZ = z;
      }
      if (z < minZ) {
        minZ = z;
      }
    }

    return cubes;
  }

  private int sumOpenFaces(List<Cube> cubes) {
    return cubes.stream().mapToInt(cube -> numOpenFaces(cube, cubes)).sum();
  }

  private int numOpenFaces(Cube current, List<Cube> all) {
    int numFacesCovered = 0;
    for (Cube cube : all) {
      if (cube == current) {
        continue;
      }
      if (isAdjacent(current.coord(), cube.coord())) {
        numFacesCovered++;
      }
    }
    return 6 - numFacesCovered;
  }

  // are adjacent if... two of 3 coords are ==, and other one is +- 1
  private boolean isAdjacent(Coord current, Coord other) {
    // xy ==, z+-1
    if (
      current.x() == other.x() &&
      current.y() == other.y() &&
      oneAway(current.z(), other.z())
    ) {
      return true;
    }
    // xz ==, y+-1
    if (
      current.x() == other.x() &&
      current.z() == other.z() &&
      oneAway(current.y(), other.y())
    ) {
      return true;
    }
    // yz ==, x+-1
    if (
      current.y() == other.y() &&
      current.z() == other.z() &&
      oneAway(current.x(), other.x())
    ) {
      return true;
    }

    return false;
  }

  private boolean oneAway(int curr, int other) {
    return Math.abs(curr - other) == 1;
  }

  private int calculateInternalSurfaceArea(List<Cube> cubes) {
    Set<Coord> lavaCoords = cubes
      .stream()
      .map(Cube::coord)
      .collect(Collectors.toSet());

    Set<Coord> surroundedAirCubes = newfindAllSurroundedAirCubes(lavaCoords);

    int internalSurfaceArea = surroundedAirCubes.size() * 6;

    for (Coord surroundedAirCube : surroundedAirCubes) {
      for (Coord c : surroundedAirCubes) {
        if (c == surroundedAirCube) {
          continue;
        }
        if (isAdjacent(surroundedAirCube, c)) {
          internalSurfaceArea -= 1;
        }
      }
    }

    return internalSurfaceArea;
  }

  private Set<Coord> newfindAllSurroundedAirCubes(Set<Coord> lavaCoords) {
    Set<Coord> airCubes = new HashSet<>();
    for (int x = minX - 1; x <= maxX + 1; x++) {
      for (int y = minY - 1; y <= maxY + 1; y++) {
        for (int z = minZ - 1; z <= maxZ + 1; z++) {
          Coord c = new Coord(x, y, z);
          if (!lavaCoords.contains(c)) {
            airCubes.add(c);
          }
        }
      }
    }

    Queue<Coord> q = new LinkedList<>();
    q.add(new Coord(minX - 1, minY - 1, minZ - 1));
    Set<Coord> visited = new HashSet<>();

    while (!q.isEmpty()) {
      Coord curr = q.poll();
      if (visited.contains(curr)) {
        continue;
      }
      visited.add(curr);

      if (airCubes.contains(curr)) {
        airCubes.remove(curr);
        q.addAll(
          List.of(
            new Coord(curr.x() - 1, curr.y(), curr.z()),
            new Coord(curr.x() + 1, curr.y(), curr.z()),
            new Coord(curr.x(), curr.y() - 1, curr.z()),
            new Coord(curr.x(), curr.y() + 1, curr.z()),
            new Coord(curr.x(), curr.y(), curr.z() - 1),
            new Coord(curr.x(), curr.y(), curr.z() + 1)
          )
        );
      }
    }

    // at this point we've removed all outside, so only trapped remain
    return airCubes;
  }

  private record Coord(int x, int y, int z) {}

  private record Cube(Coord coord) {}
}
