package yoshikihigo.cpanalyzer.gui.clist;

import java.util.Collection;

import javax.swing.table.AbstractTableModel;

import yoshikihigo.cpanalyzer.data.Change;

public class CListModel extends AbstractTableModel {

	static public final String[] TITLES = new String[] { "Revision", "Date",
			"Path", "POSITION BEFORE", "POSITION AFTER" };

	final public Change[] changes;

	public CListModel(final Collection<Change> changes) {
		this.changes = changes.toArray(new Change[] {});
	}

	@Override
	public int getRowCount() {
		return this.changes.length;
	}

	@Override
	public int getColumnCount() {
		return TITLES.length;
	}

	@Override
	public Object getValueAt(int row, int col) {

		switch (col) {
		case 0:
			return this.changes[row].revision.id;
		case 1:
			return this.changes[row].revision.date;
		case 2:
			return this.changes[row].filepath;
		case 3:
			return this.changes[row].before.position;
		case 4:
			return this.changes[row].after.position;
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
