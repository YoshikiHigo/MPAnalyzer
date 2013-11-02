package jp.ac.osaka_u.ist.sdl.mpanalyzer.gui.dcode;

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
import jp.ac.osaka_u.ist.sdl.mpanalyzer.StringUtility;
import jp.ac.osaka_u.ist.sdl.mpanalyzer.data.CodeFragment;
import jp.ac.osaka_u.ist.sdl.mpanalyzer.data.Statement;
import jp.ac.osaka_u.ist.sdl.mpanalyzer.gui.ObservedCodeFragments;
import jp.ac.osaka_u.ist.sdl.mpanalyzer.gui.ObservedCodeFragments.CFLABEL;
import jp.ac.osaka_u.ist.sdl.mpanalyzer.gui.ObservedFiles;
import jp.ac.osaka_u.ist.sdl.mpanalyzer.gui.ObservedFiles.FLABEL;
import jp.ac.osaka_u.ist.sdl.mpanalyzer.gui.ObservedRevisions;
import jp.ac.osaka_u.ist.sdl.mpanalyzer.gui.ObservedRevisions.RLABEL;

import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import org.tmatesoft.svn.core.wc.SVNRevision;
import org.tmatesoft.svn.core.wc.SVNWCClient;

public class DCode extends JTextArea implements Observer {

	static final private String PATH_TO_REPOSITORY = Config
			.getPATH_TO_REPOSITORY();
	static final private String TARGET = Config.getTARGET();
	static final private String LANGUAGE = Config.getLanguage();

	static public final int TAB_SIZE = 4;

	public final JScrollPane scrollPane;

	private Long revision;
	private CodeFragment codefragment;
	private List<Statement> statements;

	public DCode() {

		this.setTabSize(TAB_SIZE);

		this.revision = null;
		this.codefragment = null;
		this.statements = new ArrayList<Statement>();

		final Insets margin = new Insets(5, 50, 5, 5);
		this.setMargin(margin);
		this.setUI(new DCodeUI(new HashSet<Integer>(), this, margin));
		this.setText("");
		this.setEditable(false);

		this.scrollPane = new JScrollPane();
		this.scrollPane.setViewportView(this);
		this.scrollPane
				.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		this.scrollPane
				.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);

		this.setTitle(null);

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
						DCode.this.display();
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

	private void setTitle(final String path) {
		final StringBuilder title = new StringBuilder();
		title.append("Source Code View");
		if (null != path) {
			title.append(" (");
			title.append(path);
			title.append(")");
		}
		this.scrollPane.setBorder(new TitledBorder(new LineBorder(Color.black),
				title.toString()));
	}

	@Override
	public void update(final Observable o, final Object arg) {

		if (o instanceof ObservedFiles) {
			final ObservedFiles observedFiles = (ObservedFiles) o;
			if (observedFiles.label.equals(FLABEL.SELECTED)) {

				this.setText("");

				if (observedFiles.isSet()) {

					try {

						final String path = observedFiles.get().first();

						final SVNURL fileurl = SVNURL.fromFile(new File(
								PATH_TO_REPOSITORY
										+ System.getProperty("file.separator")
										+ path));
						final SVNWCClient wcClient = SVNClientManager
								.newInstance().getWCClient();

						final StringBuilder text = new StringBuilder();
						wcClient.doGetFileContents(fileurl,
								SVNRevision.create(this.revision.longValue()),
								SVNRevision.create(this.revision.longValue()),
								false, new OutputStream() {
									@Override
									public void write(int b) throws IOException {
										text.append((char) b);
									}
								});

						DCode.this.statements = StringUtility
								.splitToStatements(text.toString(), LANGUAGE);
						final SortedSet<Integer> highlightedLines = this
								.getHighlightedLines(DCode.this.statements,
										this.codefragment.statements);

						final Insets margin = new Insets(5, 50, 5, 5);
						this.setMargin(margin);
						this.setUI(new DCodeUI(highlightedLines, this, margin));
						this.setText(text.toString());
						this.setTitle(path);

					} catch (final Exception e) {
						e.printStackTrace();
					}
				}
			}
		}

		else if (o instanceof ObservedRevisions) {
			final ObservedRevisions observedRevisions = (ObservedRevisions) o;
			if (observedRevisions.label.equals(RLABEL.DETECTION)) {
				if (observedRevisions.isSet()) {
					this.revision = observedRevisions.get().first();
				}
			}
		}

		else if (o instanceof ObservedCodeFragments) {
			final ObservedCodeFragments observedCodeFragments = (ObservedCodeFragments) o;
			if (observedCodeFragments.label.equals(CFLABEL.SELECTED)) {
				if (observedCodeFragments.isSet()) {
					this.codefragment = observedCodeFragments.get().first();
				}
			}
		}
	}

	private SortedSet<Integer> getHighlightedLines(
			final List<Statement> statements, final List<Statement> pattern) {

		final SortedSet<Integer> lines = new TreeSet<Integer>();
		final SortedSet<Integer> candidates = new TreeSet<Integer>();

		int pIndex = 0;
		for (int index = 0; index < statements.size(); index++) {

			final Statement statement = statements.get(index);
			if (statement.hash == pattern.get(pIndex).hash) {
				pIndex++;
				candidates.add(statement.tokens.get(0).line - 1);
				candidates
						.add(statement.tokens.get(statement.tokens.size() - 1).line - 1);
				if (pIndex == pattern.size()) {
					pIndex = 0;
					lines.addAll(candidates);
					candidates.clear();
				}
			}

			else {
				pIndex = 0;
				candidates.clear();
			}
		}

		return lines;
	}

	private SortedSet<Integer> getPatternLines(
			final List<Statement> statements, final List<Statement> pattern) {

		final SortedSet<Integer> lines = new TreeSet<Integer>();
		int patternLine = 0;

		int pIndex = 0;
		for (int index = 0; index < statements.size(); index++) {

			final Statement statement = statements.get(index);
			if (statement.hash == pattern.get(pIndex).hash) {
				if (0 == pIndex) {
					patternLine = statement.tokens.get(0).line - 1;
				}
				pIndex++;
				if (pIndex == pattern.size()) {
					pIndex = 0;
					lines.add(patternLine);
					patternLine = 0;
				}
			}

			else {
				pIndex = 0;
				patternLine = 0;
			}
		}

		return lines;
	}

	private void display() {

		final Document doc = this.getDocument();
		final Element root = doc.getDefaultRootElement();

		final SortedSet<Integer> patternLines = this.getPatternLines(
				this.statements, this.codefragment.statements);
		if (patternLines.isEmpty()) {
			return;
		}

		final int currentCaretPosition = this.getCaretPosition();

		try {

			int nextOffset = 0;
			for (final Integer line : patternLines) {
				final Element element = root.getElement(Math.max(1, line - 2));
				if (currentCaretPosition < element.getStartOffset()) {
					nextOffset = element.getStartOffset();
					break;
				}
			}
			if (0 == nextOffset) {
				final Element element = root.getElement(Math.max(1,
						patternLines.first() - 2));
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
