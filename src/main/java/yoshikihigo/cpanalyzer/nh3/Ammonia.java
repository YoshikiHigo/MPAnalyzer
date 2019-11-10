package yoshikihigo.cpanalyzer.nh3;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import com.google.gson.Gson;
import yoshikihigo.cpanalyzer.CPAConfig;
import yoshikihigo.cpanalyzer.CPFileReader;
import yoshikihigo.cpanalyzer.data.ChangePattern;
import yoshikihigo.cpanalyzer.db.ReadOnlyDAO;
import yoshikihigo.cpanalyzer.json.LatentBug;
import yoshikihigo.cpanalyzer.json.Model;

public class Ammonia extends JFrame {

  static public void main(final String[] args) {

    CPAConfig.initialize(args);
    ReadOnlyDAO.SINGLETON.initialize();
    final CPAConfig config = CPAConfig.getInstance();
    final boolean verbose = config.isVERBOSE();

    final String warningFilePath = config.getWARN();
    final String warningFileContent = getFileContent(Paths.get(warningFilePath));
    final Gson gson = new Gson();
    final Model model = gson.fromJson(warningFileContent, Model.class);

    final Map<String, String> files = new HashMap<>();
    switch (model.targetType) {
      case "GITREPO": {
        final String repo = model.gitRepo;
        final String commit = model.gitCommit;
        files.putAll(CPFileReader.retrieveGITFiles(repo, commit));
        break;
      }
      case "SVNREPO": {
        final String repo = model.svnRepo;
        final int rev = model.svnRevision;
        files.putAll(CPFileReader.retrieveSVNFiles(repo, rev));
        break;
      }
      case "LOCALDIR": {
        final String dir = model.localDir;
        files.putAll(CPFileReader.retrieveLocalFiles(dir));
        break;
      }
    }

    final ConcurrentMap<String, List<Warning>> fWarnings = new ConcurrentHashMap<>();
    final ConcurrentMap<ChangePattern, List<Warning>> pWarnings = new ConcurrentHashMap<>();
    for (final LatentBug bug : model.latentBugs) {
      final String file = bug.file;
      final ChangePattern pattern = ReadOnlyDAO.SINGLETON.getChangePattern(bug.patternID);
      final Warning warning = new Warning(bug.fromLine, bug.toLine, pattern);

      List<Warning> warnings = fWarnings.get(file);
      if (null == warnings) {
        warnings = new ArrayList<>();
        fWarnings.put(file, warnings);
      }
      warnings.add(warning);

      warnings = pWarnings.get(pattern);
      if (null == warnings) {
        warnings = new ArrayList<>();
        pWarnings.put(pattern, warnings);
      }
      warnings.add(warning);
    }

    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch (final Exception e) {
    }

    CPAConfig.initialize(args);
    SwingUtilities.invokeLater(new Runnable() {

      public void run() {
        new Ammonia(files, fWarnings, pWarnings);
      }
    });
  }

  static private String getFileContent(final Path path) {
    try {
      final List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
      return String.join(System.lineSeparator(), lines);
    } catch (final IOException e) {
      e.printStackTrace();
      return "";
    }
  }



  public Ammonia(final Map<String, String> files, final Map<String, List<Warning>> fWarnings,
      final Map<ChangePattern, List<Warning>> pWarnings) {

    super("Ammonia");

    final Dimension d = Toolkit.getDefaultToolkit()
        .getScreenSize();
    this.setSize(new Dimension(d.width - 10, d.height - 60));
    this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    this.getContentPane()
        .setLayout(new BorderLayout());
    final PatternFilteringPanel patternFilteringPanel =
        new PatternFilteringPanel(fWarnings, pWarnings);
    this.getContentPane()
        .add(patternFilteringPanel, BorderLayout.NORTH);
    final JSplitPane mainPanel = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
    this.getContentPane()
        .add(mainPanel, BorderLayout.CENTER);
    final JSplitPane leftPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
    mainPanel.setLeftComponent(leftPane);
    final JSplitPane rightPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
    mainPanel.setRightComponent(rightPane);
    final JPanel fileListPanel = new JPanel(new BorderLayout());
    leftPane.setTopComponent(fileListPanel);
    fileListPanel.setBorder(new TitledBorder(new LineBorder(Color.black), "FILE LIST"));

    final FilePathKeywordField pathKeywordField = new FilePathKeywordField(fWarnings);
    fileListPanel.add(pathKeywordField, BorderLayout.NORTH);
    final FileListView filelist = new FileListView(fWarnings);
    fileListPanel.add(filelist.scrollPane, BorderLayout.CENTER);

    final TargetSourceCodeWindow sourcecode = new TargetSourceCodeWindow(files, fWarnings);
    leftPane.add(sourcecode.getScrollPane(), JSplitPane.BOTTOM);

    final WarningListView warninglist = new WarningListView(fWarnings, pWarnings);
    rightPane.add(warninglist.scrollPane, JSplitPane.TOP);

    final PastChangesView patternWindow = new PastChangesView();
    rightPane.add(patternWindow, JSplitPane.BOTTOM);

    SelectedEntities.getInstance(SelectedEntities.SELECTED_PATH)
        .addObserver(filelist);
    SelectedEntities.getInstance(SelectedEntities.SELECTED_PATH)
        .addObserver(sourcecode);
    SelectedEntities.getInstance(SelectedEntities.SELECTED_PATH)
        .addObserver(warninglist);
    SelectedEntities.getInstance(SelectedEntities.SELECTED_PATH)
        .addObserver(patternWindow);
    SelectedEntities.getInstance(SelectedEntities.SELECTED_PATH)
        .addObserver(patternFilteringPanel);
    SelectedEntities.getInstance(SelectedEntities.SELECTED_PATH)
        .addObserver(pathKeywordField);

    SelectedEntities.getInstance(SelectedEntities.SELECTED_WARNING)
        .addObserver(filelist);
    SelectedEntities.getInstance(SelectedEntities.SELECTED_WARNING)
        .addObserver(sourcecode);
    SelectedEntities.getInstance(SelectedEntities.SELECTED_WARNING)
        .addObserver(warninglist);
    SelectedEntities.getInstance(SelectedEntities.SELECTED_WARNING)
        .addObserver(patternWindow);
    SelectedEntities.getInstance(SelectedEntities.SELECTED_WARNING)
        .addObserver(patternFilteringPanel);
    SelectedEntities.getInstance(SelectedEntities.SELECTED_WARNING)
        .addObserver(pathKeywordField);

    SelectedEntities.getInstance(SelectedEntities.TRIVIAL_PATTERN)
        .addObserver(filelist);
    SelectedEntities.getInstance(SelectedEntities.TRIVIAL_PATTERN)
        .addObserver(sourcecode);
    SelectedEntities.getInstance(SelectedEntities.TRIVIAL_PATTERN)
        .addObserver(warninglist);
    SelectedEntities.getInstance(SelectedEntities.TRIVIAL_PATTERN)
        .addObserver(patternWindow);
    SelectedEntities.getInstance(SelectedEntities.TRIVIAL_PATTERN)
        .addObserver(patternFilteringPanel);
    SelectedEntities.getInstance(SelectedEntities.TRIVIAL_PATTERN)
        .addObserver(pathKeywordField);

    SelectedEntities.getInstance(SelectedEntities.FOCUSING_PATTERN)
        .addObserver(filelist);
    SelectedEntities.getInstance(SelectedEntities.FOCUSING_PATTERN)
        .addObserver(sourcecode);
    SelectedEntities.getInstance(SelectedEntities.FOCUSING_PATTERN)
        .addObserver(warninglist);
    SelectedEntities.getInstance(SelectedEntities.FOCUSING_PATTERN)
        .addObserver(patternWindow);
    SelectedEntities.getInstance(SelectedEntities.FOCUSING_PATTERN)
        .addObserver(patternFilteringPanel);
    SelectedEntities.getInstance(SelectedEntities.FOCUSING_PATTERN)
        .addObserver(pathKeywordField);

    SelectedEntities.getInstance(SelectedEntities.LOGKEYWORD_PATTERN)
        .addObserver(filelist);
    SelectedEntities.getInstance(SelectedEntities.LOGKEYWORD_PATTERN)
        .addObserver(sourcecode);
    SelectedEntities.getInstance(SelectedEntities.LOGKEYWORD_PATTERN)
        .addObserver(warninglist);
    SelectedEntities.getInstance(SelectedEntities.LOGKEYWORD_PATTERN)
        .addObserver(patternWindow);
    SelectedEntities.getInstance(SelectedEntities.LOGKEYWORD_PATTERN)
        .addObserver(patternFilteringPanel);
    SelectedEntities.getInstance(SelectedEntities.LOGKEYWORD_PATTERN)
        .addObserver(pathKeywordField);

    SelectedEntities.getInstance(SelectedEntities.METRICS_PATTERN)
        .addObserver(filelist);
    SelectedEntities.getInstance(SelectedEntities.METRICS_PATTERN)
        .addObserver(sourcecode);
    SelectedEntities.getInstance(SelectedEntities.METRICS_PATTERN)
        .addObserver(warninglist);
    SelectedEntities.getInstance(SelectedEntities.METRICS_PATTERN)
        .addObserver(patternWindow);
    SelectedEntities.getInstance(SelectedEntities.METRICS_PATTERN)
        .addObserver(patternFilteringPanel);
    SelectedEntities.getInstance(SelectedEntities.METRICS_PATTERN)
        .addObserver(pathKeywordField);

    SelectedEntities.getInstance(SelectedEntities.PATHKEYWORD_PATTERN)
        .addObserver(filelist);
    SelectedEntities.getInstance(SelectedEntities.PATHKEYWORD_PATTERN)
        .addObserver(sourcecode);
    SelectedEntities.getInstance(SelectedEntities.PATHKEYWORD_PATTERN)
        .addObserver(warninglist);
    SelectedEntities.getInstance(SelectedEntities.PATHKEYWORD_PATTERN)
        .addObserver(patternWindow);
    SelectedEntities.getInstance(SelectedEntities.PATHKEYWORD_PATTERN)
        .addObserver(patternFilteringPanel);
    SelectedEntities.getInstance(SelectedEntities.PATHKEYWORD_PATTERN)
        .addObserver(pathKeywordField);

    this.setVisible(true);
    mainPanel.setDividerLocation(mainPanel.getWidth() / 2);
    leftPane.setDividerLocation(leftPane.getHeight() / 2);
    rightPane.setDividerLocation(180);

  }
}
