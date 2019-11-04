package yoshikihigo.cpanalyzer.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JFrame;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;

import yoshikihigo.cpanalyzer.data.Change;
import yoshikihigo.cpanalyzer.gui.ObservedChanges.CLABEL;
import yoshikihigo.cpanalyzer.gui.ccode.CCode;
import yoshikihigo.cpanalyzer.gui.clist.CList;

public class PatternWindow extends JFrame implements Observer {

  private final JTextArea logDisplay;

  public PatternWindow() {
    super("");

    Dimension d = Toolkit.getDefaultToolkit()
        .getScreenSize();
    this.setSize(new Dimension(d.width - 5, d.height - 27));
    this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);

    ObservedChanges.getInstance(CLABEL.SELECTED)
        .addObserver(this);

    final CList list = new CList();
    ObservedChanges.getInstance(CLABEL.SELECTED)
        .addObserver(list);
    this.logDisplay = new JTextArea();
    this.logDisplay.setEditable(false);
    this.logDisplay.setLineWrap(true);
    this.logDisplay.setBorder(new TitledBorder(new LineBorder(Color.black), "Log message"));
    final JSplitPane topPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
    topPane.setLeftComponent(list.scrollPane);
    topPane.setRightComponent(this.logDisplay);

    final CCode beforeCode = new CCode(CODE.BEFORE);
    ObservedChanges.getInstance(CLABEL.SELECTED)
        .addObserver(beforeCode);
    final CCode afterCode = new CCode(CODE.AFTER);
    ObservedChanges.getInstance(CLABEL.SELECTED)
        .addObserver(afterCode);
    final JSplitPane codePane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
    codePane.add(beforeCode.scrollPane, JSplitPane.LEFT);
    codePane.add(afterCode.scrollPane, JSplitPane.RIGHT);

    final JSplitPane mainPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
    mainPane.setTopComponent(topPane);
    mainPane.setBottomComponent(codePane);
    this.getContentPane()
        .add(mainPane, BorderLayout.CENTER);

    this.setVisible(true);

    topPane.setDividerLocation(
        CList.COLUMN_LENGTH_NUMBER + CList.COLUMN_LENGTH_DATE + CList.COLUMN_LENGTH_PATH
            + CList.COLUMN_LENGTH_BEFORE_POSITION + CList.COLUMN_LENGTH_AFTER_POSITION);
    codePane.setDividerLocation(codePane.getWidth() / 2);
  }

  @Override
  public void update(final Observable o, final Object arg) {
    if (o instanceof ObservedChanges) {
      final ObservedChanges modifications = (ObservedChanges) o;
      if (modifications.label.equals(CLABEL.SELECTED)) {
        if (modifications.isSet()) {
          final Change m = modifications.get()
              .first();
          this.logDisplay.setText(m.revision.message);
        } else {
          this.logDisplay.setText("");
        }
      }
    }
  }
}
