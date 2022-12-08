package days;

import java.util.Arrays;
import java.util.List;

public class Day8 implements Day {

  // determine whether there is enough tree cover here to keep a tree house hidden.
  // To do this, you need to count the number of trees that are visible from outside the grid when looking directly along a row or column

  // input: a map with the height of each tree
  // Each tree is represented as a single digit whose value is its height, where 0 is the shortest and 9 is the tallest.

  // A tree is visible if all of the other trees between it and a single edge of the grid are SHORTER than it. (1 tree of >= height means not visible)
  // Only consider trees in the same row or column; that is, only look up, down, left, or right from any given tree -- only horizontal and vertical
  //
  // All of the trees around the edge of the grid are visible
  // - since they are already on the edge, there are no trees to block the view
  // - if grid has h x w, # edge trees = h-1 + h-1 + w-1 + w-1
  // - h is # of lines with something; w = len of lines
  // so just inner i x j to look at, where i = h - 2, j = w - 2

  // [y][x]
  // y = how many down from top, where first row is 0
  // x = how many over from left, where first column is 0
  // similar to bot right quadrant in plane, where top-left is 0,0

  // answer = edge trees + visible inner trees
  // TODO: how many trees are visible from outside the grid?

  // p2
  // To measure the viewing distance from a given tree, look up, down, left, and right from that tree;
  // stop if you reach an edge or at the first tree that is the same height or taller than the tree under consideration.
  // (If a tree is right on the edge, at least one of its viewing distances will be zero.)

  // A tree's scenic score is found by multiplying together its viewing distance in each of the four directions.
  // TODO: Consider each tree on your map. What is the highest scenic score possible for any tree?

  public void part1(List<String> input) {
    int[][] grid = constructGrid(input);
    printGrid(grid);

    int numVisible = calculateNumVisible(grid);
    System.out.println("Num visible: " + numVisible);
  }

  public void part2(List<String> input) {
    int[][] grid = constructGrid(input);
    printGrid(grid);

    int[][] scenicScores = calculateScenicScores(grid);
    int mostScenicScore = findMostScenery(scenicScores);
    System.out.println("Most scenic score: " + mostScenicScore);
  }

  private int[][] constructGrid(List<String> input) {
    int[][] grid = new int[input.size()][input.get(0).length()];
    for (int i = 0; i < input.size(); i++) {
      grid[i] =
        Arrays
          .stream(input.get(i).split(""))
          .mapToInt(Integer::parseInt)
          .toArray();
    }
    return grid;
  }

  private void printGrid(int[][] grid) {
    for (int[] row : grid) {
      for (int item : row) {
        System.out.print(item);
      }
      System.out.println();
    }
  }

  private int calculateNumVisible(int[][] grid) {
    int numVisible = 0;
    for (int y = 0; y < grid.length; y++) {
      for (int x = 0; x < grid[y].length; x++) {
        if (isVisible(x, y, grid)) {
          numVisible++;
        }
      }
    }
    return numVisible;
  }

  private boolean isVisible(int x, int y, int[][] grid) {
    if (isVisibleFromLeft(x, y, grid)) {
      return true;
    }
    if (isVisibleFromRight(x, y, grid)) {
      return true;
    }
    if (isVisibleFromTop(x, y, grid)) {
      return true;
    }
    if (isVisibleFromBottom(x, y, grid)) {
      return true;
    }
    return false;
  }

  private boolean isVisibleFromLeft(int x, int y, int[][] grid) {
    if (x == 0) {
      return true;
    }

    int tree = grid[y][x];
    int[] row = grid[y];

    for (int l = x - 1; l >= 0; l--) {
      int otherTree = row[l];
      if (otherTree >= tree) {
        return false;
      }
    }
    return true;
  }

  private boolean isVisibleFromRight(int x, int y, int[][] grid) {
    int[] row = grid[y];

    if (x == row.length - 1) {
      return true;
    }

    int tree = grid[y][x];

    for (int r = x + 1; r < row.length; r++) {
      int otherTree = row[r];
      if (otherTree >= tree) {
        return false;
      }
    }
    return true;
  }

  private boolean isVisibleFromTop(int x, int y, int[][] grid) {
    if (y == 0) {
      return true;
    }

    int tree = grid[y][x];

    for (int t = y - 1; t >= 0; t--) {
      int otherTree = grid[t][x];
      if (otherTree >= tree) {
        return false;
      }
    }
    return true;
  }

  private boolean isVisibleFromBottom(int x, int y, int[][] grid) {
    if (y == grid.length - 1) {
      return true;
    }

    int tree = grid[y][x];

    for (int b = y + 1; b < grid.length; b++) {
      int otherTree = grid[b][x];
      if (otherTree >= tree) {
        return false;
      }
    }
    return true;
  }

  private int[][] calculateScenicScores(int[][] grid) {
    int[][] scenicScores = new int[grid.length][grid[0].length];
    for (int y = 0; y < grid.length; y++) {
      for (int x = 0; x < grid[y].length; x++) {
        scenicScores[y][x] = howScenic(x, y, grid);
      }
    }
    return scenicScores;
  }

  private int howScenic(int x, int y, int[][] grid) {
    return (
      leftScenery(x, y, grid) *
      rightScenery(x, y, grid) *
      topScenery(x, y, grid) *
      bottomScenery(x, y, grid)
    );
  }

  private int leftScenery(int x, int y, int[][] grid) {
    if (x == 0) {
      return 0;
    }

    int tree = grid[y][x];
    int[] row = grid[y];

    int scenic = 0;
    for (int l = x - 1; l >= 0; l--) {
      scenic++;
      int otherTree = row[l];
      if (otherTree >= tree) {
        return scenic;
      }
    }
    return scenic;
  }

  private int rightScenery(int x, int y, int[][] grid) {
    int[] row = grid[y];

    if (x == row.length - 1) {
      return 0;
    }

    int tree = grid[y][x];

    int scenic = 0;
    for (int r = x + 1; r < row.length; r++) {
      scenic++;
      int otherTree = row[r];
      if (otherTree >= tree) {
        return scenic;
      }
    }
    return scenic;
  }

  private int topScenery(int x, int y, int[][] grid) {
    if (y == 0) {
      return 0;
    }

    int tree = grid[y][x];

    int scenic = 0;
    for (int t = y - 1; t >= 0; t--) {
      scenic++;
      int otherTree = grid[t][x];
      if (otherTree >= tree) {
        return scenic;
      }
    }
    return scenic;
  }

  private int bottomScenery(int x, int y, int[][] grid) {
    if (y == grid.length - 1) {
      return 0;
    }

    int tree = grid[y][x];

    int scenic = 0;
    for (int b = y + 1; b < grid.length; b++) {
      scenic++;
      int otherTree = grid[b][x];
      if (otherTree >= tree) {
        return scenic;
      }
    }
    return scenic;
  }

  private int findMostScenery(int[][] scenicScore) {
    int mostScenery = 0;
    for (int[] row : scenicScore) {
      for (int score : row) {
        if (score > mostScenery) {
          mostScenery = score;
        }
      }
    }
    return mostScenery;
  }
}
