package yoshikihigo.cpanalyzer.nh3;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;

public final class SelectedEntities<T> extends Observable {

  public static final String SELECTED_PATH = "SELECTED_PATH";
  public static final String SELECTED_WARNING = "SELECTED_WARNING";
  public static final String TRIVIAL_PATTERN = "TRIVIAL_PATTERN";
  public static final String FOCUSING_PATTERN = "FOCUSING_PATTERN";
  public static final String LOGKEYWORD_PATTERN = "LOGKEYWORD_PATTERN";
  public static final String METRICS_PATTERN = "METRICS_PATTERN";
  public static final String PATHKEYWORD_PATTERN = "PATHKEYWORD_PATTERN";

  private static final Map<String, SelectedEntities<?>> INSTANCES = new HashMap<>();

  public static final <S> SelectedEntities<S> getInstance(final String label) {
    SelectedEntities<S> instance = (SelectedEntities<S>) INSTANCES.get(label);
    if (null == instance) {
      instance = new SelectedEntities<S>(label);
      INSTANCES.put(label, instance);
    }
    return instance;
  }

  public void add(final T entity, final Observer source) {

    this.selectedEntities.add(entity);
    this.source = source;

    this.setChanged();
    this.notifyObservers(source);
  }

  public void addAll(final Collection<T> entities, final Observer source) {

    this.selectedEntities.addAll(entities);
    this.source = source;

    this.setChanged();
    this.notifyObservers(source);
  }

  public void remove(final T entity, final Observer source) {

    this.selectedEntities.remove(entity);
    this.source = source;

    this.setChanged();
    this.notifyObservers(source);
  }

  public void removeAll(final Collection<T> entities, final Observer source) {

    this.selectedEntities.removeAll(entities);
    this.source = source;

    this.setChanged();
    this.notifyObservers(source);
  }

  public void set(final T entity, final Observer source) {

    this.selectedEntities.clear();
    this.selectedEntities.add(entity);
    this.source = source;

    this.setChanged();
    this.notifyObservers(source);
  }

  public void setAll(final Collection<T> entities, final Observer source) {

    this.selectedEntities.clear();
    this.selectedEntities.addAll(entities);
    this.source = source;

    this.setChanged();
    this.notifyObservers(source);
  }

  public boolean isSet() {
    return !this.selectedEntities.isEmpty();
  }

  public void clear(final Observer source) {

    this.selectedEntities.clear();
    this.source = source;

    this.setChanged();
    this.notifyObservers(source);
  }

  public List<T> get() {
    return new ArrayList<T>(this.selectedEntities);
  }

  public Observer getSource() {
    return this.source;
  }

  public String getLabel() {
    return this.label;
  }

  public boolean contains(final T entity) {
    return this.selectedEntities.contains(entity);
  }

  public boolean containsAll(final Set<T> entities) {
    return this.selectedEntities.containsAll(entities);
  }

  private SelectedEntities(final String label) {
    this.selectedEntities = new HashSet<>();
    this.source = null;
    this.label = label;
  }

  private final Set<T> selectedEntities;
  private Observer source;
  private final String label;
}
