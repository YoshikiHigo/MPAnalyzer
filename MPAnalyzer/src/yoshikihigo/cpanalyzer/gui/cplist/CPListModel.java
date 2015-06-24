package yoshikihigo.cpanalyzer.gui.cplist;

import java.util.Collection;

import javax.swing.JComboBox;
import javax.swing.table.AbstractTableModel;

import yoshikihigo.cpanalyzer.data.ChangePattern;
import yoshikihigo.cpanalyzer.data.Revision;

public class CPListModel extends AbstractTableModel {

	static public final String[] TITLES = new String[] { "ID", "SUPPORT",
			"CONFIDENCE", "NOD", "NOR", "NOF", "LBM", "LAM", "MTYPE", "CTYPE",
			"START", "END", "CATEGORY" };

	final public ChangePattern[] patterns;
	final public JComboBox[] comboBoxes;

	public CPListModel(final Collection<ChangePattern> patterns,
			final JComboBox[] comboBoxes) {
		this.patterns = patterns.toArray(new ChangePattern[] {});
		this.comboBoxes = comboBoxes;
	}

	@Override
	public int getRowCount() {
		return this.patterns.length;
	}

	@Override
	public int getColumnCount() {
		return TITLES.length;
	}

	@Override
	public Object getValueAt(int row, int col) {

		switch (col) {
		case 0:
			return this.patterns[row].id;
		case 1:
			return this.patterns[row].support;
		case 2:
			return this.patterns[row].confidence;
		case 3:
			return this.patterns[row].getNOD();
		case 4:
			return this.patterns[row].getNOR();
		case 5:
			return this.patterns[row].getNOF();
		case 6:
			return this.patterns[row].getLBM();
		case 7:
			return this.patterns[row].getLAM();
		case 8:
			return this.patterns[row].changeType.toString();
		case 9:
			return this.patterns[row].diffType.toString();
		case 10:
			return this.patterns[row].getRevisions().first();
		case 11:
			return this.patterns[row].getRevisions().last();
		case 12:
			return this.comboBoxes[row].getSelectedItem().toString();
		default:
			assert false : "Here sholdn't be reached!";
			return null;
		}
	}

	@Override
	public boolean isCellEditable(final int row, final int column) {
		return 12 == column;
	}

	@Override
	public Class<?> getColumnClass(int col) {
		switch (col) {
		case 2:
			return Float.class;
		case 0:
		case 1:
		case 3:
		case 4:
		case 5:
		case 6:
		case 7:
			return Integer.class;
		case 8:
		case 9:
			return String.class;
		case 10:
		case 11:
			return Revision.class;
		case 12:
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

	public String getDataAsCSV() {

		final StringBuilder text = new StringBuilder();

		for (final String title : TITLES) {
			text.append(title);
			text.append(",");
		}
		text.append(System.getProperty("line.separator"));
		for (int i = 0; i < this.getRowCount(); i++) {
			for (int j = 0; j < this.getColumnCount(); j++) {
				text.append("\"");
				text.append(this.getValueAt(i, j));
				text.append("\"");
				text.append(",");
			}
			text.append(System.getProperty("line.separator"));
		}

		return text.toString();
	}
}
