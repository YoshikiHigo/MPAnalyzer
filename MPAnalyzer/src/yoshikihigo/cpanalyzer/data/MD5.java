package yoshikihigo.cpanalyzer.data;

import java.math.BigInteger;
import java.util.Arrays;

public class MD5 {
	
	final byte[] hash;

	public MD5(final byte[] hash) {
		this.hash = Arrays.copyOf(hash, hash.length);
	}

	@Override
	public int hashCode() {
		final BigInteger value = new BigInteger(1, this.hash);
		return value.toString(16).hashCode();
	}

	@Override
	public boolean equals(final Object o) {
		if (null == o) {
			return false;
		}
		if (!(o instanceof MD5)) {
			return false;
		}

		return Arrays.equals(this.hash, ((MD5) o).hash);
	}
}
