package yoshikihigo.cpanalyzer.gui.cplist;

import java.awt.Component;

import javax.swing.AbstractCellEditor;
import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.TableCellEditor;

public class CPListCellEditor extends AbstractCellEditor implements
		TableCellEditor {

	final JComboBox[] comboBoxes;
	Object value;

	CPListCellEditor(final JComboBox[] comboBoxes) {
		this.value = null;
		this.comboBoxes = comboBoxes;
	}

	@Override
	public Object getCellEditorValue() {
		return this.value;
	}

	@Override
	public boolean stopCellEditing() {
//		this.value = "";
//		if (this.editor instanceof JComboBox) {
//			this.value = ((JComboBox) this.editor).getSelectedItem();
//		} else if (this.editor instanceof JTextField) {
//			this.value = ((JTextField) editor).getText();
//		}
		return super.stopCellEditing();
	}

	@Override
	public Component getTableCellEditorComponent(final JTable table,
			final Object value, final boolean isSelected, final int row,
			final int column) {

		if (12 == column) {
			this.value = this.comboBoxes[row];
			return (JComboBox) this.value;
		} else {
			return (Component) value;
		}
	}
}
