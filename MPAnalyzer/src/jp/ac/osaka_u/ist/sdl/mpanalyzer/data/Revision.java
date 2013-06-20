package jp.ac.osaka_u.ist.sdl.mpanalyzer.data;

public class Revision implements Comparable<Revision> {

	public final long number;
	public final String date;
	public final String message;

	public Revision(final long number, final String date, final String message) {
		this.number = number;
		this.date = date;
		this.message = message;
	}

	@Override
	public int compareTo(final Revision revision) {
		return new Long(this.number).compareTo(revision.number);
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof Revision)) {
			return false;
		}
		final Revision target = (Revision) o;
		return this.number == target.number;
	}

	@Override
	public int hashCode() {
		return (int) this.number;
	}

	@Override
	public String toString() {
		return this.date;
	}
}
