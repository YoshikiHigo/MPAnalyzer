package yoshikihigo.cpanalyzer;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
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

public class CPFileReader {

  private final CPAConfig config;

  public CPFileReader(final CPAConfig config) {
    this.config = config;
  }

  public SortedMap<String, String> retrieveSVNFiles(final String repository, final int revision) {

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

  public SortedMap<String, String> retrieveLocalFiles(final String directory) {

    final Set<LANGUAGE> languages = config.getLANGUAGE();
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

  static public List<String> retrievePaths(final File directory, final Set<LANGUAGE> languages) {

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

  public SortedMap<String, String> retrieveGITFiles(final String repository,
      final String revision) {

    final SortedMap<String, String> fileMap = new TreeMap<>();

    final Set<LANGUAGE> languages = config.getLANGUAGE();
    try (final FileRepository repo = new FileRepository(repository);
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
}
