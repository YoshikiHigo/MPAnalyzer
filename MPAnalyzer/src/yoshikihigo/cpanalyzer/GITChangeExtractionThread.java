package yoshikihigo.cpanalyzer;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.RawText;
import org.eclipse.jgit.diff.RawTextComparator;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.AbbreviatedObjectId;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revplot.PlotWalk;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.util.io.DisabledOutputStream;

import yoshikihigo.cpanalyzer.data.Change;
import yoshikihigo.cpanalyzer.data.Revision;
import yoshikihigo.cpanalyzer.data.Statement;
import yoshikihigo.cpanalyzer.db.ChangeDAO;

public class GITChangeExtractionThread extends Thread {

	final private static Object LOCK = new Object();

	final public Revision revision;
	private Repository repository;
	private ObjectReader reader;

	public GITChangeExtractionThread(final Revision revision) {
		this.revision = revision;
		this.repository = null;
		this.reader = null;
		final String repoPath = CPAConfig.getInstance()
				.getGITREPOSITORY_FOR_MINING();
		try {
			synchronized (LOCK) {
				this.repository = new FileRepository(new File(repoPath
						+ File.separator + ".git"));
				this.reader = this.repository.newObjectReader();
			}
		} catch (final IOException e) {
			System.err.println("invalid repository path: " + repoPath);
			System.exit(0);
		}
	}

	@Override
	public void run() {

		final long id = Thread.currentThread().getId();
		final Set<LANGUAGE> languages = CPAConfig.getInstance().getLANGUAGE();
		final String software = CPAConfig.getInstance().getSOFTWARE();
		final boolean onlyCondition = CPAConfig.getInstance()
				.isONLY_CONDITION();
		final boolean ignoreImport = CPAConfig.getInstance().isIGNORE_IMPORT();
		final boolean isVerbose = CPAConfig.getInstance().isVERBOSE();

		RevCommit commit = null;
		synchronized (LOCK) {
			try (final PlotWalk revWalk = new PlotWalk(this.repository)) {
				final ObjectId commitId = this.repository
						.resolve(this.revision.id);
				commit = revWalk.parseCommit(commitId);
			} catch (final IOException e) {
				e.printStackTrace();
				return;
			}
			if (1 != commit.getParentCount()) {
				return;
			}
		}

		if (isVerbose) {
			final StringBuilder progress = new StringBuilder();
			progress.append(id);
			progress.append(": checking commit ");
			progress.append(this.revision.id);
			progress.append(" (");
			progress.append(this.revision.date);
			progress.append(")");
			System.out.println(progress.toString());
		}

		final DiffFormatter formatter = new DiffFormatter(
				DisabledOutputStream.INSTANCE);
		formatter.setRepository(this.repository);
		formatter.setDiffComparator(RawTextComparator.DEFAULT);
		formatter.setDetectRenames(true);

		try {
			List<DiffEntry> diffEntries = null;
			synchronized (LOCK) {
				final RevCommit parent = commit.getParent(0);
				diffEntries = formatter.scan(parent.getId(), commit.getId());
			}
			for (final DiffEntry entry : diffEntries) {
				final String oldPath = entry.getOldPath();
				final String newPath = entry.getNewPath();
				if (!oldPath.endsWith(".java") || !newPath.endsWith(".java")) {
					continue;
				}

				if (isVerbose) {
					final StringBuilder progress = new StringBuilder();
					progress.append(" ");
					progress.append(id);
					progress.append(": extracting changes from ");
					progress.append(oldPath);
					System.out.println(progress.toString());
				}

				final String beforeText = this.readText(entry.getOldId());
				final String afterText = this.readText(entry.getNewId());

				final LANGUAGE language = FileUtility.getLANGUAGE(oldPath);
				final List<Statement> beforeStatements = StringUtility
						.splitToStatements(beforeText.toString(), language);
				final List<Statement> afterStatements = StringUtility
						.splitToStatements(afterText.toString(), language);

				final List<Change> changes = LCS.getChanges(beforeStatements,
						afterStatements, software, oldPath,
						this.revision.author, this.revision);

				if (onlyCondition) {
					ChangeDAO.SINGLETON.addChanges(changes.stream()
							.filter(change -> change.isCondition())
							.collect(Collectors.toList()));
				}

				else if (ignoreImport) {
					ChangeDAO.SINGLETON.addChanges(changes.stream()
							.filter(change -> !change.isImport())
							.collect(Collectors.toList()));
				}

				else {
					ChangeDAO.SINGLETON.addChanges(changes);
				}
			}
		} catch (final IOException e) {
			e.printStackTrace();
		}
		formatter.close();
	}

	private synchronized String readText(final AbbreviatedObjectId blobId) {
		try {
			final ObjectLoader loader = this.reader.open(blobId.toObjectId(),
					Constants.OBJ_BLOB);
			final RawText rawText = new RawText(loader.getCachedBytes());
			return rawText.getString(0, rawText.size(), false);
		} catch (final IOException e) {
			e.printStackTrace();
			return "";
		}
	}
}
