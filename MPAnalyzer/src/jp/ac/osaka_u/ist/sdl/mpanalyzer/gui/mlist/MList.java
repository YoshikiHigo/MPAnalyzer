package jp.ac.osaka_u.ist.sdl.mpanalyzer.gui.mlist;

import java.awt.Color;
import java.awt.event.MouseEvent;
import java.util.Observable;
import java.util.Observer;
import java.util.SortedSet;

import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.RowSorter;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableRowSorter;

import jp.ac.osaka_u.ist.sdl.mpanalyzer.data.Modification;
import jp.ac.osaka_u.ist.sdl.mpanalyzer.data.ModificationPattern;
import jp.ac.osaka_u.ist.sdl.mpanalyzer.gui.ObservedModificationPatterns;
import jp.ac.osaka_u.ist.sdl.mpanalyzer.gui.ObservedModifications;
import jp.ac.osaka_u.ist.sdl.mpanalyzer.gui.ObservedModificationPatterns.MPLABEL;
import jp.ac.osaka_u.ist.sdl.mpanalyzer.gui.ObservedModifications.MLABEL;

public class MList extends JTable implements Observer {

	class MPSelectionHandler implements ListSelectionListener {

		@Override
		public void valueChanged(final ListSelectionEvent e) {

			if (e.getValueIsAdjusting()) {
				return;
			}

			final int firstIndex = e.getFirstIndex();
			final int lastIndex = e.getLastIndex();

			for (int i = firstIndex; i <= lastIndex; i++) {

				final int modelIndex = MList.this.convertRowIndexToModel(i);
				final MListModel model = (MListModel) MList.this.getModel();
				final Modification modification = model.modifications[modelIndex];

				ObservedModifications.getInstance(MLABEL.SELECTED).set(
						modification, MList.this);
			}
		}
	}

	static final int COLUMN_LENGTH_NUMBER = 50;
	static final int COLUMN_LENGTH_DATE = 100;

	final public JScrollPane scrollPane;
	final private MPSelectionHandler selectionHandler;

	final private ModificationPattern pattern;

	public MList() {

		super();

		final SortedSet<ModificationPattern> patterns = ObservedModificationPatterns
				.getInstance(MPLABEL.SELECTED).get();
		assert !patterns.isEmpty() : "Condition is unsatisfied.";
		this.pattern = patterns.first();

		this.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		this.setModel();

		this.scrollPane = new JScrollPane();
		this.scrollPane.setViewportView(this);
		this.scrollPane
				.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		this.scrollPane
				.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		this.scrollPane.setBorder(new TitledBorder(new LineBorder(Color.black),
				"Modifications in a Specified Pattern"));

		this.selectionHandler = new MPSelectionHandler();
		this.getSelectionModel()
				.addListSelectionListener(this.selectionHandler);
	}

	@Override
	public void update(final Observable o, final Object arg) {

		if (o instanceof ObservedModificationPatterns) {
			final ObservedModificationPatterns patterns = (ObservedModificationPatterns) o;
			if (patterns.label.equals(MPLABEL.FILTERED)) {

				this.getSelectionModel().removeListSelectionListener(
						this.selectionHandler);
				this.setModel();
				this.getSelectionModel().addListSelectionListener(
						this.selectionHandler);
			}
		}
	}

	@Override
	public String getToolTipText(final MouseEvent event) {
		final int row = this.rowAtPoint(event.getPoint());
		final int column = this.columnAtPoint(event.getPoint());
		if (1 == column || 2 == column) {
			return (String) this.getModel().getValueAt(row, column);
		} else {
			return ((MListModel) this.getModel()).modifications[row].revision.message;
		}
	}

	private void setModel() {
		final MListModel model = new MListModel(this.pattern.getModifications());
		this.setModel(model);
		final RowSorter<MListModel> sorter = new TableRowSorter<MListModel>(
				model);
		this.setRowSorter(sorter);
		for (int i = 0; i < this.getColumnCount(); i++) {
			this.getColumnModel().getColumn(i)
					.setCellRenderer(new MListRenderer());
		}
		this.getColumnModel().getColumn(0)
				.setPreferredWidth(COLUMN_LENGTH_NUMBER);
		this.getColumnModel().getColumn(1)
				.setPreferredWidth(COLUMN_LENGTH_DATE);
	}
}
