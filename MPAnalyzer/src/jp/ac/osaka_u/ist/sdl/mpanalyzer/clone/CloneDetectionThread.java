package jp.ac.osaka_u.ist.sdl.mpanalyzer.clone;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

import jp.ac.osaka_u.ist.sdl.mpanalyzer.Config;
import jp.ac.osaka_u.ist.sdl.mpanalyzer.data.Statement;

public class CloneDetectionThread extends Thread {

	final static private long REVISION = Config.getCloneDetectionRevision();

	final private AtomicInteger index;
	final private String[] paths;
	final private ConcurrentMap<String, List<Statement>> contents;
	final private ConcurrentMap<List<Integer>, Set<Clone>> clones;

	CloneDetectionThread(final AtomicInteger index, final String[] paths,
			final ConcurrentMap<String, List<Statement>> contents,
			final ConcurrentMap<List<Integer>, Set<Clone>> clones) {
		this.index = index;
		this.paths = paths;
		this.contents = contents;
		this.clones = clones;
	}

	@Override
	public void run() {

		while (true) {

			final int i = index.getAndIncrement();
			if (this.paths.length <= i) {
				break;
			}

			final String path1 = this.paths[i];
			final List<Statement> statements1 = this.contents.get(path1);

			for (int j = i + 1; j < this.paths.length; j++) {
				final String path2 = this.paths[j];
				final List<Statement> statements2 = this.contents.get(path2);

				final Matrix matrix = new Matrix(path1, path2, statements1,
						statements2);
				matrix.detect();
			}

			final Matrix matrix = new Matrix(path1, path1, statements1,
					statements1);
			matrix.detect();
		}
	}

	class Matrix {

		final String path1;
		final String path2;
		final List<Statement> statements1;
		final List<Statement> statements2;
		final int[][] cells;

		Matrix(final String path1, final String path2,
				final List<Statement> statements1,
				final List<Statement> statements2) {
			this.path1 = path1;
			this.path2 = path2;
			this.statements1 = statements1;
			this.statements2 = statements2;
			this.cells = new int[statements1.size()][statements2.size()];
		}

		private void detect() {
			final Set<Cell> cloneEndPositions = new HashSet<Cell>();
			final int threshold = Config.getCloneThreshold();
			for (int index1 = 0; index1 < this.statements1.size(); index1++) {
				for (int index2 = 0; index2 < this.statements2.size(); index2++) {

					final Statement statement1 = this.statements1.get(index1);
					final Statement statement2 = this.statements2.get(index2);

					if (statement1.hash != statement2.hash) {
						this.cells[index1][index2] = 0;
					}

					else {

						if ((index1 == index2)
								&& (this.path1.equals(this.path2))) {
							this.cells[index1][index2] = 0;
						}

						else if ((0 == index1) || (0 == index2)) {
							this.cells[index1][index2] = statement1.tokens
									.size();
						}

						else {
							this.cells[index1][index2] = this.cells[index1 - 1][index2 - 1]
									+ statement1.tokens.size();
						}

						if (threshold <= this.cells[index1][index2]) {
							cloneEndPositions.add(new Cell(index1, index2));
							cloneEndPositions.remove(new Cell(index1 - 1,
									index2 - 1));
						}
					}
				}
			}

			for (final Cell cell : cloneEndPositions) {

				final ArrayList<Statement> statements1 = new ArrayList<Statement>();
				final ArrayList<Statement> statements2 = new ArrayList<Statement>();
				for (int back = 0; (0 <= (cell.index1 - back))
						&& (0 <= (cell.index2 - back))
						&& (0 < this.cells[cell.index1 - back][cell.index2
								- back]); back++) {
					statements1
							.add(0, this.statements1.get(cell.index1 - back));
					statements2
							.add(0, this.statements2.get(cell.index2 - back));
				}
				final Clone clone1 = new Clone(this.path1, REVISION,
						statements1);
				final Clone clone2 = new Clone(this.path2, REVISION,
						statements2);

				final ArrayList<Integer> pattern = new ArrayList<Integer>();
				for (final Statement s : statements1) {
					pattern.add(s.hash);
				}

				synchronized (this) {
					Set<Clone> set = CloneDetectionThread.this.clones
							.get(pattern);
					if (null == set) {
						set = new HashSet<Clone>();
						CloneDetectionThread.this.clones.put(pattern, set);
					}
					set.add(clone1);
					set.add(clone2);
				}
			}

		}

		class Cell {
			final int index1;
			final int index2;

			Cell(final int index1, final int index2) {
				this.index1 = index1;
				this.index2 = index2;
			}

			@Override
			public int hashCode() {
				return this.index1 * 10000 + this.index2;
			}

			@Override
			public boolean equals(final Object o) {

				if (o instanceof Cell) {
					final Cell cell = (Cell) o;
					return (this.index1 == cell.index1)
							&& (this.index2 == cell.index2);
				}

				return false;
			}
		}
	}
}
