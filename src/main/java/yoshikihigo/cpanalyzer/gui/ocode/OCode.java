package yoshikihigo.cpanalyzer.gui.ocode;

import java.awt.Color;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
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
import org.tmatesoft.svn.core.wc.SVNClientManager;
import org.tmatesoft.svn.core.wc.SVNRevision;
import org.tmatesoft.svn.core.wc.SVNWCClient;

import yoshikihigo.cpanalyzer.CPAConfig;
import yoshikihigo.cpanalyzer.FileUtility;
import yoshikihigo.cpanalyzer.LANGUAGE;
import yoshikihigo.cpanalyzer.StringUtility;
import yoshikihigo.cpanalyzer.data.Code;
import yoshikihigo.cpanalyzer.data.Revision;
import yoshikihigo.cpanalyzer.data.Statement;
import yoshikihigo.cpanalyzer.gui.ObservedCodeFragments;
import yoshikihigo.cpanalyzer.gui.ObservedCodeFragments.CFLABEL;
import yoshikihigo.cpanalyzer.gui.ObservedFiles;
import yoshikihigo.cpanalyzer.gui.ObservedFiles.FLABEL;
import yoshikihigo.cpanalyzer.gui.ObservedRevisions;
import yoshikihigo.cpanalyzer.gui.ObservedRevisions.RLABEL;

public class OCode extends JTextArea implements Observer {

  static public final int TAB_SIZE = 4;

  public final JScrollPane scrollPane;

  private Revision revision;
  private Code codefragment;
  private List<Statement> statements;

  public OCode() {

    this.setTabSize(TAB_SIZE);

    this.revision = null;
    this.codefragment = null;
    this.statements = new ArrayList<Statement>();

    final Insets margin = new Insets(5, 50, 5, 5);
    this.setMargin(margin);
    this.setUI(new OCodeUI(new HashSet<Integer>(), this, margin));
    this.setText("");
    this.setEditable(false);

    this.scrollPane = new JScrollPane();
    this.scrollPane.setViewportView(this);
    this.scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
    this.scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);

    this.setTitle(null);

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
                OCode.this.display();
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

  private void setTitle(final String path) {
    final StringBuilder title = new StringBuilder();
    title.append("Source Code View");
    if (null != path) {
      title.append(" (");
      title.append(path);
      title.append(")");
    }
    this.scrollPane.setBorder(new TitledBorder(new LineBorder(Color.black), title.toString()));
  }

  @Override
  public void update(final Observable o, final Object arg) {

    if (o instanceof ObservedFiles) {
      final ObservedFiles observedFiles = (ObservedFiles) o;
      if (observedFiles.label.equals(FLABEL.OVERLOOKED)) {

        this.setText("");

        if (observedFiles.isSet()) {

          try {

            final String path = observedFiles.get()
                .first();

            final String repository = CPAConfig.getInstance()
                .getREPOSITORY_FOR_TEST();
            final SVNURL fileurl =
                SVNURL.fromFile(new File(repository + System.getProperty("file.separator") + path));
            final SVNWCClient wcClient = SVNClientManager.newInstance()
                .getWCClient();

            final StringBuilder text = new StringBuilder();
            final long revNumber = Long.valueOf(this.revision.id);
            wcClient.doGetFileContents(fileurl, SVNRevision.create(revNumber),
                SVNRevision.create(revNumber), false, new OutputStream() {

                  @Override
                  public void write(int b) throws IOException {
                    text.append((char) b);
                  }
                });

            final LANGUAGE language = FileUtility.getLANGUAGE(path);
            OCode.this.statements = StringUtility.splitToStatements(text.toString(), language);
            final SortedSet<Integer> highlightedLines = new TreeSet<Integer>();
            for (int line = this.codefragment.getStartLine(); line <= this.codefragment
                .getEndLine(); line++) {
              highlightedLines.add(line - 1);
            }

            final Insets margin = new Insets(5, 50, 5, 5);
            this.setMargin(margin);
            this.setUI(new OCodeUI(highlightedLines, this, margin));
            this.setText(text.toString());
            this.setTitle(path);

            this.display();

          } catch (final Exception e) {
            e.printStackTrace();
          }
        }
      }
    }

    else if (o instanceof ObservedRevisions) {
      final ObservedRevisions observedRevisions = (ObservedRevisions) o;
      if (observedRevisions.label.equals(RLABEL.OVERLOOKED)) {
        if (observedRevisions.isSet()) {
          this.revision = observedRevisions.get()
              .first();
        }
      }
    }

    else if (o instanceof ObservedCodeFragments) {
      final ObservedCodeFragments observedCodeFragments = (ObservedCodeFragments) o;
      if (observedCodeFragments.label.equals(CFLABEL.OVERLOOKED)) {
        if (observedCodeFragments.isSet()) {
          this.codefragment = observedCodeFragments.get()
              .first();
        }
      }
    }
  }

  private SortedSet<Integer> getPatternLines(final List<Statement> statements,
      final List<Statement> pattern) {

    final SortedSet<Integer> lines = new TreeSet<Integer>();
    int patternLine = 0;

    int pIndex = 0;
    for (int index = 0; index < statements.size(); index++) {

      final Statement statement = statements.get(index);
      if (statement.hash == pattern.get(pIndex).hash) {
        if (0 == pIndex) {
          patternLine = statement.tokens.get(0).line - 1;
        }
        pIndex++;
        if (pIndex == pattern.size()) {
          pIndex = 0;
          lines.add(patternLine);
          patternLine = 0;
        }
      }

      else {
        pIndex = 0;
        patternLine = 0;
      }
    }

    return lines;
  }

  private void display() {

    final Document doc = this.getDocument();
    final Element root = doc.getDefaultRootElement();

    final SortedSet<Integer> patternLines =
        this.getPatternLines(this.statements, this.codefragment.statements);
    if (patternLines.isEmpty()) {
      return;
    }

    final int currentCaretPosition = this.getCaretPosition();

    try {

      int nextOffset = 0;
      for (final Integer line : patternLines) {
        final Element element = root.getElement(Math.max(1, line - 2));
        if (currentCaretPosition < element.getStartOffset()) {
          nextOffset = element.getStartOffset();
          break;
        }
      }
      if (0 == nextOffset) {
        final Element element = root.getElement(Math.max(1, patternLines.first() - 2));
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
