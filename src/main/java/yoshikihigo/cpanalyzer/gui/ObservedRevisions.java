package yoshikihigo.cpanalyzer.gui;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.SortedSet;
import java.util.TreeSet;

import yoshikihigo.cpanalyzer.data.Revision;

public class ObservedRevisions extends Observable {

  public enum RLABEL {
    DETECTION, OVERLOOKED;
  }

  private static final Map<RLABEL, ObservedRevisions> INSTANCES =
      new HashMap<RLABEL, ObservedRevisions>();

  private final SortedSet<Revision> revisions;
  public final RLABEL label;
  private Observer source;

  private ObservedRevisions(final RLABEL label) {

    if (null == label) {
      throw new NullPointerException();
    }

    this.revisions = new TreeSet<>();
    this.source = null;
    this.label = label;
  }

  public static final ObservedRevisions getInstance(final RLABEL label) {
    ObservedRevisions instance = INSTANCES.get(label);
    if (null == instance) {
      instance = new ObservedRevisions(label);
      INSTANCES.put(label, instance);
    }
    return instance;
  }

  public boolean add(final Revision revision, final Observer source) {

    if (null == revision) {
      return false;
    }

    this.revisions.add(revision);
    this.source = source;

    this.setChanged();
    this.notifyObservers(source);

    return true;
  }

  public boolean addAll(final Collection<Revision> revisions, final Observer source) {

    if (null == revisions) {
      return false;
    }

    this.revisions.addAll(revisions);
    this.source = source;

    this.setChanged();
    this.notifyObservers(source);

    return true;
  }

  public boolean remove(final Revision revision, final Observer source) {

    if (null == revision) {
      return false;
    }

    this.revisions.remove(revision);
    this.source = source;

    this.setChanged();
    this.notifyObservers(source);

    return true;
  }

  public boolean removeAll(final Collection<Revision> revisions, final Observer source) {

    if (null == revisions) {
      return false;
    }

    this.revisions.removeAll(revisions);
    this.source = source;

    this.setChanged();
    this.notifyObservers(source);

    return true;
  }

  public boolean set(final Revision revision, final Observer source) {

    if (null == revision) {
      return false;
    }

    this.revisions.clear();
    this.revisions.add(revision);
    this.source = source;

    this.setChanged();
    this.notifyObservers(source);

    return true;
  }

  public boolean setAll(final Collection<Revision> revisions, final Observer source) {

    if (null == revisions) {
      return false;
    }

    this.revisions.clear();
    this.revisions.addAll(revisions);
    this.source = source;

    this.setChanged();
    this.notifyObservers(source);

    return true;
  }

  public boolean isSet() {
    return !this.revisions.isEmpty();
  }

  public void clear(final Observer source) {

    this.revisions.clear();
    this.source = source;

    this.setChanged();
    this.notifyObservers(source);
  }

  public SortedSet<Revision> get() {
    return new TreeSet<>(this.revisions);
  }

  public Observer getSource() {
    return this.source;
  }
}
