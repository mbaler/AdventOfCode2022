package days;

import com.google.common.base.Splitter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Day21 implements Day {

  private static final String ROOT = "root";
  private static final String US = "humn";

  private Map<String, Long> numByMonkey = new HashMap<>();
  private Map<String, String> mathByMonkey = new HashMap<>();

  private String rootArg1;
  private String rootArg2;

  public void part1(List<String> input) {
    parseInput(input, false);
    long result = calc(ROOT);
    System.out.println("root's #: " + result);
  }

  public void part2(List<String> input) {
    parseInput(input, true);
    long result = findHumanBackwards();
    System.out.println("To pass root's == test, we should yell: " + result);
  }

  private static final Splitter COLON = Splitter.on(": ");

  public void parseInput(List<String> input, boolean weAreDaCaptainNow) {
    for (String line : input) {
      List<String> parts = COLON.splitToList(line);
      String monkeyName = parts.get(0);
      String jobString = parts.get(1);

      if (weAreDaCaptainNow && monkeyName.equals(US)) {
        continue;
      }

      if (weAreDaCaptainNow && monkeyName.equals(ROOT)) {
        Matcher m = R.matcher(jobString);
        m.find();
        rootArg1 = m.group(1);
        rootArg2 = m.group(3);
        continue;
      }

      if (Character.isDigit(jobString.charAt(0))) {
        numByMonkey.put(monkeyName, Long.parseLong(jobString));
      } else {
        mathByMonkey.put(monkeyName, jobString);
      }
    }
  }

  private static final String PATTERN = "(.*) (\\+|-|\\*|\\/) (.*)";
  private static final Pattern R = Pattern.compile(PATTERN);

  private long calc(String name) {
    if (numByMonkey.containsKey(name)) {
      return numByMonkey.get(name);
    }

    String operation = mathByMonkey.get(name);

    Matcher m = R.matcher(operation);
    m.find();
    String first = m.group(1);
    String op = m.group(2);
    String second = m.group(3);

    return switch (op) {
      case "+":
        yield calc(first) + calc(second);
      case "-":
        yield calc(first) - calc(second);
      case "*":
        yield calc(first) * calc(second);
      case "/":
        yield calc(first) / calc(second);
      default:
        throw new IllegalArgumentException("!");
    };
  }

  private long findHumanBackwards() {
    if (eventuallyUsesUs(rootArg1)) {
      return inverseCalc(calc(rootArg2), rootArg1);
    } else {
      return inverseCalc(calc(rootArg1), rootArg2);
    }
  }

  private long inverseCalc(long currentVal, String targetToEqualVal) {
    if (targetToEqualVal.equals(US)) {
      return currentVal;
    }

    String operation = mathByMonkey.get(targetToEqualVal);

    Matcher m = R.matcher(operation);
    m.find();
    String first = m.group(1);
    String op = m.group(2);
    String second = m.group(3);

    boolean firstUsesUs = eventuallyUsesUs(first);

    if (firstUsesUs) {
      long secondVal = calc(second);
      return switch (op) {
        case "*" -> inverseCalc(currentVal / secondVal, first);
        case "/" -> inverseCalc(currentVal * secondVal, first);
        case "+" -> inverseCalc(currentVal - secondVal, first);
        default -> inverseCalc(currentVal + secondVal, first); // -
      };
    }

    // secondUsesUs
    long firstVal = calc(first);
    return switch (op) {
      case "*" -> inverseCalc(currentVal / firstVal, second);
      case "/" -> inverseCalc(firstVal / currentVal, second);
      case "+" -> inverseCalc(currentVal - firstVal, second);
      default -> inverseCalc(firstVal - currentVal, second); // -
    };
  }

  private boolean eventuallyUsesUs(String monkey) {
    if (monkey.equals(US)) {
      return true;
    }
    if (numByMonkey.containsKey(monkey)) {
      return false;
    }
    Matcher m = R.matcher(mathByMonkey.get(monkey));
    m.find();
    return eventuallyUsesUs(m.group(1)) || eventuallyUsesUs(m.group(3));
  }
}
