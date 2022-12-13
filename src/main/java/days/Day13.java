package days;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Day13 implements Day {

  public void part1(List<String> input) {
    List<PacketPair> packetPairList = parseToPacketPairList(input);

    List<Integer> pairIndicesInRightOrder = new ArrayList<>();
    for (int p = 0; p < packetPairList.size(); p++) {
      PacketPair pair = packetPairList.get(p);
      if (packetInRightOrder(pair.getLeft(), pair.getRight()) == 1) {
        pairIndicesInRightOrder.add(p + 1); // packets indexed from 1
      }
    }
    int rightIndicesSum = pairIndicesInRightOrder
      .stream()
      .mapToInt(Integer::intValue)
      .sum();
    System.out.println("Sum of indices in right order: " + rightIndicesSum);
  }

  public void part2(List<String> input) {
    List<PacketPair> packetPairList = parseToPacketPairList(input);

    List<List<Object>> allPacketsFromPairs = packetPairList
      .stream()
      .flatMap(pp -> Stream.of(pp.getLeft(), pp.getRight()))
      .collect(Collectors.toList());

    // two divider packets
    List<Object> divider1 = List.of(2);
    List<Object> divider2 = List.of(6);
    allPacketsFromPairs.add(divider1);
    allPacketsFromPairs.add(divider2);

    List<List<Object>> sorted = allPacketsFromPairs
      .stream()
      .sorted(
        Comparator.comparing(
          Function.identity(),
          (l, r) -> packetInRightOrder(l, r) * -1
          // packetInRightOrder and comparator use opposite signs, so flip
        )
      )
      .collect(Collectors.toList());

    System.out.println(
      "Decoder's multiplied: " +
      ((sorted.indexOf(divider1) + 1) * (sorted.indexOf(divider2) + 1))
    );
  }

  private List<PacketPair> parseToPacketPairList(List<String> input) {
    List<PacketPair> packetPairList = new ArrayList<>();
    for (int i = 0; i < input.size(); i += 3) {
      List<Object> left = parsePacket(input.get(i));
      List<Object> right = parsePacket(input.get(i + 1));
      packetPairList.add(new PacketPair(left, right));
    }
    return packetPairList;
  }

  private List<Object> parsePacket(String packet) {
    String contents = packet.substring(1, packet.length() - 1);
    return parseListContents(contents);
  }

  private List<Object> parseListContents(String contents) {
    if (contents.isEmpty()) {
      return Collections.emptyList();
    }

    List<Object> result = new ArrayList<>();

    for (int i = 0; i < contents.length(); i++) {
      char c = contents.charAt(i);
      if (c == '[') {
        int close = findSubListCloseFromOpeningBracket(i, contents);
        String sublistContents = contents.substring(i + 1, i + close);
        result.add(parseListContents(sublistContents));
        i = i + close;
      } else if (c == ',') {
        continue;
      } else {
        String nextCommaSearchSpace = contents.substring(i);
        int nextComma = nextCommaSearchSpace.indexOf(",");
        if (nextComma == -1) {
          result.add(Integer.parseInt(contents.substring(i)));
        } else {
          result.add(
            Integer.parseInt(nextCommaSearchSpace.substring(0, nextComma))
          );
        }
      }
    }

    return result;
  }

  private int findSubListCloseFromOpeningBracket(
    int openingIndex,
    String contents
  ) {
    int numCloseToUse = 0;
    String sub = contents.substring(openingIndex + 1);
    for (int i = 0; i < sub.length(); i++) {
      char c = sub.charAt(i);
      if (c == '[') {
        // found another opening, so don't want next closer
        numCloseToUse++;
        continue;
      }
      if (c == ']') {
        if (numCloseToUse == 0) {
          return i + 1; // since we substringed starting 1 over
        } else {
          numCloseToUse--;
        }
      }
    }
    throw new IllegalArgumentException("impossible! no closing bracket!");
  }

  private int packetInRightOrder(List<Object> left, List<Object> right) {
    for (int i = 0; i < left.size(); i++) {
      if (i == right.size()) {
        // right ran out
        return -1;
      }

      Object l = left.get(i);
      Object r = right.get(i);

      if (l instanceof Integer && r instanceof Integer) {
        int lInt = (int) l;
        int rInt = (int) r;
        if (lInt < rInt) {
          return 1;
        }
        if (rInt < lInt) {
          return -1;
        }
        continue;
      }

      if (l instanceof List<?> && r instanceof List<?>) {
        int result = packetInRightOrder((List<Object>) l, (List<Object>) r);
        if (result != 0) {
          return result;
        }
      }

      if (l instanceof List<?> && r instanceof Integer) {
        int result = packetInRightOrder((List<Object>) l, List.of(r));
        if (result != 0) {
          return result;
        }
      }
      if (l instanceof Integer && r instanceof List<?>) {
        int result = packetInRightOrder(List.of(l), (List<Object>) r);
        if (result != 0) {
          return result;
        }
      }
    }

    if (left.size() == right.size()) {
      // ran out same time, inconclusive
      return 0;
    }
    // otherwise left ran out first
    return 1;
  }

  private static class PacketPair {

    private final List<Object> left;
    private final List<Object> right;

    PacketPair(List<Object> left, List<Object> right) {
      this.left = left;
      this.right = right;
    }

    public List<Object> getLeft() {
      return left;
    }

    public List<Object> getRight() {
      return right;
    }

    @Override
    public String toString() {
      return "PacketPair{" + "left=" + left + ", right=" + right + '}';
    }
  }
}
