package yoshikihigo.cpanalyzer.gui.cplist;

import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import yoshikihigo.cpanalyzer.data.ChangePattern;

class CPListRenderer extends DefaultTableCellRenderer {

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column) {

		final DefaultTableCellRenderer renderer = (DefaultTableCellRenderer) super
				.getTableCellRendererComponent(table, value, isSelected,
						hasFocus, row, column);

		final int modelRow = table.convertRowIndexToModel(row);
		final int modelColumn = table.convertColumnIndexToModel(column);
		final CPListModel model = (CPListModel) table.getModel();
		final ChangePattern pattern = model.patterns[modelRow];
		switch (modelColumn) {
		case 0:
			renderer.setHorizontalAlignment(JLabel.RIGHT);
			renderer.setText(Integer.toString(pattern.id));
			break;
		case 1:
			renderer.setHorizontalAlignment(JLabel.RIGHT);
			renderer.setText(Integer.toString(pattern.support));
			break;
		case 2:
			renderer.setHorizontalAlignment(JLabel.RIGHT);
			renderer.setText(Float.toString(pattern.confidence));
			break;
		case 3:
			renderer.setHorizontalAlignment(JLabel.RIGHT);
			renderer.setText(Integer.toString(pattern.getNOD()));
			break;
		case 4:
			renderer.setHorizontalAlignment(JLabel.RIGHT);
			renderer.setText(Integer.toString(pattern.getNOF()));
			break;
		case 5:
			renderer.setHorizontalAlignment(JLabel.RIGHT);
			renderer.setText(Integer.toString(pattern.getLBM()));
			break;
		case 6:
			renderer.setHorizontalAlignment(JLabel.RIGHT);
			renderer.setText(Integer.toString(pattern.getLAM()));
			break;
		case 7:
			renderer.setHorizontalAlignment(JLabel.CENTER);
			renderer.setText(pattern.changeType.toString());
			break;
		case 8:
			renderer.setHorizontalAlignment(JLabel.CENTER);
			renderer.setText(pattern.diffType.toString());
			break;
		case 9:
			renderer.setHorizontalAlignment(JLabel.CENTER);
			renderer.setText(pattern.getOldestRevision().date);
			break;
		case 10:
			renderer.setHorizontalAlignment(JLabel.CENTER);
			renderer.setText(pattern.getLatestRevision().date);
			break;
		case 11:
			renderer.setHorizontalAlignment(JLabel.CENTER);
			break;
		default:
			assert false : "Here shouldn't be reached!";
		}

		return this;
	}
}
