package jp.ac.osaka_u.ist.sdl.mpanalyzer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import jp.ac.osaka_u.ist.sdl.mpanalyzer.Cell.BASE;
import jp.ac.osaka_u.ist.sdl.mpanalyzer.data.CodeFragment;
import jp.ac.osaka_u.ist.sdl.mpanalyzer.data.Modification;
import jp.ac.osaka_u.ist.sdl.mpanalyzer.data.Modification.ChangeType;
import jp.ac.osaka_u.ist.sdl.mpanalyzer.data.Modification.ModificationType;
import jp.ac.osaka_u.ist.sdl.mpanalyzer.data.Revision;
import jp.ac.osaka_u.ist.sdl.mpanalyzer.data.Statement;
import jp.ac.osaka_u.ist.sdl.mpanalyzer.lexer.token.Token;

public class LCS {

	public static final int MAX = 5000;

	private final Cell[][] table;

	public LCS() {
		this.table = new Cell[MAX][MAX];
	}

	public List<Modification> getModifications(final List<Statement> array1,
			final List<Statement> array2, final String filepath,
			final Revision revision) {

		if (array1.isEmpty() || array2.isEmpty()) {
			return new ArrayList<Modification>();
		}

		if (MAX < array1.size() || MAX < array2.size()) {
			System.out.println("large file!");
			return new ArrayList<Modification>();
		}

		if (array1.get(0).hash == array2.get(0).hash) {
			this.table[0][0] = new Cell(1, true, 0, 0, null);
		} else {
			this.table[0][0] = new Cell(0, false, 0, 0, null);
		}
		for (int x = 1; x < array1.size(); x++) {
			if (array1.get(x).hash == array2.get(0).hash) {
				this.table[x][0] = new Cell(1, true, x, 0, null);
			} else {
				this.table[x][0] = new Cell(this.table[x - 1][0].value, false,
						x, 0, BASE.LEFT);
			}
		}
		for (int y = 1; y < array2.size(); y++) {
			if (array1.get(0).hash == array2.get(y).hash) {
				this.table[0][y] = new Cell(1, true, 0, y, null);
			} else {
				this.table[0][y] = new Cell(this.table[0][y - 1].value, false,
						0, y, BASE.UP);
			}
		}
		for (int x = 1; x < array1.size(); x++) {
			for (int y = 1; y < array2.size(); y++) {
				final Cell left = this.table[x - 1][y];
				final Cell up = this.table[x][y - 1];
				final Cell upleft = this.table[x - 1][y - 1];
				if (array1.get(x).hash == array2.get(y).hash) {
					this.table[x][y] = new Cell(upleft.value + 1, true, x, y,
							BASE.UPLEFT);
				} else {
					this.table[x][y] = (left.value >= up.value) ? new Cell(
							left.value, false, x, y, BASE.LEFT) : new Cell(
							up.value, false, x, y, BASE.UP);
				}
			}
		}

		final List<Modification> modifications = new ArrayList<Modification>();
		Cell current = this.table[array1.size() - 1][array2.size() - 1];
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
					final ChangeType changeType = getType(xTokens, yTokens);

					final CodeFragment beforeCodeFragment = new CodeFragment(
							xStatements);
					final CodeFragment afterCodeFragment = new CodeFragment(
							yStatements);
					final ModificationType modificationType = beforeCodeFragment.text
							.isEmpty() ? ModificationType.ADD
							: afterCodeFragment.text.isEmpty() ? ModificationType.DELETE
									: ModificationType.CHANGE;
					final Modification modification = new Modification(
							filepath, beforeCodeFragment, afterCodeFragment,
							revision, modificationType, changeType);
					modifications.add(modification);
					xdiff.clear();
					ydiff.clear();
				}

			} else {
				final BASE previous = current.base;
				if (null != previous) {
					if (BASE.LEFT == previous) {
						xdiff.add(current.x);
					}
					if (BASE.UP == previous) {
						ydiff.add(current.y);
					}
				}
			}

			if (null != current.base) {
				final int baseX = BASE.UP == current.base ? current.x
						: current.x - 1;
				final int baseY = BASE.LEFT == current.base ? current.y
						: current.y - 1;
				current = this.table[baseX][baseY];
			} else {
				break;
			}
		}

		return modifications;
	}

	public static List<Token> getTokens(final List<Statement> statements) {
		final List<Token> tokens = new ArrayList<Token>();
		for (final Statement statement : statements) {
			tokens.addAll(statement.tokens);
		}
		return tokens;
	}

	public static ChangeType getType(final List<Token> tokens1,
			final List<Token> tokens2) {

		if (tokens1.isEmpty() || tokens2.isEmpty()
				|| tokens1.size() != tokens2.size()) {
			return ChangeType.TYPE3;
		}

		{
			final Cell[][] kindTable = new Cell[tokens1.size()][tokens2.size()];
			if (tokens1.get(0).getClass() == tokens2.get(0).getClass()) {
				kindTable[0][0] = new Cell(1, true, 0, 0, null);
			} else {
				kindTable[0][0] = new Cell(0, false, 0, 0, null);
			}
			for (int x = 1; x < tokens1.size(); x++) {
				if (tokens1.get(x).getClass() == tokens2.get(0).getClass()) {
					kindTable[x][0] = new Cell(1, true, x, 0, null);
				} else {
					kindTable[x][0] = new Cell(kindTable[x - 1][0].value,
							false, x, 0, BASE.LEFT);
				}
			}
			for (int y = 1; y < tokens2.size(); y++) {
				if (tokens1.get(0).getClass() == tokens2.get(y).getClass()) {
					kindTable[0][y] = new Cell(1, true, 0, y, null);
				} else {
					kindTable[0][y] = new Cell(kindTable[0][y - 1].value,
							false, 0, y, BASE.UP);
				}
			}
			for (int x = 1; x < tokens1.size(); x++) {
				for (int y = 1; y < tokens2.size(); y++) {
					final Cell left = kindTable[x - 1][y];
					final Cell up = kindTable[x][y - 1];
					final Cell upleft = kindTable[x - 1][y - 1];
					if (tokens1.get(x).getClass() == tokens2.get(y).getClass()) {
						kindTable[x][y] = new Cell(upleft.value + 1, true, x,
								y, BASE.UPLEFT);
					} else {
						kindTable[x][y] = (left.value >= up.value) ? new Cell(
								left.value, false, x, y, BASE.LEFT) : new Cell(
								up.value, false, x, y, BASE.UP);
					}
				}
			}

			Cell cell = kindTable[tokens1.size() - 1][tokens2.size() - 1];
			while (true) {
				if (null != cell.base) {
					if (BASE.UP == cell.base || BASE.LEFT == cell.base) {
						for (int i = 0; i < tokens1.size(); i++) {
							for (int j = 0; j < tokens2.size(); j++) {
								kindTable[i][j] = null;
							}
						}
						return ChangeType.TYPE3;
					}
					cell = kindTable[cell.x - 1][cell.y - 1];
				} else {
					break;
				}
			}

			for (int i = 0; i < tokens1.size(); i++) {
				for (int j = 0; j < tokens2.size(); j++) {
					kindTable[i][j] = null;
				}
			}
		}

		{
			final Cell[][] valueTable = new Cell[tokens1.size()][tokens2.size()];
			if (tokens1.get(0).value == tokens2.get(0).value) {
				valueTable[0][0] = new Cell(1, true, 0, 0, null);
			} else {
				valueTable[0][0] = new Cell(0, false, 0, 0, null);
			}
			for (int x = 1; x < tokens1.size(); x++) {
				if (tokens1.get(x).value == tokens2.get(0).value) {
					valueTable[x][0] = new Cell(1, true, x, 0, null);
				} else {
					valueTable[x][0] = new Cell(valueTable[x - 1][0].value,
							false, x, 0, BASE.LEFT);
				}
			}
			for (int y = 1; y < tokens2.size(); y++) {
				if (tokens1.get(0).value == tokens2.get(y).value) {
					valueTable[0][y] = new Cell(1, true, 0, y, null);
				} else {
					valueTable[0][y] = new Cell(valueTable[0][y - 1].value,
							false, 0, y, BASE.UP);
				}
			}
			for (int x = 1; x < tokens1.size(); x++) {
				for (int y = 1; y < tokens2.size(); y++) {
					final Cell left = valueTable[x - 1][y];
					final Cell up = valueTable[x][y - 1];
					final Cell upleft = valueTable[x - 1][y - 1];
					if (tokens1.get(x).value == tokens2.get(y).value) {
						valueTable[x][y] = new Cell(upleft.value + 1, true, x,
								y, BASE.UPLEFT);
					} else {
						valueTable[x][y] = (left.value >= up.value) ? new Cell(
								left.value, false, x, y, BASE.LEFT) : new Cell(
								up.value, false, x, y, BASE.UP);
					}
				}
			}

			Cell cell = valueTable[tokens1.size() - 1][tokens2.size() - 1];
			while (true) {
				if (null != cell.base) {
					if (BASE.UP == cell.base || BASE.LEFT == cell.base) {
						for (int i = 0; i < tokens1.size(); i++) {
							for (int j = 0; j < tokens2.size(); j++) {
								valueTable[i][j] = null;
							}
						}
						return ChangeType.TYPE2;
					}
					cell = valueTable[cell.x - 1][cell.y - 1];
				} else {
					break;
				}
			}

			for (int i = 0; i < tokens1.size(); i++) {
				for (int j = 0; j < tokens2.size(); j++) {
					valueTable[i][j] = null;
				}
			}
		}

		return ChangeType.TYPE1;
	}
}

class Cell {

	enum BASE {
		LEFT, UP, UPLEFT;
	}

	final public int value;
	final public boolean match;
	final public int x;
	final public int y;
	final public BASE base;

	public Cell(final int value, final boolean match, final int x, final int y,
			final BASE base) {
		this.value = value;
		this.match = match;
		this.x = x;
		this.y = y;
		this.base = base;
	}
}
