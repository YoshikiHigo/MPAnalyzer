package jp.ac.osaka_u.ist.sdl.mpanalyzer.gui.mpcode;

import java.awt.Color;
import java.awt.Insets;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;

import jp.ac.osaka_u.ist.sdl.mpanalyzer.data.ModificationPattern;
import jp.ac.osaka_u.ist.sdl.mpanalyzer.gui.CODE;
import jp.ac.osaka_u.ist.sdl.mpanalyzer.gui.ObservedModificationPatterns;
import jp.ac.osaka_u.ist.sdl.mpanalyzer.gui.ObservedModificationPatterns.MPLABEL;

public class MPCode extends JTextArea implements Observer {

	static public final int TAB_SIZE = 4;

	public final JScrollPane scrollPane;
	public final CODE code;

	public MPCode(final CODE code) {

		this.setTabSize(TAB_SIZE);

		final Insets margin = new Insets(5, 50, 5, 5);
		this.setMargin(margin);
		this.setUI(new MPCodeUI(this, margin));
		this.setText("");
		this.setEditable(false);

		this.scrollPane = new JScrollPane();
		this.scrollPane.setViewportView(this);
		this.scrollPane
				.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		this.scrollPane
				.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);

		switch (code) {
		case BEFORE:
			this.scrollPane.setBorder(new TitledBorder(new LineBorder(
					Color.black), "Code BEFORE Modification"));
			break;
		case AFTER:
			this.scrollPane.setBorder(new TitledBorder(new LineBorder(
					Color.black), "Code AFTER Modification"));
			break;
		default:
			assert false : "here shouldn't be reached!";
			System.exit(0);
		}

		this.code = code;
	}

	@Override
	public void update(final Observable o, final Object arg) {

		if (o instanceof ObservedModificationPatterns) {
			final ObservedModificationPatterns patterns = (ObservedModificationPatterns) o;
			if (patterns.label.equals(MPLABEL.SELECTED)) {

				this.setText("");

				if (patterns.isSet()) {
					final ModificationPattern pattern = patterns.get().first();
					final String text = this.code == CODE.BEFORE ? pattern
							.getModifications().get(0).before.text : pattern
							.getModifications().get(0).after.text;
					if (!text.isEmpty()) {
						this.setText(text);
					} else {
						this.setText("NO CODE");
					}
				}
			}

			else if (patterns.label.equals(MPLABEL.OVERLOOKED)) {

				this.setText("");

				if (patterns.isSet()) {
					final ModificationPattern pattern = patterns.get().first();
					final String text = pattern.getModifications().get(0).after.text;
					if (!text.isEmpty()) {
						this.setText(text);
					} else {
						this.setText("NO CODE");
					}
				}
			}
		}
	}
}
