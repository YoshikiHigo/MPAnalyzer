package yoshikihigo.cpanalyzer;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class CPAConfig {

  static private CPAConfig SINGLETON = null;
  static final public Options OPTIONS = new Options();
  static {
    {
      final Option option =
          new Option("lang", "language", true, "programming language for analysis");
      option.setArgName("language");
      option.setArgs(1);
      option.setRequired(false);
      OPTIONS.addOption(option);
    }

    {
      final Option option = new Option("soft", "software", true, "software name");
      option.setArgName("software");
      option.setArgs(1);
      option.setRequired(false);
      OPTIONS.addOption(option);
    }

    {
      final Option option =
          new Option("svnrepo", "svnrepository", true, "svn repository for mining");
      option.setArgName("svnrepository");
      option.setArgs(1);
      option.setRequired(false);
      OPTIONS.addOption(option);
    }

    {
      final Option option =
          new Option("gitrepo", "gitrepository", true, "git repository for mining");
      option.setArgName("gitrepository");
      option.setArgs(1);
      option.setRequired(false);
      OPTIONS.addOption(option);
    }

    {
      final Option option =
          new Option("startrev", "startrevision", true, "start revision of repository for mining");
      option.setArgName("revision");
      option.setArgs(1);
      option.setRequired(false);
      OPTIONS.addOption(option);
    }

    {
      final Option option =
          new Option("endrev", "endrevision", true, "end revision of repository for mining");
      option.setArgName("revision");
      option.setArgs(1);
      option.setRequired(false);
      OPTIONS.addOption(option);
    }

    {
      final Option option =
          new Option("startdate", "startdate", true, "start date of repository for mining");
      option.setArgName("date (dd:mm:yyyy)");
      option.setArgs(1);
      option.setRequired(false);
      OPTIONS.addOption(option);
    }

    {
      final Option option =
          new Option("enddate", "enddate", true, "end date of repository for mining");
      option.setArgName("date (dd:mm:yyyy)");
      option.setArgs(1);
      option.setRequired(false);
      OPTIONS.addOption(option);
    }

    {
      final Option option =
          new Option("db", "database", true, "database for storing modification patterns");
      option.setArgName("revision");
      option.setArgs(1);
      option.setRequired(false);
      OPTIONS.addOption(option);
    }

    {
      final Option option =
          new Option("cs", "changesize", true, "upper limit of change size to retrieve");
      option.setArgName("size (number of statements)");
      option.setArgs(1);
      option.setRequired(false);
      OPTIONS.addOption(option);
    }

    {
      final Option option = new Option("test", "test", true, "repository for testing");
      option.setArgName("repository");
      option.setArgs(1);
      option.setRequired(false);
      OPTIONS.addOption(option);
    }

    {
      final Option option = new Option("teststartrev", "teststartrevision", true,
          "start revision of repository for test");
      option.setArgName("revision");
      option.setArgs(1);
      option.setRequired(false);
      OPTIONS.addOption(option);
    }

    {
      final Option option =
          new Option("testendrev", "testendrevision", true, "end revision of repository for test");
      option.setArgName("revision");
      option.setArgs(1);
      option.setRequired(false);
      OPTIONS.addOption(option);
    }

    {
      final Option option =
          new Option("thd", "thread", true, "end revision of repository for test");
      option.setArgName("thread");
      option.setArgs(1);
      option.setRequired(false);
      OPTIONS.addOption(option);
    }

    {
      final Option option =
          new Option("large", "large", true, "threshold for ignoring large files");
      option.setArgName("size (number of statements)");
      option.setArgs(1);
      option.setRequired(false);
      OPTIONS.addOption(option);
    }

    {
      final Option option =
          new Option("ignind", "ignoreindent", false, "canceling ignoring indent");
      option.setRequired(false);
      OPTIONS.addOption(option);
    }

    {
      final Option option =
          new Option("ignspc", "ignorespace", false, "canceling ignoring whitespace");
      option.setRequired(false);
      OPTIONS.addOption(option);
    }

    {
      final Option option =
          new Option("ignimp", "ignoreimport", false, "canceling ignoring import");
      option.setRequired(false);
      OPTIONS.addOption(option);
    }

    {
      final Option option =
          new Option("igninc", "ignoreinclude", false, "canceling ignoring include");
      option.setRequired(false);
      OPTIONS.addOption(option);
    }

    {
      final Option option =
          new Option("modifier", "countmodifier", false, "count modifiers for mining");
      option.setRequired(false);
      OPTIONS.addOption(option);
    }

    {
      final Option option =
          new Option("n", "normalization", false, "normalizing variables and literals for mining");
      option.setRequired(false);
      OPTIONS.addOption(option);
    }

    {
      final Option option =
          new Option("onlycond", "onlycondition", false, "extracting only condition");
      option.setRequired(false);
      OPTIONS.addOption(option);
    }

    {
      final Option option = new Option("v", "verbose", false, "verbose output for progressing");
      option.setRequired(false);
      OPTIONS.addOption(option);
    }

    {
      final Option option =
          new Option("q", "quiet", false, "do not output anything for progressing");
      option.setRequired(false);
      OPTIONS.addOption(option);
    }

    {
      final Option option =
          new Option("csv", "csv", true, "CSV file for writing modification patterns");
      option.setArgName("file");
      option.setArgs(1);
      option.setRequired(false);
      OPTIONS.addOption(option);
    }

    {
      final Option option =
          new Option("a", "all", false, "use all changes to make change patterns");
      option.setRequired(false);
      OPTIONS.addOption(option);
    }

    {
      final Option option = new Option("f", "force", false,
          "force to make database file even if the same name file already exists");
      option.setRequired(false);
      OPTIONS.addOption(option);
    }

    {
      final Option option =
          new Option("i", "insert", false, "insert analysis results into an existing database");
      option.setRequired(false);
      OPTIONS.addOption(option);
    }

    {
      final Option option =
          new Option("debug", "debug", false, "print some informlation for debugging");
      option.setRequired(false);
      OPTIONS.addOption(option);
    }

    {
      final Option option = new Option("esvnrepo", "esvnrepository", true,
          "svn repository for exploring latent buggy code");
      option.setArgName("svnrepository");
      option.setArgs(1);
      option.setRequired(false);
      OPTIONS.addOption(option);
    }

    {
      final Option option = new Option("egitrepo", "egitrepository", true,
          "git repository for exploring latent buggy code");
      option.setArgName("gitrepository");
      option.setArgs(1);
      option.setRequired(false);
      OPTIONS.addOption(option);
    }

    {
      final Option option =
          new Option("edir", "edirectory", true, "source files for exploring latent buggy code");
      option.setArgName("directory");
      option.setArgs(1);
      option.setRequired(false);
      OPTIONS.addOption(option);
    }

    {
      final Option revision = new Option("esvnrev", "esvnrevision", true,
          "SVN target revision for exploring latent buggy code");
      revision.setArgName("number");
      revision.setArgs(1);
      revision.setRequired(false);
      OPTIONS.addOption(revision);
    }

    {
      final Option commit = new Option("egitcommit", "egitcommit", true,
          "GIT target commit for exploring latent buggy code");
      commit.setArgName("id");
      commit.setArgs(1);
      commit.setRequired(false);
      OPTIONS.addOption(commit);
    }

    {
      final Option commit = new Option("echarset", "echarset", true,
          "charset of source code for exploring latent buggy code");
      commit.setArgName("charset");
      commit.setArgs(1);
      commit.setRequired(false);
      OPTIONS.addOption(commit);
    }

    {
      final Option option = new Option("esupport", "esupport", true,
          "minimum suppport of change patterns used for exploring latent buggy code");
      option.setArgName("number");
      option.setArgs(1);
      option.setRequired(false);
      OPTIONS.addOption(option);
    }

    {
      final Option option = new Option("econfidence", "econfidence", true,
          "minimum confidence of change patterns used for exploring latent buggy code");
      option.setArgName("number");
      option.setArgs(1);
      option.setRequired(false);
      OPTIONS.addOption(option);
    }

    {
      final Option source = new Option("bug", "bugfile", true, "a csv file include bug IDs");
      source.setArgName("file");
      source.setArgs(1);
      source.setRequired(false);
      OPTIONS.addOption(source);
    }

    {
      final Option option =
          new Option("warn", "warning", true, "a file to output found latent problems");
      option.setArgName("file");
      option.setArgs(1);
      option.setRequired(false);
      OPTIONS.addOption(option);
    }

    {
      final Option option = new Option("onlybugfix", "onlybugfix", false,
          "use only bugfix change patterns to detect latent buggy code");
      option.setRequired(false);
      OPTIONS.addOption(option);
    }
  }

  static public Collection<Option> getOptions() {
    return OPTIONS.getOptions();
  }

  static public boolean initialize(final String[] args) {

    if (null != SINGLETON) {
      return false;
    }

    try {
      final CommandLineParser parser = new DefaultParser();
      final CommandLine commandLine = parser.parse(OPTIONS, args);
      SINGLETON = new CPAConfig(commandLine);

      if (SINGLETON.isVERBOSE() && SINGLETON.isQUIET()) {
        System.err
            .println("\"-v\" (\"--verbose\") and \"-q\" (\"--quiet\") can not be used together.");
        System.exit(0);
      }

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
          final LANGUAGE language = LANGUAGE.valueOf(value.toUpperCase());
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

  public Date getSTART_DATE_FOR_MINING() {
    Date date = new Date(0);
    if (this.commandLine.hasOption("startdate")) {
      final SimpleDateFormat format = new SimpleDateFormat("dd:MM:yyyy");
      final String value = this.commandLine.getOptionValue("startdate");
      try {
        date = format.parse(value);
      } catch (final java.text.ParseException e) {
        System.out.println("\"-startdate\" must be specified with dd:MM:yyyy format");
        System.exit(0);
      }
    }

    return date;
  }

  public Date getEND_DATE_FOR_MINING() {
    Date date = new Date();
    if (this.commandLine.hasOption("enddate")) {
      final SimpleDateFormat format = new SimpleDateFormat("dd:MM:yyyy");
      final String value = this.commandLine.getOptionValue("enddate");
      try {
        date = format.parse(value);
      } catch (final java.text.ParseException e) {
        System.out.println("\"-enddate\" must be specified with dd:MM:yyyy format");
        System.exit(0);
      }
    }

    return date;
  }

  public String getDATABASE() {
    if (!this.commandLine.hasOption("db")) {
      System.err.println("option \"db\" is not specified.");
      System.exit(0);
    }
    return this.commandLine.getOptionValue("db");
  }

  public int getCHANGESIZE() {
    if (!this.commandLine.hasOption("cs")) {
      return Integer.MAX_VALUE;
    } else {
      return Integer.parseInt(this.commandLine.getOptionValue("cs"));
    }
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
      return Long.parseLong(this.commandLine.getOptionValue("teststartrev"));
    }
    return -1;
  }

  public long getEND_REVISION_FOR_TEST() {
    if (this.commandLine.hasOption("testendrev")) {
      return Long.parseLong(this.commandLine.getOptionValue("testendrev"));
    }
    return -1;
  }

  public int getTHREAD() {
    return this.commandLine.hasOption("thd")
        ? Integer.parseInt(this.commandLine.getOptionValue("thd"))
        : 1;
  }

  public int getLARGE() {
    return this.commandLine.hasOption("large")
        ? Integer.parseInt(this.commandLine.getOptionValue("large"))
        : 10000;
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

  public boolean isCOUNT_MODIFIER() {
    return this.commandLine.hasOption("modifier");
  }

  public boolean isNORMALIZATION() {
    return this.commandLine.hasOption("n");
  }

  public boolean isONLY_CONDITION() {
    return this.commandLine.hasOption("onlycond");
  }

  public boolean isVERBOSE() {
    return this.commandLine.hasOption("v");
  }

  public boolean isQUIET() {
    return this.commandLine.hasOption("q");
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

  public boolean isALL() {
    return this.commandLine.hasOption("a");
  }

  public boolean isFORCE() {
    return this.commandLine.hasOption("f");
  }

  public boolean isINSERT() {
    return this.commandLine.hasOption("i");
  }

  public boolean hasESVNREPO() {
    return this.commandLine.hasOption("esvnrepo");
  }

  public String getESVNREPO() {
    if (!this.commandLine.hasOption("esvnrepo")) {
      System.err.println("option \"esvnrepo\" is not specified.");
      System.exit(0);
    }
    return this.commandLine.getOptionValue("esvnrepo");
  }

  public boolean hasEGITREPO() {
    return this.commandLine.hasOption("egitrepo");
  }

  public String getEGITREPO() {
    if (!this.commandLine.hasOption("egitrepo")) {
      System.err.println("option \"egitrepo\" is not specified.");
      System.exit(0);
    }
    return this.commandLine.getOptionValue("egitrepo");
  }

  public boolean hasEDIR() {
    return this.commandLine.hasOption("edir");
  }

  public String getEDIR() {
    if (!this.commandLine.hasOption("edir")) {
      System.err.println("option \"edir\" is not specified.");
      System.exit(0);
    }
    final File file = new File(this.commandLine.getOptionValue("edir"));
    if (!file.exists()) {
      System.err.println("specified path by option \"edir\" does not exist.");
      System.exit(0);
    }
    return file.getAbsolutePath();
  }

  public boolean hasESVNREV() {
    return this.commandLine.hasOption("esvnrev");
  }

  public int getESVNREV() {
    if (!this.commandLine.hasOption("esvnrev")) {
      System.err.println("option \"esvnrev\" is not specified.");
      System.exit(0);
    }
    return Integer.parseInt(this.commandLine.getOptionValue("esvnrev"));
  }

  public boolean hasEGITCOMMIT() {
    return this.commandLine.hasOption("egitcommit");
  }

  public String getEGITCOMMIT() {
    if (!this.commandLine.hasOption("egitcommit")) {
      System.err.println("option \"egitcommit\" is not specified.");
      System.exit(0);
    }
    return this.commandLine.getOptionValue("egitcommit");
  }

  public String getECHARSET() {
    return this.commandLine.hasOption("echarset") ? this.commandLine.getOptionValue("echarset")
        : "";
  }

  public int getESUPPORT() {
    return this.commandLine.hasOption("esupport")
        ? Integer.parseInt(this.commandLine.getOptionValue("esupport"))
        : 1;
  }

  public float getECONFIDENCE() {
    return this.commandLine.hasOption("econfidence")
        ? Float.parseFloat(this.commandLine.getOptionValue("econfidence"))
        : 0.0f;
  }

  public Path getBUG() {
    if (!this.commandLine.hasOption("bug")) {
      System.err.println("option \"bug\" is not specified.");
      System.exit(0);
    }
    final Path bugFilePath = Paths.get(this.commandLine.getOptionValue("bug"));
    if (!Files.isReadable(bugFilePath)) {
      System.err.println("\'" + bugFilePath.toString() + "\' is not readable.");
      System.exit(0);
    }
    return bugFilePath;
  }

  public boolean hasWARN() {
    return this.commandLine.hasOption("warn");
  }

  public String getWARN() {
    if (!this.commandLine.hasOption("warn")) {
      System.err.println("option \"warn\" is not specified.");
      System.exit(0);
    }
    return this.commandLine.getOptionValue("warn");
  }

  public boolean isONLYBUGFIX() {
    return this.commandLine.hasOption("onlybugfix");
  }
}
