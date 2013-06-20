package jp.ac.osaka_u.ist.sdl.mpanalyzer.gui.mcode;

import java.awt.Color;
import java.awt.Insets;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;

import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;

import jp.ac.osaka_u.ist.sdl.mpanalyzer.Config;
import jp.ac.osaka_u.ist.sdl.mpanalyzer.LCS;
import jp.ac.osaka_u.ist.sdl.mpanalyzer.data.CodeFragment;
import jp.ac.osaka_u.ist.sdl.mpanalyzer.data.Modification;
import jp.ac.osaka_u.ist.sdl.mpanalyzer.data.Statement;
import jp.ac.osaka_u.ist.sdl.mpanalyzer.data.Token;
import jp.ac.osaka_u.ist.sdl.mpanalyzer.gui.CODE;
import jp.ac.osaka_u.ist.sdl.mpanalyzer.gui.ObservedModifications;
import jp.ac.osaka_u.ist.sdl.mpanalyzer.gui.ObservedModifications.MLABEL;

import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.internal.io.fs.FSRepositoryFactory;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import org.tmatesoft.svn.core.wc.SVNRevision;
import org.tmatesoft.svn.core.wc.SVNWCClient;

public class MCode extends JTextArea implements Observer {

	static public final int TAB_SIZE = 4;

	public final JScrollPane scrollPane;
	public final CODE code;

	public MCode(final CODE code) {

		this.setTabSize(TAB_SIZE);

		final Insets margin = new Insets(5, 50, 5, 5);
		this.setMargin(margin);
		this.setUI(new MCodeUI(code, new HashSet<Integer>(), this, margin));
		this.setText("");

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

						// 変更前ファイルの中身を取得
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

						// 変更前ファイルの中身を取得
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

						final List<Token> beforeTokens = Token
								.getTokens(beforeText.toString());
						final List<Token> afterTokens = Token
								.getTokens(afterText.toString());

						final List<Statement> beforeStatements = Statement
								.getStatements(beforeTokens);
						final List<Statement> afterStatements = Statement
								.getStatements(afterTokens);

						final List<Modification> modifications = LCS
								.getModifications(beforeStatements,
										afterStatements, filepath,
										modification.revision);

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
}
