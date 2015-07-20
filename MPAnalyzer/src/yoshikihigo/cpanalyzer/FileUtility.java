package yoshikihigo.cpanalyzer;

public class FileUtility {

	public static LANGUAGE getLANGUAGE(final String name) {
		for (final LANGUAGE language : LANGUAGE.values()) {
			if (language.isTarget(name)) {
				return language;
			}
		}
		return null;
	}
}
