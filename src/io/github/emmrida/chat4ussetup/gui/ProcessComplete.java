/*
 * Copyright (c) 2025 El Mhadder Mohamed Rida. All rights reserved.
 * This code is licensed under the [MIT License](https://opensource.org/licenses/MIT).
 */
package io.github.emmrida.chat4ussetup.gui;

import javax.swing.JPanel;

import io.github.emmrida.chat4ussetup.gui.MainWindow.OpType;
import io.github.emmrida.chat4ussetup.util.ContentOpener;
import io.github.emmrida.chat4ussetup.util.Helper;
import io.github.emmrida.chat4ussetup.util.Messages;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JTextArea;
import javax.swing.JCheckBox;
import javax.swing.LayoutStyle.ComponentPlacement;
import java.awt.FlowLayout;
import java.io.File;
import java.util.Locale;
import java.awt.Dimension;
import java.awt.Component;
import javax.swing.Box;
import java.awt.Font;

/**
 * ProcessComplete class, shows the result of the setup process and let the user choose
 * the final tasks to perform: Run the installed app, Open a web page, etc.
 * @author El Mhadder Mohamed Rida
 */
public class ProcessComplete extends JPanel implements WizardStep {
	private static final long serialVersionUID = 1L;

	private JTextArea taMessage;
	private JPanel panelFinalTasks;

    /**
     * @see WizarStep#getTitle()
     */
	@Override
	public String getTitle() {
		Object success = MainWindow.getPreference(MainWindow.ALL_SUCCESS);
		if(MainWindow.getOpType() == OpType.INSTALL) {
			if(success != null && (boolean)success) {
				return Messages.getString("ProcessComplete.TITLE_INSTALL_SUCCESS"); //$NON-NLS-1$
			} else return Messages.getString("ProcessComplete.TITLE_INSTALL_FAILURE"); //$NON-NLS-1$
		} else if(MainWindow.getOpType() == OpType.UPDATE) {
    		if(success != null && (boolean)success) {
        		return Messages.getString("ProcessComplete.TITLE_UPDATE_SUCCESS"); //$NON-NLS-1$
    		} else return Messages.getString("ProcessComplete.TITLE_UPDATE_FAILURE"); //$NON-NLS-1$
		} else if(MainWindow.getOpType() == OpType.RECOVER) {
    		if(success != null && (boolean)success) {
        		return Messages.getString("ProcessComplete.TITLE_RECOVER_SUCCESS"); //$NON-NLS-1$
    		} else return Messages.getString("ProcessComplete.TITLE_RECOVER_FAILURE"); //$NON-NLS-1$
		} else if(MainWindow.getOpType() == OpType.UNINSTALL) {
            if(success != null && (boolean)success) {
                return Messages.getString("ProcessComplete.TITLE_UNINSTALL_SUCCESS"); //$NON-NLS-1$
            } else return Messages.getString("ProcessComplete.TITLE_UNINSTALL_FAILURE"); //$NON-NLS-1$
		}
        return ""; //$NON-NLS-1$
	}

    /**
     * @see WizarStep#getDescription()
     */
	@Override
	public String getDescription() {
		String name = (String)MainWindow.getPreference(MainWindow.APP_NAME) + " v" + (String)MainWindow.getPreference(MainWindow.APP_VERSION); //$NON-NLS-1$
		Object success = MainWindow.getPreference(MainWindow.ALL_SUCCESS);
		if(MainWindow.getOpType() == OpType.INSTALL) {
			if(success != null && (boolean)success) {
				return name + Messages.getString("ProcessComplete.DESC_INSTALL_SUCCESS"); //$NON-NLS-1$
			} else return name + Messages.getString("ProcessComplete.DESC_INSTALL_FAILURE"); //$NON-NLS-1$
		} else if(MainWindow.getOpType() == OpType.UPDATE) {
    		if(success != null && (boolean)success) {
        		return name + Messages.getString("ProcessComplete.DESC_UPDATE_SUCCESS"); //$NON-NLS-1$
    		} else return name + Messages.getString("ProcessComplete.DESC_UPDATE_FAILURE"); //$NON-NLS-1$
		} else if(MainWindow.getOpType() == OpType.RECOVER) {
			if(success != null && (boolean)success) {
                return name + Messages.getString("ProcessComplete.DESC_RECOVER_SUCCESS"); //$NON-NLS-1$
            } else return name + Messages.getString("ProcessComplete.DESC_RECOVER_FAILURE"); //$NON-NLS-1$
		} else if(MainWindow.getOpType() == OpType.UNINSTALL) {
			if(success != null && (boolean)success) {
                return name + Messages.getString("ProcessComplete.DESC_UNINSTALL_SUCCESS"); //$NON-NLS-1$
            } else return name + Messages.getString("ProcessComplete.DESC_UNINSTALL_FAILURE"); //$NON-NLS-1$
		}
        return ""; //$NON-NLS-1$
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
		MainWindow.getNextButton().setVisible(false);
		MainWindow.getBackButton().setVisible(false);
		MainWindow.getInstallButton().setVisible(false);
		MainWindow.getCancelButton().setText(Messages.getString("ProcessComplete.BUTTON_FINISH")); //$NON-NLS-1$
		MainWindow.getCancelButton().setEnabled(true);
		MainWindow.getCancelButton().putClientProperty("role", "finish"); //$NON-NLS-1$ //$NON-NLS-2$
		String finalTasks = (String)MainWindow.getPreference(MainWindow.FINAL_TASKS);
		String name = (String)MainWindow.getPreference(MainWindow.APP_NAME) + " v" + (String)MainWindow.getPreference(MainWindow.APP_VERSION); //$NON-NLS-1$
        Object success = MainWindow.getPreference(MainWindow.ALL_SUCCESS);
		if(MainWindow.getOpType() == OpType.INSTALL) {
			if(success != null && (boolean)success) {
				taMessage.setText(name + Messages.getString("ProcessComplete.MSG_INSTALL_SUCCESSS")); //$NON-NLS-1$
			} else taMessage.setText(name + Messages.getString("ProcessComplete.MSG_INSTALL_FAILURE")); //$NON-NLS-1$
		} else if(MainWindow.getOpType() == OpType.UPDATE) {
			if(success != null && (boolean)success) {
				taMessage.setText(name + Messages.getString("ProcessComplete.MSG_UPDATE_SUCCESS")); //$NON-NLS-1$
			} else taMessage.setText(name + Messages.getString("ProcessComplete.MSG_UPDATE_FAILURE")); //$NON-NLS-1$
		} else if(MainWindow.getOpType() == OpType.RECOVER) {
            if(success != null && (boolean)success) {
                taMessage.setText(name + Messages.getString("ProcessComplete.MSG_RECOVER_SUCCESS")); //$NON-NLS-1$
            } else taMessage.setText(name + Messages.getString("ProcessComplete.MSG_RECOVER_FAILURE")); //$NON-NLS-1$
        } else if(MainWindow.getOpType() == OpType.UNINSTALL) {
        	finalTasks = null;
            if(success != null && (boolean)success) {
            	taMessage.setText(name + Messages.getString("ProcessComplete.MSG_UNINSTALL_SUCCESSS")); //$NON-NLS-1$
            } else taMessage.setText(name + Messages.getString("ProcessComplete.MSG_UNINSTALL_FAILURE")); //$NON-NLS-1$
        }
		if(finalTasks != null && (success != null && (boolean)success)) {
			String lng = Locale.getDefault().getLanguage();
			String finalTasksXX = (String)MainWindow.getPreference(MainWindow.FINAL_TASKS + "_" + lng); //$NON-NLS-1$
            if(finalTasksXX != null)
            	finalTasks = finalTasksXX;
			String[] tasks = finalTasks.split(";"); //$NON-NLS-1$
			for(String task : tasks) {
				String[] parts = task.split(":", 2); //$NON-NLS-1$
				JCheckBox chk = new JCheckBox(parts[0]);
				chk.setToolTipText(parts[1]);
				panelFinalTasks.add(chk);
				Component horizontalGlue = Box.createHorizontalGlue();
				horizontalGlue.setPreferredSize(new Dimension(panelFinalTasks.getWidth(), 0));
				panelFinalTasks.add(horizontalGlue);
			}
			Helper.enableRtlWhenNeeded(panelFinalTasks);
		}
	}

    /**
     * @see WizarStep#onStepDeactivated(boolean)
     */
	@Override
	public void onStepDeactivated(boolean nextClicked) {
		for(Component c : panelFinalTasks.getComponents()) {
			if(c instanceof JCheckBox) {
				JCheckBox chk = (JCheckBox)c;
				if(!chk.isSelected()) continue;
				String cmd = chk.getToolTipText();
				if(cmd != null) {
					if(!cmd.startsWith("http")) { //$NON-NLS-1$
                       	cmd = MainWindow.getPreference(MainWindow.DESTINATION_FOLDER) + File.separator + cmd;
					}
					ContentOpener.openOrExecute(cmd);
				}
			}
		}
	}

	/**
	 * Create the panel.
	 */
	public ProcessComplete() {
		taMessage = new JTextArea();
		taMessage.setFont(new Font("Tahoma", Font.PLAIN, 11)); //$NON-NLS-1$
		taMessage.setFocusable(false);
		taMessage.setText("Installation Complete/Error message."); //$NON-NLS-1$
		taMessage.setWrapStyleWord(true);
		taMessage.setOpaque(false);
		taMessage.setLineWrap(true);
		taMessage.setEditable(false);

		panelFinalTasks = new JPanel();
		GroupLayout groupLayout = new GroupLayout(this);
		groupLayout.setHorizontalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(Alignment.TRAILING, groupLayout.createSequentialGroup()
					.addGap(22)
					.addGroup(groupLayout.createParallelGroup(Alignment.TRAILING)
						.addComponent(panelFinalTasks, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 406, Short.MAX_VALUE)
						.addComponent(taMessage, Alignment.LEADING))
					.addGap(22))
		);
		groupLayout.setVerticalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addGap(22)
					.addComponent(taMessage, GroupLayout.PREFERRED_SIZE, 81, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(panelFinalTasks, GroupLayout.DEFAULT_SIZE, 180, Short.MAX_VALUE)
					.addGap(22))
		);
		panelFinalTasks.setLayout(new FlowLayout(FlowLayout.LEADING, 5, 5));
		setLayout(groupLayout);

		Helper.enableRtlWhenNeeded(ProcessComplete.this);
	}
}
