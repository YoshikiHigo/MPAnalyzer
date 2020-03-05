package yoshikihigo.cpanalyzer.nh3;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter.DefaultHighlightPainter;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.internal.io.fs.FSRepositoryFactory;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import org.tmatesoft.svn.core.wc.SVNRevision;
import org.tmatesoft.svn.core.wc.SVNWCClient;
import yoshikihigo.cpanalyzer.StringUtility;
import yoshikihigo.cpanalyzer.data.Change;
import yoshikihigo.cpanalyzer.db.ReadOnlyDAO;

public class PastChangesView extends JTabbedPane implements Observer {

  public PastChangesView() {
    this.setBorder(new TitledBorder(new LineBorder(Color.black), "PAST CHANGES"));

    this.addChangeListener(e -> {
      this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

      final PastChange pastChange = (PastChange) this.getSelectedComponent();
      if (null != pastChange) {
        pastChange.loadCode(this.getHeight());
      }

      this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    });
  }

  @Override
  public void update(final Observable o, final Object arg) {

    if (o instanceof SelectedEntities) {

      final SelectedEntities selectedEntities = (SelectedEntities) o;

      if (selectedEntities.getLabel()
          .equals(SelectedEntities.SELECTED_WARNING)) {

        this.removeAll();

        if (selectedEntities.isSet()) {
          final Warning warning = (Warning) selectedEntities.get()
              .get(0);

          final List<Change> changes = ReadOnlyDAO.SINGLETON.getChanges(warning.pattern.beforeHash,
              warning.pattern.afterHash);

          for (final Change change : changes) {
            final PastChange pastChange = new PastChange(change);
            this.addTab(Integer.toString(this.getTabCount() + 1), pastChange);
          }
        }

        this.repaint();
      }

      else if (selectedEntities.getLabel()
          .equals(SelectedEntities.SELECTED_PATH)) {
        this.removeAll();
        this.repaint();
      }
    }
  }
}


class PastChange extends JPanel {

  final private Change change;
  private JSplitPane srcPane;

  PastChange(final Change change) {
    super(new BorderLayout());
    this.change = change;

    final JLabel label1 = new JLabel("Revision: " + change.revision + ", Author: "
        + change.revision.author + ", Path: " + change.filepath);
    final JLabel label2 = new JLabel("Commit log: " + change.revision.message);
    final JPanel labelPanel = new JPanel(new BorderLayout());
    labelPanel.add(label1, BorderLayout.NORTH);
    labelPanel.add(label2, BorderLayout.CENTER);
    this.add(labelPanel, BorderLayout.NORTH);

    this.srcPane = null;

    labelPanel.addMouseListener(new MouseAdapter() {

      @Override
      public void mouseClicked(final MouseEvent e) {
        if (e.getClickCount() == 2) {
          final JFrame frame = new JFrame("Commit Information");
          frame.setSize(500, 600);
          final JTextArea text = new JTextArea();
          text.append("Revision: ");
          text.append(change.revision.id);
          text.append(System.lineSeparator());
          text.append(System.lineSeparator());
          text.append("Author: ");
          text.append(change.revision.author);
          text.append(System.lineSeparator());
          text.append(System.lineSeparator());
          text.append("File: ");
          text.append(change.filepath);
          text.append(System.lineSeparator());
          text.append(System.lineSeparator());
          text.append("Log: ");
          text.append(change.revision.message);
          text.setEditable(false);
          text.setLineWrap(true);
          frame.getContentPane()
              .add(text);
          frame.setVisible(true);
        }
      }
    });
  }

  void loadCode(final int height) {

    if (null != this.srcPane) {
      return;
    }

    String beforeText = "";
    String afterText = "";
    final String repoType = "GITREPO"; // TODO for SVNREPO
    switch (repoType) {
      case "SVNREPO":
        beforeText = this.getSVNText(this.change.filepath, this.change.repo,
            Integer.parseInt(this.change.revision.id) - 1);
        afterText = this.getSVNText(this.change.filepath, this.change.repo,
            Integer.parseInt(this.change.revision.id));
        break;
      case "GITREPO": {
        beforeText =
            this.getGITText(this.change.filepath, this.change.repo, this.change.revision.id, false);
        afterText =
            this.getGITText(this.change.filepath, this.change.repo, this.change.revision.id, true);
        break;
      }
      default: {
        System.err.println("invalid repository type: " + repoType);
        System.exit(0);
      }
    }

    final ChangeInstanceView beforeView = new ChangeInstanceView("BEFORE TEXT", beforeText);
    final ChangeInstanceView afterView = new ChangeInstanceView("AFTER TEXT", afterText);

    this.srcPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
    this.srcPane.add(beforeView.scrollPane, JSplitPane.TOP);
    this.srcPane.add(afterView.scrollPane, JSplitPane.BOTTOM);
    this.add(this.srcPane, BorderLayout.CENTER);
    this.srcPane.setDividerLocation((height - 100) / 2);

    final int beforeChangeStartLine = this.change.before.getStartLine();
    final int beforeChangeEndLine = this.change.before.getEndLine();
    final int afterChangeStartLine = this.change.after.getStartLine();
    final int afterChangeEndLine = this.change.after.getEndLine();

    beforeView.addHighlight(beforeChangeStartLine, beforeChangeEndLine);
    afterView.addHighlight(afterChangeStartLine, afterChangeEndLine);

    beforeView.displayAt((0 < beforeChangeEndLine) ? beforeChangeEndLine : afterChangeEndLine);
    afterView.displayAt((0 < afterChangeEndLine) ? afterChangeEndLine : beforeChangeEndLine);
  }

  private String getSVNText(final String path, final String svnrepo, final int revision) {

    final SVNURL url = StringUtility.getSVNURL(svnrepo, path);
    FSRepositoryFactory.setup();
    SVNWCClient wcClient = SVNClientManager.newInstance()
        .getWCClient();

    final StringBuilder text = new StringBuilder();
    try {
      wcClient.doGetFileContents(url, SVNRevision.create(revision), SVNRevision.create(revision),
          false, new OutputStream() {

            @Override
            public void write(int b) throws IOException {
              text.append((char) b);
            }
          });
    } catch (final SVNException | NullPointerException e) {
      e.printStackTrace();
      return "";
    }

    return text.toString();
  }

  private String getGITText(final String path, final String gitrepo, final String revision,
      final boolean after) {

    String text = "";
    try (final FileRepository repo = new FileRepository(new File(gitrepo));
        final ObjectReader reader = repo.newObjectReader();
        final RevWalk revWalk = new RevWalk(reader)) {

      final ObjectId rootId = repo.resolve(revision);
      revWalk.markStart(revWalk.parseCommit(rootId));
      final RevCommit afterCommit = revWalk.next();
      final RevCommit beforeCommit = revWalk.next();
      // System.out.println("after: " + afterCommit.name());
      // System.out.println("before: " + beforeCommit.name());

      RevTree tree = null;
      if (after) {
        tree = afterCommit.getTree();
      } else {
        tree = beforeCommit.getTree();
      }

      final TreeWalk treeWalk = TreeWalk.forPath(reader, path, tree);
      final byte[] data = reader.open(treeWalk.getObjectId(0))
          .getBytes();
      text = new String(data, "utf-8");

    } catch (final IOException e) {
      e.printStackTrace();
    }

    return text;
  }

  class ChangeInstanceView extends JTextArea {

    final JScrollPane scrollPane;

    public ChangeInstanceView(final String title, final String code) {

      super();

      final Insets margin = new Insets(5, 50, 5, 5);
      this.setMargin(margin);

      final TargetSourceCodeUI sourceCodeUI = new TargetSourceCodeUI(this, margin);
      this.setUI(sourceCodeUI);
      this.setTabSize(2);

      this.scrollPane = new JScrollPane();
      this.scrollPane.setViewportView(this);
      this.scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
      this.scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
      this.scrollPane.setBorder(new TitledBorder(new LineBorder(Color.black), title));
      this.setText(code);
    }

    private void addHighlight(final int startline, final int endline) {

      final DefaultHighlightPainter painter = new DefaultHighlightPainter(new Color(200, 0, 0, 50));

      try {

        int startOffset = 0;
        int endOffset = 0;

        if (0 < startline) {
          startOffset = super.getLineStartOffset(startline - 1);
        }

        if (0 < endline) {
          endOffset = super.getLineEndOffset(endline - 1);
        }

        this.getHighlighter()
            .addHighlight(startOffset, endOffset, painter);

      } catch (final BadLocationException e) {
        e.printStackTrace();
      }
    }

    public void displayAt(final int line) {
      int offset = 0;
      try {
        offset = super.getLineEndOffset(line);
      } catch (final BadLocationException e) {
        e.printStackTrace();
      }
      this.setCaretPosition(offset);
    }
  }
}
