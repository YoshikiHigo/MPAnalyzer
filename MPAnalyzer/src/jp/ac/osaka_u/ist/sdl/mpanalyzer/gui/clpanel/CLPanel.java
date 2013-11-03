package jp.ac.osaka_u.ist.sdl.mpanalyzer.gui.clpanel;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;

import jp.ac.osaka_u.ist.sdl.mpanalyzer.data.Modification;
import jp.ac.osaka_u.ist.sdl.mpanalyzer.data.ModificationPattern;
import jp.ac.osaka_u.ist.sdl.mpanalyzer.gui.ObservedModificationPatterns;
import jp.ac.osaka_u.ist.sdl.mpanalyzer.gui.ObservedModificationPatterns.MPLABEL;

public class CLPanel extends JTextArea implements Observer {

	public final JScrollPane scrollPane;

	public CLPanel() {
		this.setEditable(false);

		this.scrollPane = new JScrollPane();
		this.scrollPane.setViewportView(this);
		this.scrollPane
				.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		this.scrollPane
				.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);

		this.scrollPane.setBorder(new TitledBorder(new LineBorder(Color.black),
				"Revisions and Commit Logs"));

		this.setRows(5);
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
					final Map<Long, String> messages = new HashMap<Long, String>();
					for (final Modification m : pattern.getModifications()) {
						final long revision = m.revision.number;
						final String message = m.revision.message;
						if (!messages.containsKey(revision)) {
							messages.put(revision, message);
						}
					}
					final StringBuilder text = new StringBuilder();
					for (final Entry<Long, String> entry : messages.entrySet()) {
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
