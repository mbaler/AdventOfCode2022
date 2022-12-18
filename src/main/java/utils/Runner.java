package utils;

import com.google.common.reflect.ClassPath;
import days.Day;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class Runner {

  private static final int DAY = 19;
  private static final boolean TEST = true;

  public static void main(String[] args)
    throws IOException, NoSuchMethodException, InvocationTargetException, IllegalAccessException, InstantiationException {
    Class<?> dayClass = loadDayClass();
    List<String> input = parseInputToLines();

    runPart1(dayClass, input);
    System.out.println();
    runPart2(dayClass, input);
  }

  private static Class<?> loadDayClass() throws IOException {
    Class<?> dayClass = ClassPath
      .from(ClassLoader.getSystemClassLoader())
      .getAllClasses()
      .stream()
      .filter(clazz -> clazz.getSimpleName().equals("Day" + DAY))
      .map(ClassPath.ClassInfo::load)
      .filter(Day.class::isAssignableFrom)
      .findFirst()
      .orElseThrow(() ->
        new IllegalArgumentException("No Day" + DAY + " class yet!")
      );

    System.out.println("'Tis Day " + DAY + "... let's get crackin':\n");
    return dayClass;
  }

  private static List<String> parseInputToLines() throws IOException {
    Path path = Paths.get(
      TEST
        ? "src/main/resources/dayinputs/testinput.txt"
        : "src/main/resources/dayinputs/day" + DAY + "input.txt"
    );
    return Files.readAllLines(path);
  }

  private static void runPart1(Class<?> dayClass, List<String> input)
    throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
    runPart("1", dayClass, input);
  }

  private static void runPart2(Class<?> dayClass, List<String> input)
    throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
    runPart("2", dayClass, input);
  }

  private static void runPart(
    String partNumber,
    Class<?> dayClass,
    List<String> input
  )
    throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
    Object dayInstance = dayClass.getDeclaredConstructor().newInstance();
    System.out.println("__Part " + partNumber + "__");
    dayClass
      .getDeclaredMethod("part" + partNumber, List.class)
      .invoke(dayInstance, input);
  }
}
