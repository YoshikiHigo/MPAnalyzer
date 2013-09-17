package jp.ac.osaka_u.ist.sdl.mpanalyzer.gui.dtree;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.tree.TreeCellRenderer;

class FileNodeRenderer extends JLabel implements TreeCellRenderer {

	public FileNodeRenderer() {
		this.setOpaque(true);
	}

	public Component getTreeCellRendererComponent(JTree tree, Object value,
			boolean selected, boolean expanded, boolean leaf, int row,
			boolean hasFocus) {

		final FileNode node = (FileNode) value;

		if (leaf) {

			if (0 == node.getCloneNumber()) {

				setText(node.getName());

				if (selected) {
					this.setBackground(Color.blue);
					this.setForeground(Color.white);
				} else {
					this.setBackground(Color.white);
					this.setForeground(Color.black);
				}

			} 
			
			else {

				setText(node.toString());

				if (selected) {
					this.setBackground(Color.blue);
					this.setForeground(Color.red);
				} else {
					this.setBackground(Color.white);
					this.setForeground(Color.blue);
				}
			}

			this.setIcon(UIManager.getIcon("Tree.leafIcon"));

		} 
		
		else {

			if (expanded) {

				setText(node.getName());

				if (selected) {
					this.setBackground(Color.blue);
					this.setForeground(Color.white);
				} else {
					this.setBackground(Color.white);
					this.setForeground(Color.black);
				}

				this.setIcon(UIManager.getIcon("Tree.openIcon"));

			} 
			
			else {

				if (0 == node.getCloneNumber()) {

					setText(node.getName());

					if (selected) {
						this.setBackground(Color.blue);
						this.setForeground(Color.white);
					} else {
						this.setBackground(Color.white);
						this.setForeground(Color.black);
					}

				} 
				
				else {

					setText(node.toString());

					if (selected) {
						this.setBackground(Color.blue);
						this.setForeground(Color.red);
					} else {
						this.setBackground(Color.white);
						this.setForeground(Color.blue);
					}
				}

				this.setIcon(UIManager.getIcon("Tree.closedIcon"));
			}
		}

		return this;
	}
}
