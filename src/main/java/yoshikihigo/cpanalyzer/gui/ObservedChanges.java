package yoshikihigo.cpanalyzer.gui;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.SortedSet;
import java.util.TreeSet;

import yoshikihigo.cpanalyzer.data.Change;

public class ObservedChanges extends Observable {

  public enum CLABEL {
    SELECTED;
  }

  private static final Map<CLABEL, ObservedChanges> INSTANCES =
      new HashMap<CLABEL, ObservedChanges>();

  private final SortedSet<Change> changes;
  public final CLABEL label;
  private Observer source;

  private ObservedChanges(final CLABEL label) {

    if (null == label) {
      throw new NullPointerException();
    }

    this.changes = new TreeSet<Change>();
    this.source = null;
    this.label = label;
  }

  public static final ObservedChanges getInstance(final CLABEL label) {
    ObservedChanges instance = INSTANCES.get(label);
    if (null == instance) {
      instance = new ObservedChanges(label);
      INSTANCES.put(label, instance);
    }
    return instance;
  }

  public boolean add(final Change modification, final Observer source) {

    if (null == modification) {
      return false;
    }

    this.changes.add(modification);
    this.source = source;

    this.setChanged();
    this.notifyObservers(source);

    return true;
  }

  public boolean addAll(final Collection<Change> modifications, final Observer source) {

    if (null == modifications) {
      return false;
    }

    this.changes.addAll(modifications);
    this.source = source;

    this.setChanged();
    this.notifyObservers(source);

    return true;
  }

  public boolean remove(final Change modification, final Observer source) {

    if (null == modification) {
      return false;
    }

    this.changes.remove(modification);
    this.source = source;

    this.setChanged();
    this.notifyObservers(source);

    return true;
  }

  public boolean removeAll(final Collection<Change> modifications, final Observer source) {

    if (null == modifications) {
      return false;
    }

    this.changes.removeAll(modifications);
    this.source = source;

    this.setChanged();
    this.notifyObservers(source);

    return true;
  }

  public boolean set(final Change modification, final Observer source) {

    if (null == modification) {
      return false;
    }

    this.changes.clear();
    this.changes.add(modification);
    this.source = source;

    this.setChanged();
    this.notifyObservers(source);

    return true;
  }

  public boolean setAll(final Collection<Change> modifications, final Observer source) {

    if (null == modifications) {
      return false;
    }

    this.changes.clear();
    this.changes.addAll(modifications);
    this.source = source;

    this.setChanged();
    this.notifyObservers(source);

    return true;
  }

  public boolean isSet() {
    return !this.changes.isEmpty();
  }

  public void clear(final Observer source) {

    this.changes.clear();
    this.source = source;

    this.setChanged();
    this.notifyObservers(source);
  }

  public SortedSet<Change> get() {
    return Collections.unmodifiableSortedSet(this.changes);
  }

  public Observer getSource() {
    return this.source;
  }
}
