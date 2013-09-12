package jp.ac.osaka_u.ist.sdl.mpanalyzer.gui.dtree;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
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
import jp.ac.osaka_u.ist.sdl.mpanalyzer.data.CodeFragment;
import jp.ac.osaka_u.ist.sdl.mpanalyzer.data.Token;

import org.tmatesoft.svn.core.SVNDepth;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.internal.io.fs.FSRepositoryFactory;
import org.tmatesoft.svn.core.internal.wc17.SVNRemoteStatusEditor17.FileInfo;
import org.tmatesoft.svn.core.wc.ISVNDiffStatusHandler;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import org.tmatesoft.svn.core.wc.SVNDiffClient;
import org.tmatesoft.svn.core.wc.SVNDiffStatus;
import org.tmatesoft.svn.core.wc.SVNRevision;
import org.tmatesoft.svn.core.wc.SVNWCClient;

public class DTree extends JTree {

	class DTreeSelectionEventHandler implements TreeSelectionListener {
		public void valueChanged(TreeSelectionEvent e) {

			final TreePath[] selectionPath = DTree.this.getSelectionPaths();
			final Set<FileInfo> selectedFiles = new HashSet<FileInfo>();

			if (null != selectionPath) {
				for (int i = 0; i < selectionPath.length; i++) {
					final FileNode fileNode = (FileNode) selectionPath[i]
							.getLastPathComponent();
					if (fileNode.isLeaf()) {
						selectedFiles.add((FileInfo) fileNode.getUserObject());
					}
				}
			}

			// SelectedEntities.<FileInfo> getInstance(FILES).setAll(
			// selectedFiles, DirectoryTree.this);
		}
	}

	static final private String PATH_TO_REPOSITORY = Config
			.getPATH_TO_REPOSITORY();
	static final private String TARGET = Config.getTARGET();

	private final JScrollPane scrollPane;

	private final FileNode rootNode;

	private final DTreeSelectionEventHandler directoryTreeSelectionEventHandler;

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
	}

	public void update(final long revision, final CodeFragment codeFragment) {
		final List<FileData> data = this.getFileData(revision, codeFragment);
		final FileNode rootNode = this.makeTreeNode(data);
		DefaultTreeModel treeModel = new DefaultTreeModel(rootNode);
		this.setModel(treeModel);
		this.setCellRenderer(new FileNodeRenderer());
		this.expandTreeNode(this.rootNode);
		this.setRootVisible(false);

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
			final SVNWCClient wcClient = SVNClientManager.newInstance()
					.getWCClient();
			final SVNDiffClient diffClient = SVNClientManager.newInstance()
					.getDiffClient();

			final SortedSet<String> files = new TreeSet<String>();

			diffClient.doDiffStatus(url, SVNRevision.create(0), url,
					SVNRevision.create(revision), SVNDepth.INFINITY, true,
					new ISVNDiffStatusHandler() {

						@Override
						public void handleDiffStatus(
								final SVNDiffStatus diffStatus) {
							final String path = diffStatus.getPath();
							if (path.endsWith(".java")
									&& path.startsWith(TARGET)) {
								files.add(path);
							}
						}
					});

			for (final String path : files) {
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

				final List<Token> tokens = Token.getTokens(text.toString());
				final int count = this.getCount(tokens,
						codeFragment.getTokens());
				final String[] separatedPath = path.split("/");
				final FileData fileData = new FileData(separatedPath, count);
				data.add(fileData);
			}

		} catch (final SVNException exception) {
			exception.printStackTrace();
		}

		return data;
	}

	private int getCount(final List<Token> tokens, final List<Token> pattern) {

		int count = 0;

		int pIndex = 0;
		for (int index = 0; index < tokens.size(); index++) {

			if (tokens.get(index).equals(pattern.get(pIndex))) {
				pIndex++;
				if (pIndex == pattern.size()) {
					pIndex = 0;
				}
			} else {
				pIndex = 0;
			}
		}

		return count;
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
