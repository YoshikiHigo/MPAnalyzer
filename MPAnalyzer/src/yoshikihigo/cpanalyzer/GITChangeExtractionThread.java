package yoshikihigo.cpanalyzer;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.RawText;
import org.eclipse.jgit.diff.RawTextComparator;
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
	private PlotWalk revWalk;
	private ObjectReader reader;

	public GITChangeExtractionThread(final Revision revision,
			final Repository repository, final PlotWalk revWalk,
			final ObjectReader reader) {
		this.revision = revision;
		this.repository = repository;
		this.revWalk = revWalk;
		this.reader = reader;
	}

	@Override
	public void run() {

		final long id = Thread.currentThread().getId();
		final Set<LANGUAGE> languages = CPAConfig.getInstance().getLANGUAGE();
		final String software = CPAConfig.getInstance().getSOFTWARE();
		final boolean ONLY_CONDITION = CPAConfig.getInstance()
				.isONLY_CONDITION();
		final boolean IGNORE_IMPORT = CPAConfig.getInstance().isIGNORE_IMPORT();
		final boolean IS_VERBOSE = CPAConfig.getInstance().isVERBOSE();

		RevCommit commit = null;
		synchronized (LOCK) {
			try {
				final ObjectId commitId = this.repository
						.resolve(this.revision.id);
				commit = this.revWalk.parseCommit(commitId);
			} catch (final IOException e) {
				e.printStackTrace();
				return;
			}
			if (1 != commit.getParentCount()) {
				return;
			}
		}

		if (IS_VERBOSE) {
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
				formatter.close();
			}

			final LCS lcs = new LCS(software, this.revision);

			for (final DiffEntry entry : diffEntries) {
				final String oldPath = entry.getOldPath();
				final String newPath = entry.getNewPath();

				if (!languages.stream()
						.anyMatch(lang -> lang.isTarget(oldPath))
						|| !languages.stream().anyMatch(
								lang -> lang.isTarget(newPath))) {
					continue;
				}

				if (IS_VERBOSE) {
					final StringBuilder progress = new StringBuilder();
					progress.append(" ");
					progress.append(id);
					progress.append(": extracting changes from ");
					progress.append(oldPath);
					System.out.println(progress.toString());
				}

				String beforeText = "";
				String afterText = "";
				synchronized (LOCK) {
					beforeText = this.readText(entry.getOldId());
					afterText = this.readText(entry.getNewId());
				}

				final LANGUAGE language = FileUtility.getLANGUAGE(oldPath);
				final List<Statement> beforeStatements = StringUtility
						.splitToStatements(beforeText.toString(), language);
				final List<Statement> afterStatements = StringUtility
						.splitToStatements(afterText.toString(), language);

				final List<Change> changes = lcs.getChanges(beforeStatements,
						afterStatements, oldPath);

				for (final Change change : changes) {

					if (ONLY_CONDITION && !change.isCondition()) {
						continue;
					}

					if (IGNORE_IMPORT && change.isImport()) {
						continue;
					}

					ChangeDAO.SINGLETON.addChange(change);
				}

			}
			diffEntries = null;
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

	private String readText(final AbbreviatedObjectId blobId) {
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
