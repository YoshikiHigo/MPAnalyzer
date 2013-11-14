package jp.ac.osaka_u.ist.sdl.mpanalyzer.gui.dtree;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import jp.ac.osaka_u.ist.sdl.mpanalyzer.Config;
import jp.ac.osaka_u.ist.sdl.mpanalyzer.StringUtility;
import jp.ac.osaka_u.ist.sdl.mpanalyzer.data.CodeFragment;
import jp.ac.osaka_u.ist.sdl.mpanalyzer.data.Statement;
import jp.ac.osaka_u.ist.sdl.mpanalyzer.gui.ObservedFiles;
import jp.ac.osaka_u.ist.sdl.mpanalyzer.gui.ObservedFiles.FLABEL;
import jp.ac.osaka_u.ist.sdl.mpanalyzer.gui.progress.ProgressDialog;

import org.tmatesoft.svn.core.ISVNDirEntryHandler;
import org.tmatesoft.svn.core.SVNDepth;
import org.tmatesoft.svn.core.SVNDirEntry;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNNodeKind;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.internal.io.fs.FSRepositoryFactory;
import org.tmatesoft.svn.core.internal.wc17.SVNRemoteStatusEditor17.FileInfo;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import org.tmatesoft.svn.core.wc.SVNLogClient;
import org.tmatesoft.svn.core.wc.SVNRevision;
import org.tmatesoft.svn.core.wc.SVNWCClient;

public class DTree extends JTree implements Observer {

	class DTreeSelectionEventHandler implements TreeSelectionListener {
		public void valueChanged(TreeSelectionEvent e) {

			final TreePath[] selectionPath = DTree.this.getSelectionPaths();
			final Set<FileInfo> selectedFiles = new HashSet<FileInfo>();

			if (null != selectionPath) {
				for (int i = 0; i < selectionPath.length; i++) {
					final FileNode fileNode = (FileNode) selectionPath[i]
							.getLastPathComponent();
					if (fileNode.isLeaf()) {
						final Object[] objects = fileNode.getUserObjectPath();
						final String path = this.concatenate(objects);
						ObservedFiles.getInstance(FLABEL.SELECTED).set(path,
								DTree.this);
					}

					else {
						ObservedFiles.getInstance(FLABEL.SELECTED).clear(
								DTree.this);
					}
				}
			}
		}

		private String concatenate(final Object[] objects) {
			final StringBuilder text = new StringBuilder();
			for (int index = 1; index < objects.length; index++) {
				text.append(objects[index].toString());
				text.append("/");
			}
			text.deleteCharAt(text.length() - 1);
			return text.toString();
		}
	}

	static final private String PATH_TO_REPOSITORY = Config
			.getPATH_TO_REPOSITORY();
	static final private String LANGUAGE = Config.getLanguage();

	public final JScrollPane scrollPane;

	private final FileNode rootNode;

	private final DTreeSelectionEventHandler directoryTreeSelectionEventHandler;

	private ProgressDialog progressDialog;

	public DTree() {

		super();

		this.rootNode = this.makeTreeNode(new ArrayList<FileData>());
		DefaultTreeModel treeModel = new DefaultTreeModel(this.rootNode);
		this.setModel(treeModel);
		this.setCellRenderer(new FileNodeRenderer());

		this.scrollPane = new JScrollPane();
		this.scrollPane.setViewportView(this);
		this.scrollPane
				.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		this.scrollPane
				.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

		this.setRootVisible(false);

		this.getSelectionModel().setSelectionMode(
				TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);

		this.scrollPane.setBorder(new TitledBorder(new LineBorder(Color.black),
				"Directory Tree"));

		this.directoryTreeSelectionEventHandler = new DTreeSelectionEventHandler();
		this.addTreeSelectionListener(this.directoryTreeSelectionEventHandler);

		this.progressDialog = null;
	}

	public void setProgressDialog(final ProgressDialog progressDialog) {
		this.progressDialog = progressDialog;
	}

	public void update(final long revision, final CodeFragment codeFragment) {
		final List<FileData> data = this.getFileData(revision, codeFragment);
		final FileNode rootNode = this.makeTreeNode(data);
		DefaultTreeModel treeModel = new DefaultTreeModel(rootNode);
		this.setModel(treeModel);
		this.setCellRenderer(new FileNodeRenderer());
		this.expandTreeNode(this.rootNode);
		this.setRootVisible(false);

		if (this.progressDialog.isVisible()) {
			this.progressDialog.dispose();
		}

		this.repaint();
	}

	public JScrollPane getScrollPane() {
		return this.scrollPane;
	}

	private FileNode makeTreeNode(final List<FileData> data) {

		final FileNode rootNode = new FileNode("root", 0);

		for (final FileData fileData : data) {

			FileNode currentNode = rootNode;

			final int cloneNumber = fileData.clones;
			final String[] path = fileData.path;

			for (int j = 0; j < path.length - 1; j++) {

				FileNode newFileNode = new FileNode(path[j], 0);
				currentNode = currentNode.add(newFileNode, cloneNumber);
			}

			final String fileName = fileData.path[fileData.path.length - 1];
			final FileNode newLeafFileNode = new FileNode(fileName,
					fileData.clones);
			currentNode.add(newLeafFileNode, cloneNumber);
		}

		return rootNode;
	}

	public void expandTreeNode(final FileNode fileNode) {

		TreePath treePath = new TreePath(fileNode.getPath());
		this.expandPath(treePath);

		Enumeration enumeration = fileNode.children();
		while (enumeration.hasMoreElements()) {

			FileNode subFileNode = (FileNode) enumeration.nextElement();
			if (!subFileNode.isLeaf()) {
				this.expandTreeNode(subFileNode);
			}
		}
	}

	private List<FileData> getFileData(final long revision,
			final CodeFragment codeFragment) {

		final List<FileData> data = new ArrayList<FileData>();

		try {
			final SVNURL url = SVNURL.fromFile(new File(PATH_TO_REPOSITORY));
			FSRepositoryFactory.setup();
			final SVNLogClient logClient = SVNClientManager.newInstance()
					.getLogClient();
			final SVNWCClient wcClient = SVNClientManager.newInstance()
					.getWCClient();

			this.progressDialog.note.setText("preparing a file list ...");
			this.progressDialog.repaint();

			final SortedSet<String> files = new TreeSet<String>();

			logClient.doList(url, SVNRevision.create(revision),
					SVNRevision.create(revision), true, SVNDepth.INFINITY,
					SVNDirEntry.DIRENT_ALL, new ISVNDirEntryHandler() {

						@Override
						public void handleDirEntry(final SVNDirEntry entry)
								throws SVNException {

							if (progressDialog.canceled.isCanceled()) {
								return;
							}

							progressDialog.progressBar
									.setMaximum(progressDialog.progressBar
											.getMaximum() + 1);
							progressDialog.progressBar
									.setValue(progressDialog.progressBar
											.getValue() + 1);
							progressDialog.repaint();

							if (entry.getKind() == SVNNodeKind.FILE) {
								final String path = entry.getRelativePath();
								if (path.endsWith(".java")) {

									progressDialog.note
											.setText("preparing files ... "
													+ path);
									progressDialog.repaint();

									files.add(path);
								}
							}
						}
					});

			this.progressDialog.progressBar.setMaximum(files.size());
			this.progressDialog.progressBar.setValue(0);

			int progress = 1;
			for (final String path : files) {

				if (progressDialog.canceled.isCanceled()) {
					return new ArrayList<FileData>();
				}

				this.progressDialog.progressBar.setValue(progress++);
				this.progressDialog.note.setText("detecting patterns ... "
						+ path);
				this.progressDialog.repaint();

				final SVNURL fileurl = SVNURL.fromFile(new File(
						PATH_TO_REPOSITORY
								+ System.getProperty("file.separator") + path));

				final StringBuilder text = new StringBuilder();
				wcClient.doGetFileContents(fileurl,
						SVNRevision.create(revision),
						SVNRevision.create(revision), false,
						new OutputStream() {
							@Override
							public void write(int b) throws IOException {
								text.append((char) b);
							}
						});

				final List<Statement> statements = StringUtility
						.splitToStatements(text.toString(), LANGUAGE);
				final int count = this.getCount(statements,
						codeFragment.statements);

				final String[] separatedPath = path.split("/");
				final FileData fileData = new FileData(separatedPath, count);
				data.add(fileData);
			}

		} catch (final SVNException exception) {
			exception.printStackTrace();
		}

		return data;
	}

	private int getCount(final List<Statement> statements,
			final List<Statement> pattern) {

		int count = 0;
		int pIndex = 0;
		for (int index = 0; index < statements.size(); index++) {

			if (statements.get(index).hash == pattern.get(pIndex).hash) {
				pIndex++;
				if (pIndex == pattern.size()) {
					count++;
					pIndex = 0;
				}
			}

			else {
				pIndex = 0;
			}
		}

		return count;
	}

	@Override
	public void update(final Observable o, final Object arg) {
	}

	class FileData {

		final private String[] path;
		final private int clones;

		FileData(final String[] path, final int clones) {
			this.path = path;
			this.clones = clones;
		}
	}
}
