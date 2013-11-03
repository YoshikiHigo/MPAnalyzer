package jp.ac.osaka_u.ist.sdl.mpanalyzer.gui.clpanel;

import java.awt.Color;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Map.Entry;
import java.util.Observable;
import java.util.Observer;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;

import jp.ac.osaka_u.ist.sdl.mpanalyzer.data.Modification;
import jp.ac.osaka_u.ist.sdl.mpanalyzer.data.ModificationPattern;
import jp.ac.osaka_u.ist.sdl.mpanalyzer.gui.ObservedModificationPatterns;
import jp.ac.osaka_u.ist.sdl.mpanalyzer.gui.ObservedModificationPatterns.MPLABEL;

public class CLPanel extends JTextArea implements Observer {

	public final JScrollPane scrollPane;
	private SortedMap<Long, String> revisions;

	public CLPanel() {
		super("");

		this.setEditable(false);
		this.setLineWrap(true);
		this.setRows(5);

		this.scrollPane = new JScrollPane();
		this.scrollPane.setViewportView(this);
		this.scrollPane
				.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		this.scrollPane
				.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);

		this.scrollPane.setBorder(new TitledBorder(new LineBorder(Color.black),
				"Revisions and Commit Logs"));

		this.addMouseListener(new MouseListener() {

			@Override
			public void mouseClicked(final MouseEvent e) {
				final int button = e.getButton();
				if (MouseEvent.BUTTON1 == button) {
					if (e.getClickCount() == 2) {

						final StringBuilder text = new StringBuilder();
						for (final Entry<Long, String> entry : CLPanel.this.revisions
								.entrySet()) {
							text.append(entry.getKey());
							text.append(" :  ");
							text.append(entry.getValue());
							text.append(System.getProperty("line.separator"));
							text.append("------------------------------");
							text.append(System.getProperty("line.separator"));
						}

						SwingUtilities.invokeLater(new Runnable() {
							@Override
							public void run() {
								new CLWindow(text.toString());
							}
						});
					}
				}
			}

			@Override
			public void mousePressed(MouseEvent e) {
			}

			@Override
			public void mouseReleased(MouseEvent e) {
			}

			@Override
			public void mouseEntered(MouseEvent e) {
			}

			@Override
			public void mouseExited(MouseEvent e) {
			}
		});
	}

	@Override
	public void update(final Observable o, final Object arg) {

		if (o instanceof ObservedModificationPatterns) {
			final ObservedModificationPatterns observedModificationPatterns = (ObservedModificationPatterns) o;
			if (observedModificationPatterns.label.equals(MPLABEL.SELECTED)) {
				this.setText("");
				if (observedModificationPatterns.isSet()) {
					final ModificationPattern pattern = observedModificationPatterns
							.get().first();
					this.revisions = new TreeMap<Long, String>();
					for (final Modification m : pattern.getModifications()) {
						final long revision = m.revision.number;
						final String message = m.revision.message;
						if (!this.revisions.containsKey(revision)) {
							this.revisions.put(revision, message);
						}
					}
					final StringBuilder text = new StringBuilder();
					for (final Entry<Long, String> entry : this.revisions
							.entrySet()) {
						text.append(entry.getKey());
						text.append(": ");
						text.append(entry.getValue());
					}
					this.setText(text.toString());
				}
			}
		}
	}
}
