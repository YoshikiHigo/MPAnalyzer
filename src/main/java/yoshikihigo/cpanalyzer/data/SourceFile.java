package yoshikihigo.cpanalyzer.data;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import yoshikihigo.cpanalyzer.CPAConfig;
import yoshikihigo.cpanalyzer.StringUtility;

public class SourceFile implements Comparable<SourceFile> {

  final public boolean IGNORE_INDENT;
  final public boolean IGNORE_WHITESPACE;

  public final String filepath;
  private final List<String> originalLines;
  private final StringBuilder normalizedSequence;
  private final List<Integer> positionMapper;

  public SourceFile(final String filepath) throws IOException {
    this.filepath = filepath;
    this.originalLines = new ArrayList<>();
    this.originalLines.add("");
    this.normalizedSequence = new StringBuilder();
    this.positionMapper = new ArrayList<>();

    this.IGNORE_INDENT = CPAConfig.getInstance()
        .isIGNORE_INDENT();
    this.IGNORE_WHITESPACE = CPAConfig.getInstance()
        .isIGNORE_WHITESPACE();
  }

  public void addLine(final String line) {

    final String noindent = this.IGNORE_INDENT ? StringUtility.removeIndent(line) : line;
    final String nospace =
        this.IGNORE_WHITESPACE ? StringUtility.removeSpaceTab(noindent) : noindent;
    final String trim = nospace.trim();
    this.normalizedSequence.append(trim);

    final int lineNumber = this.originalLines.size();
    for (int i = 0; i < trim.length(); i++) {
      this.positionMapper.add(lineNumber);
    }

    this.originalLines.add(line);
  }

  public String getNormalizedSequence() {
    return this.normalizedSequence.toString();
  }

  public List<SortedSet<Integer>> getMatchedLineNumbersList(final String beforeSequence,
      final String afterSequence) {

    final String content = this.normalizedSequence.toString();
    final boolean[] checkingArray = new boolean[content.length()];
    Arrays.fill(checkingArray, false);

    if (!beforeSequence.isEmpty()) {
      int fromIndex = 0;
      while (true) {
        final int matchedIndex = content.indexOf(beforeSequence, fromIndex);
        if (matchedIndex < 0) {
          break;
        } else {
          for (int index = 0; index < beforeSequence.length(); index++) {
            checkingArray[matchedIndex + index] = true;
          }
          fromIndex = matchedIndex + 1;
        }
      }
    }

    if (!afterSequence.isEmpty()) {
      int fromIndex = 0;
      while (true) {
        final int matchedIndex = content.indexOf(afterSequence, fromIndex);
        if (matchedIndex < 0) {
          break;
        } else {
          for (int index = 0; index < afterSequence.length(); index++) {
            checkingArray[matchedIndex + index] = false;
          }
          fromIndex = matchedIndex + 1;
        }
      }
    }

    final List<SortedSet<Integer>> linesList = new ArrayList<>();
    SortedSet<Integer> lines = new TreeSet<>();
    for (int index = 0; index < checkingArray.length; index++) {

      if (true == checkingArray[index]) {
        lines.add(this.positionMapper.get(index));
      }

      else if (false == checkingArray[index]) {
        if (0 < lines.size()) {
          linesList.add(lines);
          lines = new TreeSet<>();
        }
      }
    }

    return linesList;
  }

  public String getLine(final int lineNumber) {
    return this.originalLines.get(lineNumber);
  }

  @Override
  public boolean equals(final Object o) {
    if (!(o instanceof SourceFile)) {
      return false;
    }
    final SourceFile target = (SourceFile) o;
    return this.filepath.equals(target.filepath);
  }

  @Override
  public int hashCode() {
    return this.filepath.hashCode();
  }

  @Override
  public int compareTo(final SourceFile file) {
    return this.filepath.compareTo(file.filepath);
  }
}
