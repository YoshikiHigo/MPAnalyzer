package jp.ac.osaka_u.ist.sdl.mpanalyzer.gui;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.SortedSet;
import java.util.TreeSet;

public class ObservedFiles extends Observable {

	public enum FLABEL {
		SELECTED;
	}

	private static final Map<FLABEL, ObservedFiles> INSTANCES = new HashMap<FLABEL, ObservedFiles>();

	private final SortedSet<String> files;
	public final FLABEL label;
	private Observer source;

	private ObservedFiles(final FLABEL label) {

		if (null == label) {
			throw new NullPointerException();
		}

		this.files = new TreeSet<String>();
		this.source = null;
		this.label = label;
	}

	public static final ObservedFiles getInstance(final FLABEL label) {
		ObservedFiles instance = INSTANCES.get(label);
		if (null == instance) {
			instance = new ObservedFiles(label);
			INSTANCES.put(label, instance);
		}
		return instance;
	}

	public boolean add(final String file, final Observer source) {

		if (null == file) {
			return false;
		}

		this.files.add(file);
		this.source = source;

		this.setChanged();
		this.notifyObservers(source);

		return true;
	}

	public boolean addAll(final Collection<String> files, final Observer source) {

		if (null == files) {
			return false;
		}

		this.files.addAll(files);
		this.source = source;

		this.setChanged();
		this.notifyObservers(source);

		return true;
	}

	public boolean remove(final String file, final Observer source) {

		if (null == file) {
			return false;
		}

		this.files.remove(file);
		this.source = source;

		this.setChanged();
		this.notifyObservers(source);

		return true;
	}

	public boolean removeAll(final Collection<String> files,
			final Observer source) {

		if (null == files) {
			return false;
		}

		this.files.removeAll(files);
		this.source = source;

		this.setChanged();
		this.notifyObservers(source);

		return true;
	}

	public boolean set(final String file, final Observer source) {

		if (null == file) {
			return false;
		}

		this.files.clear();
		this.files.add(file);
		this.source = source;

		this.setChanged();
		this.notifyObservers(source);

		return true;
	}

	public boolean setAll(final Collection<String> files, final Observer source) {

		if (null == files) {
			return false;
		}

		this.files.clear();
		this.files.addAll(files);
		this.source = source;

		this.setChanged();
		this.notifyObservers(source);

		return true;
	}

	public boolean isSet() {
		return !this.files.isEmpty();
	}

	public void clear(final Observer source) {

		this.files.clear();
		this.source = source;

		this.setChanged();
		this.notifyObservers(source);
	}

	public SortedSet<String> get() {
		return Collections.unmodifiableSortedSet(this.files);
	}

	public Observer getSource() {
		return this.source;
	}
}
