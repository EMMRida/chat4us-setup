/*
 * Copyright (c) 2025 El Mhadder Mohamed Rida. All rights reserved.
 * This code is licensed under the [MIT License](https://opensource.org/licenses/MIT).
 */
package io.github.emmrida.chat4ussetup.gui;

import javax.swing.JPanel;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JRadioButton;
import javax.swing.LayoutStyle.ComponentPlacement;

import io.github.emmrida.chat4ussetup.util.Helper;
import io.github.emmrida.chat4ussetup.util.Messages;

import javax.swing.JTextArea;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

import javax.swing.ButtonGroup;
import java.awt.Font;

/**
 * LicenceAgreement class, shows the software licence the user must aprove to continue setup.
 */
public class LicenceAgreement extends JPanel implements WizardStep {
	private static final long serialVersionUID = 1L;

	private final ButtonGroup buttonGroup = new ButtonGroup();
	private JRadioButton rbtnAccept;
	private JRadioButton rbtnRefuse;
	private JTextArea taLicence;

	/**
	 * @see WizarStep#getTitle()
	 */
	@Override
	public String getTitle() {
		return Messages.getString("LicenceAgreement.TITLE_LIC_AGREEMENT"); //$NON-NLS-1$
	}

    /**
     * @see WizarStep#getDescription()
     */
	@Override
	public String getDescription() {
		return Messages.getString("LicenceAgreement.DESC_LIC_READ"); //$NON-NLS-1$
	}

    /**
     * @see WizarStep#validateStep()
     */
	@Override
	public boolean validateStep() {
		boolean valid = rbtnAccept.isSelected();
		if(!valid)
			JOptionPane.showMessageDialog(MainWindow.getMainFrame(), Messages.getString("LicenceAgreement.MB_MSG_MUST_ACCEPT"), Messages.getString("LicenceAgreement.MB_TITLE_INFO"), JOptionPane.ERROR_MESSAGE); //$NON-NLS-1$ //$NON-NLS-2$
		return valid;
	}

    /**
     * @see WizarStep#onStepActivated(boolean)
     */
	@Override
	public void onStepActivated(boolean nextClicked) {
		buttonGroup.clearSelection();
		try {
			String lng = Locale.getDefault().getLanguage();
			String licenceFile = (String)MainWindow.getPreference(MainWindow.LICENCE_FILE + "_" + lng); //$NON-NLS-1$
			if(licenceFile == null)
				licenceFile = (String)MainWindow.getPreference(MainWindow.LICENCE_FILE);
			String licence = Files.readString(Path.of(licenceFile));
			taLicence.setText(licence);
			taLicence.setCaretPosition(0);
			rbtnAccept.setEnabled(true);
			rbtnRefuse.setEnabled(true);
		} catch (IOException ex) {
			JOptionPane.showMessageDialog(MainWindow.getMainFrame(), Messages.getString("LicenceAgreement.MB_MSG_LIC_FILE_READ_ERROR") + ex.getMessage(), Messages.getString("LicenceAgreement.MB_TITLE_ERROR"), JOptionPane.ERROR_MESSAGE); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

    /**
     * @see WizarStep#onStepDeactivated(boolean)
     */
	@Override
	public void onStepDeactivated(boolean nextClicked) { }

	/**
	 * Create the panel.
	 */
	public LicenceAgreement() {

		JScrollPane scrollPane = new JScrollPane();

		rbtnRefuse = new JRadioButton(Messages.getString("LicenceAgreement.RBTN_REFUSE_LIC")); //$NON-NLS-1$
		rbtnRefuse.setEnabled(false);
		buttonGroup.add(rbtnRefuse);

		rbtnAccept = new JRadioButton(Messages.getString("LicenceAgreement.RBTN_ACCEPT_LIC")); //$NON-NLS-1$
		rbtnAccept.setEnabled(false);
		buttonGroup.add(rbtnAccept);
		GroupLayout groupLayout = new GroupLayout(this);
		groupLayout.setHorizontalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addGap(22)
					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
						.addComponent(scrollPane, GroupLayout.DEFAULT_SIZE, 430, Short.MAX_VALUE)
						.addComponent(rbtnAccept)
						.addComponent(rbtnRefuse))
					.addGap(22))
		);
		groupLayout.setVerticalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(Alignment.TRAILING, groupLayout.createSequentialGroup()
					.addGap(22)
					.addComponent(scrollPane, GroupLayout.DEFAULT_SIZE, 226, Short.MAX_VALUE)
					.addPreferredGap(ComponentPlacement.UNRELATED)
					.addComponent(rbtnAccept)
					.addPreferredGap(ComponentPlacement.UNRELATED)
					.addComponent(rbtnRefuse)
					.addGap(22))
		);

		taLicence = new JTextArea();
		taLicence.setFont(new Font("Tahoma", Font.PLAIN, 11)); //$NON-NLS-1$
		taLicence.setEditable(false);
		taLicence.setWrapStyleWord(true);
		taLicence.setLineWrap(true);
		scrollPane.setViewportView(taLicence);
		setLayout(groupLayout);

		Helper.enableRtlWhenNeeded(LicenceAgreement.this);
	}
}
