package days;

import com.google.common.base.Splitter;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class Day15 implements Day {

  public void part1(List<String> input) {
    Map<Coord, Coord> sensorToClosestBeacon = parseInput(input);

    Map<Coord, Integer> rangeWhereCantBeBySensor = calculateRangeWhereCantBeBySensor(
      sensorToClosestBeacon
    );

    int farthestLeftX = rangeWhereCantBeBySensor
      .entrySet()
      .stream()
      .map(entry -> entry.getKey().getX() - entry.getValue())
      .min(Integer::compareTo)
      .get();
    int farthestRightX = rangeWhereCantBeBySensor
      .entrySet()
      .stream()
      .map(entry -> entry.getKey().getX() + entry.getValue())
      .max(Integer::compareTo)
      .get();

    int y = 2000000;
    Collection<Coord> beacons = sensorToClosestBeacon.values();

    int numCantBeOnYRow = 0;
    for (int x = farthestLeftX; x <= farthestRightX; x++) {
      Coord current = new Coord(x, y);
      if (beacons.contains(current)) {
        continue;
      }
      if (withinRangeOfAnySensor(rangeWhereCantBeBySensor, current)) {
        numCantBeOnYRow++;
      }
    }

    System.out.println(
      "Num positions that can't contain a beacon on row " +
      y +
      ": " +
      numCantBeOnYRow
    );
  }

  public void part2(List<String> input) {
    Map<Coord, Coord> sensorToClosestBeacon = parseInput(input);

    Map<Coord, Integer> rangeWhereCantBeBySensor = calculateRangeWhereCantBeBySensor(
      sensorToClosestBeacon
    );

    // the uncovered point must be in between four diamond edges:
    // - to the right of a bot-right line (/)
    // - to the right of a top-right line (\)
    // - to the left of a bot-left line (\)
    // - to the left of a top-left line (/)
    // so, we can arbitrarily pick one of these four "sides" of the diamonds to use
    // -- let's pick the bot-right line, and grab the coords that define it for each sensor -- the bot and right points of the diamond
    Set<List<Coord>> botAndRightDiamondPointCoordsForAllSensors = calcBotAndRightDiamondPointCoordsForAllSensors(
      rangeWhereCantBeBySensor
    );

    long limit = 4000000;

    // now, let's walk along that bot-right line for each sensor diamond
    // - so, we know the uncovered point would be x+1 to the right
    // - if that point is not within range of any sensors at all, that's gotta be it
    Coord uncoveredSpot = findAvailablePointOneToRightOfBotRightLine(
      botAndRightDiamondPointCoordsForAllSensors,
      rangeWhereCantBeBySensor,
      limit
    );

    long tuningFreq =
      ((long) uncoveredSpot.getX() * limit) + uncoveredSpot.getY();
    System.out.println("Tuning frequency of distress beacon: " + tuningFreq);
  }

  private static final Splitter COLON_SPLITTER = Splitter.on(": ");
  private static final Splitter AT_SPLITTER = Splitter.on("at ");
  private static final Splitter COMMA_SPLITTER = Splitter.on(", ");

  private Map<Coord, Coord> parseInput(List<String> input) {
    Map<Coord, Coord> sensorToClosestBeacon = new HashMap<>(input.size());
    for (String line : input) {
      List<String> parts = COLON_SPLITTER.splitToList(line);
      Coord sensor = extractCoordFromHalf(parts.get(0));
      Coord beacon = extractCoordFromHalf(parts.get(1));
      sensorToClosestBeacon.put(sensor, beacon);
    }
    return sensorToClosestBeacon;
  }

  private Coord extractCoordFromHalf(String half) {
    String coord = AT_SPLITTER.splitToList(half).get(1);
    List<String> coordParts = COMMA_SPLITTER.splitToList(coord);
    return new Coord(
      getNumFromCordPart(coordParts.get(0)),
      getNumFromCordPart(coordParts.get(1))
    );
  }

  private int getNumFromCordPart(String coordPart) {
    // e.g. "x=16"
    return Integer.parseInt(coordPart.substring(coordPart.indexOf('=') + 1));
  }

  private Map<Coord, Integer> calculateRangeWhereCantBeBySensor(
    Map<Coord, Coord> sensorToClosestBeacon
  ) {
    Map<Coord, Integer> rangeWhereCantBeBySensor = new HashMap<>();
    for (Map.Entry<Coord, Coord> entry : sensorToClosestBeacon.entrySet()) {
      Coord sensor = entry.getKey();
      Coord closestBeacon = entry.getValue();
      rangeWhereCantBeBySensor.put(
        sensor,
        calculateManhattanDistance(sensor, closestBeacon)
      );
    }
    return rangeWhereCantBeBySensor;
  }

  private int calculateManhattanDistance(Coord start, Coord end) {
    return (
      Math.abs(start.getX() - end.getX()) + Math.abs(start.getY() - end.getY())
    );
  }

  private boolean withinRangeOfAnySensor(
    Map<Coord, Integer> rangeWhereCantBeBySensor,
    Coord current
  ) {
    for (Coord sensor : rangeWhereCantBeBySensor.keySet()) {
      int range = rangeWhereCantBeBySensor.get(sensor);
      int distToSensor = calculateManhattanDistance(current, sensor);
      if (distToSensor <= range) {
        return true;
      }
    }
    return false;
  }

  private Set<List<Coord>> calcBotAndRightDiamondPointCoordsForAllSensors(
    Map<Coord, Integer> rangeWhereCantBeBySensor
  ) {
    Set<List<Coord>> calcBotAndRightDiamondPointCoordsForAllSensors = new HashSet<>();
    for (Coord sensor : rangeWhereCantBeBySensor.keySet()) {
      int range = rangeWhereCantBeBySensor.get(sensor);
      Coord bot = new Coord(sensor.getX(), sensor.getY() - range);
      Coord right = new Coord(sensor.getX() + range, sensor.getY());
      calcBotAndRightDiamondPointCoordsForAllSensors.add(List.of(bot, right));
    }
    return calcBotAndRightDiamondPointCoordsForAllSensors;
  }

  private Coord findAvailablePointOneToRightOfBotRightLine(
    Set<List<Coord>> botAndRightDiamondPointCoordsForAllSensors,
    Map<Coord, Integer> rangeWhereCantBeBySensor,
    long bound
  ) {
    for (List<Coord> botAndRightDiamondPointCoordsForSensor : botAndRightDiamondPointCoordsForAllSensors) {
      Coord bot = botAndRightDiamondPointCoordsForSensor.get(0);
      Coord right = botAndRightDiamondPointCoordsForSensor.get(1);

      for (int i = 0; i <= calculateManhattanDistance(bot, right) / 2; i++) {
        Coord currentPointOnLine = new Coord(bot.getX() + i, bot.getY() + i);
        Coord oneToRight = new Coord(
          currentPointOnLine.getX() + 1,
          currentPointOnLine.getY()
        );

        if (
          isInBounds(oneToRight, bound) &&
          !withinRangeOfAnySensor(rangeWhereCantBeBySensor, oneToRight)
        ) {
          return oneToRight;
        }
      }
    }
    throw new IllegalArgumentException("theoretically impossible");
  }

  private boolean isInBounds(Coord coord, long bound) {
    return (
      (coord.getX() >= 0 && coord.getX() <= bound) &&
      (coord.getY() >= 0 && coord.getY() <= bound)
    );
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

    @Override
    public String toString() {
      return "(" + x + "," + y + ")";
    }
  }
}
