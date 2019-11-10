package yoshikihigo.cpanalyzer.viewer.olist;

import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

class OListRenderer extends DefaultTableCellRenderer {

  @Override
  public Component getTableCellRendererComponent(final JTable table, final Object value,
      final boolean isSelected, final boolean hasFocus, final int row, final int column) {

    final DefaultTableCellRenderer renderer =
        (DefaultTableCellRenderer) super.getTableCellRendererComponent(table, value, isSelected,
            hasFocus, row, column);

    final int modelRow = table.convertRowIndexToModel(row);
    final int modelColumn = table.convertColumnIndexToModel(column);
    final OListModel model = (OListModel) table.getModel();
    final Object modelValue = model.getValueAt(modelRow, modelColumn);
    renderer.setText(modelValue.toString());
    switch (modelColumn) {
      case 0: {
        renderer.setHorizontalAlignment(JLabel.CENTER);
        break;
      }
      case 1: {
        renderer.setHorizontalAlignment(JLabel.LEADING);
        break;
      }
      case 2: {
        renderer.setHorizontalAlignment(JLabel.CENTER);
        break;
      }
      default: {
        assert false : "Here shouldn't be reached!";
      }
    }

    return this;
  }
}
