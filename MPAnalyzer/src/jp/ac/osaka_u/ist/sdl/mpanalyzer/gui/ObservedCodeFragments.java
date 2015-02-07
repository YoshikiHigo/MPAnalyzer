package jp.ac.osaka_u.ist.sdl.mpanalyzer.gui;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.SortedSet;
import java.util.TreeSet;

import jp.ac.osaka_u.ist.sdl.mpanalyzer.data.Code;

public class ObservedCodeFragments extends Observable {

	public enum CFLABEL {
		DETECTION, OVERLOOKED;
	}

	private static final Map<CFLABEL, ObservedCodeFragments> INSTANCES = new HashMap<CFLABEL, ObservedCodeFragments>();

	private final SortedSet<Code> codefragments;
	public final CFLABEL label;
	private Observer source;

	private ObservedCodeFragments(final CFLABEL label) {

		if (null == label) {
			throw new NullPointerException();
		}

		this.codefragments = new TreeSet<Code>();
		this.source = null;
		this.label = label;
	}

	public static final ObservedCodeFragments getInstance(final CFLABEL label) {
		ObservedCodeFragments instance = INSTANCES.get(label);
		if (null == instance) {
			instance = new ObservedCodeFragments(label);
			INSTANCES.put(label, instance);
		}
		return instance;
	}

	public boolean add(final Code codefragment, final Observer source) {

		if (null == codefragment) {
			return false;
		}

		this.codefragments.add(codefragment);
		this.source = source;

		this.setChanged();
		this.notifyObservers(source);

		return true;
	}

	public boolean addAll(final Collection<Code> codefragments,
			final Observer source) {

		if (null == codefragments) {
			return false;
		}

		this.codefragments.addAll(codefragments);
		this.source = source;

		this.setChanged();
		this.notifyObservers(source);

		return true;
	}

	public boolean remove(final Code codefragment, final Observer source) {

		if (null == codefragment) {
			return false;
		}

		this.codefragments.remove(codefragment);
		this.source = source;

		this.setChanged();
		this.notifyObservers(source);

		return true;
	}

	public boolean removeAll(final Collection<Code> codefragments,
			final Observer source) {

		if (null == codefragments) {
			return false;
		}

		this.codefragments.removeAll(codefragments);
		this.source = source;

		this.setChanged();
		this.notifyObservers(source);

		return true;
	}

	public boolean set(final Code codefragment, final Observer source) {

		if (null == codefragment) {
			return false;
		}

		this.codefragments.clear();
		this.codefragments.add(codefragment);
		this.source = source;

		this.setChanged();
		this.notifyObservers(source);

		return true;
	}

	public boolean setAll(final Collection<Code> codefragments,
			final Observer source) {

		if (null == codefragments) {
			return false;
		}

		this.codefragments.clear();
		this.codefragments.addAll(codefragments);
		this.source = source;

		this.setChanged();
		this.notifyObservers(source);

		return true;
	}

	public boolean isSet() {
		return !this.codefragments.isEmpty();
	}

	public void clear(final Observer source) {

		this.codefragments.clear();
		this.source = source;

		this.setChanged();
		this.notifyObservers(source);
	}

	public SortedSet<Code> get() {
		return Collections.unmodifiableSortedSet(this.codefragments);
	}

	public Observer getSource() {
		return this.source;
	}
}
