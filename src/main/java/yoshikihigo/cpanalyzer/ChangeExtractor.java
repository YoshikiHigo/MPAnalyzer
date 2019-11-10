package yoshikihigo.cpanalyzer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.RawTextComparator;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revplot.PlotWalk;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.util.io.DisabledOutputStream;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.internal.io.fs.FSRepositoryFactory;
import org.tmatesoft.svn.core.io.SVNRepository;
import yoshikihigo.cpanalyzer.data.Revision;
import yoshikihigo.cpanalyzer.db.ChangeDAO;
import yoshikihigo.cpanalyzer.db.ConfigurationDAO;

public class ChangeExtractor {

  public static void main(String[] args) {

    CPAConfig.initialize(args);
    final CPAConfig config = CPAConfig.getInstance();
    final String db = config.getDATABASE();
    final File dbFile = new File(db);
    if (dbFile.exists()) {
      final boolean isForce = config.isFORCE();
      final boolean isInsert = config.isINSERT();
      if (isForce && isInsert) {
        System.err.println("options \"-f\" and \"-i\" cannot be used together.");
        return;
      }

      System.out.println(db + " already exists in your file system.");
      if (isForce) {
        if (!dbFile.delete()) {
          if (!config.isQUIET()) {
            System.err.println("The file cannot be removed.");
          }
          return;
        } else {
          if (!config.isQUIET()) {
            System.out.println("The db has been removed.");
          }
        }
      }

      else if (isInsert) {
        System.out.println("analysis results will be inserted to the existing db.");
      }

      else {
        return;
      }
    }


    final long startTime = System.nanoTime();

    if (!config.isQUIET()) {
      System.out.println("working on software \"" + CPAConfig.getInstance()
          .getSOFTWARE() + "\"");
      System.out.print("identifing revisions to be checked ... ");
    }
    if (config.isVERBOSE()) {
      System.out.println();
    }

    RepoType repoType = null;
    String repoDir = null;
    List<Revision> revisions = Collections.emptyList();
    if (config.hasSVNREPO() && config.hasGITREPO()) {
      System.out.println("-svnrepo and -gitrepo cannot be used together.");
      System.out.println("please specify either of them.");
      System.exit(0);
    } else if (config.hasSVNREPO()) {
      revisions = getSVNRevisions();
      repoType = RepoType.SVNREPO;
      repoDir = config.getSVNREPOSITORY_FOR_MINING();
    } else if (config.hasGITREPO()) {
      revisions = getGITRevisions();
      repoType = RepoType.GITREPO;
      repoDir = config.getGITREPOSITORY_FOR_MINING();
    } else {
      System.out.println("either of -svnrepo or -gitrepo must be specified.");
      System.exit(0);
    }

    ConfigurationDAO.SINGLETON.initialize();
    ConfigurationDAO.SINGLETON.setRepoType(repoType.toString());
    ConfigurationDAO.SINGLETON.setRepoDir(repoDir);
    final String currentDir = System.getProperty("user.dir");
    ConfigurationDAO.SINGLETON.setCurrentDir(currentDir);
    final Calendar calendar = Calendar.getInstance();
    final Date date = calendar.getTime();
    ConfigurationDAO.SINGLETON.setDate(date.toString());
    final String user = System.getProperty("user.name");
    ConfigurationDAO.SINGLETON.setUser(user);

    ChangeDAO.SINGLETON.initialize();
    ChangeDAO.SINGLETON.addRevisions(revisions.toArray(new Revision[0]));
    if (!config.isQUIET()) {
      System.out.println("done.");
    }

    if (revisions.isEmpty()) {
      if (!config.isQUIET()) {
        System.out.println("no revision.");
      }
      System.exit(0);
    }

    if (!config.isQUIET()) {
      System.out.print("extracting code changes ... ");
    }
    if (config.isVERBOSE()) {
      System.out.println();
    }

    final int THREADS = config.getTHREAD();
    final ExecutorService threadPool = Executors.newFixedThreadPool(THREADS);
    final List<Future<?>> futures = new ArrayList<>();

    if (config.hasSVNREPO()) {
      for (; 2 <= revisions.size(); revisions.remove(0)) {
        final Revision beforeRevision = revisions.get(0);
        final Revision afterRevision = revisions.get(1);
        final Future<?> future =
            threadPool.submit(new SVNChangeExtractionThread(beforeRevision, afterRevision));
        futures.add(future);
      }
    } else if (config.hasGITREPO()) {

      final String repoPath = config.getGITREPOSITORY_FOR_MINING();
      Repository repository = null;
      PlotWalk revWalk = null;
      try {
        repository = new FileRepository(new File(repoPath + File.separator + ".git"));
        revWalk = new PlotWalk(repository);
      } catch (final IOException e) {
        e.printStackTrace();
      }
      final ObjectReader reader = repository.newObjectReader();
      for (; !revisions.isEmpty(); revisions.remove(0)) {
        final Future<?> future = threadPool
            .submit(new GITChangeExtractionThread(revisions.get(0), repository, revWalk, reader));
        futures.add(future);
      }
    }

    try {
      for (final Future<?> future : futures) {
        future.get();
      }
      ChangeDAO.SINGLETON.flush();
      ChangeDAO.SINGLETON.close();
    } catch (final ExecutionException | InterruptedException e) {
      e.printStackTrace();
      System.exit(0);
    } finally {
      threadPool.shutdown();
    }

    if (!config.isVERBOSE() && !config.isQUIET()) {
      System.out.println("done.");
    }

    final long endTime = System.nanoTime();
    if (!config.isQUIET()) {
      System.out.print("execution time: ");
      System.out.println(TimingUtility.getExecutionTime(startTime, endTime));
    }
  }

  private static List<Revision> getSVNRevisions() {

    final CPAConfig config = CPAConfig.getInstance();
    final String repository = config.getSVNREPOSITORY_FOR_MINING();
    final Set<LANGUAGE> languages = config.getLANGUAGE();
    final String software = config.getSOFTWARE();
    final boolean isVerbose = config.isVERBOSE();

    long startRevision = config.getSTART_REVISION_FOR_MINING();
    long endRevision = config.getEND_REVISION_FOR_MINING();

    if (startRevision < 0) {
      startRevision = 0l;
    }

    final List<Revision> revisions = new LinkedList<>();

    SVNURL url;
    SVNRepository svnRepository;
    try {
      FSRepositoryFactory.setup();
      url = SVNURL.fromFile(new File(repository));
      svnRepository = FSRepositoryFactory.create(url);
    } catch (final SVNException | NullPointerException e) {
      e.printStackTrace();
      return Collections.emptyList();
    }

    try {

      if (endRevision < 0) {
        endRevision = svnRepository.getLatestRevision();
      }

      svnRepository.log(null, startRevision, endRevision, true, true, entry -> {
        final String id = Long.toString(entry.getRevision());
        final String date = StringUtility.getDateString(entry.getDate());
        final String message = entry.getMessage();
        final String author = entry.getAuthor();
        final Revision revision = new Revision(software, id, date, message, author);
        for (final String path : entry.getChangedPaths()
            .keySet()) {
          for (final LANGUAGE language : languages) {
            if (isVerbose && language.isTarget(path)) {
              System.out.println(id + " (" + date + ") has been identified.");
            }
            revisions.add(revision);
            return;
          }
        }
      });

    } catch (final Exception e) {
      e.printStackTrace();
      System.exit(0);
    }

    Collections.sort(revisions);
    return revisions;
  }

  private static List<Revision> getGITRevisions() {

    final CPAConfig config = CPAConfig.getInstance();
    final String repoPath = config.getGITREPOSITORY_FOR_MINING();
    final Set<LANGUAGE> languages = config.getLANGUAGE();
    final String software = config.getSOFTWARE();
    final Date startDate = config.getSTART_DATE_FOR_MINING();
    final Date endDate = config.getEND_DATE_FOR_MINING();
    final boolean isVerbose = config.isVERBOSE();

    final List<Revision> revisions = new LinkedList<>();

    Repository repo = null;
    try {
      repo = new FileRepository(new File(repoPath + File.separator + ".git"));
    } catch (final IOException e) {
      System.err.println("invalid repository path: " + repoPath);
      System.exit(0);
    }

    try (final Git git = new Git(repo);
        final DiffFormatter formatter = new DiffFormatter(DisabledOutputStream.INSTANCE)) {

      formatter.setRepository(repo);
      formatter.setDiffComparator(RawTextComparator.DEFAULT);
      formatter.setDetectRenames(true);

      COMMIT: for (final RevCommit commit : git.log()
          .all()
          .call()) {

        final Date date = new Date(commit.getCommitTime() * 1000L);
        if (date.before(startDate) || date.after(endDate)) {
          continue;
        }

        if (1 != commit.getParentCount()) {
          continue;
        }

        final RevCommit parent = commit.getParent(0);

        List<DiffEntry> diffEntries = null;
        try {
          diffEntries = formatter.scan(parent.getId(), commit.getId());
        } catch (final IOException e) {
          e.printStackTrace();
          System.exit(0);
        }

        for (final DiffEntry entry : diffEntries) {
          final String oldPath = entry.getOldPath();
          final String newPath = entry.getNewPath();

          for (final LANGUAGE language : languages) {
            if (language.isTarget(oldPath) && language.isTarget(newPath)) {
              final String message = commit.getFullMessage();
              final String id = commit.getId()
                  .getName();
              final String author = commit.getAuthorIdent()
                  .getName();
              final Revision revision =
                  new Revision(software, id, StringUtility.getDateString(date), message, author);
              revisions.add(revision);
              if (isVerbose) {
                System.out.println(id + " (" + date + ") has been identified.");
              }
              continue COMMIT;
            }
          }
        }
      }
    } catch (final Exception e) {
      e.printStackTrace();
      System.exit(0);
    }

    Collections.sort(revisions);
    return revisions;
  }
}
