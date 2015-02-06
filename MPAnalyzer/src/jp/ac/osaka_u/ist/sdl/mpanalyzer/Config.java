package jp.ac.osaka_u.ist.sdl.mpanalyzer;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.StringTokenizer;

import org.apache.commons.cli.CommandLine;

public class Config {

	static private Config SINGLETON = null;

	static public boolean initialize(final CommandLine commandLine) {

		if (null != SINGLETON) {
			return false;
		}

		SINGLETON = new Config(commandLine);

		return true;
	}

	static public Config getInstance() {

		if (null == SINGLETON) {
			System.err.println("SINGLETON is not initialized.");
			System.exit(0);
		}

		return SINGLETON;
	}

	private final CommandLine commandLine;

	private Config(final CommandLine commandLine) {
		this.commandLine = commandLine;
	}

	public String getPATH_TO_REPOSITORY() {
		return this.commandLine.getOptionValue("repo");
	}

	public static String getPATH_TO_DATABASEREPOSITORY() {
		return getConfig("DATABASEREPOSITORY");
	}

	
	public static String getDATABASELOCATION() {
		return getConfig("DATABASELOCATION");
	}

	public static String getDATABASENAME() {
		return getConfig("DATABASENAME");
	}

	public static String getMPCSVFILE() {
		return getConfig("MPCSVFILE");
	}

	public static String getLanguage() {
		return getConfig("LANGUAGE");
	}

	public static boolean isOnlyCondition() {
		return getConfig("ONLYCONDITION").equalsIgnoreCase("YES");
	}

	public static long getDatabaseStartRevision() {
		final String startRevision = getConfig("DATABASESTARTREVISION");
		if (startRevision.isEmpty()) {
			return -1;
		}
		try {
			return Long.parseLong(startRevision);
		} catch (NumberFormatException e) {
			e.printStackTrace();
			System.exit(0);
		}
		return -1;
	}

	public static long getDatabaseEndRevision() {
		final String startRevision = getConfig("DATABASEENDREVISION");
		if (startRevision.isEmpty()) {
			return -1;
		}
		try {
			return Long.parseLong(startRevision);
		} catch (NumberFormatException e) {
			e.printStackTrace();
			System.exit(0);
		}
		return -1;
	}

	public static long getStartRevision() {
		final String startRevision = getConfig("STARTREVISION");
		if (startRevision.isEmpty()) {
			return -1;
		}
		try {
			return Long.parseLong(startRevision);
		} catch (NumberFormatException e) {
			e.printStackTrace();
			System.exit(0);
		}
		return -1;
	}

	public static long getEndRevision() {
		final String startRevision = getConfig("ENDREVISION");
		if (startRevision.isEmpty()) {
			return -1;
		}
		try {
			return Long.parseLong(startRevision);
		} catch (NumberFormatException e) {
			e.printStackTrace();
			System.exit(0);
		}
		return -1;
	}

	public static long getCloneDetectionRevision() {
		final String cloneDetectionRevision = getConfig("CLONEDETECTIONREVISION");
		return Long.parseLong(cloneDetectionRevision);
	}

	public static int getCloneThreshold() {
		final String cloneThreshold = getConfig("CLONETHRESHOLD");
		return Integer.parseInt(cloneThreshold);
	}

	public static String getCloneOutputFile() {
		return getConfig("CLONEOUTPUTFILE");
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

	public static int getThreadsValue() {
		final String text = getConfig("THREADS");
		return Integer.parseInt(text);
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
				if (line.startsWith("#")) {
					continue;
				}
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
