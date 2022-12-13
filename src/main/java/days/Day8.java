package days;

import java.util.Arrays;
import java.util.List;

public class Day8 implements Day {

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
