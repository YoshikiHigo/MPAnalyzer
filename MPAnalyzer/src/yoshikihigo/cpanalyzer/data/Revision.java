package yoshikihigo.cpanalyzer.data;

public class Revision implements Comparable<Revision> {

	public final String software;
	public final long number;
	public final String date;
	public final String message;
	public final String author;

	public Revision(final String software, final long number,
			final String date, final String message, final String author) {
		this.software = software;
		this.number = number;
		this.date = date;
		this.message = message;
		this.author = author;
	}

	@Override
	public int compareTo(final Revision revision) {
		final int softwareOrder = this.software.compareTo(revision.software);
		if (0 != softwareOrder) {
			return softwareOrder;
		}
		return Long.compare(this.number, revision.number);
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
