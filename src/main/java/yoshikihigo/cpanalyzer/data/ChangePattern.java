package yoshikihigo.cpanalyzer.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import yoshikihigo.cpanalyzer.StringUtility;
import yoshikihigo.cpanalyzer.data.Change.ChangeType;
import yoshikihigo.cpanalyzer.data.Change.DiffType;
import yoshikihigo.cpanalyzer.db.ReadOnlyDAO;

public class ChangePattern implements Comparable<ChangePattern> {

  public final int id;
  public final int support;
  public final float confidence;
  public final int authors;
  public final int files;
  public final int projects;
  public final byte[] beforeHash;
  public final byte[] afterHash;
  public final ChangeType changeType;
  public final DiffType diffType;

  private final List<Change> changes;

  public ChangePattern(final int id, final int support, final float confidence, final int authors,
      final int files, final int projects, final byte[] beforeHash, final byte[] afterHash,
      final ChangeType changeType, final DiffType diffType) {
    this.id = id;
    this.support = support;
    this.confidence = confidence;
    this.authors = authors;
    this.files = files;
    this.projects = projects;
    this.beforeHash = Arrays.copyOf(beforeHash, beforeHash.length);
    this.afterHash = Arrays.copyOf(afterHash, afterHash.length);
    this.changeType = changeType;
    this.diffType = diffType;
    this.changes = new ArrayList<Change>();
  }

  @Override
  public int compareTo(final ChangePattern o) {
    return Integer.compare(this.id, o.id);
  }

  @Override
  public int hashCode() {
    return this.id;
  }

  @Override
  public boolean equals(final Object obj) {
    if (!(obj instanceof ChangePattern)) {
      return false;
    }
    return this.hashCode() == obj.hashCode();
  }

  public List<Change> getChanges() {
    if (0 == this.changes.size()) {
      this.setChanges();
    }
    return new ArrayList<Change>(this.changes);
  }

  public List<String> getFilePaths() {
    if (0 == this.changes.size()) {
      this.setChanges();
    }
    return this.changes.stream()
        .map(change -> change.filepath)
        .distinct()
        .sorted()
        .collect(Collectors.toList());
  }

  public List<Revision> getRevisions() {
    if (0 == this.changes.size()) {
      this.setChanges();
    }
    return this.changes.stream()
        .map(change -> change.revision)
        .distinct()
        .sorted()
        .collect(Collectors.toList());
  }

  public Revision getOldestRevision() {
    return this.getRevisions()
        .get(0);
  }

  public Revision getLatestRevision() {
    final List<Revision> revisions = this.getRevisions();
    return revisions.get(revisions.size() - 1);
  }

  public int getNOF() {
    return this.getFilePaths()
        .size();
  }

  public int getNOD() {
    if (0 == this.changes.size()) {
      this.setChanges();
    }
    final Revision firstRevision = this.changes.get(0).revision;
    final Revision lastRevision = this.changes.get(this.changes.size() - 1).revision;
    final Date firstDate = StringUtility.getDateObject(firstRevision.date);
    final Date lastDate = StringUtility.getDateObject(lastRevision.date);
    final long day = (lastDate.getTime() - firstDate.getTime()) / (24 * 60 * 60 * 1000);
    return (int) day;
  }

  public int getLBM() {
    if (0 == this.changes.size()) {
      this.setChanges();
    }
    return this.changes.get(0).before.statements.size();
  }

  public int getLAM() {
    if (0 == this.changes.size()) {
      this.setChanges();
    }
    return this.changes.get(0).after.statements.size();
  }

  private void setChanges() {
    this.changes.clear();
    try {
      final List<Change> changes =
          ReadOnlyDAO.SINGLETON.getChanges(this.beforeHash, this.afterHash);
      this.changes.addAll(changes);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
