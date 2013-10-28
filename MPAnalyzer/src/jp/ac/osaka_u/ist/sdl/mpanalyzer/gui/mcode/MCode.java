package jp.ac.osaka_u.ist.sdl.mpanalyzer.gui.mcode;

import java.awt.Color;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Element;

import jp.ac.osaka_u.ist.sdl.mpanalyzer.Config;
import jp.ac.osaka_u.ist.sdl.mpanalyzer.LCS;
import jp.ac.osaka_u.ist.sdl.mpanalyzer.StringUtility;
import jp.ac.osaka_u.ist.sdl.mpanalyzer.data.CodeFragment;
import jp.ac.osaka_u.ist.sdl.mpanalyzer.data.Modification;
import jp.ac.osaka_u.ist.sdl.mpanalyzer.data.Statement;
import jp.ac.osaka_u.ist.sdl.mpanalyzer.gui.CODE;
import jp.ac.osaka_u.ist.sdl.mpanalyzer.gui.ObservedModifications;
import jp.ac.osaka_u.ist.sdl.mpanalyzer.gui.ObservedModifications.MLABEL;

import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.internal.io.fs.FSRepositoryFactory;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import org.tmatesoft.svn.core.wc.SVNRevision;
import org.tmatesoft.svn.core.wc.SVNWCClient;

public class MCode extends JTextArea implements Observer {

	private static final String LANGUAGE = Config.getLanguage();

	static public final int TAB_SIZE = 4;

	public final List<Modification> modifications;
	public final JScrollPane scrollPane;
	public final CODE code;

	public MCode(final CODE code) {

		this.setTabSize(TAB_SIZE);

		final Insets margin = new Insets(5, 50, 5, 5);
		this.setMargin(margin);
		this.setUI(new MCodeUI(code, new HashSet<Integer>(), this, margin));
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
					Color.black), "File BEFORE Modification"));
			break;
		case AFTER:
			this.scrollPane.setBorder(new TitledBorder(new LineBorder(
					Color.black), "File AFTER Modification"));
			break;
		default:
			assert false : "here shouldn't be reached!";
			System.exit(0);
		}

		this.code = code;
		this.modifications = new ArrayList<Modification>();

		this.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {

				final int button = e.getButton();
				final int clickCount = e.getClickCount();

				switch (button) {
				case MouseEvent.BUTTON1:
					switch (clickCount) {
					case 1:
						break;
					case 2:
						MCode.this.display();
						break;
					default:
					}
					break;
				case MouseEvent.BUTTON2:
					break;
				case MouseEvent.BUTTON3:
					break;
				default:
				}
			}
		});
	}

	@Override
	public void update(final Observable o, final Object arg) {

		if (o instanceof ObservedModifications) {
			final ObservedModifications observedModifications = (ObservedModifications) o;
			if (observedModifications.label.equals(MLABEL.SELECTED)) {

				this.setText("");

				if (observedModifications.isSet()) {

					try {

						final Modification modification = observedModifications
								.get().first();
						final long revision = modification.revision.number;
						final String filepath = modification.filepath;
						final String PATH_TO_REPOSITORY = Config
								.getPATH_TO_REPOSITORY();
						final SVNURL url = SVNURL.fromFile(new File(
								PATH_TO_REPOSITORY + "/" + filepath));
						FSRepositoryFactory.setup();
						final SVNWCClient wcClient = SVNClientManager
								.newInstance().getWCClient();

						final StringBuilder beforeText = new StringBuilder();
						wcClient.doGetFileContents(url,
								SVNRevision.create(revision - 1l),
								SVNRevision.create(revision - 1l), false,
								new OutputStream() {
									@Override
									public void write(int b) throws IOException {
										beforeText.append((char) b);
									}
								});

						final StringBuilder afterText = new StringBuilder();
						wcClient.doGetFileContents(url,
								SVNRevision.create(revision),
								SVNRevision.create(revision), false,
								new OutputStream() {
									@Override
									public void write(int b) throws IOException {
										afterText.append((char) b);
									}
								});

						final List<Statement> beforeStatements = StringUtility
								.splitToStatements(beforeText.toString(),
										LANGUAGE);
						final List<Statement> afterStatements = StringUtility
								.splitToStatements(afterText.toString(),
										LANGUAGE);

						this.modifications.clear();
						final List<Modification> modifications = LCS
								.getModifications(beforeStatements,
										afterStatements, filepath,
										modification.revision);
						for (final Modification m : modifications) {
							if (m.isSamePattern(modification)) {
								this.modifications.add(m);
							}
						}

						final Set<Integer> lines = new HashSet<Integer>();
						for (final Modification m : modifications) {

							final CodeFragment cf;
							switch (this.code) {
							case BEFORE:
								cf = m.before;
								break;
							case AFTER:
								cf = m.after;
								break;
							default:
								cf = new CodeFragment("");
								assert false : "here shouldn't be reached!";
								System.exit(0);
							}
							final int startLine = cf.getStartLine();
							final int endLine = cf.getEndLine();
							for (int line = startLine - 1; line < endLine; line++) {
								lines.add(line);
							}
						}

						final Insets margin = new Insets(5, 50, 5, 5);
						this.setMargin(margin);
						this.setUI(new MCodeUI(this.code, lines, this, margin));
						switch (this.code) {
						case BEFORE:
							this.setText(beforeText.toString());
							break;
						case AFTER:
							this.setText(afterText.toString());
							break;
						default:
							assert false : "here shouldn't be reached!";
							System.exit(0);
						}

					} catch (final Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

	private SortedSet<Integer> getModificationLines() {

		final SortedSet<Integer> lines = new TreeSet<Integer>();

		for (final Modification m : this.modifications) {
			final CodeFragment c;
			switch (this.code) {
			case BEFORE:
				c = m.before;
				break;
			case AFTER:
				c = m.after;
				break;
			default:
				c = null;
				assert false : "here shouldn't be reached!";
				System.exit(0);
			}
			if (!c.statements.isEmpty()) {
				lines.add(c.statements.get(0).tokens.get(0).line - 1);
			}
		}

		return lines;
	}

	private void display() {

		final Document doc = this.getDocument();
		final Element root = doc.getDefaultRootElement();

		final SortedSet<Integer> lines = this.getModificationLines();
		if (lines.isEmpty()) {
			return;
		}

		final int currentCaretPosition = this.getCaretPosition();

		try {

			int nextOffset = 0;
			for (final Integer line : lines) {
				final Element element = root.getElement(Math.max(1, line - 2));
				if (currentCaretPosition < element.getStartOffset()) {
					nextOffset = element.getStartOffset();
					break;
				}
			}
			if (0 == nextOffset) {
				final Element element = root.getElement(Math.max(1,
						lines.first() - 2));
				nextOffset = element.getStartOffset();
			}

			final Rectangle rect = this.modelToView(nextOffset);
			final Rectangle vr = this.scrollPane.getViewport().getViewRect();

			if ((null != rect) && (null != vr)) {
				rect.setSize(10, vr.height);
				this.scrollRectToVisible(rect);
				this.setCaretPosition(nextOffset);
			}
		} catch (BadLocationException e) {
			System.err.println(e.getMessage());
		}
	}
}
