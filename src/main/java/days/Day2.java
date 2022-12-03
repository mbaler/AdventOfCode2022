package days;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import java.util.List;

// input: RPS encrypted strategy guide
// -- 1st column: opponent's play:  A for Rock, B for Paper, and C for Scissors
// -- 2nd column: your response:    X for Rock, Y for Paper, and Z for Scissors
//
// Total score = sum of scores for each round
//  - score for a single round =
//      score for your shape (1 for Rock, 2 for Paper, and 3 for Scissors)
//        +
//      score for round outcome (0 if lost, 3 if draw, and 6 if won)

// TODO: calculate your total score if you follow the strategy guide

// strategy: calculate score for each row (round), add scores up for all rows

// p2
// 2nd column: how the round needs to end:  X you lose, Y you draw, and Z you win
// you need to figure out what shape to choose so the round ends as indicated

public class Day2 implements ADay {

  private static final Splitter SPLITTER = Splitter.on(" ");

  public void part1(List<String> input) {
    List<Integer> roundScores = p1calculateScoresPerRound(input);
    System.out.println("Round scores: " + roundScores);

    int totalScore = roundScores.stream().mapToInt(Integer::intValue).sum();
    System.out.println("Total score: " + totalScore);
  }

  public void part2(List<String> input) {
    List<Integer> roundScores = p2calculateScoresPerRound(input);
    System.out.println("Round scores: " + roundScores);

    int totalScore = roundScores.stream().mapToInt(Integer::intValue).sum();
    System.out.println("Total score: " + totalScore);
  }

  private List<Integer> p1calculateScoresPerRound(List<String> roundInputs) {
    ImmutableList.Builder<Integer> roundScores = ImmutableList.builder();
    for (String roundInput : roundInputs) {
      if (Strings.isNullOrEmpty(roundInput)) {
        continue;
      }

      List<String> roundInputSplit = SPLITTER.splitToList(roundInput);
      Play opponent = extractOpponentPlay(roundInputSplit);
      Play mine = extractMyPlay(roundInputSplit);
      roundScores.add(calculateRoundScore(opponent, mine));
    }
    return roundScores.build();
  }

  private List<Integer> p2calculateScoresPerRound(List<String> roundInputs) {
    ImmutableList.Builder<Integer> roundScores = ImmutableList.builder();
    for (String roundInput : roundInputs) {
      if (Strings.isNullOrEmpty(roundInput)) {
        continue;
      }

      List<String> roundInputSplit = SPLITTER.splitToList(roundInput);
      Play opponent = extractOpponentPlay(roundInputSplit);
      Outcome desiredOutcome = extractDesiredOutcome(roundInputSplit);
      Play mine = chooseWhatToPlayForDesiredOutcome(opponent, desiredOutcome);
      roundScores.add(calculateRoundScore(opponent, mine));
    }
    return roundScores.build();
  }

  private int calculateRoundScore(Play opponent, Play mine) {
    Outcome outcome = playRound(opponent, mine);

    int scoreForMyPlay = mine.getScore();
    int scoreForOutcome = outcome.getScore();

    return scoreForMyPlay + scoreForOutcome;
  }

  private Play extractOpponentPlay(List<String> roundInputSplit) {
    return toOpponent(roundInputSplit.get(0));
  }

  private Play extractMyPlay(List<String> roundInputSplit) {
    return toMine(roundInputSplit.get(1));
  }

  private Outcome extractDesiredOutcome(List<String> roundInputSplit) {
    return toDesiredOutcome(roundInputSplit.get(1));
  }

  private Play toOpponent(String opponent) {
    if (opponent.equals("A")) {
      return Play.ROCK;
    }
    if (opponent.equals("B")) {
      return Play.PAPER;
    }
    if (opponent.equals("C")) {
      return Play.SCISSORS;
    }
    throw new IllegalArgumentException("unknown input for opponent!");
  }

  private Play toMine(String mine) {
    if (mine.equals("X")) {
      return Play.ROCK;
    }
    if (mine.equals("Y")) {
      return Play.PAPER;
    }
    if (mine.equals("Z")) {
      return Play.SCISSORS;
    }
    throw new IllegalArgumentException("unknown input for mine!");
  }

  private Outcome toDesiredOutcome(String desiredOutcome) {
    if (desiredOutcome.equals("X")) {
      return Outcome.LOSE;
    }
    if (desiredOutcome.equals("Y")) {
      return Outcome.DRAW;
    }
    if (desiredOutcome.equals("Z")) {
      return Outcome.WIN;
    }
    throw new IllegalArgumentException("unknown input for desiredOutcome!");
  }

  private enum Play {
    ROCK(1),
    PAPER(2),
    SCISSORS(3);

    private final int score;

    Play(int score) {
      this.score = score;
    }

    public int getScore() {
      return score;
    }
  }

  private enum Outcome {
    WIN(6),
    DRAW(3),
    LOSE(0);

    private final int score;

    Outcome(int score) {
      this.score = score;
    }

    public int getScore() {
      return score;
    }
  }

  private Play chooseWhatToPlayForDesiredOutcome(
    Play opponent,
    Outcome desiredOutcome
  ) {
    switch (opponent) {
      case ROCK:
        if (desiredOutcome == Outcome.WIN) {
          return Play.PAPER;
        }
        if (desiredOutcome == Outcome.DRAW) {
          return Play.ROCK;
        }
        if (desiredOutcome == Outcome.LOSE) {
          return Play.SCISSORS;
        }
      case PAPER:
        if (desiredOutcome == Outcome.WIN) {
          return Play.SCISSORS;
        }
        if (desiredOutcome == Outcome.DRAW) {
          return Play.PAPER;
        }
        if (desiredOutcome == Outcome.LOSE) {
          return Play.ROCK;
        }
      case SCISSORS:
        if (desiredOutcome == Outcome.WIN) {
          return Play.ROCK;
        }
        if (desiredOutcome == Outcome.DRAW) {
          return Play.SCISSORS;
        }
        if (desiredOutcome == Outcome.LOSE) {
          return Play.PAPER;
        }
    }
    throw new IllegalArgumentException("unknown play!");
  }

  private Outcome playRound(Play opponent, Play mine) {
    switch (mine) {
      case ROCK:
        if (opponent == Play.SCISSORS) {
          return Outcome.WIN;
        }
        if (opponent == Play.ROCK) {
          return Outcome.DRAW;
        }
        if (opponent == Play.PAPER) {
          return Outcome.LOSE;
        }
        break;
      case PAPER:
        if (opponent == Play.ROCK) {
          return Outcome.WIN;
        }
        if (opponent == Play.PAPER) {
          return Outcome.DRAW;
        }
        if (opponent == Play.SCISSORS) {
          return Outcome.LOSE;
        }
        break;
      case SCISSORS:
        if (opponent == Play.PAPER) {
          return Outcome.WIN;
        }
        if (opponent == Play.SCISSORS) {
          return Outcome.DRAW;
        }
        if (opponent == Play.ROCK) {
          return Outcome.LOSE;
        }
        break;
    }
    throw new IllegalArgumentException("unknown play!");
  }
}
