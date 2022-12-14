package days;

import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Day16 implements Day {

  private Map<Traversal, Integer> distancesBetweenValves;
  private Map<String, Integer> flowRateByValve;
  private Map<FlowEntry, Integer> totalFlowForEntry = new HashMap<>();

  public void part1(List<String> input) {
    parseInput(input);

    totalFlowForEntry = new HashMap<>();
    int maxFlow = findMaxFlow(
      "AA",
      30,
      new HashSet<>(flowRateByValve.keySet())
    );
    System.out.println("max flow: " + maxFlow);
  }

  public void part2(List<String> input) {
    parseInput(input);

    Set<String> v = flowRateByValve.keySet();
    Set<Set<String>> power = Sets.powerSet(v);
    Set<Set<Set<String>>> combosOfMeAndEle = new HashSet<>();
    for (Set<String> p : power) {
      Set<String> other = Sets.difference(v, p);
      combosOfMeAndEle.add(Set.of(p, other));
    }

    int best = 0;
    for (Set<Set<String>> combo : combosOfMeAndEle) {
      List<Set<String>> items = new ArrayList<>(combo);
      Set<String> valvesForMe = items.get(0);
      Set<String> valvesForEle = items.get(1);

      totalFlowForEntry = new HashMap<>();
      int forMe = findMaxFlow("AA", 26, valvesForMe);

      totalFlowForEntry = new HashMap<>();
      int forEle = findMaxFlow("AA", 26, valvesForEle);

      if (forMe + forEle > best) {
        best = forMe + forEle;
      }
    }

    System.out.println("max flow: " + best);
  }

  private static final int DISTANCE_UPPER = 1000;

  private void parseInput(List<String> input) {
    distancesBetweenValves = new HashMap<>();
    flowRateByValve = new HashMap<>();

    List<String> valves = new ArrayList<>();

    String pattern = "Valve (\\D+) has.*=(\\d+).*valves? (.*)";
    Pattern r = Pattern.compile(pattern);
    Splitter COMMA = Splitter.on(", ");

    for (String line : input) {
      Matcher m = r.matcher(line);
      m.find();

      String valveName = m.group(1);
      valves.add(valveName);

      int flowRate = Integer.parseInt(m.group(2));
      if (flowRate > 0) {
        flowRateByValve.put(valveName, flowRate);
      }

      List<String> targets = COMMA.splitToList(m.group(3));
      for (String target : targets) {
        distancesBetweenValves.put(new Traversal(valveName, target), 1);
      }
    }

    // Floyd-Warshall
    for (String valve : valves) {
      distancesBetweenValves.put(new Traversal(valve, valve), 0);
    }
    for (int k = 0; k < valves.size(); k++) {
      for (int i = 0; i < valves.size(); i++) {
        for (int j = 0; j < valves.size(); j++) {
          int dist_ij = distancesBetweenValves.computeIfAbsent(
            new Traversal(valves.get(i), valves.get(j)),
            ignore -> DISTANCE_UPPER
          );
          int dist_ik = distancesBetweenValves.computeIfAbsent(
            new Traversal(valves.get(i), valves.get(k)),
            ignore -> DISTANCE_UPPER
          );
          int dist_kj = distancesBetweenValves.computeIfAbsent(
            new Traversal(valves.get(k), valves.get(j)),
            ignore -> DISTANCE_UPPER
          );

          if (dist_ik + dist_kj < dist_ij) {
            distancesBetweenValves.put(
              new Traversal(valves.get(i), valves.get(j)),
              dist_ik + dist_kj
            );
          }
        }
      }
    }
  }

  private int findMaxFlow(
    String currentValve,
    int timeRemaining,
    Set<String> availableValves
  ) {
    if (availableValves.isEmpty()) {
      return 0;
    }

    if (timeRemaining <= 0) {
      return 0;
    }

    if (availableValves.size() == 1) {
      String targetValve = Iterables.getOnlyElement(availableValves);
      int toGetThere = distancesBetweenValves.get(
        new Traversal(currentValve, targetValve)
      );
      if (toGetThere + 1 < timeRemaining) {
        return (
          (timeRemaining - toGetThere - 1) * flowRateByValve.get(targetValve)
        );
      } else {
        return 0;
      }
    }

    FlowEntry key = new FlowEntry(currentValve, timeRemaining, availableValves);
    if (totalFlowForEntry.containsKey(key)) {
      return totalFlowForEntry.get(key);
    }

    int best = 0;
    for (String availableValve : availableValves) {
      Set<String> availableAsideFromThis = newSetExcluding(
        availableValves,
        availableValve
      );
      int toGetThere = distancesBetweenValves.get(
        new Traversal(currentValve, availableValve)
      );
      int curr =
        (
          flowRateByValve.get(availableValve) * (timeRemaining - toGetThere - 1)
        ) +
        findMaxFlow(
          availableValve,
          timeRemaining - toGetThere - 1,
          availableAsideFromThis
        );
      if (curr > best) {
        best = curr;
      }
    }

    totalFlowForEntry.put(key, best);
    return best;
  }

  private Set<String> newSetExcluding(Set<String> raw, String toExclude) {
    Set<String> result = new HashSet<>();
    for (String item : raw) {
      if (!item.equals(toExclude)) {
        result.add(item);
      }
    }
    return result;
  }

  private static class Traversal {

    private final String from;
    private final String to;

    Traversal(String from, String to) {
      this.from = from;
      this.to = to;
    }

    @Override
    public String toString() {
      return "(" + from + "->" + to + ")";
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }
      Traversal traversal = (Traversal) o;
      return (
        Objects.equals(from, traversal.from) && Objects.equals(to, traversal.to)
      );
    }

    @Override
    public int hashCode() {
      return Objects.hash(from, to);
    }
  }

  private static class FlowEntry {

    private final String valve;
    private final int timeRemaining;
    private final Set<String> availableValves;

    FlowEntry(String valve, int timeRemaining, Set<String> availableValves) {
      this.valve = valve;
      this.timeRemaining = timeRemaining;
      this.availableValves = availableValves;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }
      FlowEntry flowEntry = (FlowEntry) o;
      return (
        timeRemaining == flowEntry.timeRemaining &&
        Objects.equals(valve, flowEntry.valve) &&
        Objects.equals(availableValves, flowEntry.availableValves)
      );
    }

    @Override
    public int hashCode() {
      return Objects.hash(valve, timeRemaining, availableValves);
    }
  }
}
