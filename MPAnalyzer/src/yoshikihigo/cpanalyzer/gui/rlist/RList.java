package yoshikihigo.cpanalyzer.gui.rlist;

import java.awt.Color;
import java.awt.GridLayout;
import java.io.File;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;

import org.tmatesoft.svn.core.ISVNLogEntryHandler;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNLogEntry;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.internal.io.fs.FSRepositoryFactory;
import org.tmatesoft.svn.core.io.SVNRepository;

import yoshikihigo.cpanalyzer.CPAConfig;
import yoshikihigo.cpanalyzer.LANGUAGE;
import yoshikihigo.cpanalyzer.StringUtility;
import yoshikihigo.cpanalyzer.data.Revision;

public class RList extends JPanel {

	final public JScrollPane scrollPane;

	final ButtonGroup group;
	final Map<JRadioButton, Revision> buttonRevisionMap;

	public RList() {
		super();
		this.group = new ButtonGroup();
		this.buttonRevisionMap = new HashMap<JRadioButton, Revision>();
		try {
			final SortedSet<Revision> revisions = new TreeSet<Revision>(
					new Comparator<Revision>() {
						@Override
						public int compare(final Revision r1, final Revision r2) {
							if (r1.number < r2.number) {
								return 1;
							} else if (r1.number > r2.number) {
								return -1;
							} else {
								return 0;
							}
						}
					});

			// revisions.addAll(ReadOnlyDAO.getInstance().getRevisions());
			revisions.addAll(getRevisionsFromRepository());

			// final int threshold = 100;
			// this.setLayout(new GridLayout(
			// revisions.size() < threshold ? revisions.size() : threshold,
			// 1));
			this.setLayout(new GridLayout(revisions.size() > 100 ? 100
					: revisions.size(), 1));
			int number = 1;
			for (final Revision revision : revisions) {
				final StringBuilder text = new StringBuilder();
				text.append(revision.number);
				text.append(" (");
				text.append(revision.date);
				text.append(")");
				final JRadioButton button = new JRadioButton(text.toString(),
						true);
				this.group.add(button);
				this.add(button);
				this.buttonRevisionMap.put(button, revision);
				if (100 < ++number) {
					break;
				}
			}
		} catch (final Exception e) {
			this.add(new JLabel("Error happened in getting revisions."));
		}

		this.scrollPane = new JScrollPane();
		this.scrollPane.setViewportView(this);
		this.scrollPane
				.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		this.scrollPane
				.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		this.scrollPane.setBorder(new TitledBorder(new LineBorder(Color.black),
				"Revisions"));
	}

	public final long getSelectedRevision() {

		for (final Entry<JRadioButton, Revision> entry : this.buttonRevisionMap
				.entrySet()) {
			if (entry.getKey().isSelected()) {
				return entry.getValue().number;
			}
		}
		return 0;

		// Object[] selectedObjects = this.group.getSelection()
		// .getSelectedObjects();
		// if (null != selectedObjects) {
		// return Long.parseLong((String) selectedObjects[0]);
		// } else {
		// return 0;
		// }
	}

	private SortedSet<Revision> getRevisionsFromRepository() {

		try {

			final String repository = CPAConfig.getInstance()
					.getREPOSITORY_FOR_TEST();
			final Set<LANGUAGE> languages = CPAConfig.getInstance()
					.getLANGUAGE();

			final SVNURL url = SVNURL.fromFile(new File(repository));
			FSRepositoryFactory.setup();
			final SVNRepository svnRepository = FSRepositoryFactory.create(url);

			long startRevision = CPAConfig.getInstance()
					.getSTART_REVISION_FOR_TEST();
			long endRevision = CPAConfig.getInstance()
					.getEND_REVISION_FOR_TEST();

			if (startRevision < 0) {
				startRevision = 0l;
			}

			if (endRevision < 0) {
				endRevision = svnRepository.getLatestRevision();
			}

			final SortedSet<Revision> revisions = new TreeSet<Revision>();

			svnRepository.log(null, startRevision, endRevision, true, true,
					new ISVNLogEntryHandler() {
						public void handleLogEntry(SVNLogEntry logEntry)
								throws SVNException {
							for (final Object key : logEntry.getChangedPaths()
									.keySet()) {
								final String path = (String) key;
								final int number = (int) logEntry.getRevision();
								final String date = StringUtility
										.getDateString(logEntry.getDate());
								final String message = logEntry.getMessage();
								final String author = logEntry.getAuthor();
								final Revision revision = new Revision("",
										number, date, message, author);
								for (final LANGUAGE language : languages) {
									if (language.isTarget(path)) {
										revisions.add(revision);
										break;
									}
								}
							}
						}
					});

			return revisions;

		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}

		return new TreeSet<Revision>();
	}
}
