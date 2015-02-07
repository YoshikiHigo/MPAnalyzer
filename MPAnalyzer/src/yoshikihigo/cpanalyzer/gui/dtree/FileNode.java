package yoshikihigo.cpanalyzer.gui.dtree;

import java.util.Enumeration;

import javax.swing.tree.DefaultMutableTreeNode;

import org.tmatesoft.svn.core.internal.wc17.SVNRemoteStatusEditor17.FileInfo;

public final class FileNode extends DefaultMutableTreeNode {

	final private String name;
	private int cloneNumber;

	public FileNode(final String name, final int cloneNumber) {

		super(name);

		this.name = name;
		this.cloneNumber = cloneNumber;
	}

	FileNode add(final FileNode newFileNode, int cloneNumber) {

		for (Enumeration enumeration = this.children(); enumeration
				.hasMoreElements();) {

			final FileNode subNode = (FileNode) enumeration.nextElement();
			if (subNode.equals(newFileNode)) {
				this.cloneNumber += cloneNumber;
				return subNode;
			}
		}

		super.add(newFileNode);
		this.cloneNumber += cloneNumber;

		return newFileNode;
	}

	public boolean equals(Object o) {

		if (o instanceof FileNode) {

			final Object thisObject = this.getUserObject();
			final Object targetObject = ((FileNode) o).getUserObject();

			if ((thisObject instanceof String)
					&& (targetObject instanceof String)) {

				return thisObject.equals(targetObject);

			} else if ((thisObject instanceof FileInfo)
					&& (targetObject instanceof FileInfo)) {

				return thisObject.equals(targetObject);

			} else {
				return false;
			}

		} else {
			return super.equals(o);
		}
	}

	public String toString() {

		final StringBuilder text = new StringBuilder();
		text.append(this.name);
		text.append(" (");
		text.append(this.cloneNumber);
		text.append(")");
		return text.toString();
	}

	public String getName() {
		return this.name;
	}

	public int getCloneNumber() {
		return this.cloneNumber;
	}
}
