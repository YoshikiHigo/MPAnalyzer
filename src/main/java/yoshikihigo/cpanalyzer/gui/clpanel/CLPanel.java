package yoshikihigo.cpanalyzer.gui.clpanel;

import java.awt.Color;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Observable;
import java.util.Observer;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;

import yoshikihigo.cpanalyzer.data.Change;
import yoshikihigo.cpanalyzer.data.ChangePattern;
import yoshikihigo.cpanalyzer.gui.ObservedChangePatterns;
import yoshikihigo.cpanalyzer.gui.ObservedChangePatterns.CPLABEL;

public class CLPanel extends JTextArea implements Observer {

  public final JScrollPane scrollPane;
  private SortedMap<String, String> messages;
  private SortedMap<String, String> dates;

  public CLPanel() {
    super("");

    this.setEditable(false);
    this.setLineWrap(true);
    this.setRows(5);

    this.scrollPane = new JScrollPane();
    this.scrollPane.setViewportView(this);
    this.scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
    this.scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);

    this.scrollPane
        .setBorder(new TitledBorder(new LineBorder(Color.black), "Revisions and Commit Logs"));

    this.addMouseListener(new MouseListener() {

      @Override
      public void mouseClicked(final MouseEvent e) {
        final int button = e.getButton();
        if (MouseEvent.BUTTON1 == button) {
          if (e.getClickCount() == 2) {

            final StringBuilder text = new StringBuilder();
            for (final String revision : CLPanel.this.messages.keySet()) {
              text.append(revision);
              text.append(" (");
              text.append(CLPanel.this.dates.get(revision));
              text.append(")");
              text.append(" :  ");
              text.append(CLPanel.this.messages.get(revision));
              text.append(System.getProperty("line.separator"));
              text.append("------------------------------");
              text.append(System.getProperty("line.separator"));
            }

            SwingUtilities.invokeLater(new Runnable() {

              @Override
              public void run() {
                new CLWindow(text.toString());
              }
            });
          }
        }
      }

      @Override
      public void mousePressed(MouseEvent e) {}

      @Override
      public void mouseReleased(MouseEvent e) {}

      @Override
      public void mouseEntered(MouseEvent e) {}

      @Override
      public void mouseExited(MouseEvent e) {}
    });
  }

  @Override
  public void update(final Observable o, final Object arg) {

    if (o instanceof ObservedChangePatterns) {
      final ObservedChangePatterns observedModificationPatterns = (ObservedChangePatterns) o;
      if (observedModificationPatterns.label.equals(CPLABEL.SELECTED)) {
        this.setText("");
        if (observedModificationPatterns.isSet()) {
          final ChangePattern pattern = observedModificationPatterns.get()
              .first();
          this.messages = new TreeMap<String, String>();
          this.dates = new TreeMap<String, String>();
          for (final Change m : pattern.getChanges()) {
            final String id = m.revision.id;
            final String date = m.revision.date;
            final String message = m.revision.message;
            if (!this.messages.containsKey(id)) {
              this.messages.put(id, message);
            }
            if (!this.dates.containsKey(id)) {
              this.dates.put(id, date);
            }
          }
          final StringBuilder text = new StringBuilder();
          for (final String revision : this.messages.keySet()) {
            text.append(revision);
            text.append(" (");
            text.append(this.dates.get(revision));
            text.append(") ");
            text.append(": ");
            text.append(this.messages.get(revision));
          }
          this.setText(text.toString());
        }
      }

      else if (observedModificationPatterns.label.equals(CPLABEL.OVERLOOKED)) {
        this.setText("");
        if (observedModificationPatterns.isSet()) {
          final ChangePattern pattern = observedModificationPatterns.get()
              .first();
          this.messages = new TreeMap<String, String>();
          this.dates = new TreeMap<String, String>();
          for (final Change c : pattern.getChanges()) {
            final String revision = c.revision.id;
            final String date = c.revision.date;
            final String message = c.revision.message;
            if (!this.messages.containsKey(revision)) {
              this.messages.put(revision, message);
            }
            if (!this.dates.containsKey(revision)) {
              this.dates.put(revision, date);
            }
          }
          final StringBuilder text = new StringBuilder();
          for (final String revision : this.messages.keySet()) {
            text.append(revision);
            text.append(" (");
            text.append(this.dates.get(revision));
            text.append(") ");
            text.append(": ");
            text.append(this.messages.get(revision));
          }
          this.setText(text.toString());
        }
      }
    }
  }
}
