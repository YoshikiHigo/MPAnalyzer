package jp.ac.osaka_u.ist.sdl.mpanalyzer.gui;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;

import jp.ac.osaka_u.ist.sdl.mpanalyzer.data.ModificationPattern;
import jp.ac.osaka_u.ist.sdl.mpanalyzer.data.Modification.ChangeType;
import jp.ac.osaka_u.ist.sdl.mpanalyzer.data.Modification.ModificationType;
import jp.ac.osaka_u.ist.sdl.mpanalyzer.db.ReadOnlyDAO;
import jp.ac.osaka_u.ist.sdl.mpanalyzer.gui.ObservedModificationPatterns.MPLABEL;

public class ThresholdPanel extends JPanel implements Observer {

	public static final int TEXTFIELD_LENGTH = 5;

	public ThresholdPanel() {
		this.setBorder(new TitledBorder(new LineBorder(Color.black),
				"Configuration"));

		final JButton queryButton = new JButton("Quering DB");
		final JTextField supportMinField = new JTextField(TEXTFIELD_LENGTH);
		final JTextField supportMaxField = new JTextField(TEXTFIELD_LENGTH);
		final JTextField confidenceMinField = new JTextField(TEXTFIELD_LENGTH);
		final JTextField confidenceMaxField = new JTextField(TEXTFIELD_LENGTH);
		final JTextField nodMinField = new JTextField(TEXTFIELD_LENGTH);
		final JTextField nodMaxField = new JTextField(TEXTFIELD_LENGTH);
		final JTextField norMinField = new JTextField(TEXTFIELD_LENGTH);
		final JTextField norMaxField = new JTextField(TEXTFIELD_LENGTH);
		final JTextField nofMinField = new JTextField(TEXTFIELD_LENGTH);
		final JTextField nofMaxField = new JTextField(TEXTFIELD_LENGTH);
		final JTextField lbmMinField = new JTextField(TEXTFIELD_LENGTH);
		final JTextField lbmMaxField = new JTextField(TEXTFIELD_LENGTH);
		final JTextField lamMinField = new JTextField(TEXTFIELD_LENGTH);
		final JTextField lamMaxField = new JTextField(TEXTFIELD_LENGTH);
		final JCheckBox type2CheckBox = new JCheckBox("TYPE2", true);
		final JCheckBox type3CheckBox = new JCheckBox("TYPE3", true);
		final JCheckBox changeCheckBox = new JCheckBox("Change", true);
		final JCheckBox addCheckBox = new JCheckBox("add", true);
		final JCheckBox deleteCheckBox = new JCheckBox("delete", true);

		supportMinField.setText("2");
		supportMaxField.setText("9999");
		confidenceMinField.setText("0");
		confidenceMaxField.setText("1");
		nodMinField.setText("0");
		nodMaxField.setText("9999");
		norMinField.setText("0");
		norMaxField.setText("9999");
		nofMinField.setText("1");
		nofMaxField.setText("9999");
		lbmMinField.setText("1");
		lbmMaxField.setText("9999");
		lamMinField.setText("1");
		lamMaxField.setText("9999");

		// this.setLayout(new FlowLayout(7, 5, 0));
		this.setLayout(new GridLayout(2, 5));

		{
			final JPanel supportPanel = new JPanel(new FlowLayout(
					FlowLayout.CENTER, 5, 0));
			supportPanel.setBorder(new TitledBorder(
					new LineBorder(Color.black), "SUPPORT"));
			supportPanel.add(new JLabel("Min: "));
			supportPanel.add(supportMinField);
			supportPanel.add(new JPanel());
			supportPanel.add(new JLabel("Max: "));
			supportPanel.add(supportMaxField);
			this.add(supportPanel);
		}

		{
			final JPanel confidencePanel = new JPanel(new FlowLayout(
					FlowLayout.CENTER, 5, 0));
			confidencePanel.setBorder(new TitledBorder(new LineBorder(
					Color.black), "CONFIDENCE"));
			confidencePanel.add(new JLabel("Min: "));
			confidencePanel.add(confidenceMinField);
			confidencePanel.add(new JPanel());
			confidencePanel.add(new JLabel("Max: "));
			confidencePanel.add(confidenceMaxField);
			this.add(confidencePanel);
		}

		{
			final JPanel nodPanel = new JPanel(new FlowLayout(
					FlowLayout.CENTER, 5, 0));
			nodPanel.setBorder(new TitledBorder(new LineBorder(Color.black),
					"NOD"));
			nodPanel.add(new JLabel("Min: "));
			nodPanel.add(nodMinField);
			nodPanel.add(new JPanel());
			nodPanel.add(new JLabel("Max: "));
			nodPanel.add(nodMaxField);
			this.add(nodPanel);
		}

		{
			final JPanel norPanel = new JPanel(new FlowLayout(
					FlowLayout.CENTER, 5, 0));
			norPanel.setBorder(new TitledBorder(new LineBorder(Color.black),
					"NOR"));
			norPanel.add(new JLabel("Min: "));
			norPanel.add(norMinField);
			norPanel.add(new JPanel());
			norPanel.add(new JLabel("Max: "));
			norPanel.add(norMaxField);
			this.add(norPanel);
		}

		{
			final JPanel nofPanel = new JPanel(new FlowLayout(
					FlowLayout.CENTER, 5, 0));
			nofPanel.setBorder(new TitledBorder(new LineBorder(Color.black),
					"NOF"));
			nofPanel.add(new JLabel("Min: "));
			nofPanel.add(nofMinField);
			nofPanel.add(new JPanel());
			nofPanel.add(new JLabel("Max: "));
			nofPanel.add(nofMaxField);
			this.add(nofPanel);
		}

		{
			final JPanel lbmPanel = new JPanel(new FlowLayout(
					FlowLayout.CENTER, 5, 0));
			lbmPanel.setBorder(new TitledBorder(new LineBorder(Color.black),
					"LBM (Length Before Modification)"));
			lbmPanel.add(new JLabel("Min: "));
			lbmPanel.add(lbmMinField);
			lbmPanel.add(new JPanel());
			lbmPanel.add(new JLabel("Max: "));
			lbmPanel.add(lbmMaxField);
			this.add(lbmPanel);
		}

		{
			final JPanel lamPanel = new JPanel(new FlowLayout(
					FlowLayout.CENTER, 5, 0));
			lamPanel.setBorder(new TitledBorder(new LineBorder(Color.black),
					"LAM (Length After Modification)"));
			lamPanel.add(new JLabel("Min: "));
			lamPanel.add(lamMinField);
			lamPanel.add(new JPanel());
			lamPanel.add(new JLabel("Max: "));
			lamPanel.add(lamMaxField);
			this.add(lamPanel);
		}

		{
			final JPanel changePanel = new JPanel(new FlowLayout(
					FlowLayout.CENTER, 3, 0));
			changePanel.setBorder(new TitledBorder(new LineBorder(Color.black),
					"Modification Type"));
			changePanel.add(changeCheckBox);
			changePanel.add(addCheckBox);
			changePanel.add(deleteCheckBox);
			this.add(changePanel);
		}

		{
			final JPanel typePanel = new JPanel(new FlowLayout(
					FlowLayout.CENTER, 2, 0));
			typePanel.setBorder(new TitledBorder(new LineBorder(Color.black),
					"Change Type"));
			typePanel.add(type2CheckBox);
			typePanel.add(type3CheckBox);
			this.add(typePanel);
		}

		this.add(queryButton);

		queryButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int supportMin = 1;
				int supportMax = Integer.MAX_VALUE;
				try {
					final String textMin = supportMinField.getText();
					supportMin = Integer.parseInt(textMin);
					if (supportMin < 1) {
						throw new NumberFormatException();
					}
					final String textMax = supportMaxField.getText();
					supportMax = Integer.parseInt(textMax);
					if (supportMax < 1) {
						throw new NumberFormatException();
					}
				} catch (final NumberFormatException supportException) {
					JOptionPane.showMessageDialog(ThresholdPanel.this,
							"SUPPORT must be positive integer.", "",
							JOptionPane.ERROR_MESSAGE);
					return;
				}

				float confidenceMin = 0f;
				float confidenceMax = 1f;
				try {
					final String textMin = confidenceMinField.getText();
					confidenceMin = Float.parseFloat(textMin);
					if ((confidenceMin < 0f) || (1f < confidenceMin)) {
						throw new NumberFormatException();
					}
					final String textMax = confidenceMaxField.getText();
					confidenceMax = Float.parseFloat(textMax);
					if ((confidenceMax < 0f) || (1f < confidenceMax)) {
						throw new NumberFormatException();
					}

				} catch (final NumberFormatException confidenceException) {
					JOptionPane.showMessageDialog(ThresholdPanel.this,
							"CONFIDENCE must be float value between 0 and 1.",
							"", JOptionPane.ERROR_MESSAGE);
					return;
				}

				int nodMin = 0;
				int nodMax = Integer.MAX_VALUE;
				try {
					final String textMin = nodMinField.getText();
					nodMin = Integer.parseInt(textMin);
					if (nodMin < 0) {
						throw new NumberFormatException();
					}
					final String textMax = nodMaxField.getText();
					nodMax = Integer.parseInt(textMax);
					if (nodMax < 0) {
						throw new NumberFormatException();
					}
				} catch (final NumberFormatException nodException) {
					JOptionPane.showMessageDialog(ThresholdPanel.this,
							"NOD must be 0 or positive integer.", "",
							JOptionPane.ERROR_MESSAGE);
					return;
				}

				int norMin = 0;
				int norMax = Integer.MAX_VALUE;
				try {
					final String textMin = norMinField.getText();
					norMin = Integer.parseInt(textMin);
					if (norMin < 0) {
						throw new NumberFormatException();
					}
					final String textMax = norMaxField.getText();
					norMax = Integer.parseInt(textMax);
					if (norMax < 0) {
						throw new NumberFormatException();
					}
				} catch (final NumberFormatException norException) {
					JOptionPane.showMessageDialog(ThresholdPanel.this,
							"NOR must be 0 or positive integer.", "",
							JOptionPane.ERROR_MESSAGE);
					return;
				}

				int nofMin = 0;
				int nofMax = Integer.MAX_VALUE;
				try {
					final String textMin = nofMinField.getText();
					nofMin = Integer.parseInt(textMin);
					if (nofMin < 1) {
						throw new NumberFormatException();
					}
					final String textMax = nofMaxField.getText();
					nofMax = Integer.parseInt(textMax);
					if (nofMax < 1) {
						throw new NumberFormatException();
					}
				} catch (final NumberFormatException nofException) {
					JOptionPane.showMessageDialog(ThresholdPanel.this,
							"NOF must be positive integer.", "",
							JOptionPane.ERROR_MESSAGE);
					return;
				}

				int lbmMin = 0;
				int lbmMax = Integer.MAX_VALUE;
				try {
					final String textMin = lbmMinField.getText();
					lbmMin = Integer.parseInt(textMin);
					if (lbmMin < 1) {
						throw new NumberFormatException();
					}
					final String textMax = lbmMaxField.getText();
					lbmMax = Integer.parseInt(textMax);
					if (lbmMax < 1) {
						throw new NumberFormatException();
					}
				} catch (final NumberFormatException nofException) {
					JOptionPane.showMessageDialog(ThresholdPanel.this,
							"LBM must be positive integer.", "",
							JOptionPane.ERROR_MESSAGE);
					return;
				}

				int lamMin = 0;
				int lamMax = Integer.MAX_VALUE;
				try {
					final String textMin = lamMinField.getText();
					lamMin = Integer.parseInt(textMin);
					if (lamMin < 1) {
						throw new NumberFormatException();
					}
					final String textMax = lamMaxField.getText();
					lamMax = Integer.parseInt(textMax);
					if (lamMax < 1) {
						throw new NumberFormatException();
					}
				} catch (final NumberFormatException nofException) {
					JOptionPane.showMessageDialog(ThresholdPanel.this,
							"LAM must be positive integer.", "",
							JOptionPane.ERROR_MESSAGE);
					return;
				}

				final boolean type2checked = type2CheckBox.isSelected();
				final boolean type3checked = type3CheckBox.isSelected();
				if (!type2checked && !type3checked) {
					JOptionPane.showMessageDialog(ThresholdPanel.this,
							"At least one check box must be checked.", "",
							JOptionPane.ERROR_MESSAGE);
					return;
				}

				final boolean changeChecked = changeCheckBox.isSelected();
				final boolean addChecked = addCheckBox.isSelected();
				final boolean deleteChecked = deleteCheckBox.isSelected();
				if (!changeChecked && !addChecked && !deleteChecked) {
					JOptionPane.showMessageDialog(ThresholdPanel.this,
							"At least one check box must be checked.", "",
							JOptionPane.ERROR_MESSAGE);
					return;
				}

				try {
					final List<ModificationPattern> patterns = ReadOnlyDAO
							.getInstance().getModificationPatterns(supportMin,
									confidenceMin);
					for (final Iterator<ModificationPattern> iterator = patterns
							.iterator(); iterator.hasNext();) {
						final ModificationPattern pattern = iterator.next();
						if ((pattern.getNOD() < nodMin)
								|| (pattern.getNOR() < norMin)
								|| (pattern.getNOF() < nofMin)
								|| (pattern.getLBM() < lbmMin)
								|| (pattern.getLAM() < lamMin)) {
							iterator.remove();
							continue;
						}

						if ((pattern.getNOD() > nodMax)
								|| (pattern.getNOR() > norMax)
								|| (pattern.getNOF() > nofMax)
								|| (pattern.getLBM() > lbmMax)
								|| (pattern.getLAM() > lamMax)) {
							iterator.remove();
							continue;
						}

						if (!type2checked
								&& pattern.changeType.getValue() == ChangeType.TYPE2
										.getValue()) {
							iterator.remove();
							continue;
						}

						if (!type3checked
								&& pattern.changeType.getValue() == ChangeType.TYPE3
										.getValue()) {
							iterator.remove();
							continue;
						}

						if (!changeChecked
								&& pattern.modificationType.getValue() == ModificationType.CHANGE
										.getValue()) {
							iterator.remove();
							continue;
						}

						if (!addChecked
								&& pattern.modificationType.getValue() == ModificationType.ADD
										.getValue()) {
							iterator.remove();
							continue;
						}

						if (!deleteChecked
								&& pattern.modificationType.getValue() == ModificationType.DELETE
										.getValue()) {
							iterator.remove();
							continue;
						}
					}

					ObservedModificationPatterns.getInstance(MPLABEL.ALL)
							.setAll(patterns, ThresholdPanel.this);
					ObservedModificationPatterns.getInstance(MPLABEL.FILTERED)
							.setAll(patterns, ThresholdPanel.this);
					ObservedModificationPatterns.getInstance(MPLABEL.SELECTED)
							.clear(ThresholdPanel.this);

				} catch (final Exception databaseException) {
					JOptionPane.showMessageDialog(ThresholdPanel.this,
							"Couldn't connect to database.", "",
							JOptionPane.ERROR_MESSAGE);
					return;
				}
			}
		});
	}

	@Override
	public void update(Observable o, Object arg) {
	}
}
