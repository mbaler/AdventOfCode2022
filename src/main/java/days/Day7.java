package days;

import com.google.common.base.Splitter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Day7 implements Day {

  // input: terminal output of filesystem

  // root dir: /
  // 123 abc means that the current directory contains a file named abc with size 123.
  // dir xyz means that the current directory contains a directory named xyz.

  // determine the total size of each directory
  //  - the total size of a directory is the sum of the sizes of the files it contains, directly or indirectly
  // TODO: Find all of the directories with a total size of at most 100000.
  //  What is the sum of the total sizes of those directories?
  // (can count files more than once when dirs nested)

  // p2
  // total fs diskspace: 70000000
  // need at least 30000000 unused space to run update
  // total diskspace - root space (which is all) = currently unused
  // that unused needs to get to 30000000
  // amount that needs to be freed = 30000000 - unused
  // that amount needing to be freed can be achieved by deleting 1 directory
  // directory needs space >= that amount so it frees enough
  // -- and choose the one that is smallest but meets that criteria
  // TODO: Find the smallest directory that, if deleted, would free up enough space on the filesystem to run the update.
  //  What is the total size of that directory?

  private static final int SIZE_LIMIT = 100000;

  public void part1(List<String> input) {
    Folder root = createFilesystemHierarchyFromRoot(input);
    printFolder(0, root, false);

    Map<String, Integer> dirSizesByPath = getDirSizesByPathFromRoot(root);
    System.out.println("dirSizesByPath: " + dirSizesByPath);

    Map<String, Integer> sizesByPathBelowLimit = dirSizesByPath
      .entrySet()
      .stream()
      .filter(entry -> entry.getValue() <= SIZE_LIMIT)
      .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    System.out.println("dirSizesByPath below limit: " + sizesByPathBelowLimit);

    int totalSizeBelowLimit = sizesByPathBelowLimit
      .values()
      .stream()
      .mapToInt(Integer::intValue)
      .sum();
    System.out.println(
      "Total sizes below " + SIZE_LIMIT + ": " + totalSizeBelowLimit
    );
  }

  private static final int TOTAL_DISK_SPACE = 70000000;
  private static final int UNUSED_SPACE_NEEDED_TO_UPDATE = 30000000;

  public void part2(List<String> input) {
    Folder root = createFilesystemHierarchyFromRoot(input);
    printFolder(0, root, false);

    Map<String, Integer> dirSizesByPath = getDirSizesByPathFromRoot(root);
    System.out.println("dirSizesByPath: " + dirSizesByPath);

    int rootSpace = dirSizesByPath.get(DIR_ROOT);
    int currentlyUnused = TOTAL_DISK_SPACE - rootSpace;
    int amountNeedsToBeFreed = UNUSED_SPACE_NEEDED_TO_UPDATE - currentlyUnused;

    Map.Entry<String, Integer> entryToDelete = dirSizesByPath
      .entrySet()
      .stream()
      .filter(entry -> entry.getValue() >= amountNeedsToBeFreed)
      .sorted(Map.Entry.comparingByValue())
      .findFirst()
      .get();
    System.out.println(
      "Should delete dir with path=" +
      entryToDelete.getKey() +
      " as it takes up " +
      entryToDelete.getValue()
    );
  }

  private static final String DIR_ROOT = "/";
  private static final String DIR_BACK = "..";
  private static final Splitter FILE_SPLITTER = Splitter.on(" ");

  private Folder createFilesystemHierarchyFromRoot(List<String> input) {
    Folder root = new Folder(
      DIR_ROOT,
      new ArrayList<>(),
      new ArrayList<>(),
      null
    );

    // assume we start at root
    Folder current = root;

    for (String line : input) {
      // cd
      Optional<String> cdTarget = getCdTarget(line);
      if (cdTarget.isPresent()) {
        if (cdTarget.get().equals(DIR_ROOT)) {
          current = root;
          continue;
        }
        if (cdTarget.get().equals(DIR_BACK)) {
          current = current.getParent();
          continue;
        }
        Optional<Folder> childFolder = current.getFolder(cdTarget.get());
        if (childFolder.isPresent()) {
          current = childFolder.get();
          continue;
        } else {
          // add to fs if somehow not already there
          Folder newChild = new Folder(
            cdTarget.get(),
            new ArrayList<>(),
            new ArrayList<>(),
            current
          );
          current.addFolder(newChild);
          current = newChild;
          continue;
        }
      }

      // ls
      if (isLs(line)) {
        continue;
      }

      // if not cd and not ls, line is post-ls file or folder

      // is folder
      Optional<String> dirMaybe = getDir(line);
      if (dirMaybe.isPresent()) {
        // add to fs if not already there
        if (current.getFolder(dirMaybe.get()).isEmpty()) {
          current.addFolder(
            new Folder(
              dirMaybe.get(),
              new ArrayList<>(),
              new ArrayList<>(),
              current
            )
          );
        }
        continue;
      }

      // is file
      List<String> fileParts = FILE_SPLITTER.splitToList(line);
      int fileSize = Integer.parseInt(fileParts.get(0));
      String fileName = fileParts.get(1);
      current.addFile(new File(fileSize, fileName));
    }

    return root;
  }

  private Optional<String> getCdTarget(String command) {
    if (command.startsWith("$ cd")) {
      return Optional.of(command.substring(5));
    }
    return Optional.empty();
  }

  private boolean isLs(String command) {
    return command.equals("$ ls");
  }

  private Optional<String> getDir(String command) {
    if (command.startsWith("dir ")) {
      return Optional.of(command.substring(4));
    }
    return Optional.empty();
  }

  private Map<String, Integer> getDirSizesByPathFromRoot(Folder root) {
    return getDirSizesByPath(new HashMap<>(), root);
  }

  private Map<String, Integer> getDirSizesByPath(
    Map<String, Integer> sizesByPath,
    Folder root
  ) {
    sizesByPath.put(root.getPathIncludingName(), root.getSize());

    for (Folder folder : root.getFolders()) {
      sizesByPath = getDirSizesByPath(sizesByPath, folder);
    }

    return sizesByPath;
  }

  private abstract static class FileSystemItem {

    private final String name;

    FileSystemItem(String name) {
      this.name = name;
    }

    public String getName() {
      return name;
    }
  }

  private static class File extends FileSystemItem {

    private final int size;

    File(int size, String name) {
      super(name);
      this.size = size;
    }

    public int getSize() {
      return size;
    }
  }

  private static class Folder extends FileSystemItem {

    private final List<File> files;
    private final List<Folder> folders;
    private final Folder parent;

    Folder(String name, List<File> files, List<Folder> folders, Folder parent) {
      super(name);
      this.files = files;
      this.folders = folders;
      this.parent = parent;
    }

    public void addFile(File file) {
      files.add(file);
    }

    public List<File> getFiles() {
      return files
        .stream()
        .sorted(Comparator.comparing(File::getName))
        .collect(Collectors.toList());
    }

    public void addFolder(Folder folder) {
      folders.add(folder);
    }

    public List<Folder> getFolders() {
      return folders
        .stream()
        .sorted(Comparator.comparing(Folder::getName))
        .collect(Collectors.toList());
    }

    public Optional<Folder> getFolder(String name) {
      return folders
        .stream()
        .filter(folder -> folder.getName().equals(name))
        .findFirst();
    }

    public Folder getParent() {
      return parent;
    }

    public int getSize() {
      int sizeOfFilesHere = files.stream().mapToInt(File::getSize).sum();
      int sizeOfInnerFolders = folders.stream().mapToInt(Folder::getSize).sum();
      return sizeOfFilesHere + sizeOfInnerFolders;
    }

    public String getPathIncludingName() {
      if (getParent() == null) {
        return getName();
      }

      String parentPathIncludingName = getParent().getPathIncludingName();
      if (parentPathIncludingName.equals(DIR_ROOT)) {
        return DIR_ROOT + getName();
      }

      return parentPathIncludingName + "/" + getName();
    }
  }

  //////////////////////////////////////////////////////////
  ////////// printing out an ASCII directory tree //////////
  //////////////////////////////////////////////////////////

  // e.g.
  /*
  ~~ /
   ├─ a/
   │  ├─ e/
   │  │  └─ i (size=584)
   │  ├─ f (size=29116)
   │  ├─ g (size=2557)
   │  ├─ h.lst (size=62596)
   │  └─ z/
   │     └─ zboi.zig (size=420)
   ├─ b.txt (size=14848514)
   ├─ c.dat (size=8504156)
   └─ d/
      ├─ d.ext (size=5626152)
      ├─ d.log (size=8033020)
      ├─ j (size=4060174)
      └─ k (size=7214296)
  */

  private void printFolder(int indent, Folder root, boolean isLastInFolder) {
    printFolderName(indent, root, isLastInFolder);
    printFolderContents(indent + 1, root, isLastInFolder);
  }

  private static final String DASH_SPACE = "─ ";
  private static final String CONTINUING_PIPE = "├";
  private static final String FINAL_ELBOW = "└";

  private void printFolderName(
    int indentLevel,
    Folder folder,
    boolean isLastInFolder
  ) {
    if (folder.getName().equals(DIR_ROOT)) {
      System.out.println("~~ " + DIR_ROOT);
      return;
    }

    System.out.println(
      properIndentation(indentLevel, false) + // only do last indentation with files
      formatDashPrefix(isLastInFolder) +
      folder.getName() +
      "/"
    );
  }

  private void printFolderContents(
    int indent,
    Folder folder,
    boolean isLastInFolder
  ) {
    List<FileSystemItem> sortedItems = Stream
      .of(folder.getFiles(), folder.getFolders())
      .flatMap(Collection::stream)
      .sorted(Comparator.comparing(FileSystemItem::getName))
      .collect(Collectors.toList());

    for (int i = 0; i < sortedItems.size(); i++) {
      boolean isLast = i == sortedItems.size() - 1;
      FileSystemItem item = sortedItems.get(i);
      if (item instanceof File) {
        printFile((File) item, indent, isLast, isLastInFolder);
      } else {
        printFolder(indent, (Folder) item, isLast);
      }
    }
  }

  private void printFile(
    File file,
    int indent,
    boolean isLastInFolder,
    boolean containingFolderIsLastInFolder
  ) {
    System.out.println(
      properIndentation(indent, containingFolderIsLastInFolder) +
      formatDashPrefix(isLastInFolder) +
      file.getName() +
      " (size=" +
      file.getSize() +
      ")"
    );
  }

  private String formatDashPrefix(boolean isLastInFolder) {
    return (isLastInFolder ? FINAL_ELBOW : CONTINUING_PIPE) + DASH_SPACE;
  }

  private static final String SPACING_WITHOUT_BAR = "   ";
  private static final String SPACING_WITH_BAR = "│  ";

  private String properIndentation(int indentLevel, boolean isLastInFolder) {
    if (indentLevel == 0) {
      return "";
    }

    if (indentLevel == 1) {
      return SPACING_WITHOUT_BAR;
    }

    if (!isLastInFolder) {
      return (SPACING_WITHOUT_BAR + SPACING_WITH_BAR.repeat(indentLevel - 1));
    }
    return (
      SPACING_WITHOUT_BAR +
      SPACING_WITH_BAR.repeat(indentLevel - 2) +
      SPACING_WITHOUT_BAR
    );
  }
}
