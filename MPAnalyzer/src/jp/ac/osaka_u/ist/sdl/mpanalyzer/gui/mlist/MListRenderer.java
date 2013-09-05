package jp.ac.osaka_u.ist.sdl.mpanalyzer.gui.mlist;

import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import jp.ac.osaka_u.ist.sdl.mpanalyzer.data.Modification;

class MListRenderer extends DefaultTableCellRenderer {

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column) {

		final DefaultTableCellRenderer renderer = (DefaultTableCellRenderer) super
				.getTableCellRendererComponent(table, value, isSelected,
						hasFocus, row, column);

		final int modelRow = table.convertRowIndexToModel(row);
		final int modelColumn = table.convertColumnIndexToModel(column);
		final MListModel model = (MListModel) table.getModel();
		final Modification modification = model.modifications[modelRow];
		switch (modelColumn) {
		case 0:
			renderer.setHorizontalAlignment(JLabel.RIGHT);
			renderer.setText(Long.toString(modification.revision.number));
			break;
		case 1:
			renderer.setHorizontalAlignment(JLabel.RIGHT);
			renderer.setText(modification.revision.date);
			break;
		case 2:
			renderer.setHorizontalAlignment(JLabel.RIGHT);
			renderer.setText(modification.filepath);
			break;
		default:
			assert false : "Here shouldn't be reached!";
		}

		return this;
	}
}