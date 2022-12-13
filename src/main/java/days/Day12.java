package days;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.function.Function;

public class Day12 implements Day {

  public void part1(List<String> input) {
    Character[][] grid = constructGridFromInput(input);
    Node start = constructGraphFromGrid(
      grid,
      false,
      c -> c == 'S',
      c -> c == 'E'
    );
    int len = doBFS(start);
    System.out.println("Num steps for shortest path: " + len);
  }

  public void part2(List<String> input) {
    Character[][] grid = constructGridFromInput(input);
    Node start = constructGraphFromGrid( // go backwards starting at E and ending at any 'a' or S
      grid,
      true,
      c -> c == 'E',
      c -> c == 'S' || c == 'a'
    );
    int len = doBFS(start);
    System.out.println("Actual shortest path is: " + len);
  }

  // x is how many over from left -- farthest left is 0
  // y is how many down from top -- top row is 0
  // [y][x]
  private Character[][] constructGridFromInput(List<String> input) {
    int height = input.size();
    int width = input.get(0).length();
    Character[][] map = new Character[height][width];
    for (int y = 0; y < height; y++) {
      String line = input.get(y);
      for (int x = 0; x < width; x++) {
        char c = line.charAt(x);
        map[y][x] = c;
      }
    }
    return map;
  }

  private Node constructGraphFromGrid(
    Character[][] grid,
    boolean useReverseReach,
    Function<Character, Boolean> startFunc,
    Function<Character, Boolean> endFunc
  ) {
    int height = grid.length;
    int width = grid[0].length;

    Node[][] nodeMap = new Node[height][width];
    Node start = null;

    for (int y = 0; y < height; y++) {
      Character[] row = grid[y];
      for (int x = 0; x < width; x++) {
        char c = row[x];

        Node node;
        if (nodeMap[y][x] != null) {
          node = nodeMap[y][x];
        } else {
          node = new Node(c, new Coord(x, y));
          nodeMap[y][x] = node;
        }

        addReachableNeighbors(grid, node, nodeMap, useReverseReach);

        if (startFunc.apply(c)) {
          start = node;
        }
        if (endFunc.apply(c)) {
          node.setIsEnd(true);
        }
      }
    }

    return start;
  }

  private void addReachableNeighbors(
    Character[][] map,
    Node currentNode,
    Node[][] nodeMap,
    boolean useReverse
  ) {
    char current = currentNode.getSymbol();
    int x = currentNode.getCoord().getX();
    int y = currentNode.getCoord().getY();

    // if can still go up
    if (y > 0) {
      char upOption = map[y - 1][x];
      if (
        useReverse ? canGoReverse(current, upOption) : canGo(current, upOption)
      ) {
        Node existingUp = nodeMap[y - 1][x];
        if (existingUp != null) {
          currentNode.addNeighbor(existingUp);
        } else {
          Node up = new Node(upOption, new Coord(x, y - 1));
          currentNode.addNeighbor(up);
          nodeMap[y - 1][x] = up;
        }
      }
    }
    // if can still go down
    if (y < map.length - 1) {
      char downOption = map[y + 1][x];
      if (
        useReverse
          ? canGoReverse(current, downOption)
          : canGo(current, downOption)
      ) {
        Node existingDown = nodeMap[y + 1][x];
        if (existingDown != null) {
          currentNode.addNeighbor(existingDown);
        } else {
          Node down = new Node(downOption, new Coord(x, y + 1));
          currentNode.addNeighbor(down);
          nodeMap[y + 1][x] = down;
        }
      }
    }
    // if can still go left
    if (x > 0) {
      char leftOption = map[y][x - 1];
      if (
        useReverse
          ? canGoReverse(current, leftOption)
          : canGo(current, leftOption)
      ) {
        Node existingLeft = nodeMap[y][x - 1];
        if (existingLeft != null) {
          currentNode.addNeighbor(existingLeft);
        } else {
          Node left = new Node(leftOption, new Coord(x - 1, y));
          currentNode.addNeighbor(left);
          nodeMap[y][x - 1] = left;
        }
      }
    }
    // if can still go right
    if (x < map[0].length - 1) {
      char rightOption = map[y][x + 1];
      if (
        useReverse
          ? canGoReverse(current, rightOption)
          : canGo(current, rightOption)
      ) {
        Node existingRight = nodeMap[y][x + 1];
        if (existingRight != null) {
          currentNode.addNeighbor(existingRight);
        } else {
          Node right = new Node(rightOption, new Coord(x + 1, y));
          currentNode.addNeighbor(right);
          nodeMap[y][x + 1] = right;
        }
      }
    }
  }

  private boolean canGo(char from, char to) {
    to = to == 'S' ? 'a' : to == 'E' ? 'z' : to;
    from = from == 'S' ? 'a' : from == 'E' ? 'z' : from;
    return (int) to - (int) from <= 1;
  }

  private boolean canGoReverse(char from, char to) {
    to = to == 'S' ? 'a' : to == 'E' ? 'z' : to;
    from = from == 'S' ? 'a' : from == 'E' ? 'z' : from;
    return (int) from - (int) to <= 1;
  }

  private int doBFS(Node startingNode) {
    Node end = null;
    Queue<Node> queue = new LinkedList<>();
    queue.add(startingNode);
    startingNode.setVisited(true);

    while (!queue.isEmpty()) {
      Node current = queue.poll();
      for (Node neighbor : current.getReachableNeighbors()) {
        if (!neighbor.getVisited()) {
          neighbor.setVisited(true);
          neighbor.setPrev(current);
          queue.add(neighbor);
          if (neighbor.isEnd()) {
            end = neighbor;
            queue.clear();
            break;
          }
        }
      }
    }

    int count = 0;
    Node c = end;
    while (c != null) {
      count++;
      c = c.getPrev();
    }
    return count - 1;
  }

  private static class Node {

    private final char symbol;
    private boolean visited = false;
    private Node prev = null;
    private final Set<Node> reachableNeighbors = new HashSet<>();
    private final Coord coord;
    private boolean isEnd = false;

    Node(char symbol, Coord coord) {
      this.symbol = symbol;
      this.coord = coord;
    }

    public char getSymbol() {
      return symbol;
    }

    public Coord getCoord() {
      return coord;
    }

    public void setVisited(boolean visited) {
      this.visited = visited;
    }

    private boolean getVisited() {
      return visited;
    }

    public Node getPrev() {
      return prev;
    }

    public void setPrev(Node prev) {
      this.prev = prev;
    }

    public Set<Node> getReachableNeighbors() {
      return reachableNeighbors;
    }

    public void addNeighbor(Node neighbor) {
      reachableNeighbors.add(neighbor);
    }

    public boolean isEnd() {
      return isEnd;
    }

    public void setIsEnd(boolean isEnd) {
      this.isEnd = isEnd;
    }
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
    public String toString() {
      return "(" + x + "," + y + ")";
    }
  }
}
