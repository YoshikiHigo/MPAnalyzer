package yoshikihigo.cpanalyzer.viewer.clist;

import java.awt.Color;
import java.awt.event.MouseEvent;
import java.util.Observable;
import java.util.Observer;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.RowSorter;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableRowSorter;

import yoshikihigo.cpanalyzer.data.Change;
import yoshikihigo.cpanalyzer.data.ChangePattern;
import yoshikihigo.cpanalyzer.viewer.ObservedChangePatterns;
import yoshikihigo.cpanalyzer.viewer.ObservedChanges;
import yoshikihigo.cpanalyzer.viewer.ObservedChangePatterns.CPLABEL;
import yoshikihigo.cpanalyzer.viewer.ObservedChanges.CLABEL;

public class CList extends JTable implements Observer {

  class MPSelectionHandler implements ListSelectionListener {

    @Override
    public void valueChanged(final ListSelectionEvent e) {

      if (e.getValueIsAdjusting()) {
        return;
      }

      final int firstIndex = e.getFirstIndex();
      final int lastIndex = e.getLastIndex();

      final SortedSet<Change> modifications = new TreeSet<Change>();
      for (int index = firstIndex; index <= lastIndex; index++) {

        if (!CList.this.selectionModel.isSelectedIndex(index)) {
          continue;
        }

        final int modelIndex = CList.this.convertRowIndexToModel(index);
        final CListModel model = (CListModel) CList.this.getModel();
        final Change modification = model.changes[modelIndex];
        modifications.add(modification);

      }

      ObservedChanges.getInstance(CLABEL.SELECTED)
          .setAll(modifications, CList.this);
    }
  }

  static final public int COLUMN_LENGTH_NUMBER = 70;
  static final public int COLUMN_LENGTH_DATE = 130;
  static final public int COLUMN_LENGTH_PATH = 400;
  static final public int COLUMN_LENGTH_BEFORE_POSITION = 120;
  static final public int COLUMN_LENGTH_AFTER_POSITION = 120;

  final public JScrollPane scrollPane;
  final private MPSelectionHandler selectionHandler;

  public CList() {

    super();

    this.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

    this.setModel();

    this.scrollPane = new JScrollPane();
    this.scrollPane.setViewportView(this);
    this.scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
    this.scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    this.scrollPane
        .setBorder(new TitledBorder(new LineBorder(Color.black), "Changes in a Specified Pattern"));

    this.selectionHandler = new MPSelectionHandler();
    this.getSelectionModel()
        .addListSelectionListener(this.selectionHandler);
  }

  @Override
  public void update(final Observable o, final Object arg) {

    if (o instanceof ObservedChangePatterns) {
      final ObservedChangePatterns patterns = (ObservedChangePatterns) o;
      if (patterns.label.equals(CPLABEL.FILTERED)) {

        this.getSelectionModel()
            .removeListSelectionListener(this.selectionHandler);
        this.setModel();
        this.getSelectionModel()
            .addListSelectionListener(this.selectionHandler);
      }
    }
  }

  @Override
  public String getToolTipText(final MouseEvent event) {
    final int row = this.rowAtPoint(event.getPoint());
    final int column = this.columnAtPoint(event.getPoint());
    if (1 == column || 2 == column) {
      return (String) this.getModel()
          .getValueAt(row, column);
    } else {
      return ((CListModel) this.getModel()).changes[row].revision.message;
    }
  }

  private void setModel() {

    final SortedSet<ChangePattern> patterns = ObservedChangePatterns.getInstance(CPLABEL.SELECTED)
        .get();
    assert !patterns.isEmpty() : "Condition is unsatisfied.";
    final ChangePattern pattern = patterns.first();

    final CListModel model = new CListModel(pattern.getChanges());
    this.setModel(model);
    final RowSorter<CListModel> sorter = new TableRowSorter<CListModel>(model);
    this.setRowSorter(sorter);
    for (int i = 0; i < this.getColumnCount(); i++) {
      this.getColumnModel()
          .getColumn(i)
          .setCellRenderer(new CListRenderer());
    }
    this.getColumnModel()
        .getColumn(0)
        .setPreferredWidth(COLUMN_LENGTH_NUMBER);
    this.getColumnModel()
        .getColumn(1)
        .setPreferredWidth(COLUMN_LENGTH_DATE);
    this.getColumnModel()
        .getColumn(2)
        .setPreferredWidth(COLUMN_LENGTH_PATH);
    this.getColumnModel()
        .getColumn(3)
        .setPreferredWidth(COLUMN_LENGTH_BEFORE_POSITION);
    this.getColumnModel()
        .getColumn(4)
        .setPreferredWidth(COLUMN_LENGTH_AFTER_POSITION);
  }
}
