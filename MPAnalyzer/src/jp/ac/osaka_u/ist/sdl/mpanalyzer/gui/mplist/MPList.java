package jp.ac.osaka_u.ist.sdl.mpanalyzer.gui.mplist;

import java.awt.Color;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Observable;
import java.util.Observer;
import java.util.SortedSet;

import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.RowSorter;
import javax.swing.SwingUtilities;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableRowSorter;

import jp.ac.osaka_u.ist.sdl.mpanalyzer.data.ModificationPattern;
import jp.ac.osaka_u.ist.sdl.mpanalyzer.gui.DetectionWindow;
import jp.ac.osaka_u.ist.sdl.mpanalyzer.gui.ObservedModificationPatterns;
import jp.ac.osaka_u.ist.sdl.mpanalyzer.gui.ObservedModificationPatterns.MPLABEL;
import jp.ac.osaka_u.ist.sdl.mpanalyzer.gui.PatternWindow;

public class MPList extends JTable implements Observer {

	class MPSelectionHandler extends MouseAdapter implements
			ListSelectionListener {

		@Override
		public void valueChanged(final ListSelectionEvent e) {

			if (e.getValueIsAdjusting()) {
				return;
			}

			final int firstIndex = e.getFirstIndex();
			final int lastIndex = e.getLastIndex();

			for (int i = firstIndex; i <= lastIndex; i++) {

				final int modelIndex = MPList.this.convertRowIndexToModel(i);
				final MPListModel model = (MPListModel) MPList.this.getModel();
				final ModificationPattern pattern = model.patterns[modelIndex];

				ObservedModificationPatterns.getInstance(MPLABEL.SELECTED).set(
						pattern, MPList.this);
			}
		}

		@Override
		public void mouseClicked(final MouseEvent e) {

			final int modifier = e.getModifiers();
			if ((modifier & MouseEvent.BUTTON1_MASK) != 0) {
				if (e.getClickCount() == 2) {

					if (e.isShiftDown()) {
						SwingUtilities.invokeLater(new Runnable() {
							@Override
							public void run() {
								new DetectionWindow();
							}
						});
					} else {
						SwingUtilities.invokeLater(new Runnable() {
							@Override
							public void run() {
								new PatternWindow();
							}
						});
					}
				}
			} else if ((modifier & MouseEvent.BUTTON3_MASK) != 0) {

				// get user home
				JFileChooser fileChooser = new JFileChooser(
						new File(".").getAbsolutePath());
				fileChooser.setAcceptAllFileFilterUsed(false);
				fileChooser.setMultiSelectionEnabled(true);
				fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

				// show dialog
				int returnValue = fileChooser.showSaveDialog(MPList.this);
				if (returnValue == JFileChooser.APPROVE_OPTION) {

					try {
						final File file = fileChooser.getSelectedFile();
						final BufferedWriter bw = new BufferedWriter(
								new FileWriter(file));

						final MPListModel model = (MPListModel) MPList.this
								.getModel();
						final String data = model.getDataAsCSV();
						bw.write(data);
						bw.close();

					} catch (IOException evt) {
						evt.printStackTrace();
					}

				} else if (returnValue == JFileChooser.CANCEL_OPTION) {
				} else if (returnValue == JFileChooser.ERROR_OPTION) {
				}
			}
		}
	}

	static final int COLUMN_LENGTH_SUPPORT = 110;
	static final int COLUMN_LENGTH_CONFIDENCE = 110;
	static final int COLUMN_LENGTH_START = 170;
	static final int COLUMN_LENGTH_END = 170;
	static final int COLUMN_LENGTH_CATEGORY = 170;

	final public JScrollPane scrollPane;
	final private MPSelectionHandler selectionHandler;

	public MPList() {

		super();

		this.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		this.setModel();

		this.scrollPane = new JScrollPane();
		this.scrollPane.setViewportView(this);
		this.scrollPane
				.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		this.scrollPane
				.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		this.scrollPane.setBorder(new TitledBorder(new LineBorder(Color.black),
				"Modification Pattern List"));

		this.selectionHandler = new MPSelectionHandler();
		this.getSelectionModel()
				.addListSelectionListener(this.selectionHandler);
		this.addMouseListener(this.selectionHandler);
	}

	@Override
	public void update(final Observable o, final Object arg) {

		if (o instanceof ObservedModificationPatterns) {
			final ObservedModificationPatterns patterns = (ObservedModificationPatterns) o;
			if (patterns.label.equals(MPLABEL.FILTERED)) {

				this.getSelectionModel().removeListSelectionListener(
						this.selectionHandler);
				this.removeMouseListener(this.selectionHandler);
				this.setModel();
				this.getSelectionModel().addListSelectionListener(
						this.selectionHandler);
				this.addMouseListener(this.selectionHandler);
			}
		}
	}

	private void setModel() {
		final SortedSet<ModificationPattern> patterns = ObservedModificationPatterns
				.getInstance(MPLABEL.FILTERED).get();

		final JComboBox[] comboBoxes = new JComboBox[patterns.size()];
		for (int i = 0; i < comboBoxes.length; i++) {
			comboBoxes[i] = new JComboBox();
			comboBoxes[i].addItem("not selected");
			comboBoxes[i].addItem("Refactoring");
			comboBoxes[i].addItem("Enhancement");
			comboBoxes[i].addItem("Bug fix");
			comboBoxes[i].addItem("Comment");
		}

		final MPListModel model = new MPListModel(patterns, comboBoxes);
		this.setModel(model);
		final RowSorter<MPListModel> sorter = new TableRowSorter<MPListModel>(
				model);
		this.setRowSorter(sorter);
		for (int i = 0; i < this.getColumnCount(); i++) {
			this.getColumnModel().getColumn(i)
					.setCellRenderer(new MPListRenderer());
		}
		this.getColumnModel().getColumn(1)
				.setPreferredWidth(COLUMN_LENGTH_SUPPORT);
		this.getColumnModel().getColumn(2)
				.setPreferredWidth(COLUMN_LENGTH_CONFIDENCE);
		this.getColumnModel().getColumn(10)
				.setPreferredWidth(COLUMN_LENGTH_START);
		this.getColumnModel().getColumn(11)
				.setPreferredWidth(COLUMN_LENGTH_END);
		if (10 <= model.getColumnCount()) {
			this.getColumnModel().getColumn(12)
					.setCellEditor(new MPListCellEditor(comboBoxes));
			this.getColumnModel().getColumn(12)
					.setPreferredWidth(COLUMN_LENGTH_CATEGORY);
		}
	}
}
