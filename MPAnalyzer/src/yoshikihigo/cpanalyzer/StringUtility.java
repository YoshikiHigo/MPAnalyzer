package yoshikihigo.cpanalyzer;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNURL;

import yoshikihigo.commentremover.CRConfig;
import yoshikihigo.commentremover.CommentRemover;
import yoshikihigo.commentremover.CommentRemoverJC;
import yoshikihigo.commentremover.CommentRemoverPY;
import yoshikihigo.cpanalyzer.data.Statement;
import yoshikihigo.cpanalyzer.lexer.CLineLexer;
import yoshikihigo.cpanalyzer.lexer.JavaLineLexer;
import yoshikihigo.cpanalyzer.lexer.LineLexer;
import yoshikihigo.cpanalyzer.lexer.PythonLineLexer;
import yoshikihigo.cpanalyzer.lexer.token.STATEMENT;
import yoshikihigo.cpanalyzer.lexer.token.Token;

public class StringUtility {

	public static List<String> removeLineHeader(final List<String> beforeText)
			throws IOException {
		final List<String> afterText = new ArrayList<>();
		beforeText.stream().forEach(
				line -> afterText.add(removeLineHeader(line)));
		return afterText;
	}

	public static String removeLineHeader(final String line) {
		if (line.startsWith("+") || line.startsWith("-")) {
			return line.substring(1);
		} else {
			return line;
		}
	}

	public static List<String> addLineHeader(final List<String> beforeText,
			final String prefix) throws IOException {
		final List<String> afterText = new ArrayList<>();
		beforeText.stream().forEach(line -> afterText.add(prefix + line));
		return afterText;
	}

	public static String addLineHeader(final String line, final String prefix) {
		final StringBuilder builder = new StringBuilder();
		builder.append(prefix);
		builder.append(line);
		return builder.toString();
	}

	public static List<String> removeIndent(final List<String> beforeText)
			throws IOException {
		final List<String> afterText = new ArrayList<>();
		beforeText.stream().forEach(line -> afterText.add(removeIndent(line)));
		return afterText;
	}

	public static String removeIndent(final String line) {
		int startIndex = 0;
		for (int index = 0; index < line.length(); index++) {
			if ((' ' != line.charAt(index)) && ('\t' != line.charAt(index))) {
				startIndex = index;
				break;
			}
		}
		return line.substring(startIndex);
	}

	public static List<String> removeSpaceTab(final List<String> beforeText)
			throws IOException {
		final List<String> afterText = new ArrayList<>();
		beforeText.stream()
				.forEach(line -> afterText.add(removeSpaceTab(line)));
		return afterText;
	}

	public static String removeSpaceTab(final String line) {
		final StringBuilder newLine = new StringBuilder();
		for (int index = 0; index < line.length(); index++) {
			final char c = line.charAt(index);
			if (' ' != c && '\t' != c) {
				newLine.append(c);
			}
		}
		return newLine.toString();
	}

	public static String removeNewLine(final List<String> beforeText)
			throws IOException {
		return String.join(" ", beforeText);
	}

	public static List<String> trim(final List<String> beforeText)
			throws IOException {
		final List<String> afterText = new ArrayList<>();
		for (final String line : beforeText) {
			boolean blankLine = true;
			for (int index = 0; index < line.length(); index++) {
				final char c = line.charAt(index);
				if (' ' != c && '\t' != c) {
					blankLine = false;
					break;
				}
			}
			if (!blankLine) {
				afterText.add(line);
			}
		}
		return afterText;
	}

	public static List<Statement> splitToStatements(final String text,
			final int startLine, final int endLine) {

		if (text.isEmpty()) {
			return new ArrayList<Statement>();
		}

		final List<Statement> statements = new ArrayList<>();
		final List<String> lines = Arrays.asList(text.split(System
				.lineSeparator()));
		lines.stream().forEach(
				line -> {
					final List<Token> tokens = new ArrayList<>();
					tokens.add(new STATEMENT(line));
					final byte[] hash = Statement.getMD5(line);
					final Statement statement = new Statement(startLine,
							endLine, -1, true, tokens, line, line, hash);
					statements.add(statement);
				});

		statements.get(0).tokens.get(0).line = startLine;
		statements.get(statements.size() - 1).tokens.get(0).line = endLine;

		return statements;
	}

	public static String[] splitToLines(final String text) {
		if (null == text) {
			return new String[0];
		}
		return text.split(System.lineSeparator());
	}

	public static List<Statement> splitToStatements(final String text,
			final LANGUAGE language) {

		if (text.isEmpty()) {
			return new ArrayList<Statement>();
		}

		switch (language) {
		case JAVA: {
			final String[] args = new String[7];
			args[0] = "-q";
			args[1] = "-blankline";
			args[2] = "retain";
			args[3] = "-bracketline";
			args[4] = "retain";
			args[5] = "-indent";
			args[6] = "retain";
			final CRConfig config = CRConfig.initialize(args);
			final CommentRemover remover = new CommentRemoverJC(config);
			final String normalizedText = remover.perform(text);
			final LineLexer lexer = new JavaLineLexer();
			final List<Token> tokens = lexer.lexFile(normalizedText);
			final List<Statement> statements = Statement
					.getJCStatements(tokens);
			return statements;
		}
		case C:
		case CPP: {
			final String[] args = new String[7];
			args[0] = "-q";
			args[1] = "-blankline";
			args[2] = "retain";
			args[3] = "-bracketline";
			args[4] = "retain";
			args[5] = "-indent";
			args[6] = "retain";
			final CRConfig config = CRConfig.initialize(args);
			final CommentRemover remover = new CommentRemoverJC(config);
			final String normalizedText = remover.perform(text);
			final LineLexer lexer = new CLineLexer();
			final List<Token> tokens = lexer.lexFile(normalizedText);
			final List<Statement> statements = Statement
					.getJCStatements(tokens);
			return statements;
		}
		case PYTHON: {
			final String[] args = new String[3];
			args[0] = "-q";
			args[1] = "-blankline";
			args[2] = "retain";
			final CRConfig config = CRConfig.initialize(args);
			final CommentRemover remover = new CommentRemoverPY(config);
			final String normalizedText = remover.perform(text);
			final LineLexer lexer = new PythonLineLexer();
			final List<Token> tokens = lexer.lexFile(normalizedText);
			final List<Statement> statements = Statement
					.getPYStatements(tokens);
			return statements;
		}
		default: {
			System.err.println("invalid programming language.");
			System.exit(0);
		}
		}

		return new ArrayList<Statement>();
	}

	public static int getLOC(final String text) {
		return splitToLines(text).length;
	}

	public static boolean isImport(final List<String> text) {
		return text.stream().anyMatch(
				line -> line.startsWith("import ")
						|| line.startsWith("package "));
	}

	public static boolean isInclude(final List<String> text) {
		return text.stream().anyMatch(line -> line.startsWith("#include"));
	}

	public static boolean isJavaFile(final String path) {
		return path.endsWith(".java");
	}

	public static boolean isCFile(final String path) {
		return path.endsWith(".c") || path.endsWith(".cc")
				|| path.endsWith(".cpp") || path.endsWith(".cxx");
	}

	public static String convertListToString(final List<String> list) {
		return String.join(System.lineSeparator(), list);
	}

	public static List<String> convertStringToList(final String text) {
		final BufferedReader reader = new BufferedReader(new StringReader(text));
		return reader.lines().collect(Collectors.toList());
	}

	public static String getSQLITELiteral(final String text) {
		final StringBuilder builder = new StringBuilder();
		builder.append("\'");
		for (int index = 0; index < text.length(); index++) {
			final char c = text.charAt(index);
			if ('\'' == c) {
				builder.append('\'');
			}
			builder.append(c);
		}

		builder.append("\'");
		return builder.toString();
	}

	public static String getFilePath(final String diffHeaderLine) {
		if (diffHeaderLine.startsWith("Index: ")) {
			return diffHeaderLine.substring("Index: ".length());
		} else {
			return null;
		}
	}

	public static String getDateString(final Date date) {
		final DateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		return df.format(date);
	}

	public static Date getDateObject(final String date) {
		try {
			final DateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
			return df.parse(date);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static String removeTime(final String date) {
		return date.indexOf(' ') > 0 ? date.substring(0, date.indexOf(' '))
				: date;
	}

	static public SVNURL getSVNURL(final String repository, final String path) {
		SVNURL fileurl = null;
		try {
			if (repository.startsWith("http://")) {
				fileurl = SVNURL.parseURIEncoded(repository + "/" + path);
			} else if (repository.startsWith("/")) {
				fileurl = SVNURL.fromFile(new File(repository
						+ System.getProperty("file.separator") + path));
			} else {
				throw new IllegalStateException(
						"illegal URL (, which is specified by \"-repo\" option)");
			}

		} catch (final SVNException | IllegalStateException e) {
			e.printStackTrace();
		}
		return fileurl;
	}
}
