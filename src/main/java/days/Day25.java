package days;

import java.util.ArrayList;
import java.util.List;

public class Day25 implements Day {

  public void part1(List<String> input) {
    List<Long> decimalNums = toDecimals(input);

    long sumDecimal = decimalNums.stream().mapToLong(Long::longValue).sum();
    System.out.println("Decimal sum: " + sumDecimal);

    System.out.println("to snafu: " + toSnafu(sumDecimal));
  }

  public void part2(List<String> input) {
    System.out.println("Reindeer smoothie time :)");
  }

  private List<Long> toDecimals(List<String> snafuNums) {
    List<Long> decimalNums = new ArrayList<>();
    for (String snafuNum : snafuNums) {
      decimalNums.add(toDecimals(snafuNum));
    }
    return decimalNums;
  }

  private long toDecimals(String snafuNum) {
    long num = 0;
    for (int d = snafuNum.length() - 1; d >= 0; d--) {
      int numDigit = (snafuNum.length() - 1) - d; // starting at 0, is exponent
      long val = toNum(snafuNum.charAt(d)) * (long) Math.pow(5, numDigit);
      num += val;
    }
    return num;
  }

  private int toNum(char c) {
    if (c == '-') {
      return -1;
    }
    if (c == '=') {
      return -2;
    }
    return Integer.parseInt(Character.toString(c));
  }

  private static final String SNAFU = "=-012";

  private String toSnafu(long decimalRemaining) {
    String ans = "";

    while (decimalRemaining != 0L) {
      long offset = decimalRemaining + 2L;
      decimalRemaining = Math.floorDiv(offset, 5);
      long remainder = (offset) % 5;

      ans = SNAFU.charAt((int) remainder) + ans;
    }

    return ans;
  }
  ////// alternative
  //  private static final String SNAFU_REV = "012=-";
  //  private String toSnafu(long decimalToParse) {
  //    if (decimalToParse == 0L || decimalToParse == 1L || decimalToParse == 2L) {
  //      return String.valueOf(decimalToParse);
  //    }
  //
  //    String ans = "";
  //
  //    while (decimalToParse != 0L) {
  //      long remainder = (decimalToParse) % 5;
  //      ans = SNAFU_REV.charAt((int) remainder) + ans;
  //
  //      long offset = decimalToParse + 2L;
  //      decimalToParse = Math.floorDiv(offset, 5);
  //    }
  //
  //    return ans;
  //  }
}
