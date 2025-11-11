/*
 * Copyright (c) 2025 El Mhadder Mohamed Rida. All rights reserved.
 * This code is licensed under the [MIT License](https://opensource.org/licenses/MIT).
 */
package io.github.emmrida.chat4ussetup.gui;

import javax.swing.JPanel;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;

import io.github.emmrida.chat4ussetup.util.Helper;
import io.github.emmrida.chat4ussetup.util.Messages;

import java.awt.Font;
import java.util.Locale;

import javax.swing.JLabel;
import javax.swing.JTextArea;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.JComboBox;
import javax.swing.DefaultComboBoxModel;

/**
 * WelcomeStep class, shows the welcome message to the user.
 * @author El Mhadder Mohamed Rida
 */
public class WelcomeStep extends JPanel implements WizardStep {
	private static final long serialVersionUID = 1L;

	private static final String[] LANGUAGES = new String[] {"ar", "en", "fr"};

	private JLabel lblWelcome;
	private JComboBox<String> comboBox;

	/**
	 * @see WizarStep#getTitle()
	 */
	@Override
	public String getTitle() {
		return MainWindow.getPreference(MainWindow.APP_NAME) + Messages.getString("WelcomeStep.TITLE_SUFFIX"); //$NON-NLS-1$
	}

    /**
     * @see WizarStep#getDescription()
     */
	@Override
	public String getDescription() {
		return Messages.getString("WelcomeStep.DESC_WELCOME_PREFIX") + MainWindow.getPreference(MainWindow.APP_NAME) + Messages.getString("WelcomeStep.DESC_WELCOME_SUFFIX"); //$NON-NLS-1$ //$NON-NLS-2$
	}

    /**
     * @see WizarStep#validateStep()
     */
	@Override
	public boolean validateStep() {
		return true;
	}

    /**
     * @see WizarStep#onStepActivated(boolean)
     */
	@Override
	public void onStepActivated(boolean nextClicked) {
		String pnv = MainWindow.getPreference(MainWindow.APP_NAME) + " v" + MainWindow.getPreference(MainWindow.APP_VERSION); //$NON-NLS-1$
		lblWelcome.setText(lblWelcome.getText().replace("##project_name_version##", pnv)); //$NON-NLS-1$
	}

    /**
     * @see WizarStep#onStepDeactivated(boolean)
     */
	@Override
	public void onStepDeactivated(boolean nextClicked) { }

	/**
	 * Create the panel.
	 */
	public WelcomeStep() {
		lblWelcome = new JLabel(Messages.getString("WelcomeStep.LBL_WELCOME")); //$NON-NLS-1$
		lblWelcome.setFont(new Font("Tahoma", Font.BOLD, 14)); //$NON-NLS-1$

		JTextArea taInstallResume = new JTextArea();
		taInstallResume.setLineWrap(true);
		taInstallResume.setWrapStyleWord(true);
		taInstallResume.setFocusable(false);
		taInstallResume.setBorder(null);
		taInstallResume.setOpaque(false);
		taInstallResume.setEditable(false);
		taInstallResume.setFont(new Font("Tahoma", Font.PLAIN, 11)); //$NON-NLS-1$
		taInstallResume.setText(Messages.getString("WelcomeStep.MSG_WELCOME")); //$NON-NLS-1$

		JLabel lblClickNext = new JLabel(Messages.getString("WelcomeStep.LBL_CLICK_NEXT_TO_CONTINUE")); //$NON-NLS-1$

		comboBox = new JComboBox<>();
		comboBox.setModel(new DefaultComboBoxModel<>(new String[] {"تغيير اللغة إلى العربية", "Change the language to English", "Changer la langue vers le Français"}));
		String lng = Locale.getDefault().getLanguage();
		for (int i = 0; i < LANGUAGES.length; i++) {
            if (LANGUAGES[i].equals(lng)) {
                comboBox.setSelectedIndex(i);
                break;
            }
        }
		comboBox.addItemListener(e -> {
            if (e.getStateChange() == 1) {
                MainWindow.onLanguageChanged(LANGUAGES[comboBox.getSelectedIndex()]);
            }
		});

		GroupLayout groupLayout = new GroupLayout(this);
		groupLayout.setHorizontalGroup(
			groupLayout.createParallelGroup(Alignment.TRAILING)
				.addGroup(groupLayout.createSequentialGroup()
					.addGroup(groupLayout.createParallelGroup(Alignment.TRAILING)
						.addGroup(groupLayout.createSequentialGroup()
							.addContainerGap()
							.addComponent(lblClickNext))
						.addGroup(groupLayout.createSequentialGroup()
							.addGap(22)
							.addGroup(groupLayout.createParallelGroup(Alignment.TRAILING)
								.addComponent(lblWelcome, Alignment.LEADING, GroupLayout.PREFERRED_SIZE, 406, Short.MAX_VALUE)
								.addComponent(comboBox, Alignment.LEADING, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
								.addComponent(taInstallResume, Alignment.LEADING, GroupLayout.PREFERRED_SIZE, 406, Short.MAX_VALUE))))
					.addGap(22))
		);
		groupLayout.setVerticalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addGap(22)
					.addComponent(lblWelcome)
					.addGap(22)
					.addComponent(taInstallResume, GroupLayout.PREFERRED_SIZE, 80, GroupLayout.PREFERRED_SIZE)
					.addGap(18)
					.addComponent(comboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED, 87, Short.MAX_VALUE)
					.addComponent(lblClickNext)
					.addGap(20))
		);
		setLayout(groupLayout);

		Helper.enableRtlWhenNeeded(WelcomeStep.this);
	}
}
