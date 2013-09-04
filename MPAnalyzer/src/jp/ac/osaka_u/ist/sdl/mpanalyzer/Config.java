package jp.ac.osaka_u.ist.sdl.mpanalyzer;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.StringTokenizer;

public class Config {

	public static String getPATH_TO_REPOSITORY() {
		return getConfig("REPOSITORY");
	}

	public static String getDATABASELOCATION() {
		return getConfig("DATABASELOCATION");
	}

	public static String getDATABASENAME() {
		return getConfig("DATABASENAME");
	}
	
	public static String getCSV(){
		return getConfig("CSV");
	}

	public static String getLanguage() {
		return getConfig("LANGUAGE");
	}

	public static String getTARGET() {
		return getConfig("TARGET");
	}

	public static long getStartRevision() {
		final String startRevision = getConfig("STARTREVISION");
		return startRevision.equalsIgnoreCase("INITIAL") ? 0l : Long
				.parseLong(startRevision);
	}

	public static long getEndtRevision() {
		final String endRevision = getConfig("ENDREVISION");
		return endRevision.equalsIgnoreCase("LATEST") ? Long.MAX_VALUE : Long
				.parseLong(endRevision);
	}

	public static String getPATH_TO_PATTERNSFILE() {
		return getConfig("PATTERNS");
	}

	public static String getPATH_TO_INCONSISTENCIESFILE() {
		return getConfig("INCONSISTENCIES");
	}

	public static boolean IGNORE_INDENT() {
		final String term = getConfig("IGNOREINDENT");
		if (term.equalsIgnoreCase("true")) {
			return true;
		} else if (term.equalsIgnoreCase("false")) {
			return false;
		} else {
			System.out
					.println("\'true\' or \'false\' must be specified for IGNOREINDENT");
			System.exit(0);
			return false;
		}
	}

	public static boolean IGNORE_WHITESPACE() {
		final String term = getConfig("IGNOREWHITESPACE");
		if (term.equalsIgnoreCase("true")) {
			return true;
		} else if (term.equalsIgnoreCase("false")) {
			return false;
		} else {
			System.out
					.println("\'true\' or \'false\' must be specified for IGNOREWHITESPACE");
			System.exit(0);
			return false;
		}
	}

	public static int getSupportValue() {
		final String text = getConfig("SUPPORT");
		return Integer.parseInt(text);
	}

	public static float getConfidenceValue() {
		final String text = getConfig("CONFIDENCE");
		return Float.parseFloat(text);
	}

	public static int getPlaceValue() {
		final String text = getConfig("PLACE");
		return Integer.parseInt(text);
	}

	public static int getThreadsValue() {
		final String text = getConfig("THREADS");
		return Integer.parseInt(text);
	}

	public static int getLargeModification() {
		final String text = getConfig("LARGEMODIFICATION");
		return text.equalsIgnoreCase("NOLIMIT") ? Integer.MAX_VALUE : Integer
				.parseInt(text);
	}

	public static boolean IGNORE_IMPORT() {
		final String term = getConfig("IGNOREIMPORT");
		if (term.equalsIgnoreCase("true")) {
			return true;
		} else if (term.equalsIgnoreCase("false")) {
			return false;
		} else {
			System.out
					.println("\'true\' or \'false\' must be specified for IGNOREIMPORT");
			System.exit(0);
			return false;
		}
	}

	public static boolean IGNORE_INCLUDE() {
		final String term = getConfig("IGNOREINCLUDE");
		if (term.equalsIgnoreCase("true")) {
			return true;
		} else if (term.equalsIgnoreCase("false")) {
			return false;
		} else {
			System.out
					.println("\'true\' or \'false\' must be specified for IGNOREINCLUDE");
			System.exit(0);
			return false;
		}
	}

	private static String getConfig(final String term) {

		try {
			final BufferedReader reader = new BufferedReader(new FileReader(
					"config.txt"));
			while (reader.ready()) {
				final String line = reader.readLine();
				final StringTokenizer tokenizer = new StringTokenizer(line, "=");
				final String variable = tokenizer.nextToken();
				if (variable.equals(term)) {
					reader.close();
					if (tokenizer.hasMoreTokens()) {
						return tokenizer.nextToken();
					} else {
						return "";
					}
				}
			}

			reader.close();

		} catch (final IOException e) {
			System.err.println("invalid program: " + term);
			System.exit(0);
		}

		return "";
	}
}
