package yoshikihigo.cpanalyzer.gui.olist;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Observable;
import java.util.Observer;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

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

import yoshikihigo.cpanalyzer.data.ChangePattern;
import yoshikihigo.cpanalyzer.data.Code;
import yoshikihigo.cpanalyzer.gui.ObservedCodeFragments;
import yoshikihigo.cpanalyzer.gui.ObservedFiles;
import yoshikihigo.cpanalyzer.gui.ObservedChangePatterns;
import yoshikihigo.cpanalyzer.gui.PatternWindow;
import yoshikihigo.cpanalyzer.gui.ObservedCodeFragments.CFLABEL;
import yoshikihigo.cpanalyzer.gui.ObservedFiles.FLABEL;
import yoshikihigo.cpanalyzer.gui.ObservedChangePatterns.MPLABEL;

public class OList extends JTable implements Observer {

	static final int COLUMN_LENGTH_ID = 10;
	static final int COLUMN_LENGTH_NAME = 200;
	static final int COLUMN_LENGTH_POSITION = 50;

	final public JScrollPane scrollPane;
	final private OSelectionHandler selectionHandler;

	class OSelectionHandler implements ListSelectionListener {

		@Override
		public void valueChanged(final ListSelectionEvent e) {

			if (e.getValueIsAdjusting()) {
				return;
			}

			final int firstIndex = e.getFirstIndex();
			final int lastIndex = e.getLastIndex();

			final SortedSet<Code> codefragments = new TreeSet<Code>();
			final SortedSet<String> paths = new TreeSet<String>();
			final SortedSet<ChangePattern> mps = new TreeSet<ChangePattern>();

			for (int index = firstIndex; index <= lastIndex; index++) {

				if (!OList.this.selectionModel.isSelectedIndex(index)) {
					continue;
				}

				final int modelIndex = OList.this.convertRowIndexToModel(index);
				final OListModel model = (OListModel) OList.this.getModel();
				final Object[] element = model.oCodefragments.get(modelIndex);

				final ChangePattern mp = (ChangePattern) element[0];
				mps.add(mp);

				final String path = (String) element[1];
				paths.add(path);

				final Code codefragment = (Code) element[2];
				codefragments.add(codefragment);
			}

			if (!codefragments.isEmpty()) {
				ObservedCodeFragments.getInstance(CFLABEL.OVERLOOKED).setAll(
						codefragments, OList.this);
			} else {
				ObservedCodeFragments.getInstance(CFLABEL.OVERLOOKED).clear(
						OList.this);
			}

			if (!paths.isEmpty()) {
				ObservedFiles.getInstance(FLABEL.OVERLOOKED).setAll(paths,
						OList.this);
			} else {
				ObservedFiles.getInstance(FLABEL.OVERLOOKED).clear(OList.this);
			}

			if (!mps.isEmpty()) {
				ObservedChangePatterns.getInstance(MPLABEL.OVERLOOKED)
						.setAll(mps, OList.this);
			} else {
				ObservedChangePatterns.getInstance(MPLABEL.OVERLOOKED)
						.clear(OList.this);
			}
		}
	}

	class OListPopupMenu extends JPopupMenu {

		OListPopupMenu() {
			final JMenuItem seeItem = new JMenuItem(
					"see the details of the selected MP");
			this.add(seeItem);

			seeItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {

					final OListModel model = (OListModel) OList.this.getModel();
					final SortedSet<ChangePattern> mps = new TreeSet<ChangePattern>();
					for (final int index : OList.this.getSelectedRows()) {
						final int modelIndex = OList.this
								.convertRowIndexToModel(index);
						final ChangePattern mp = (ChangePattern) model.oCodefragments
								.get(modelIndex)[0];
						mps.add(mp);
					}
					ObservedChangePatterns.getInstance(MPLABEL.SELECTED)
							.setAll(mps, OList.this);

					SwingUtilities.invokeLater(new Runnable() {
						@Override
						public void run() {
							new PatternWindow();
						}
					});
				}
			});
		}
	}

	public OList() {

		super();

		this.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		this.setModel(new TreeMap<ChangePattern, SortedMap<String, SortedSet<Code>>>());

		this.scrollPane = new JScrollPane();
		this.scrollPane.setViewportView(this);
		this.scrollPane
				.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		this.scrollPane
				.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		this.scrollPane.setBorder(new TitledBorder(new LineBorder(Color.black),
				"Overlooked code fragments"));

		this.selectionHandler = new OSelectionHandler();
		this.getSelectionModel()
				.addListSelectionListener(this.selectionHandler);

		this.addMouseListener(new MouseListener() {
			public void mouseClicked(MouseEvent e) {
				final int button = e.getButton();
				if (MouseEvent.BUTTON3 == button) {
					final OListPopupMenu menu = new OListPopupMenu();
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

	public void setModel(
			final SortedMap<ChangePattern, SortedMap<String, SortedSet<Code>>> oCodefragments) {

		final List<Object[]> list = this.convertToList(oCodefragments);
		final OListModel model = new OListModel(list);
		this.setModel(model);
		final RowSorter<OListModel> sorter = new TableRowSorter<OListModel>(
				model);
		this.setRowSorter(sorter);

		for (int i = 0; i < this.getColumnCount(); i++) {
			this.getColumnModel().getColumn(i)
					.setCellRenderer(new OListRenderer());
		}

		this.getColumnModel().getColumn(0).setPreferredWidth(COLUMN_LENGTH_ID);
		this.getColumnModel().getColumn(1)
				.setPreferredWidth(COLUMN_LENGTH_NAME);
		this.getColumnModel().getColumn(2)
				.setPreferredWidth(COLUMN_LENGTH_POSITION);
	}

	private List<Object[]> convertToList(
			final SortedMap<ChangePattern, SortedMap<String, SortedSet<Code>>> oCodefragments) {

		final List<Object[]> list = new ArrayList<Object[]>();
		for (final Entry<ChangePattern, SortedMap<String, SortedSet<Code>>> mEntry : oCodefragments
				.entrySet()) {
			final ChangePattern pattern = mEntry.getKey();
			final SortedMap<String, SortedSet<Code>> mCodefragments = mEntry
					.getValue();
			for (final Entry<String, SortedSet<Code>> pEntry : mCodefragments
					.entrySet()) {
				final String path = pEntry.getKey();
				final SortedSet<Code> pCodefragments = pEntry
						.getValue();
				for (final Code c : pCodefragments) {
					final Object[] element = new Object[3];
					element[0] = pattern;
					element[1] = path;
					element[2] = c;
					list.add(element);
				}
			}
		}
		return list;
	}

	@Override
	public void update(final Observable o, final Object arg) {

		this.getSelectionModel().removeListSelectionListener(
				this.selectionHandler);

		this.getSelectionModel()
				.addListSelectionListener(this.selectionHandler);
	}
}
