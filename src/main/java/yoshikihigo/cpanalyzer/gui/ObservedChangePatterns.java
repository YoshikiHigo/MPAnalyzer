package yoshikihigo.cpanalyzer.gui;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.SortedSet;
import java.util.TreeSet;

import yoshikihigo.cpanalyzer.data.ChangePattern;

public class ObservedChangePatterns extends Observable {

  public enum CPLABEL {
    ALL, FILTERED, SELECTED, OVERLOOKED;
  }

  private static final Map<CPLABEL, ObservedChangePatterns> INSTANCES =
      new HashMap<CPLABEL, ObservedChangePatterns>();

  private final SortedSet<ChangePattern> patterns;
  public final CPLABEL label;
  private Observer source;

  private ObservedChangePatterns(final CPLABEL label) {

    if (null == label) {
      throw new NullPointerException();
    }

    this.patterns = new TreeSet<ChangePattern>();
    this.source = null;
    this.label = label;
  }

  public static final ObservedChangePatterns getInstance(final CPLABEL label) {
    ObservedChangePatterns instance = INSTANCES.get(label);
    if (null == instance) {
      instance = new ObservedChangePatterns(label);
      INSTANCES.put(label, instance);
    }
    return instance;
  }

  public boolean add(final ChangePattern pattern, final Observer source) {

    if (null == pattern) {
      return false;
    }

    this.patterns.add(pattern);
    this.source = source;

    this.setChanged();
    this.notifyObservers(source);

    return true;
  }

  public boolean addAll(final Collection<ChangePattern> patterns, final Observer source) {

    if (null == patterns) {
      return false;
    }

    this.patterns.addAll(patterns);
    this.source = source;

    this.setChanged();
    this.notifyObservers(source);

    return true;
  }

  public boolean remove(final ChangePattern pattern, final Observer source) {

    if (null == pattern) {
      return false;
    }

    this.patterns.remove(pattern);
    this.source = source;

    this.setChanged();
    this.notifyObservers(source);

    return true;
  }

  public boolean removeAll(final Collection<ChangePattern> patterns, final Observer source) {

    if (null == patterns) {
      return false;
    }

    this.patterns.removeAll(patterns);
    this.source = source;

    this.setChanged();
    this.notifyObservers(source);

    return true;
  }

  public boolean set(final ChangePattern pattern, final Observer source) {

    if (null == pattern) {
      return false;
    }

    this.patterns.clear();
    this.patterns.add(pattern);
    this.source = source;

    this.setChanged();
    this.notifyObservers(source);

    return true;
  }

  public boolean setAll(final Collection<ChangePattern> patterns, final Observer source) {

    if (null == patterns) {
      return false;
    }

    this.patterns.clear();
    this.patterns.addAll(patterns);
    this.source = source;

    this.setChanged();
    this.notifyObservers(source);

    return true;
  }

  public boolean isSet() {
    return !this.patterns.isEmpty();
  }

  public void clear(final Observer source) {

    this.patterns.clear();
    this.source = source;

    this.setChanged();
    this.notifyObservers(source);
  }

  public SortedSet<ChangePattern> get() {
    return Collections.unmodifiableSortedSet(this.patterns);
  }

  public Observer getSource() {
    return this.source;
  }
}
