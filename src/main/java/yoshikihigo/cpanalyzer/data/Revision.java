package yoshikihigo.cpanalyzer.data;

public class Revision implements Comparable<Revision> {

  public final String repo;
  public final String id;
  public final String date;
  public final String message;
  public final String author;
  public final boolean bugfix;

  public Revision(final String repo, final String id, final String date, final String message,
      final String author, final boolean bugfix) {
    this.repo = repo;
    this.id = id;
    this.date = date;
    this.message = message;
    this.author = author;
    this.bugfix = false;
  }

  @Override
  public int compareTo(final Revision revision) {
    final int repoOrder = this.repo.compareTo(revision.repo);
    if (0 != repoOrder) {
      return repoOrder;
    }
    final int dateOrder = this.date.compareTo(revision.date);
    if (0 != dateOrder) {
      return dateOrder;
    }
    return this.id.compareTo(revision.id);
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof Revision)) {
      return false;
    }
    final Revision target = (Revision) o;
    return 0 == this.compareTo(target);
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
