package yoshikihigo.cpanalyzer.gui.ccode;

import java.awt.Color;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Element;

import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.internal.io.fs.FSRepositoryFactory;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import org.tmatesoft.svn.core.wc.SVNRevision;
import org.tmatesoft.svn.core.wc.SVNWCClient;

import yoshikihigo.cpanalyzer.CPAConfig;
import yoshikihigo.cpanalyzer.data.Change;
import yoshikihigo.cpanalyzer.data.Revision;
import yoshikihigo.cpanalyzer.gui.CODE;
import yoshikihigo.cpanalyzer.gui.ObservedChanges;
import yoshikihigo.cpanalyzer.gui.ObservedChanges.CLABEL;

public class CCode extends JTextArea implements Observer {

  static public final int TAB_SIZE = 4;

  public final JScrollPane scrollPane;
  public final CODE code;

  private Change change;

  public CCode(final CODE code) {

    this.setTabSize(TAB_SIZE);

    final Insets margin = new Insets(5, 50, 5, 5);
    this.setMargin(margin);
    this.setUI(new CCodeUI(code, new HashSet<Integer>(), this, margin));
    this.setText("");
    this.setEditable(false);

    this.scrollPane = new JScrollPane();
    this.scrollPane.setViewportView(this);
    this.scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
    this.scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);

    switch (code) {
      case BEFORE:
        this.scrollPane
            .setBorder(new TitledBorder(new LineBorder(Color.black), "File BEFORE Change"));
        break;
      case AFTER:
        this.scrollPane
            .setBorder(new TitledBorder(new LineBorder(Color.black), "File AFTER Change"));
        break;
      default:
        assert false : "here shouldn't be reached!";
        System.exit(0);
    }

    this.code = code;
    this.change = null;

    this.addMouseListener(new MouseAdapter() {

      @Override
      public void mouseClicked(MouseEvent e) {

        final int button = e.getButton();
        final int clickCount = e.getClickCount();

        switch (button) {
          case MouseEvent.BUTTON1:
            switch (clickCount) {
              case 1:
                break;
              case 2:
                CCode.this.display();
                break;
              default:
            }
            break;
          case MouseEvent.BUTTON2:
            break;
          case MouseEvent.BUTTON3:
            break;
          default:
        }
      }
    });
  }

  @Override
  public void update(final Observable o, final Object arg) {

    if (o instanceof ObservedChanges) {
      final ObservedChanges observedChanges = (ObservedChanges) o;
      if (observedChanges.label.equals(CLABEL.SELECTED)) {

        this.setText("");

        if (observedChanges.isSet()) {

          try {

            this.change = observedChanges.get()
                .first();
            final Revision revision = this.change.revision;
            final String filepath = this.change.filepath;
            final String REPOSITORY_FOR_MINING = CPAConfig.getInstance()
                .getSVNREPOSITORY_FOR_MINING();
            final SVNURL url = SVNURL.fromFile(new File(REPOSITORY_FOR_MINING + "/" + filepath));
            FSRepositoryFactory.setup();
            final SVNWCClient wcClient = SVNClientManager.newInstance()
                .getWCClient();

            final StringBuilder beforeText = new StringBuilder();
            final long revNumber = Long.valueOf(revision.id);
            wcClient.doGetFileContents(url, SVNRevision.create(revNumber - 1l),
                SVNRevision.create(revNumber - 1l), false, new OutputStream() {

                  @Override
                  public void write(int b) throws IOException {
                    beforeText.append((char) b);
                  }
                });

            final StringBuilder afterText = new StringBuilder();
            wcClient.doGetFileContents(url, SVNRevision.create(revNumber),
                SVNRevision.create(revNumber), false, new OutputStream() {

                  @Override
                  public void write(int b) throws IOException {
                    afterText.append((char) b);
                  }
                });

            final Set<Integer> lines = this.getChangedLines();
            final Insets margin = new Insets(5, 50, 5, 5);
            this.setMargin(margin);
            this.setUI(new CCodeUI(this.code, lines, this, margin));
            switch (this.code) {
              case BEFORE:
                this.setText(beforeText.toString());
                break;
              case AFTER:
                this.setText(afterText.toString());
                break;
              default:
                assert false : "here shouldn't be reached!";
                System.exit(0);
            }

            this.display();

          } catch (final Exception e) {
            e.printStackTrace();
          }
        }
      }
    }
  }

  private SortedSet<Integer> getChangedLines() {
    final SortedSet<Integer> lines = new TreeSet<Integer>();
    switch (this.code) {
      case BEFORE: {
        for (int line = this.change.before.getStartLine(); line <= this.change.before
            .getEndLine(); line++) {
          lines.add(line - 1);
        }
        break;
      }
      case AFTER: {
        for (int line = this.change.after.getStartLine(); line <= this.change.after
            .getEndLine(); line++) {
          lines.add(line - 1);
        }
        break;
      }
      default:
        assert false : "here shouldn't be reached!";
        System.exit(0);
    }
    return lines;
  }

  private void display() {

    final Document doc = this.getDocument();
    final Element root = doc.getDefaultRootElement();

    final SortedSet<Integer> lines = this.getChangedLines();
    if (lines.isEmpty()) {
      return;
    }

    final int currentCaretPosition = this.getCaretPosition();

    try {

      int nextOffset = 0;
      for (final Integer line : lines) {
        final Element element = root.getElement(Math.max(1, line - 2));
        if (currentCaretPosition < element.getStartOffset()) {
          nextOffset = element.getStartOffset();
          break;
        }
      }
      if (0 == nextOffset) {
        final Element element = root.getElement(Math.max(1, lines.first() - 2));
        nextOffset = element.getStartOffset();
      }

      final Rectangle rect = this.modelToView(nextOffset);
      final Rectangle vr = this.scrollPane.getViewport()
          .getViewRect();

      if ((null != rect) && (null != vr)) {
        rect.setSize(10, vr.height);
        this.scrollRectToVisible(rect);
        this.setCaretPosition(nextOffset);
      }
    } catch (BadLocationException e) {
      System.err.println(e.getMessage());
    }
  }
}
