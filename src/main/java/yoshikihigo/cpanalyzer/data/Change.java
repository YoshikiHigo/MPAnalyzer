package yoshikihigo.cpanalyzer.data;

import java.util.concurrent.atomic.AtomicInteger;

import yoshikihigo.cpanalyzer.lexer.token.IMPORT;

public class Change implements Comparable<Change> {

  public static enum ChangeType {

    REPLACE() {

      @Override
      public int getValue() {
        return 1;
      }

      @Override
      public String toString() {
        return "REPLACE";
      }
    },

    ADD() {

      @Override
      public int getValue() {
        return 2;
      }

      @Override
      public String toString() {
        return "ADD";
      }
    },

    DELETE() {

      @Override
      public int getValue() {
        return 3;
      }

      @Override
      public String toString() {
        return "DELETE";
      }
    };

    abstract public int getValue();

    @Override
    abstract public String toString();

    public static ChangeType getType(final int value) {
      switch (value) {
        case 1:
          return ChangeType.REPLACE;
        case 2:
          return ChangeType.ADD;
        case 3:
          return ChangeType.DELETE;
        default:
          return null;
      }
    }
  }

  public static enum DiffType {

    TYPE1() {

      @Override
      public int getValue() {
        return 1;
      }

      @Override
      public String toString() {
        return "TYPE1";
      }
    },

    TYPE2() {

      @Override
      public int getValue() {
        return 2;
      }

      @Override
      public String toString() {
        return "TYPE2";
      }
    },

    TYPE3() {

      @Override
      public int getValue() {
        return 3;
      }

      @Override
      public String toString() {
        return "TYPE3";
      }
    };

    public abstract int getValue();

    @Override
    public abstract String toString();

    public static DiffType getType(final int value) {
      if (2 == value) {
        return TYPE2;
      } else {
        return TYPE3;
      }
    }
  }

  private final static AtomicInteger ID_GENERATOR = new AtomicInteger();

  public final String software;
  public final int id;
  public final String filepath;
  public final Code before;
  public final Code after;
  public final Revision revision;
  public final ChangeType changeType;
  public final DiffType diffType;

  public Change(final String software, final String filepath, final Code before, final Code after,
      final Revision revision, final ChangeType changeType, final DiffType diffType) {
    this(software, ID_GENERATOR.getAndIncrement(), filepath, before, after, revision, changeType,
        diffType);
  }

  public Change(final String software, final int id, final String filepath, final Code before,
      final Code after, final Revision revision, final ChangeType changeType,
      final DiffType diffType) {

    this.software = software;
    this.id = id;
    this.filepath = filepath;
    this.before = before;
    this.after = after;
    this.revision = revision;
    this.changeType = changeType;
    this.diffType = diffType;
  }

  @Override
  public int compareTo(final Change o) {

    final int revisionComparisonResult = this.revision.compareTo(o.revision);
    if (0 != revisionComparisonResult) {
      return revisionComparisonResult;
    }

    final int fileComparisonResult = this.filepath.compareTo(o.filepath);
    if (0 != fileComparisonResult) {
      return fileComparisonResult;
    }
    return Integer.compare(this.id, o.id);
  }

  @Override
  public boolean equals(final Object o) {

    if (null == o) {
      return false;
    }

    if (!(o instanceof Change)) {
      return false;
    }

    final Change target = (Change) o;
    return this.software.equals(target.software) && (this.id == target.id);
  }

  @Override
  public int hashCode() {
    return this.software.hashCode() + this.id;
  }

  public boolean isSamePattern(final Change m) {
    return this.before.equals(m.before) && this.after.equals(m.after);
  }

  public boolean isCondition() {
    return this.before.statements.stream()
        .anyMatch(statement -> statement.nText.startsWith("if ")
            || statement.nText.startsWith("while ") || statement.nText.startsWith("for "))
        || this.after.statements.stream()
            .anyMatch(statement -> statement.nText.startsWith("if ")
                || statement.nText.startsWith("while ") || statement.nText.startsWith("for "));
  }

  public boolean isImport() {
    return this.before.statements.stream()
        .anyMatch(statement -> statement.tokens.get(0) instanceof IMPORT)
        || this.after.statements.stream()
            .anyMatch(statement -> statement.tokens.get(0) instanceof IMPORT);
  }
}
