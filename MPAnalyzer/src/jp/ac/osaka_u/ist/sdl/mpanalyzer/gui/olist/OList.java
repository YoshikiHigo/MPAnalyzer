package jp.ac.osaka_u.ist.sdl.mpanalyzer.gui.olist;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;

import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.RowSorter;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.TableRowSorter;

import jp.ac.osaka_u.ist.sdl.mpanalyzer.data.CodeFragment;
import jp.ac.osaka_u.ist.sdl.mpanalyzer.data.ModificationPattern;

public class OList extends JTable {

	static final int COLUMN_LENGTH_ID = 10;
	static final int COLUMN_LENGTH_NAME = 150;
	static final int COLUMN_LENGTH_POSITION = 100;

	final public JScrollPane scrollPane;

	public OList() {

		super();

		this.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		this.setModel(new TreeMap<ModificationPattern, SortedMap<String, SortedSet<CodeFragment>>>());

		this.scrollPane = new JScrollPane();
		this.scrollPane.setViewportView(this);
		this.scrollPane
				.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		this.scrollPane
				.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		this.scrollPane.setBorder(new TitledBorder(new LineBorder(Color.black),
				"Overlooked code fragments"));

		// this.selectionHandler = new MPSelectionHandler();
		// this.getSelectionModel()
		// .addListSelectionListener(this.selectionHandler);
	}

	public void setModel(
			final SortedMap<ModificationPattern, SortedMap<String, SortedSet<CodeFragment>>> oCodefragments) {

		final List<Object[]> list = this.convertToList(oCodefragments);
		final OListModel model = new OListModel(list);
		this.setModel(model);
		final RowSorter<OListModel> sorter = new TableRowSorter<OListModel>(
				model);
		this.setRowSorter(sorter);

		this.getColumnModel().getColumn(0).setPreferredWidth(COLUMN_LENGTH_ID);
		this.getColumnModel().getColumn(1)
				.setPreferredWidth(COLUMN_LENGTH_NAME);
		this.getColumnModel().getColumn(2)
				.setPreferredWidth(COLUMN_LENGTH_POSITION);
	}

	private List<Object[]> convertToList(
			final SortedMap<ModificationPattern, SortedMap<String, SortedSet<CodeFragment>>> oCodefragments) {

		final List<Object[]> list = new ArrayList<Object[]>();
		for (final Entry<ModificationPattern, SortedMap<String, SortedSet<CodeFragment>>> mEntry : oCodefragments
				.entrySet()) {
			final ModificationPattern pattern = mEntry.getKey();
			final SortedMap<String, SortedSet<CodeFragment>> mCodefragments = mEntry
					.getValue();
			for (final Entry<String, SortedSet<CodeFragment>> pEntry : mCodefragments
					.entrySet()) {
				final String path = pEntry.getKey();
				final SortedSet<CodeFragment> pCodefragments = pEntry
						.getValue();
				for (final CodeFragment c : pCodefragments) {
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
}
