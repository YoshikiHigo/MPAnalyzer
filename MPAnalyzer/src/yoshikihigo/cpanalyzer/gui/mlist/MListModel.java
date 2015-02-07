package yoshikihigo.cpanalyzer.gui.mlist;

import java.util.Collection;

import javax.swing.table.AbstractTableModel;

import yoshikihigo.cpanalyzer.data.Change;

public class MListModel extends AbstractTableModel {

	static public final String[] TITLES = new String[] { "Revision", "Date",
			"Path", "POSITION BEFORE", "POSITION AFTER" };

	final public Change[] modifications;

	public MListModel(final Collection<Change> modifications) {
		this.modifications = modifications.toArray(new Change[] {});
	}

	@Override
	public int getRowCount() {
		return this.modifications.length;
	}

	@Override
	public int getColumnCount() {
		return TITLES.length;
	}

	@Override
	public Object getValueAt(int row, int col) {

		switch (col) {
		case 0:
			return this.modifications[row].revision.number;
		case 1:
			return this.modifications[row].revision.date;
		case 2:
			return this.modifications[row].filepath;
		case 3:
			return this.modifications[row].before.position;
		case 4:
			return this.modifications[row].after.position;
		default:
			assert false : "Here sholdn't be reached!";
			return null;
		}
	}

	@Override
	public Class<?> getColumnClass(int col) {
		switch (col) {
		case 0:
			return Long.class;
		case 1:
		case 2:
		case 3:
		case 4:
			return String.class;
		default:
			assert false : "Here shouldn't be reached!";
			return Object.class;
		}
	}

	@Override
	public String getColumnName(int col) {
		return TITLES[col];
	}
}
