/*
 * Copyright (c) 2025 El Mhadder Mohamed Rida. All rights reserved.
 * This code is licensed under the [MIT License](https://opensource.org/licenses/MIT).
 */
package io.github.emmrida.chat4ussetup.gui;

import javax.swing.JPanel;

import io.github.emmrida.chat4ussetup.gui.MainWindow.OpType;
import io.github.emmrida.chat4ussetup.util.Helper;
import io.github.emmrida.chat4ussetup.util.Messages;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JTextArea;
import java.awt.Font;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.JCheckBox;

/**
 *  TasksSelection class, shows the tasks to perform before or after the installation process.
 *  @author El Mhadder Mohamed Rida
 */
public class TasksSelection extends JPanel implements WizardStep {
	private static final long serialVersionUID = 1L;

	private JCheckBox chkDesktopIcon;
	private JCheckBox chkSMIcon;
	private JTextArea taMessage;
	private JCheckBox chkBackup;

	/**
	 * @see WizarStep#getTitle()
	 */
	@Override
	public String getTitle() {
		return Messages.getString("TasksSelection.TITLE_SEL_TASKS"); //$NON-NLS-1$
	}

    /**
     * @see WizarStep#getDescription()
     */
	@Override
	public String getDescription() {
		return Messages.getString("TasksSelection.DESC_SEL_TASKS"); //$NON-NLS-1$
	}

    /**
     * @see WizarStep#validateStep()
     */
	@Override
	public boolean validateStep() {
		if(!chkDesktopIcon.isSelected() && !chkSMIcon.isSelected()) {
			JOptionPane.showMessageDialog(MainWindow.getMainFrame(), Messages.getString("TasksSelection.MB_MSG_MUST_SEL_TASK"), Messages.getString("TasksSelection.MB_TITLE_INFORMATION"), JOptionPane.ERROR_MESSAGE); //$NON-NLS-1$ //$NON-NLS-2$
			return false;
		}
		return true;
	}

    /**
     * @see WizarStep#onStepActivated(boolean)
     */
	@Override
	public void onStepActivated(boolean nextClicked) {
		String op = null;
        String projectName = MainWindow.getPreference(MainWindow.APP_NAME) + " v" + MainWindow.getPreference(MainWindow.APP_VERSION); //$NON-NLS-1$
		MainWindow.getInstallButton().setVisible(true);
		MainWindow.getNextButton().setVisible(false);
		chkBackup.setVisible(false);
		chkBackup.setSelected(false);
		if(MainWindow.getOpType()==OpType.UPDATE) {
			chkBackup.setSelected(true);
			chkBackup.setVisible(true);
			MainWindow.getInstallButton().setText(Messages.getString("TasksSelection.BUTTON_UPDATE")); //$NON-NLS-1$
			op = Messages.getString("TasksSelection.OP_UPDATING_TO"); //$NON-NLS-1$
    		taMessage.setText(String.format(Messages.getString("TasksSelection.MSG_SEL_TASK"), op + projectName, Messages.getString("TasksSelection.BUTTON_UPDATE"))); //$NON-NLS-1$ //$NON-NLS-1$
		} else if(MainWindow.getOpType()==OpType.INSTALL) {
			MainWindow.getInstallButton().setText(Messages.getString("TasksSelection.BUTTON_INSTALL")); //$NON-NLS-1$
            op = Messages.getString("TasksSelection.OP_INSTALLING"); //$NON-NLS-1$
    		taMessage.setText(String.format(Messages.getString("TasksSelection.MSG_SEL_TASK"), op + projectName, Messages.getString("TasksSelection.BUTTON_INSTALL"))); //$NON-NLS-1$ //$NON-NLS-1$
		} else if(MainWindow.getOpType()==OpType.RECOVER) {
			MainWindow.getInstallButton().setText(Messages.getString("TasksSelection.BUTTON_RECOVER")); //$NON-NLS-1$
            op = Messages.getString("TasksSelection.OP_RECOVERING"); //$NON-NLS-1$
    		taMessage.setText(String.format(Messages.getString("TasksSelection.MSG_SEL_TASK"), op + projectName, Messages.getString("TasksSelection.BUTTON_RECOVER"))); //$NON-NLS-1$ //$NON-NLS-1$
		}
	}

    /**
     * @see WizarStep#onStepDeactivated(boolean)
     */
	@Override
	public void onStepDeactivated(boolean nextClicked) {
		MainWindow.setPreference(MainWindow.CREATE_DESKTOP_ICON, chkDesktopIcon.isSelected());
		MainWindow.setPreference(MainWindow.CREATE_START_MENU_ICON, chkSMIcon.isSelected());
		MainWindow.setPreference(MainWindow.CREATE_BACKUP_BEFORE_UPDATE, chkBackup.isSelected());
	}

	/**
	 * Create the panel.
	 */
	public TasksSelection() {
		taMessage = new JTextArea();
		taMessage.setFocusable(false);
		taMessage.setFont(new Font("Tahoma", Font.PLAIN, 11)); //$NON-NLS-1$
		taMessage.setBorder(null);
		taMessage.setOpaque(false);
		taMessage.setLineWrap(true);
		taMessage.setWrapStyleWord(true);
		taMessage.setEditable(false);
		taMessage.setText("Install or Update message"); //$NON-NLS-1$

		JLabel lblNewLabel = new JLabel(Messages.getString("TasksSelection.LBL_ADDITIONAL_TASKS")); //$NON-NLS-1$

		chkDesktopIcon = new JCheckBox(Messages.getString("TasksSelection.CHK_CREATE_DESKTOP_LINK")); //$NON-NLS-1$
		chkDesktopIcon.setSelected(true);

		chkSMIcon = new JCheckBox(Messages.getString("TasksSelection.CHK_SYSMNU_LINK")); //$NON-NLS-1$
		chkSMIcon.setSelected(true);

		chkBackup = new JCheckBox(Messages.getString("TasksSelection.CHK_BACKUP")); //$NON-NLS-1$
		chkBackup.setSelected(true);
		GroupLayout groupLayout = new GroupLayout(this);
		groupLayout.setHorizontalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addGap(22)
					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
						.addComponent(lblNewLabel)
						.addComponent(taMessage, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
						.addGroup(groupLayout.createSequentialGroup()
							.addGap(10)
							.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
								.addComponent(chkSMIcon)
								.addComponent(chkDesktopIcon)
								.addComponent(chkBackup))
							.addGap(251)))
					.addGap(22))
		);
		groupLayout.setVerticalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addGap(22)
					.addComponent(taMessage, GroupLayout.PREFERRED_SIZE, 65, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(lblNewLabel)
					.addPreferredGap(ComponentPlacement.UNRELATED)
					.addComponent(chkDesktopIcon)
					.addPreferredGap(ComponentPlacement.UNRELATED)
					.addComponent(chkSMIcon)
					.addGap(18)
					.addComponent(chkBackup)
					.addContainerGap(96, Short.MAX_VALUE))
		);
		setLayout(groupLayout);

		Helper.enableRtlWhenNeeded(TasksSelection.this);
	}
}
