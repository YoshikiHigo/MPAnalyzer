package jp.ac.osaka_u.ist.sdl.mpanalyzer.gui.rlist;

import java.awt.Color;
import java.awt.GridLayout;
import java.util.SortedSet;

import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;

import jp.ac.osaka_u.ist.sdl.mpanalyzer.data.Revision;
import jp.ac.osaka_u.ist.sdl.mpanalyzer.db.ReadOnlyDAO;

public class RList extends JPanel {

	final public JScrollPane scrollPane;

	final ButtonGroup group;

	public RList() {
		super();
		this.group = new ButtonGroup();
		try {
			final SortedSet<Revision> revisions = ReadOnlyDAO.getInstance()
					.getRevisions();
			this.setLayout(new GridLayout(revisions.size(), 1));
			for (final Revision revision : revisions) {
				final StringBuilder text = new StringBuilder();
				text.append(revision.number);
				text.append(" (");
				text.append(revision.date);
				text.append(")");
				final JRadioButton button = new JRadioButton(text.toString(),
						false);
				this.group.add(button);
				this.add(button, 0);
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
		return Long.parseLong((String) this.group.getSelection()
				.getSelectedObjects()[0]);
	}
}
