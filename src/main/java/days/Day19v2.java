package days;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class Day19v2 implements Day {

  public void part1(List<String> input) {
    List<Blueprint> bps = parseInput(input);

    int sumQualityLevels = 0;
    for (Blueprint bp : bps) {
      int mostGeodesPossiblyOpened = calcMostGeodesViaBfs(
        bp.oreRobotOreCost(),
        bp.clayRobotOreCost(),
        bp.obsidianRobotOreCost(),
        bp.obsidianRobotClayClost(),
        bp.geodeRobotOreCost(),
        bp.geodeRobotObsidianCost(),
        24
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
      int mostGeodesPossiblyOpened = calcMostGeodesViaBfs(
        bp.oreRobotOreCost(),
        bp.clayRobotOreCost(),
        bp.obsidianRobotOreCost(),
        bp.obsidianRobotClayClost(),
        bp.geodeRobotOreCost(),
        bp.geodeRobotObsidianCost(),
        32
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

  private int calcMostGeodesViaBfs(
    int oreRobotOreCost,
    int clayRobotOreCost,
    int obsidianRobotOreCost,
    int obsidianRobotClayClost,
    int geodeRobotOreCost,
    int geodeRobotObsidianCost,
    int timeRemaining
  ) {
    int best = 0;

    OperationState initialState = new OperationState(
      0,
      0,
      0,
      0,
      1,
      0,
      0,
      0,
      timeRemaining
    );

    Queue<OperationState> q = new LinkedList<>();
    q.add(initialState);

    Set<OperationState> seen = new HashSet<>();

    while (!q.isEmpty()) {
      OperationState state = q.poll();
      int oreAmt = state.oreAmt();
      int clayAmt = state.clayAmt();
      int obsAmt = state.obsAmt();
      int geoAmt = state.geoAmt();
      int oreRobs = state.oreRobs();
      int clayRobs = state.clayRobs();
      int obsRobs = state.obsRobs();
      int geoRobs = state.geoRobs();
      int currTimeRemaining = state.timeRemaining();

      best = Math.max(best, geoAmt);
      if (currTimeRemaining == 0) {
        continue;
      }

      // robot culling
      int oreMaxCost = Stream
        .of(
          oreRobotOreCost,
          clayRobotOreCost,
          obsidianRobotOreCost,
          geodeRobotOreCost
        )
        .max(Integer::compareTo)
        .get();
      if (oreRobs >= oreMaxCost) {
        oreRobs = oreMaxCost;
      }
      if (clayRobs >= obsidianRobotClayClost) {
        clayRobs = obsidianRobotClayClost;
      }
      if (obsRobs >= geodeRobotObsidianCost) {
        obsRobs = geodeRobotObsidianCost;
      }

      // resource culling
      int oreEverNeed =
        (timeRemaining * oreMaxCost) - (oreRobs * (timeRemaining - 1));
      if (oreAmt >= oreEverNeed) {
        oreAmt = oreEverNeed;
      }
      int clayEverNeeded =
        (timeRemaining * obsidianRobotClayClost) -
        (clayRobs * (timeRemaining - 1));
      if (clayAmt >= clayEverNeeded) {
        clayAmt = clayEverNeeded;
      }
      int obsEverNeeded =
        (timeRemaining * geodeRobotObsidianCost) -
        (obsRobs * (timeRemaining - 1));
      if (obsAmt >= obsEverNeeded) {
        obsAmt = obsEverNeeded;
      }

      state =
        new OperationState(
          oreAmt,
          clayAmt,
          obsAmt,
          geoAmt,
          oreRobs,
          clayRobs,
          obsRobs,
          geoRobs,
          currTimeRemaining
        );

      if (seen.contains(state)) {
        continue;
      }
      seen.add(state);

      if (seen.size() % 1000000 == 0) {
        System.out.println(
          "t:" + currTimeRemaining + ", best:" + best + ", len:" + seen.size()
        );
      }

      // build nothing
      q.add(
        new OperationState(
          oreAmt + oreRobs,
          clayAmt + clayRobs,
          obsAmt + obsRobs,
          geoAmt + geoRobs,
          oreRobs,
          clayRobs,
          obsRobs,
          geoRobs,
          timeRemaining - 1
        )
      );

      // build stuff
      if (oreAmt >= oreRobotOreCost) {
        q.add(
          new OperationState(
            oreAmt - oreRobotOreCost + oreRobs,
            clayAmt + clayRobs,
            obsAmt + obsRobs,
            geoAmt + geoRobs,
            oreRobs + 1,
            clayRobs,
            obsRobs,
            geoRobs,
            timeRemaining - 1
          )
        );
      }

      if (oreAmt >= clayRobotOreCost) {
        q.add(
          new OperationState(
            oreAmt - clayRobotOreCost + oreRobs,
            clayAmt + clayRobs,
            obsAmt + obsRobs,
            geoAmt + geoRobs,
            oreRobs,
            clayRobs + 1,
            obsRobs,
            geoRobs,
            timeRemaining - 1
          )
        );
      }

      if (oreAmt >= obsidianRobotOreCost && clayAmt >= obsidianRobotClayClost) {
        q.add(
          new OperationState(
            oreAmt - obsidianRobotOreCost + oreRobs,
            clayAmt - obsidianRobotClayClost + clayRobs,
            obsAmt + obsRobs,
            geoAmt + geoRobs,
            oreRobs,
            clayRobs,
            obsRobs + 1,
            geoRobs,
            timeRemaining - 1
          )
        );
      }

      if (oreAmt >= geodeRobotOreCost && obsAmt >= geodeRobotObsidianCost) {
        q.add(
          new OperationState(
            oreAmt - geodeRobotOreCost + oreRobs,
            clayAmt + clayRobs,
            obsAmt - geodeRobotObsidianCost + obsRobs,
            geoAmt + geoRobs,
            oreRobs,
            clayRobs,
            obsRobs,
            geoRobs + 1,
            timeRemaining - 1
          )
        );
      }
    }

    return best;
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
    int oreAmt,
    int clayAmt,
    int obsAmt,
    int geoAmt,
    int oreRobs,
    int clayRobs,
    int obsRobs,
    int geoRobs,
    int timeRemaining
  ) {}
}
