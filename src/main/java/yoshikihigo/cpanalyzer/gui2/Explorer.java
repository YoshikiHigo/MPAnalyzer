package yoshikihigo.cpanalyzer.gui2;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.wc2.SvnExport;
import org.tmatesoft.svn.core.wc2.SvnOperationFactory;
import org.tmatesoft.svn.core.wc2.SvnTarget;
import yoshikihigo.cpanalyzer.CPAConfig;
import yoshikihigo.cpanalyzer.LANGUAGE;
import yoshikihigo.cpanalyzer.StringUtility;
import yoshikihigo.cpanalyzer.data.ChangePattern;
import yoshikihigo.cpanalyzer.data.MD5;
import yoshikihigo.cpanalyzer.data.Statement;
import yoshikihigo.cpanalyzer.db.ReadOnlyDAO;

public class Explorer extends JFrame {

  static public void main(final String[] args) {

    CPAConfig.initialize(args);
    ReadOnlyDAO.SINGLETON.initialize();
    final CPAConfig config = CPAConfig.getInstance();
    final boolean verbose = config.isVERBOSE();

    if (config.getLANGUAGE()
        .isEmpty()) {
      System.out.println("\"-lang\" option is required to specify target languages");
      System.exit(0);
    }

    System.out.println("Useful functions:");
    System.out
        .println(" double-clicking LEFT button on WARNINGLIST -> focusing the specified pattern");
    System.out.println(
        " double-clicking RIGHT button on WARNINGLIST -> marking the specified pattern as trivial");
    System.out.println(
        " double-clicking LEFT button on the header of PASTCHANGES -> showing up commit information");
    System.out.println();

    final SortedMap<String, String> files = new TreeMap<>();
    if (config.hasESVNREPO() && config.hasESVNREV()) {

      final String repository = config.getESVNREPO();
      final int revision = config.getESVNREV();
      files.putAll(retrieveSVNFiles(repository, revision));
    }

    else if (config.hasEGITREPO() && config.hasEGITCOMMIT()) {

      final String gitrepo = config.getEGITREPO();
      final String gitcommit = config.getEGITCOMMIT();
      files.putAll(retrieveGITFiles(gitrepo, gitcommit));
    }

    else if (config.hasEDIR()) {

      final String directory = config.getEDIR();
      files.putAll(retrieveLocalFiles(directory));
    }

    else {
      System.out.println("target for exploring latent buggy code is not specified correctly.");
      System.out.println(
          " if you want to use a GIT repository, specify \"egitrepo\" and \"egitcommit\".");
      System.out
          .println(" if you want to use a SVN repository, specify \"esvnrepo\" and \"esvnrev\".");
      System.out.println(" if you want to use a usual directory, specify \"edir\".");
      System.exit(0);
    }

    final Set<LANGUAGE> languages = CPAConfig.getInstance()
        .getLANGUAGE();
    final SortedMap<String, List<Statement>> pathToStatements = new TreeMap<>();
    final Map<MD5, SortedSet<String>> hashToPaths = new HashMap<>();
    for (final Entry<String, String> entry : files.entrySet()) {
      final String path = entry.getKey();
      final String contents = entry.getValue();
      for (final LANGUAGE lang : languages) {
        if (lang.isTarget(path)) {
          final List<Statement> statements = StringUtility.splitToStatements(contents, lang);
          pathToStatements.put(path, statements);

          for (final Statement statement : statements) {
            final MD5 hash = new MD5(statement.hash);
            SortedSet<String> paths = hashToPaths.get(hash);
            if (null == paths) {
              paths = new TreeSet<>();
              hashToPaths.put(hash, paths);
            }
            paths.add(path);
          }

          break;
        }
      }
    }

    final int support = config.getESUPPORT();
    final float confidence = config.getECONFIDENCE();
    final List<ChangePattern> patterns =
        ReadOnlyDAO.SINGLETON.getChangePatterns(support, confidence);

    System.out.print("finding latent buggy code from ");
    System.out.print(pathToStatements.size());
    System.out.print(" files with ");
    System.out.print(patterns.size());
    System.out.print(" change patterns ... ");

    final int threads = config.getTHREAD();

    final ExecutorService threadPool = Executors.newFixedThreadPool(threads);
    final List<Future<?>> futures = new ArrayList<>();

    final ConcurrentMap<String, List<Warning>> fWarnings = new ConcurrentHashMap<>();
    final ConcurrentMap<ChangePattern, List<Warning>> pWarnings = new ConcurrentHashMap<>();

    PATTERN: for (final ChangePattern pattern : patterns) {

      final String patternText = ReadOnlyDAO.SINGLETON.getCode(pattern.beforeHash)
          .get(0).rText;
      final List<byte[]> patternHashs = Arrays.asList(StringUtility.splitToLines(patternText))
          .stream()
          .map(line -> Statement.getMD5(line))
          .collect(Collectors.toList());

      if (patternHashs.isEmpty()) {
        continue PATTERN;
      }

      final MD5 hash1 = new MD5(patternHashs.get(0));
      if (!hashToPaths.containsKey(hash1)) {
        continue PATTERN;
      }
      final SortedSet<String> paths = hashToPaths.get(hash1);
      for (int index = 1; index < patternHashs.size(); index++) {
        final MD5 hash2 = new MD5(patternHashs.get(index));
        if (!hashToPaths.containsKey(hash2)) {
          continue PATTERN;
        }
        paths.retainAll(hashToPaths.get(hash2));
      }

      final Future<?> future = threadPool.submit(
          new MatchingThread(paths, pathToStatements, pattern, fWarnings, pWarnings, verbose));
      futures.add(future);
    }

    try {
      for (final Future<?> future : futures) {
        future.get();
      }
    } catch (final ExecutionException | InterruptedException e) {
      e.printStackTrace();
      System.exit(0);
    } finally {
      threadPool.shutdown();
    }

    System.out.print("done.");

    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch (final Exception e) {
    }

    CPAConfig.initialize(args);
    SwingUtilities.invokeLater(new Runnable() {

      public void run() {
        new Explorer(files, fWarnings, pWarnings);
      }
    });
  }

  static SortedMap<String, String> retrieveSVNFiles(final String repository, final int revision) {

    Path tmpDir = null;
    try {
      tmpDir = Files.createTempDirectory("CPAnalyzer-Explorer");
      tmpDir.toFile()
          .deleteOnExit();
    } catch (final IOException e) {
      e.printStackTrace();
      System.exit(0);
    }

    System.out.print("retrieving the specified revision ...");
    final SvnOperationFactory operationFactory = new SvnOperationFactory();
    try {
      final SvnExport svnExport = operationFactory.createExport();
      final SVNURL url = StringUtility.getSVNURL(repository, "");
      svnExport.setSource(SvnTarget.fromURL(url));
      svnExport.setSingleTarget(SvnTarget.fromFile(tmpDir.toFile()));
      svnExport.setForce(true);
      svnExport.run();
      System.out.println(" done.");

    } catch (final SVNException e) {
      e.printStackTrace();
      System.exit(0);
    } finally {
      operationFactory.dispose();
    }

    return retrieveLocalFiles(tmpDir.toFile()
        .getAbsolutePath());
  }

  static SortedMap<String, String> retrieveLocalFiles(final String directory) {

    final Set<LANGUAGE> languages = CPAConfig.getInstance()
        .getLANGUAGE();
    final List<String> paths = retrievePaths(new File(directory), languages);
    final SortedMap<String, String> files = new TreeMap<>();

    for (final String path : paths) {
      try {
        final List<String> lines = Files.readAllLines(Paths.get(path), StandardCharsets.ISO_8859_1);
        final String text = String.join(System.lineSeparator(), lines);
        files.put(path.substring(directory.length() + 1), text);

      } catch (final IOException e) {
        e.printStackTrace();
      }
    }

    return files;
  }

  static List<String> retrievePaths(final File directory, final Set<LANGUAGE> languages) {

    final List<String> paths = new ArrayList<>();

    if (directory.isFile()) {
      for (final LANGUAGE lang : languages) {
        if (lang.isTarget(directory.getName())) {
          paths.add(directory.getAbsolutePath());
          break;
        }
      }
    }

    else if (directory.isDirectory()) {
      for (final File child : directory.listFiles()) {
        paths.addAll(retrievePaths(child, languages));
      }
    }

    Collections.sort(paths);

    return paths;
  }

  static SortedMap<String, String> retrieveGITFiles(final String repository,
      final String revision) {

    final SortedMap<String, String> fileMap = new TreeMap<>();

    final CPAConfig config = CPAConfig.getInstance();
    final String gitrepo = config.getEGITREPO();
    final Set<LANGUAGE> languages = config.getLANGUAGE();

    try (final FileRepository repo = new FileRepository(gitrepo + "/.git");
        final ObjectReader reader = repo.newObjectReader();
        final TreeWalk treeWalk = new TreeWalk(reader);
        final RevWalk revWalk = new RevWalk(reader)) {

      final ObjectId rootId = repo.resolve(revision);
      revWalk.markStart(revWalk.parseCommit(rootId));
      final RevCommit commit = revWalk.next();
      final RevTree tree = commit.getTree();
      treeWalk.addTree(tree);
      treeWalk.setRecursive(true);
      final List<String> files = new ArrayList<>();
      while (treeWalk.next()) {
        final String path = treeWalk.getPathString();
        for (final LANGUAGE language : languages) {
          if (language.isTarget(path)) {
            files.add(path);
            break;
          }
        }
      }

      for (final String file : files) {
        final TreeWalk nodeWalk = TreeWalk.forPath(reader, file, tree);
        final byte[] data = reader.open(nodeWalk.getObjectId(0))
            .getBytes();
        final String text = new String(data, "utf-8");
        fileMap.put(file, text);
      }

    } catch (final IOException e) {
      e.printStackTrace();
    }

    return fileMap;
  }

  static private List<Warning> findMatchedCode(final List<Statement> statements,
      final ChangePattern pattern) {

    final String patternText = ReadOnlyDAO.SINGLETON.getCode(pattern.beforeHash)
        .get(0).rText;
    final List<byte[]> patternHashs = Arrays.asList(StringUtility.splitToLines(patternText))
        .stream()
        .map(line -> Statement.getMD5(line))
        .collect(Collectors.toList());

    if (patternHashs.isEmpty()) {
      return Collections.emptyList();
    }

    int pIndex = 0;
    final List<Warning> warnings = new ArrayList<>();
    final List<Statement> code = new ArrayList<>();
    for (int index = 0; index < statements.size(); index++) {

      if (Arrays.equals(statements.get(index).hash, patternHashs.get(pIndex))) {
        pIndex++;
        code.add(statements.get(index));
        if (pIndex == patternHashs.size()) {
          final int fromLine = code.get(0).fromLine;
          final int toLine = code.get(code.size() - 1).toLine;
          final Warning warning = new Warning(fromLine, toLine, pattern);
          warnings.add(warning);
          code.clear();
          pIndex = 0;
        }
      }

      else {
        pIndex = 0;
        code.clear();
      }
    }

    return warnings;
  }

  public Explorer(final Map<String, String> files, final Map<String, List<Warning>> fWarnings,
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


class MatchingThread extends Thread {

  final SortedSet<String> paths;
  final SortedMap<String, List<Statement>> pathToStatements;
  final ChangePattern pattern;
  final ConcurrentMap<String, List<Warning>> fWarnings;
  final ConcurrentMap<ChangePattern, List<Warning>> pWarnings;
  final boolean verbose;

  MatchingThread(final SortedSet<String> paths,
      final SortedMap<String, List<Statement>> pathToStatements, final ChangePattern pattern,
      final ConcurrentMap<String, List<Warning>> fWarnings,
      final ConcurrentMap<ChangePattern, List<Warning>> pWarnings, final boolean verbose) {

    this.paths = paths;
    this.pathToStatements = pathToStatements;
    this.pattern = pattern;
    this.fWarnings = fWarnings;
    this.pWarnings = pWarnings;
    this.verbose = verbose;
  }

  @Override
  public void run() {

    if (this.verbose) {
      final StringBuilder progress = new StringBuilder();
      progress.append(" ");
      progress.append(Thread.currentThread()
          .getId());
      progress.append(": matching code with change pattern ");
      progress.append(this.pattern.id);
      System.out.println(progress.toString());
    }

    PATH: for (final String path : this.paths) {
      final List<Statement> statements = this.pathToStatements.get(path);

      final List<Warning> warnings = this.findMatchedCode(statements, this.pattern);
      if (warnings.isEmpty()) {
        continue PATH;
      }

      List<Warning> w1 = this.fWarnings.get(path);
      if (null == w1) {
        w1 = new ArrayList<>();
        this.fWarnings.put(path, w1);
      }
      w1.addAll(warnings);

      List<Warning> w2 = this.pWarnings.get(this.pattern);
      if (null == w2) {
        w2 = new ArrayList<>();
        this.pWarnings.put(this.pattern, w2);
      }
      w2.addAll(warnings);

    }

    super.run();
  }

  private List<Warning> findMatchedCode(final List<Statement> statements,
      final ChangePattern pattern) {

    final String patternText = ReadOnlyDAO.SINGLETON.getCode(pattern.beforeHash)
        .get(0).rText;
    final List<byte[]> patternHashs = Arrays.asList(StringUtility.splitToLines(patternText))
        .stream()
        .map(line -> Statement.getMD5(line))
        .collect(Collectors.toList());

    if (patternHashs.isEmpty()) {
      return Collections.emptyList();
    }

    int pIndex = 0;
    final List<Warning> warnings = new ArrayList<>();
    final List<Statement> code = new ArrayList<>();
    for (int index = 0; index < statements.size(); index++) {

      if (Arrays.equals(statements.get(index).hash, patternHashs.get(pIndex))) {
        pIndex++;
        code.add(statements.get(index));
        if (pIndex == patternHashs.size()) {
          final int fromLine = code.get(0).fromLine;
          final int toLine = code.get(code.size() - 1).toLine;
          final Warning warning = new Warning(fromLine, toLine, pattern);
          warnings.add(warning);
          code.clear();
          pIndex = 0;
        }
      }

      else {
        pIndex = 0;
        code.clear();
      }
    }

    return warnings;
  }
}
