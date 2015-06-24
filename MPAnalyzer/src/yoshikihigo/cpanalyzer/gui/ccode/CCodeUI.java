package yoshikihigo.cpanalyzer.gui.ccode;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Rectangle;
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;

import javax.swing.JTextArea;
import javax.swing.plaf.basic.BasicTextAreaUI;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.text.PlainView;
import javax.swing.text.View;

import yoshikihigo.cpanalyzer.gui.CODE;

class CCodeUI extends BasicTextAreaUI {

	class SourceCodePlainView extends PlainView {

		final private JTextArea textArea;
		final private Insets margin;

		public SourceCodePlainView(final Element elem,
				final JTextArea textArea, final Insets margin) {
			super(elem);
			this.textArea = textArea;
			this.margin = margin;
		}

		@Override
		protected int drawUnselectedText(Graphics g, int x, int y, int p0,
				int p1) throws BadLocationException {

			final int endColumn = super.drawUnselectedText(g, x, y, p0, p1);

			int start = p0;
			final String buffer = this.textArea.getDocument().getText(p0,
					p1 - p0 + 1);
			final StringTokenizer stringTokenizer = new StringTokenizer(buffer,
					" ,;=+*-/()[]{}\n\t", true);

			while (stringTokenizer.hasMoreTokens()) {
				final String token = stringTokenizer.nextToken();
				if (RESERVED_WORD.contains(token)) {
					this.drawLangToken(g, getDrawX(start), y, token);
				}
				start += token.length();
			}

			return endColumn;
		}

		private void drawLangToken(Graphics g, int x, int y, String token) {

			g.setPaintMode();
			Color backupColor = g.getColor();
			g.setColor(VISUAL_SOURCECODEVIEW_LANG_TOKEN_COLOR);
			g.drawString(token, x, y);
			g.setColor(backupColor);
		}

		private int getDrawX(int pos) {

			int drawX = 0;

			try {
				drawX = textArea.modelToView(pos).x;
			} catch (BadLocationException e) {
				System.err.println(e.getMessage());
			}

			return drawX;
		}

		@Override
		protected void drawLine(int lineIndex, Graphics g, int x, int y) {
			this.drawLineBackground(g, lineIndex);
			this.drawLineNumber(g, lineIndex, y);
			super.drawLine(lineIndex, g, x, y);
		}

		private void drawLineBackground(final Graphics g, final int lineIndex) {

			if (!CCodeUI.this.emphasizedLines.contains(lineIndex)) {
				return;
			}

			g.setPaintMode();

			try {

				switch (CCodeUI.this.code) {
				case BEFORE:
					g.setColor(BEFORE_MODIFICATION_LINE_COLOR);
					break;
				case AFTER:
					g.setColor(AFTER_MODIFICATION_LINE_COLOR);
					break;
				}

				final int startOffset = this.textArea
						.getLineStartOffset(lineIndex);
				final Rectangle rectangle = CCodeUI.this.modelToView(
						this.textArea, startOffset);
				g.fillRect(0, rectangle.y, this.textArea.getWidth(),
						rectangle.height);

			} catch (final Exception e) {
				e.printStackTrace();
			}
		}

		private void drawLineNumber(final Graphics g, final int lineIndex,
				final int y) {

			final Color backupColor = g.getColor();

			g.setColor(VISUAL_SOURCECODEVIEW_LINENUM_COLOR);
			g.drawLine(this.margin.left - 5,
					y - g.getFontMetrics().getHeight(), this.margin.left - 5, y);

			final String lineNumber = String.valueOf(lineIndex + 1);
			final int x_location = this.margin.left - 8
					- g.getFontMetrics().stringWidth(lineNumber);

			g.drawString(lineNumber, x_location, y);

			g.setColor(backupColor);
		}
	}

	static public final Color BEFORE_MODIFICATION_LINE_COLOR = new Color(0,
			130, 130, 80);
	static public final Color AFTER_MODIFICATION_LINE_COLOR = new Color(130,
			130, 0, 80);
	static public final Color VISUAL_SOURCECODEVIEW_LANG_TOKEN_COLOR = new Color(
			12, 43, 116);
	static public final Color VISUAL_SOURCECODEVIEW_LINENUM_COLOR = new Color(
			0, 100, 0);

	final private CODE code;
	final private Set<Integer> emphasizedLines;
	final private JTextArea textArea;
	final Insets margin;

	CCodeUI(final CODE code, final Set<Integer> emphasizedLines,
			final JTextArea textArea, final Insets margin) {

		super();
		this.code = code;
		this.emphasizedLines = emphasizedLines;
		this.textArea = textArea;
		this.margin = margin;
	}

	@Override
	public View create(final Element elem) {
		return new SourceCodePlainView(elem, this.textArea, this.margin);
	}

	private static final Set<String> RESERVED_WORD = new HashSet<String>();

	static {
		RESERVED_WORD.add("abstract");
		RESERVED_WORD.add("boolean");
		RESERVED_WORD.add("break");
		RESERVED_WORD.add("byte");
		RESERVED_WORD.add("case");
		RESERVED_WORD.add("catch");
		RESERVED_WORD.add("char");
		RESERVED_WORD.add("class");
		RESERVED_WORD.add("const");
		RESERVED_WORD.add("continue");
		RESERVED_WORD.add("default");
		RESERVED_WORD.add("do");
		RESERVED_WORD.add("double");
		RESERVED_WORD.add("else");
		RESERVED_WORD.add("extends");
		RESERVED_WORD.add("final");
		RESERVED_WORD.add("finally");
		RESERVED_WORD.add("float");
		RESERVED_WORD.add("for");
		RESERVED_WORD.add("goto");
		RESERVED_WORD.add("if");
		RESERVED_WORD.add("implements");
		RESERVED_WORD.add("import");
		RESERVED_WORD.add("instanceof");
		RESERVED_WORD.add("int");
		RESERVED_WORD.add("interface");
		RESERVED_WORD.add("long");
		RESERVED_WORD.add("native");
		RESERVED_WORD.add("new");
		RESERVED_WORD.add("package");
		RESERVED_WORD.add("private");
		RESERVED_WORD.add("protected");
		RESERVED_WORD.add("public");
		RESERVED_WORD.add("return");
		RESERVED_WORD.add("short");
		RESERVED_WORD.add("static");
		RESERVED_WORD.add("strictfp");
		RESERVED_WORD.add("super");
		RESERVED_WORD.add("switch");
		RESERVED_WORD.add("synchronized");
		RESERVED_WORD.add("this");
		RESERVED_WORD.add("throw");
		RESERVED_WORD.add("throws");
		RESERVED_WORD.add("transient");
		RESERVED_WORD.add("try");
		RESERVED_WORD.add("void");
		RESERVED_WORD.add("volatile");
		RESERVED_WORD.add("while");
	}
}
