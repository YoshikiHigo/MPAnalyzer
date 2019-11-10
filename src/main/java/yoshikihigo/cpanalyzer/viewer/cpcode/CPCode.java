package yoshikihigo.cpanalyzer.viewer.cpcode;

import java.awt.Color;
import java.awt.Insets;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;

import yoshikihigo.cpanalyzer.data.ChangePattern;
import yoshikihigo.cpanalyzer.viewer.CODE;
import yoshikihigo.cpanalyzer.viewer.ObservedChangePatterns;
import yoshikihigo.cpanalyzer.viewer.ObservedChangePatterns.CPLABEL;

public class CPCode extends JTextArea implements Observer {

  static public final int TAB_SIZE = 4;

  public final JScrollPane scrollPane;
  public final CODE code;

  public CPCode(final CODE code) {

    this.setTabSize(TAB_SIZE);

    final Insets margin = new Insets(5, 50, 5, 5);
    this.setMargin(margin);
    this.setUI(new CPCodeUI(this, margin));
    this.setText("");
    this.setEditable(false);

    this.scrollPane = new JScrollPane();
    this.scrollPane.setViewportView(this);
    this.scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
    this.scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);

    switch (code) {
      case BEFORE:
        this.scrollPane
            .setBorder(new TitledBorder(new LineBorder(Color.black), "Code BEFORE Change"));
        break;
      case AFTER:
        this.scrollPane
            .setBorder(new TitledBorder(new LineBorder(Color.black), "Code AFTER Change"));
        break;
      default:
        assert false : "here shouldn't be reached!";
        System.exit(0);
    }

    this.code = code;
  }

  @Override
  public void update(final Observable o, final Object arg) {

    if (o instanceof ObservedChangePatterns) {
      final ObservedChangePatterns patterns = (ObservedChangePatterns) o;
      if (patterns.label.equals(CPLABEL.SELECTED)) {

        this.setText("");

        if (patterns.isSet()) {
          final ChangePattern pattern = patterns.get()
              .first();
          final String text = this.code == CODE.BEFORE ? pattern.getChanges()
              .get(0).before.nText
              : pattern.getChanges()
                  .get(0).after.nText;
          if (!text.isEmpty()) {
            this.setText(text);
          } else {
            this.setText("NO CODE");
          }
        }
      }

      else if (patterns.label.equals(CPLABEL.OVERLOOKED)) {

        this.setText("");

        if (patterns.isSet()) {
          final ChangePattern pattern = patterns.get()
              .first();
          final String text = pattern.getChanges()
              .get(0).after.nText;
          if (!text.isEmpty()) {
            this.setText(text);
          } else {
            this.setText("NO CODE");
          }
        }
      }
    }
  }
}
