package yoshikihigo.cpanalyzer;

import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;

public class CPAConfig {

	static private CPAConfig SINGLETON = null;

	static public boolean initialize(final String[] args) {

		if (null != SINGLETON) {
			return false;
		}

		final Options options = new Options();

		{
			final Option option = new Option("lang", "language", true,
					"programming language for analysis");
			option.setArgName("language");
			option.setArgs(1);
			option.setRequired(false);
			options.addOption(option);
		}

		{
			final Option option = new Option("soft", "software", true,
					"software name");
			option.setArgName("software");
			option.setArgs(1);
			option.setRequired(false);
			options.addOption(option);
		}

		{
			final Option option = new Option("svnrepo", "svnrepository", true,
					"svn repository for mining");
			option.setArgName("svnrepository");
			option.setArgs(1);
			option.setRequired(false);
			options.addOption(option);
		}

		{
			final Option option = new Option("gitrepo", "gitrepository", true,
					"git repository for mining");
			option.setArgName("gitrepository");
			option.setArgs(1);
			option.setRequired(false);
			options.addOption(option);
		}

		{
			final Option option = new Option("startrev", "startrevision", true,
					"start revision of repository for mining");
			option.setArgName("revision");
			option.setArgs(1);
			option.setRequired(false);
			options.addOption(option);
		}

		{
			final Option option = new Option("endrev", "endrevision", true,
					"end revision of repository for mining");
			option.setArgName("revision");
			option.setArgs(1);
			option.setRequired(false);
			options.addOption(option);
		}

		{
			final Option option = new Option("db", "database", true,
					"database for storing modification patterns");
			option.setArgName("revision");
			option.setArgs(1);
			option.setRequired(false);
			options.addOption(option);
		}

		{
			final Option option = new Option("test", "test", true,
					"repository for testing");
			option.setArgName("repository");
			option.setArgs(1);
			option.setRequired(false);
			options.addOption(option);
		}

		{
			final Option option = new Option("teststartrev",
					"teststartrevision", true,
					"start revision of repository for test");
			option.setArgName("revision");
			option.setArgs(1);
			option.setRequired(false);
			options.addOption(option);
		}

		{
			final Option option = new Option("testendrev", "testendrevision",
					true, "end revision of repository for test");
			option.setArgName("revision");
			option.setArgs(1);
			option.setRequired(false);
			options.addOption(option);
		}

		{
			final Option option = new Option("thd", "thread", true,
					"end revision of repository for test");
			option.setArgName("thread");
			option.setArgs(1);
			option.setRequired(false);
			options.addOption(option);
		}

		{
			final Option option = new Option("large", "large", true,
					"threshold for ignoring large files");
			option.setArgName("size (number of statements)");
			option.setArgs(1);
			option.setRequired(false);
			options.addOption(option);
		}

		{
			final Option option = new Option("ignind", "ignoreindent", false,
					"canceling ignoring indent");
			option.setRequired(false);
			options.addOption(option);
		}

		{
			final Option option = new Option("ignspc", "ignorespace", false,
					"canceling ignoring whitespace");
			option.setRequired(false);
			options.addOption(option);
		}

		{
			final Option option = new Option("ignimp", "ignoreimport", false,
					"canceling ignoring import");
			option.setRequired(false);
			options.addOption(option);
		}

		{
			final Option option = new Option("igninc", "ignoreinclude", false,
					"canceling ignoring include");
			option.setRequired(false);
			options.addOption(option);
		}

		{
			final Option option = new Option("normalize", "normalization",
					false, "normalizing text for mining");
			option.setRequired(false);
			options.addOption(option);
		}

		{
			final Option option = new Option("onlycond", "onlycondition",
					false, "extracting only condition");
			option.setRequired(false);
			options.addOption(option);
		}

		{
			final Option option = new Option("v", "verbose", false,
					"verbose output for progressing");
			option.setRequired(false);
			options.addOption(option);
		}

		{
			final Option option = new Option("csv", "csv", true,
					"CSV file for writing modification patterns");
			option.setArgName("file");
			option.setArgs(1);
			option.setRequired(false);
			options.addOption(option);
		}

		{
			final Option option = new Option("debug", "debug", false,
					"print some informlation for debugging");
			option.setRequired(false);
			options.addOption(option);
		}

		try {
			final CommandLineParser parser = new PosixParser();
			final CommandLine commandLine = parser.parse(options, args);
			SINGLETON = new CPAConfig(commandLine);
		} catch (ParseException e) {
			e.printStackTrace();
			System.exit(0);
		}

		return true;
	}

	static public CPAConfig getInstance() {

		if (null == SINGLETON) {
			System.err.println("Config is not initialized.");
			System.exit(0);
		}

		return SINGLETON;
	}

	private final CommandLine commandLine;

	private CPAConfig(final CommandLine commandLine) {
		this.commandLine = commandLine;
	}

	public final Set<LANGUAGE> getLANGUAGE() {

		final Set<LANGUAGE> languages = new HashSet<>();

		if (this.commandLine.hasOption("lang")) {
			final String option = this.commandLine.getOptionValue("lang");
			final StringTokenizer tokenizer = new StringTokenizer(option, ":");
			while (tokenizer.hasMoreTokens()) {
				try {
					final String value = tokenizer.nextToken();
					final LANGUAGE language = LANGUAGE.valueOf(value
							.toUpperCase());
					languages.add(language);
				} catch (final IllegalArgumentException e) {
					System.err.println("invalid option value for \"-lang\"");
					System.exit(0);
				}
			}
		}

		else {
			for (final LANGUAGE language : LANGUAGE.values()) {
				languages.add(language);
			}
		}

		return languages;
	}

	public String getSOFTWARE() {
		if (!this.commandLine.hasOption("soft")) {
			System.err.println("option \"soft\" is not specified.");
			System.exit(0);
		}
		return this.commandLine.getOptionValue("soft");
	}

	public String getSVNREPOSITORY_FOR_MINING() {
		if (!this.commandLine.hasOption("svnrepo")) {
			System.err.println("option \"svnrepo\" is not specified.");
			System.exit(0);
		}
		return this.commandLine.getOptionValue("svnrepo");
	}

	public String getGITREPOSITORY_FOR_MINING() {
		if (!this.commandLine.hasOption("gitrepo")) {
			System.err.println("option \"gitrepo\" is not specified.");
			System.exit(0);
		}
		return this.commandLine.getOptionValue("gitrepo");
	}

	public long getSTART_REVISION_FOR_MINING() {
		if (this.commandLine.hasOption("startrev")) {
			return Long.parseLong(this.commandLine.getOptionValue("startrev"));
		}
		return -1;
	}

	public long getEND_REVISION_FOR_MINING() {
		if (this.commandLine.hasOption("endrev")) {
			return Long.parseLong(this.commandLine.getOptionValue("endrev"));
		}
		return -1;
	}

	public String getDATABASE() {
		if (!this.commandLine.hasOption("db")) {
			System.err.println("option \"db\" is not specified.");
			System.exit(0);
		}
		return this.commandLine.getOptionValue("db");
	}

	public String getREPOSITORY_FOR_TEST() {
		if (!this.commandLine.hasOption("test")) {
			System.err.println("option \"test\" is not specified.");
			System.exit(0);
		}
		return this.commandLine.getOptionValue("test");
	}

	public long getSTART_REVISION_FOR_TEST() {
		if (this.commandLine.hasOption("teststartrev")) {
			return Long.parseLong(this.commandLine
					.getOptionValue("teststartrev"));
		}
		return -1;
	}

	public long getEND_REVISION_FOR_TEST() {
		if (this.commandLine.hasOption("testendrev")) {
			return Long
					.parseLong(this.commandLine.getOptionValue("testendrev"));
		}
		return -1;
	}

	public int getTHREAD() {
		return this.commandLine.hasOption("thd") ? Integer
				.parseInt(this.commandLine.getOptionValue("thd")) : 1;
	}

	public int getLARGE() {
		return this.commandLine.hasOption("large") ? Integer
				.parseInt(this.commandLine.getOptionValue("large")) : 5000;
	}

	public boolean isIGNORE_INDENT() {
		return !this.commandLine.hasOption("ignind");
	}

	public boolean isIGNORE_WHITESPACE() {
		return !this.commandLine.hasOption("ignspc");
	}

	public boolean isIGNORE_IMPORT() {
		return !this.commandLine.hasOption("ignimp");
	}

	public boolean isIGNORE_INCLUDE() {
		return !this.commandLine.hasOption("inginc");
	}

	public boolean isNORMALIZATION() {
		return this.commandLine.hasOption("normalize");
	}

	public boolean isONLY_CONDITION() {
		return this.commandLine.hasOption("onlycond");
	}

	public boolean isVERBOSE() {
		return this.commandLine.hasOption("v");
	}

	public boolean hasSVNREPO() {
		return this.commandLine.hasOption("svnrepo");
	}

	public boolean hasGITREPO() {
		return this.commandLine.hasOption("gitrepo");
	}

	public String getCSV_FILE() {
		if (!this.commandLine.hasOption("csv")) {
			System.err.println("option \"csv\" is not specified.");
			System.exit(0);
		}
		return this.commandLine.getOptionValue("csv");
	}

	public boolean isDEBUG() {
		return this.commandLine.hasOption("debug");
	}
}
