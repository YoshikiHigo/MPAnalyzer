package yoshikihigo.cpanalyzer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import yoshikihigo.cpanalyzer.data.Change;
import yoshikihigo.cpanalyzer.data.Change.ChangeType;
import yoshikihigo.cpanalyzer.data.Change.DiffType;
import yoshikihigo.cpanalyzer.data.Code;
import yoshikihigo.cpanalyzer.data.Revision;
import yoshikihigo.cpanalyzer.data.Statement;
import yoshikihigo.cpanalyzer.lexer.token.Token;

public class LCS {

	public static List<Change> getChanges(final List<Statement> array1,
			final List<Statement> array2, final String software,
			final String filepath, final String author, final Revision revision) {

		if (array1.isEmpty() || array2.isEmpty()) {
			return new ArrayList<Change>();
		}

		final int large = CPAConfig.getInstance().getLARGE();
		if (large < array1.size() || large < array2.size()) {
			System.out.println("large file. (" + array1.size() + " x "
					+ array2.size() + ")");
			return new ArrayList<Change>();
		}

		final Cell[][] table = new Cell[array1.size()][array2.size()];
		if (Arrays.equals(array1.get(0).hash, array2.get(0).hash)) {
			table[0][0] = new Cell(1, true, 0, 0, null);
		} else {
			table[0][0] = new Cell(0, false, 0, 0, null);
		}
		for (int x = 1; x < array1.size(); x++) {
			if (Arrays.equals(array1.get(x).hash, array2.get(0).hash)) {
				table[x][0] = new Cell(1, true, x, 0, null);
			} else {
				table[x][0] = new Cell(table[x - 1][0].value, false, x, 0,
						table[x - 1][0]);
			}
		}
		for (int y = 1; y < array2.size(); y++) {
			if (Arrays.equals(array1.get(0).hash, array2.get(y).hash)) {
				table[0][y] = new Cell(1, true, 0, y, null);
			} else {
				table[0][y] = new Cell(table[0][y - 1].value, false, 0, y,
						table[0][y - 1]);
			}
		}
		for (int x = 1; x < array1.size(); x++) {
			for (int y = 1; y < array2.size(); y++) {
				final Cell left = table[x - 1][y];
				final Cell up = table[x][y - 1];
				final Cell upleft = table[x - 1][y - 1];
				if (Arrays.equals(array1.get(x).hash, array2.get(y).hash)) {
					table[x][y] = new Cell(upleft.value + 1, true, x, y, upleft);
				} else {
					table[x][y] = (left.value >= up.value) ? new Cell(
							left.value, false, x, y, left) : new Cell(up.value,
							false, x, y, up);
				}
			}
		}

		final List<Change> changes = new ArrayList<Change>();
		Cell current = table[array1.size() - 1][array2.size() - 1];
		final SortedSet<Integer> xdiff = new TreeSet<Integer>();
		final SortedSet<Integer> ydiff = new TreeSet<Integer>();
		while (true) {

			if (current.match) {

				if (!xdiff.isEmpty() || !ydiff.isEmpty()) {
					final List<Statement> xStatements = xdiff.isEmpty() ? Collections
							.<Statement> emptyList() : array1.subList(
							xdiff.first(), xdiff.last() + 1);
					final List<Statement> yStatements = ydiff.isEmpty() ? Collections
							.<Statement> emptyList() : array2.subList(
							ydiff.first(), ydiff.last() + 1);
					final List<Token> xTokens = getTokens(xStatements);
					final List<Token> yTokens = getTokens(yStatements);
					final DiffType diffType = getType(xTokens, yTokens);

					final Code beforeCodeFragment = new Code(software,
							xStatements);
					final Code afterCodeFragment = new Code(software,
							yStatements);
					final ChangeType changeType = beforeCodeFragment.text
							.isEmpty() ? ChangeType.ADD
							: afterCodeFragment.text.isEmpty() ? ChangeType.DELETE
									: ChangeType.REPLACE;
					final Change change = new Change(software, filepath,
							author, beforeCodeFragment, afterCodeFragment,
							revision, changeType, diffType);
					changes.add(change);
					xdiff.clear();
					ydiff.clear();
				}

			} else {
				final Cell previous = current.base;
				if (null != previous) {
					if (previous.x < current.x) {
						xdiff.add(current.x);
					}
					if (previous.y < current.y) {
						ydiff.add(current.y);
					}
				}
			}

			if (null != current.base) {
				current = current.base;
			} else {
				break;
			}
		}

		return changes;
	}

	public static List<Token> getTokens(final List<Statement> statements) {
		final List<Token> tokens = new ArrayList<Token>();
		for (final Statement statement : statements) {
			tokens.addAll(statement.tokens);
		}
		return tokens;
	}

	public static DiffType getType(final List<Token> tokens1,
			final List<Token> tokens2) {

		if (tokens1.isEmpty() || tokens2.isEmpty()
				|| tokens1.size() != tokens2.size()) {
			return DiffType.TYPE3;
		}

		final Cell[][] table = new Cell[tokens1.size()][tokens2.size()];

		{

			if (tokens1.get(0).getClass() == tokens2.get(0).getClass()) {
				table[0][0] = new Cell(1, true, 0, 0, null);
			} else {
				table[0][0] = new Cell(0, false, 0, 0, null);
			}
			for (int x = 1; x < tokens1.size(); x++) {
				if (tokens1.get(x).getClass() == tokens2.get(0).getClass()) {
					table[x][0] = new Cell(1, true, x, 0, null);
				} else {
					table[x][0] = new Cell(table[x - 1][0].value, false, x, 0,
							table[x - 1][0]);
				}
			}
			for (int y = 1; y < tokens2.size(); y++) {
				if (tokens1.get(0).getClass() == tokens2.get(y).getClass()) {
					table[0][y] = new Cell(1, true, 0, y, null);
				} else {
					table[0][y] = new Cell(table[0][y - 1].value, false, 0, y,
							table[0][y - 1]);
				}
			}
			for (int x = 1; x < tokens1.size(); x++) {
				for (int y = 1; y < tokens2.size(); y++) {
					final Cell left = table[x - 1][y];
					final Cell up = table[x][y - 1];
					final Cell upleft = table[x - 1][y - 1];
					if (tokens1.get(x).getClass() == tokens2.get(y).getClass()) {
						table[x][y] = new Cell(upleft.value + 1, true, x, y,
								upleft);
					} else {
						table[x][y] = (left.value >= up.value) ? new Cell(
								left.value, false, x, y, left) : new Cell(
								up.value, false, x, y, up);
					}
				}
			}

			Cell cell = table[tokens1.size() - 1][tokens2.size() - 1];
			while (true) {
				if (null != cell.base) {
					Cell previous = cell.base;
					if (previous.x == cell.x || previous.y == cell.y) {
						return DiffType.TYPE3;
					}
					cell = previous;
				} else {
					break;
				}
			}
		}

		{
			if (tokens1.get(0).value == tokens2.get(0).value) {
				table[0][0] = new Cell(1, true, 0, 0, null);
			} else {
				table[0][0] = new Cell(0, false, 0, 0, null);
			}
			for (int x = 1; x < tokens1.size(); x++) {
				if (tokens1.get(x).value == tokens2.get(0).value) {
					table[x][0] = new Cell(1, true, x, 0, null);
				} else {
					table[x][0] = new Cell(table[x - 1][0].value, false, x, 0,
							table[x - 1][0]);
				}
			}
			for (int y = 1; y < tokens2.size(); y++) {
				if (tokens1.get(0).value == tokens2.get(y).value) {
					table[0][y] = new Cell(1, true, 0, y, null);
				} else {
					table[0][y] = new Cell(table[0][y - 1].value, false, 0, y,
							table[0][y - 1]);
				}
			}
			for (int x = 1; x < tokens1.size(); x++) {
				for (int y = 1; y < tokens2.size(); y++) {
					final Cell left = table[x - 1][y];
					final Cell up = table[x][y - 1];
					final Cell upleft = table[x - 1][y - 1];
					if (tokens1.get(x).value == tokens2.get(y).value) {
						table[x][y] = new Cell(upleft.value + 1, true, x, y,
								upleft);
					} else {
						table[x][y] = (left.value >= up.value) ? new Cell(
								left.value, false, x, y, left) : new Cell(
								up.value, false, x, y, up);
					}
				}
			}

			Cell cell = table[tokens1.size() - 1][tokens2.size() - 1];
			while (true) {
				if (null != cell.base) {
					Cell previous = cell.base;
					if (previous.x == cell.x || previous.y == cell.y) {
						return DiffType.TYPE2;
					}
					cell = previous;
				} else {
					break;
				}
			}
		}

		return DiffType.TYPE1;
	}
}

class Cell {

	final public int value;
	final public boolean match;
	final public int x;
	final public int y;
	final public Cell base;

	public Cell(final int value, final boolean match, final int x, final int y,
			final Cell base) {
		this.value = value;
		this.match = match;
		this.x = x;
		this.y = y;
		this.base = base;
	}
}
