package yoshikihigo.cpanalyzer.data;

public class Revision implements Comparable<Revision> {

  public final String software;
  public final String id;
  public final String date;
  public final String message;
  public final String author;

  public Revision(final String software, final String id, final String date, final String message,
      final String author) {
    this.software = software;
    this.id = id;
    this.date = date;
    this.message = message;
    this.author = author;
  }

  @Override
  public int compareTo(final Revision revision) {
    final int softwareOrder = this.software.compareTo(revision.software);
    if (0 != softwareOrder) {
      return softwareOrder;
    }
    return this.date.compareTo(revision.date);
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof Revision)) {
      return false;
    }
    final Revision target = (Revision) o;
    return this.id == target.id;
  }

  @Override
  public int hashCode() {
    return this.id.hashCode();
  }

  @Override
  public String toString() {
    return this.date;
  }
}
