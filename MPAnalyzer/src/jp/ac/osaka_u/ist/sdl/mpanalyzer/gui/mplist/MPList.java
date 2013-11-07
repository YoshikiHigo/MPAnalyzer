package jp.ac.osaka_u.ist.sdl.mpanalyzer.gui.mplist;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Observable;
import java.util.Observer;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
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
import jp.ac.osaka_u.ist.sdl.mpanalyzer.gui.OverlookedWindow;
import jp.ac.osaka_u.ist.sdl.mpanalyzer.gui.PatternWindow;

public class MPList extends JTable implements Observer {

	class MPSelectionHandler implements ListSelectionListener {

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
	}

	class MPListPopupMenu extends JPopupMenu {

		final JMenu exportMenu;
		final JMenu removeMenu;
		final JMenu analyzeMenu;
		final JMenuItem exportCSVItem;
		final JMenuItem removeMPItem;
		final JMenuItem analyzeMPItem;
		final JMenuItem analyzeOverlookedItem;
		final JMenuItem analyzeCloneItem;

		MPListPopupMenu() {
			this.exportMenu = new JMenu("export");
			this.removeMenu = new JMenu("remove");
			this.analyzeMenu = new JMenu("analyze");
			this.exportCSVItem = new JMenuItem("MPs into a CVS file");
			this.removeMPItem = new JMenuItem("this MP from this list");
			this.analyzeMPItem = new JMenuItem("this MP");
			this.analyzeOverlookedItem = new JMenuItem("overlooked code");
			this.analyzeCloneItem = new JMenuItem("clones");

			this.exportMenu.add(this.exportCSVItem);
			this.removeMenu.add(this.removeMPItem);
			this.analyzeMenu.add(this.analyzeMPItem);
			this.analyzeMenu.add(this.analyzeOverlookedItem);
			this.analyzeMenu.add(this.analyzeCloneItem);

			this.add(this.exportMenu);
			this.add(this.removeMenu);
			this.add(this.analyzeMenu);

			this.exportCSVItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {

					// get user home
					JFileChooser fileChooser = new JFileChooser(new File(".")
							.getAbsolutePath());
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
			});

			this.removeMPItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					final MPListModel model = (MPListModel) MPList.this
							.getModel();
					final SortedSet<ModificationPattern> mps = new TreeSet<ModificationPattern>();
					for (final int index : MPList.this.getSelectedRows()) {
						final int modelIndex = MPList.this
								.convertRowIndexToModel(index);
						final ModificationPattern mp = model.patterns[modelIndex];
						mps.add(mp);
					}
					ObservedModificationPatterns.getInstance(MPLABEL.FILTERED)
							.removeAll(mps, MPList.this);
				}
			});

			this.analyzeMPItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					SwingUtilities.invokeLater(new Runnable() {
						@Override
						public void run() {
							new PatternWindow();
						}
					});
				}
			});

			this.analyzeOverlookedItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					SwingUtilities.invokeLater(new Runnable() {
						@Override
						public void run() {
							new OverlookedWindow();
						}
					});
				}
			});

			this.analyzeCloneItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					SwingUtilities.invokeLater(new Runnable() {
						@Override
						public void run() {
							new DetectionWindow();
						}
					});
				}
			});
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

		this.addMouseListener(new MouseListener() {
			public void mouseClicked(MouseEvent e) {
				final int button = e.getButton();
				if (MouseEvent.BUTTON3 == button) {
					final MPListPopupMenu menu = new MPListPopupMenu();
					menu.show(e.getComponent(), e.getX(), e.getY());
				}
			}

			@Override
			public void mouseEntered(MouseEvent e) {
			}

			@Override
			public void mouseExited(MouseEvent e) {
			}

			@Override
			public void mousePressed(MouseEvent e) {
			}

			@Override
			public void mouseReleased(MouseEvent e) {
			}
		});
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
