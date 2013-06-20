package jp.ac.osaka_u.ist.sdl.mpanalyzer.data;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import jp.ac.osaka_u.ist.sdl.mpanalyzer.Config;
import jp.ac.osaka_u.ist.sdl.mpanalyzer.StringUtility;

public class SourceFile implements Comparable<SourceFile> {

	static final public boolean IGNORE_INDENT = Config.IGNORE_INDENT();
	static final public boolean IGNORE_WHITESPACE = Config.IGNORE_WHITESPACE();

	public final String filepath;
	private final List<String> originalLines;
	private final StringBuilder normalizedSequence;
	private final List<Integer> positionMapper;

	public SourceFile(final String filepath) throws IOException {
		this.filepath = filepath;
		this.originalLines = new ArrayList<String>();
		this.originalLines.add("");
		this.normalizedSequence = new StringBuilder();
		this.positionMapper = new ArrayList<Integer>();
	}

	public void addLine(final String line) {

		final String noindent = IGNORE_INDENT ? StringUtility
				.removeIndent(line) : line;
		final String nospace = IGNORE_WHITESPACE ? StringUtility
				.removeSpaceTab(noindent) : noindent;
		final String trim = nospace.trim();
		this.normalizedSequence.append(trim);

		final int lineNumber = this.originalLines.size();
		for (int i = 0; i < trim.length(); i++) {
			this.positionMapper.add(lineNumber);
		}

		// lineNumberの計算よりも後にある必要あり
		this.originalLines.add(line);
	}

	public String getNormalizedSequence() {
		return this.normalizedSequence.toString();
	}

	public List<SortedSet<Integer>> getMatchedLineNumbersList(
			final String beforeSequence, final String afterSequence) {

		final String content = this.normalizedSequence.toString();
		final boolean[] checkingArray = new boolean[content.length()];
		for (int index = 0; index < checkingArray.length; index++) {
			checkingArray[index] = false;
		}

		// before文字列を用いて一致する部分をtrueに
		if (!beforeSequence.isEmpty()) {
			int fromIndex = 0;
			while (true) {
				final int matchedIndex = content.indexOf(beforeSequence,
						fromIndex);
				if (matchedIndex < 0) {
					break;
				} else {
					for (int index = 0; index < beforeSequence.length(); index++) {
						checkingArray[matchedIndex + index] = true;
					}
					fromIndex = matchedIndex + 1;
				}
			}
		}

		// after文字列を用いて一致する部分をfalseに
		if (!afterSequence.isEmpty()) {
			int fromIndex = 0;
			while (true) {
				final int matchedIndex = content.indexOf(afterSequence,
						fromIndex);
				if (matchedIndex < 0) {
					break;
				} else {
					for (int index = 0; index < afterSequence.length(); index++) {
						checkingArray[matchedIndex + index] = false;
					}
					fromIndex = matchedIndex + 1;
				}
			}
		}

		final List<SortedSet<Integer>> linesList = new ArrayList<SortedSet<Integer>>();
		SortedSet<Integer> lines = new TreeSet<Integer>();
		for (int index = 0; index < checkingArray.length; index++) {

			if (true == checkingArray[index]) {
				lines.add(this.positionMapper.get(index));
			}

			else if (false == checkingArray[index]) {
				if (0 < lines.size()) {
					linesList.add(lines);
					lines = new TreeSet<Integer>();
				}
			}
		}

		return linesList;
	}

	public String getLine(final int lineNumber) {
		return this.originalLines.get(lineNumber);
	}

	@Override
	public boolean equals(final Object o) {
		if (!(o instanceof SourceFile)) {
			return false;
		}
		final SourceFile target = (SourceFile) o;
		return this.filepath.equals(target.filepath);
	}

	@Override
	public int hashCode() {
		return this.filepath.hashCode();
	}

	@Override
	public int compareTo(final SourceFile file) {
		return this.filepath.compareTo(file.filepath);
	}
}
