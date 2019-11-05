package yoshikihigo.cpanalyzer.gui.olist;

import java.util.Collections;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import yoshikihigo.cpanalyzer.data.ChangePattern;
import yoshikihigo.cpanalyzer.data.Code;

public class OListModel extends AbstractTableModel {

  static public final String[] TITLES = new String[] {"Pattern ID", "Path", "Position"};

  final public List<Object[]> oCodefragments;

  public OListModel(final List<Object[]> oCodefragments) {
    this.oCodefragments = Collections.unmodifiableList(oCodefragments);
  }

  @Override
  public int getRowCount() {
    return this.oCodefragments.size();
  }

  @Override
  public int getColumnCount() {
    return TITLES.length;
  }

  @Override
  public Object getValueAt(int row, int col) {

    switch (col) {
      case 0:
        final ChangePattern pattern = (ChangePattern) this.oCodefragments.get(row)[0];
        return pattern.id;
      case 1:
        final String path = (String) this.oCodefragments.get(row)[1];
        return path;
      case 2:
        final Code cf = (Code) this.oCodefragments.get(row)[2];
        return cf.position;
      default:
        assert false : "Here sholdn't be reached!";
        return null;
    }
  }

  @Override
  public Class<?> getColumnClass(int col) {
    switch (col) {
      case 0:
        return Integer.class;
      case 1:
      case 2:
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
