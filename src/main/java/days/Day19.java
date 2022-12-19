package days;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class Day19 implements Day {

  private Map<OperationState, Integer> maxGeodesCanBeOpenByState;

  public void part1(List<String> input) {
    List<Blueprint> bps = parseInput(input);

    int sumQualityLevels = 0;
    for (Blueprint bp : bps) {
      maxGeodesCanBeOpenByState = new HashMap<>();

      Map<Resource, Integer> numRobotsByTypeAtStart = new HashMap<>();
      numRobotsByTypeAtStart.put(Resource.ORE, 1);
      numRobotsByTypeAtStart.put(Resource.CLAY, 0);
      numRobotsByTypeAtStart.put(Resource.OBSIDIAN, 0);
      numRobotsByTypeAtStart.put(Resource.GEODE, 0);

      Map<Resource, Integer> numResourcesByTypeAtStart = new HashMap<>();
      numResourcesByTypeAtStart.put(Resource.ORE, 0);
      numResourcesByTypeAtStart.put(Resource.CLAY, 0);
      numResourcesByTypeAtStart.put(Resource.OBSIDIAN, 0);
      numResourcesByTypeAtStart.put(Resource.GEODE, 0);

      int mostGeodesPossiblyOpened = calcMostGeodesPossiblyOpened(
        bp,
        24,
        numResourcesByTypeAtStart,
        numRobotsByTypeAtStart
      );

      int qualityLevel = bp.id() * mostGeodesPossiblyOpened;
      System.out.println(
        "bp " +
        bp.id() +
        ": max geodes: " +
        mostGeodesPossiblyOpened +
        " | quality level: " +
        qualityLevel
      );
      sumQualityLevels += qualityLevel;
    }
    System.out.println("Total quality level sum: " + sumQualityLevels);
  }

  public void part2(List<String> input) {
    List<Blueprint> bps = parseInput(input);

    int productOfMaxGeodes = 1;
    for (int i = 0; i < 3; i++) {
      Blueprint bp = bps.get(i);
      maxGeodesCanBeOpenByState = new HashMap<>();

      Map<Resource, Integer> numRobotsByTypeAtStart = new HashMap<>();
      numRobotsByTypeAtStart.put(Resource.ORE, 1);
      numRobotsByTypeAtStart.put(Resource.CLAY, 0);
      numRobotsByTypeAtStart.put(Resource.OBSIDIAN, 0);
      numRobotsByTypeAtStart.put(Resource.GEODE, 0);

      Map<Resource, Integer> numResourcesByTypeAtStart = new HashMap<>();
      numResourcesByTypeAtStart.put(Resource.ORE, 0);
      numResourcesByTypeAtStart.put(Resource.CLAY, 0);
      numResourcesByTypeAtStart.put(Resource.OBSIDIAN, 0);
      numResourcesByTypeAtStart.put(Resource.GEODE, 0); // dont need

      int mostGeodesPossiblyOpened = calcMostGeodesPossiblyOpened(
        bp,
        32,
        numResourcesByTypeAtStart,
        numRobotsByTypeAtStart
      );

      System.out.println(
        "bp " + bp.id() + ": max geodes: " + mostGeodesPossiblyOpened
      );
      productOfMaxGeodes *= mostGeodesPossiblyOpened;
    }
    System.out.println(
      "Product of first 3 bp max geodes: " + productOfMaxGeodes
    );
  }

  List<Blueprint> parseInput(List<String> input) {
    List<Blueprint> bps = new ArrayList<>(input.size());
    String pattern =
      "Blueprint (\\d+): Each ore robot costs (\\d+) ore. Each clay robot costs (\\d+) ore. Each obsidian robot costs (\\d+) ore and (\\d+) clay. Each geode robot costs (\\d+) ore and (\\d+) obsidian";
    Pattern r = Pattern.compile(pattern);
    for (String line : input) {
      Matcher m = r.matcher(line);
      m.find();
      bps.add(
        new Blueprint(
          Integer.parseInt(m.group(1)),
          Integer.parseInt(m.group(2)),
          Integer.parseInt(m.group(3)),
          Integer.parseInt(m.group(4)),
          Integer.parseInt(m.group(5)),
          Integer.parseInt(m.group(6)),
          Integer.parseInt(m.group(7))
        )
      );
    }
    return bps;
  }

  private int calcMostGeodesPossiblyOpened(
    Blueprint bp,
    int timeRemaining,
    Map<Resource, Integer> numResourcesByType,
    Map<Resource, Integer> numRobotsByType
  ) {
    if (timeRemaining <= 0) {
      return 0;
    }
    if (timeRemaining == 1) {
      return numRobotsByType.get(Resource.GEODE);
    }

    OperationState state = new OperationState(
      timeRemaining,
      numResourcesByType,
      numRobotsByType
    );
    if (maxGeodesCanBeOpenByState.containsKey(state)) {
      return maxGeodesCanBeOpenByState.get(state);
    }

    int best = 0;

    // building nothing
    Map<Resource, Integer> numResourcesByTypePostBuildingNothing = calculateNewResourcesPostCollection(
      bp,
      timeRemaining,
      numResourcesByType,
      numRobotsByType
    );
    int ifNothing =
      numRobotsByType.get(Resource.GEODE) +
      calcMostGeodesPossiblyOpened(
        bp,
        timeRemaining - 1,
        numResourcesByTypePostBuildingNothing,
        numRobotsByType
      );
    if (ifNothing > best) {
      best = ifNothing;
    }

    // building something
    for (Resource resource : Resource.values()) {
      if (
        noNeedToBuild(resource, bp, numRobotsByType) ||
        unableToBuild(resource, bp, numResourcesByType)
      ) {
        continue;
      }

      int built = ifBuiltRobot(
        resource,
        bp,
        timeRemaining,
        numResourcesByType,
        numRobotsByType
      );
      if (built > best) {
        best = built;
      }
    }

    maxGeodesCanBeOpenByState.put(state, best);
    return best;
  }

  private boolean noNeedToBuild(
    Resource toBuild,
    Blueprint bp,
    Map<Resource, Integer> numRobotsByType
  ) {
    // no need for more ore robots if already have so many that we'll always have enough to build whatever we want in a round
    return switch (toBuild) {
      case ORE -> numRobotsByType.get(Resource.ORE) >=
      getMaxResourceCost(Resource.ORE, bp);
      case CLAY -> numRobotsByType.get(Resource.CLAY) >=
      getMaxResourceCost(Resource.CLAY, bp);
      case OBSIDIAN -> numRobotsByType.get(Resource.OBSIDIAN) >=
      getMaxResourceCost(Resource.OBSIDIAN, bp);
      case GEODE -> false; // always want more
    };
  }

  private int getMaxResourceCost(Resource resource, Blueprint bp) {
    return switch (resource) {
      case ORE -> maxOreCost(bp);
      case CLAY -> bp.obsidianRobotClayClost();
      case OBSIDIAN -> bp.geodeRobotObsidianCost();
      case GEODE -> throw new IllegalArgumentException("weird yo");
    };
  }

  private int maxOreCost(Blueprint bp) {
    return Stream
      .of(
        bp.oreRobotOreCost(),
        bp.clayRobotOreCost(),
        bp.obsidianRobotOreCost(),
        bp.geodeRobotOreCost()
      )
      .max(Integer::compareTo)
      .get();
  }

  private boolean unableToBuild(
    Resource toBuild,
    Blueprint bp,
    Map<Resource, Integer> numResourcesByType
  ) {
    return switch (toBuild) {
      case ORE -> numResourcesByType.get(Resource.ORE) < bp.oreRobotOreCost();
      case CLAY -> numResourcesByType.get(Resource.ORE) < bp.clayRobotOreCost();
      case OBSIDIAN -> numResourcesByType.get(Resource.ORE) <
      bp.obsidianRobotOreCost() ||
      numResourcesByType.get(Resource.CLAY) < bp.obsidianRobotClayClost();
      case GEODE -> numResourcesByType.get(Resource.ORE) <
      bp.geodeRobotOreCost() ||
      numResourcesByType.get(Resource.OBSIDIAN) < bp.geodeRobotObsidianCost();
    };
  }

  private int ifBuiltRobot(
    Resource toBuild,
    Blueprint bp,
    int timeRemaining,
    Map<Resource, Integer> numResourcesByType,
    Map<Resource, Integer> numRobotsByType
  ) {
    Map<Resource, Integer> numResourcesByTypeMinusBuildCosts = new HashMap<>(
      numResourcesByType
    );
    switch (toBuild) {
      case ORE -> {
        numResourcesByTypeMinusBuildCosts.put(
          Resource.ORE,
          numResourcesByTypeMinusBuildCosts.get(Resource.ORE) -
          bp.oreRobotOreCost()
        );
      }
      case CLAY -> {
        numResourcesByTypeMinusBuildCosts.put(
          Resource.ORE,
          numResourcesByTypeMinusBuildCosts.get(Resource.ORE) -
          bp.clayRobotOreCost()
        );
      }
      case OBSIDIAN -> {
        numResourcesByTypeMinusBuildCosts.put(
          Resource.ORE,
          numResourcesByTypeMinusBuildCosts.get(Resource.ORE) -
          bp.obsidianRobotOreCost()
        );
        numResourcesByTypeMinusBuildCosts.put(
          Resource.CLAY,
          numResourcesByTypeMinusBuildCosts.get(Resource.CLAY) -
          bp.obsidianRobotClayClost()
        );
      }
      case GEODE -> {
        numResourcesByTypeMinusBuildCosts.put(
          Resource.ORE,
          numResourcesByTypeMinusBuildCosts.get(Resource.ORE) -
          bp.geodeRobotOreCost()
        );
        numResourcesByTypeMinusBuildCosts.put(
          Resource.OBSIDIAN,
          numResourcesByTypeMinusBuildCosts.get(Resource.OBSIDIAN) -
          bp.geodeRobotObsidianCost()
        );
      }
    }

    Map<Resource, Integer> numResourcesByTypeAtRoundEnd = calculateNewResourcesPostCollection(
      bp,
      timeRemaining,
      numResourcesByTypeMinusBuildCosts,
      numRobotsByType
    );

    Map<Resource, Integer> numRobotsByTypeIncludingBuilt = new HashMap<>(
      numRobotsByType
    );
    numRobotsByTypeIncludingBuilt.put(
      toBuild,
      numRobotsByTypeIncludingBuilt.get(toBuild) + 1
    );

    return (
      numRobotsByType.get(Resource.GEODE) +
      calcMostGeodesPossiblyOpened(
        bp,
        timeRemaining - 1,
        numResourcesByTypeAtRoundEnd,
        numRobotsByTypeIncludingBuilt
      )
    );
  }

  private Map<Resource, Integer> calculateNewResourcesPostCollection(
    Blueprint bp,
    int timeRemaining,
    Map<Resource, Integer> currentResources,
    Map<Resource, Integer> currentRobotsByType
  ) {
    Map<Resource, Integer> numResourcesByTypePostCollection = new HashMap<>();

    // if have more resources than would ever need, don't keep incrementing
    for (Resource resource : List.of(
      Resource.ORE,
      Resource.CLAY,
      Resource.OBSIDIAN
    )) {
      int maxCost = getMaxResourceCost(resource, bp);
      int maxEverNeeded = (timeRemaining - 1) * maxCost;
      int willProduce = currentRobotsByType.get(resource) * (timeRemaining - 2);
      if (currentResources.get(resource) + willProduce >= maxEverNeeded) {
        numResourcesByTypePostCollection.put(resource, maxEverNeeded);
      } else {
        numResourcesByTypePostCollection.put(
          resource,
          currentResources.get(resource) + currentRobotsByType.get(resource)
        );
      }
    }

    return numResourcesByTypePostCollection;
  }

  private enum Resource {
    ORE,
    CLAY,
    OBSIDIAN,
    GEODE,
  }

  private record Blueprint(
    int id,
    int oreRobotOreCost,
    int clayRobotOreCost,
    int obsidianRobotOreCost,
    int obsidianRobotClayClost,
    int geodeRobotOreCost,
    int geodeRobotObsidianCost
  ) {}

  private record OperationState(
    int timeRemaining,
    Map<Resource, Integer> numResourcesByType,
    Map<Resource, Integer> numRobotsByType
  ) {}
}
