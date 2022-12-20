package days;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Day20 implements Day {

  private int len = 0;
  private Node front = null;

  public void part1(List<String> input) {
    List<Long> encrypted = parseInput(input);

    List<Long> decrypted = mix(new ArrayList<>(encrypted), 1);

    long thouth = findCoordinate(1000, decrypted);
    long twothouth = findCoordinate(2000, decrypted);
    long threthouth = findCoordinate(3000, decrypted);

    System.out.println("Sum of coords: " + (thouth + twothouth + threthouth));
  }

  public void part2(List<String> input) {
    List<Long> encrypted = parseInput(input);

    long key = 811589153L;
    encrypted =
      encrypted.stream().map(val -> val * key).collect(Collectors.toList());

    List<Long> decrypted = mix(new ArrayList<>(encrypted), 10);

    long thouth = findCoordinate(1000, decrypted);
    long twothouth = findCoordinate(2000, decrypted);
    long threthouth = findCoordinate(3000, decrypted);

    System.out.println("Sum of coords: " + (thouth + twothouth + threthouth));
  }

  private List<Long> parseInput(List<String> input) {
    List<Long> encrypted = input
      .stream()
      .map(Long::parseLong)
      .collect(Collectors.toList());
    len = encrypted.size();
    return encrypted;
  }

  private List<Long> mix(List<Long> encrypted, int numMixes) {
    Map<Integer, Node> nodesByInitialIndex = toCyclicLinkedList(encrypted);
    for (int m = 0; m < numMixes; m++) {
      for (int e = 0; e < len; e++) {
        Node toMove = nodesByInitialIndex.get(e);
        move(toMove);
      }
    }
    return toList(front);
  }

  private void move(Node curr) {
    long val = curr.val();

    if (val == 0L) {
      return;
    }

    boolean forward = val > 0L;
    if (!forward) {
      val = Math.abs(val);
    }

    long numSwaps = val % (len - 1);
    for (long s = 1L; s <= numSwaps; s++) {
      boolean isFront = front.equals(curr);

      // if forward or backward && front, on your first swap, make your currently next thing front
      if (isFront && s == 1L) {
        front = curr.next();
      }
      // if forward && not front && is last swap && target's /next/ is front node, you're now front
      if (forward && !isFront && s == val) {
        if (front.equals(curr.next().next())) {
          front = curr;
        }
      }
      // if backward && not isFront && is last swap && target is front, you're now front
      if (!forward && !isFront && s == val) {
        if (front.equals(curr.next())) {
          front = curr;
        }
      }

      if (forward) {
        swapForward(curr);
      } else {
        swapBackward(curr);
      }
    }
  }

  private void swapForward(Node curr) {
    Node prev = curr.prev();
    Node next = curr.next();

    prev.setNext(next);
    next.setPrev(prev);

    curr.setNext(next.next());
    next.next().setPrev(curr);

    curr.setPrev(next);
    next.setNext(curr);
  }

  private void swapBackward(Node curr) {
    Node prev = curr.prev();
    Node next = curr.next();

    prev.setNext(next);
    next.setPrev(prev);

    curr.setPrev(prev.prev());
    prev.prev().setNext(curr);

    curr.setNext(prev);
    prev.setPrev(curr);
  }

  private Map<Integer, Node> toCyclicLinkedList(List<Long> encrypted) {
    Map<Integer, Node> nodesByInitialIndex = new HashMap<>();

    List<Node> nodes = encrypted
      .stream()
      .map(val -> new Node(val, null, null))
      .collect(Collectors.toList());

    for (int i = 0; i < nodes.size(); i++) {
      Node n = nodes.get(i);

      if (i == 0) { // head
        n.setNext(nodes.get(i + 1));
        n.setPrev(nodes.get(nodes.size() - 1));
        front = n;
      } else if (i == nodes.size() - 1) { // tail
        n.setNext(nodes.get(0));
        n.setPrev(nodes.get(i - 1));
      } else {
        n.setNext(nodes.get(i + 1));
        n.setPrev(nodes.get(i - 1));
      }

      nodesByInitialIndex.put(i, n);
    }

    return nodesByInitialIndex;
  }

  private List<Long> toList(Node front) {
    List<Long> list = new ArrayList<>();
    list.add(front.val());
    Node curr = front.next();
    while (curr != front) {
      list.add(curr.val());
      curr = curr.next();
    }
    return list;
  }

  private long findCoordinate(int offset, List<Long> decrypted) {
    return decrypted.get(
      (decrypted.indexOf(0L) + (offset % decrypted.size())) % decrypted.size()
    );
  }

  private class Node {

    private long val;
    private Node prev;
    private Node next;

    Node(long val, Node prev, Node next) {
      this.val = val;
      this.prev = prev;
      this.next = next;
    }

    public long val() {
      return val;
    }

    public Node prev() {
      return prev;
    }

    public void setPrev(Node prev) {
      this.prev = prev;
    }

    public Node next() {
      return next;
    }

    public void setNext(Node next) {
      this.next = next;
    }
  }
}
