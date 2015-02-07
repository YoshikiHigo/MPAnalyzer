package jp.ac.osaka_u.ist.sdl.mpanalyzer.gui;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.SortedSet;
import java.util.TreeSet;

import jp.ac.osaka_u.ist.sdl.mpanalyzer.data.Change;

public class ObservedModifications extends Observable {

	public enum MLABEL {
		SELECTED;
	}

	private static final Map<MLABEL, ObservedModifications> INSTANCES = new HashMap<MLABEL, ObservedModifications>();

	private final SortedSet<Change> modifications;
	public final MLABEL label;
	private Observer source;

	private ObservedModifications(final MLABEL label) {

		if (null == label) {
			throw new NullPointerException();
		}

		this.modifications = new TreeSet<Change>();
		this.source = null;
		this.label = label;
	}

	public static final ObservedModifications getInstance(final MLABEL label) {
		ObservedModifications instance = INSTANCES.get(label);
		if (null == instance) {
			instance = new ObservedModifications(label);
			INSTANCES.put(label, instance);
		}
		return instance;
	}

	public boolean add(final Change modification, final Observer source) {

		if (null == modification) {
			return false;
		}

		this.modifications.add(modification);
		this.source = source;

		this.setChanged();
		this.notifyObservers(source);

		return true;
	}

	public boolean addAll(final Collection<Change> modifications,
			final Observer source) {

		if (null == modifications) {
			return false;
		}

		this.modifications.addAll(modifications);
		this.source = source;

		this.setChanged();
		this.notifyObservers(source);

		return true;
	}

	public boolean remove(final Change modification, final Observer source) {

		if (null == modification) {
			return false;
		}

		this.modifications.remove(modification);
		this.source = source;

		this.setChanged();
		this.notifyObservers(source);

		return true;
	}

	public boolean removeAll(final Collection<Change> modifications,
			final Observer source) {

		if (null == modifications) {
			return false;
		}

		this.modifications.removeAll(modifications);
		this.source = source;

		this.setChanged();
		this.notifyObservers(source);

		return true;
	}

	public boolean set(final Change modification, final Observer source) {

		if (null == modification) {
			return false;
		}

		this.modifications.clear();
		this.modifications.add(modification);
		this.source = source;

		this.setChanged();
		this.notifyObservers(source);

		return true;
	}

	public boolean setAll(final Collection<Change> modifications,
			final Observer source) {

		if (null == modifications) {
			return false;
		}

		this.modifications.clear();
		this.modifications.addAll(modifications);
		this.source = source;

		this.setChanged();
		this.notifyObservers(source);

		return true;
	}

	public boolean isSet() {
		return !this.modifications.isEmpty();
	}

	public void clear(final Observer source) {

		this.modifications.clear();
		this.source = source;

		this.setChanged();
		this.notifyObservers(source);
	}

	public SortedSet<Change> get() {
		return Collections.unmodifiableSortedSet(this.modifications);
	}

	public Observer getSource() {
		return this.source;
	}
}
