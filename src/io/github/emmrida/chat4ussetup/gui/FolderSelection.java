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
import javax.swing.JTextField;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

import java.awt.Font;
import java.awt.Insets;
import java.awt.event.ActionListener;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.awt.event.ActionEvent;

/**
 * FolderSelection class is used to select the installation folder of the application
 * @author El Mhadder Mohamed Rida
 */
public class FolderSelection extends JPanel implements WizardStep {
	private static final long serialVersionUID = 1L;

	private JTextField tfFolder;
	private JLabel lblRequired;
	private JLabel lblAvailable;
	private JTextArea taMessage;
	private JButton btnBrowse;
	private JPanel panelSpace;

    /**
     * @see WizardStep#getTitle()
     */
	@Override
	public String getTitle() {
		if(MainWindow.getOpType()==OpType.INSTALL) {
			return Messages.getString("FolderSelection.TITLE_INSTALL"); //$NON-NLS-1$
		} else if(MainWindow.getOpType()==OpType.UPDATE) {
			return Messages.getString("FolderSelection.TITLE_UPDATE"); //$NON-NLS-1$
		} else if(MainWindow.getOpType()==OpType.RECOVER) {
            return Messages.getString("FolderSelection.TITLE_RECOVER"); //$NON-NLS-1$
		} else if(MainWindow.getOpType()==OpType.UNINSTALL) {
    		return Messages.getString("FolderSelection.TITLE_UNINSTALL"); //$NON-NLS-1$
        } else return ""; //$NON-NLS-1$
	}

    /**
     * @see WizardStep#getDescription()
     */
	@Override
	public String getDescription() {
		if(MainWindow.getOpType()==OpType.INSTALL) {
			return Messages.getString("FolderSelection.DESC_INSTALL"); //$NON-NLS-1$
        } else if(MainWindow.getOpType()==OpType.UPDATE) {
            return Messages.getString("FolderSelection.DESC_UPDATE"); //$NON-NLS-1$
		} else if(MainWindow.getOpType()==OpType.RECOVER) {
    		return Messages.getString("FolderSelection.DESC_RECOVER"); //$NON-NLS-1$
        } else if(MainWindow.getOpType()==OpType.UNINSTALL) {
            return Messages.getString("FolderSelection.DESC_UNINSTALL"); //$NON-NLS-1$
		} else return ""; //$NON-NLS-1$
	}

    /**
     * @see WizardStep#validateStep()
     */
	@Override
	public boolean validateStep() {
		if(MainWindow.getOpType()==OpType.INSTALL) {
			try {
				if(Helper.getDriveFreeSpace(tfFolder.getText()) < (int)MainWindow.getPreference(MainWindow.REQUIRED_SIZE)) {
					JOptionPane.showMessageDialog(MainWindow.getMainFrame(), Messages.getString("FolderSelection.MB_MSG_STORAGE_TOO_SMALL"), Messages.getString("FolderSelection.MB_TITLE_ERROR"), JOptionPane.ERROR_MESSAGE); //$NON-NLS-1$ //$NON-NLS-2$
					return false;
				}
				String installFolder = tfFolder.getText();
				if(Files.exists(Paths.get(installFolder))) {
					int ret = JOptionPane.showConfirmDialog(MainWindow.getMainFrame(), Messages.getString("FolderSelection.MB_MSG_OVERWRITE_FOLDER"), Messages.getString("FolderSelection.MB_TITLE_CONFIRM"), JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE); //$NON-NLS-1$ //$NON-NLS-2$
					if(ret != JOptionPane.YES_OPTION)
						return false;
				}
				MainWindow.setPreference(MainWindow.DESTINATION_FOLDER, installFolder);
				return true;
			} catch (Exception ex) {
				JOptionPane.showMessageDialog(MainWindow.getMainFrame(), Messages.getString("FolderSelection.MB_MSG_INVALID_FOLDER"), Messages.getString("FolderSelection.MB_TITLE_ERROR"), JOptionPane.ERROR_MESSAGE); //$NON-NLS-1$ //$NON-NLS-2$
			}
		} else if(MainWindow.getOpType()==OpType.UPDATE) {
			if(Files.exists(Paths.get((String)MainWindow.getPreference(MainWindow.DESTINATION_FOLDER)))) {
				return true;
			} else {
				JOptionPane.showMessageDialog(MainWindow.getMainFrame(), Messages.getString("FolderSelection.MB_MSG_FOLDER_NEXISTS"), Messages.getString("FolderSelection.MB_TITLE_ERROR"), JOptionPane.ERROR_MESSAGE); //$NON-NLS-1$ //$NON-NLS-2$
				return false;
			}
		} else if(MainWindow.getOpType()==OpType.RECOVER) {
            if(Files.exists(Paths.get((String)MainWindow.getPreference(MainWindow.DESTINATION_FOLDER)))) {
                return true;
            } else {
                JOptionPane.showMessageDialog(MainWindow.getMainFrame(), Messages.getString("FolderSelection.MB_MSG_RECOVER_FOLDER_NEXISTS"), Messages.getString("FolderSelection.MB_TITLE_ERROR"), JOptionPane.ERROR_MESSAGE); //$NON-NLS-1$ //$NON-NLS-2$
                return false;
            }
        } else if(MainWindow.getOpType()==OpType.UNINSTALL) {
            if(Files.exists(Paths.get((String)MainWindow.getPreference(MainWindow.DESTINATION_FOLDER)))) {
                return JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(MainWindow.getMainFrame(), Messages.getString("FolderSelection.MB_MSG_CONFIRM_UNINSTALL"), Messages.getString("FolderSelection.MB_TITLE_CONFIRM"), JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE); //$NON-NLS-1$ //$NON-NLS-2$
            } else {
                JOptionPane.showMessageDialog(MainWindow.getMainFrame(), Messages.getString("FolderSelection.MB_MSG_UNINSTALL_FOLDER_NEXISTS"), Messages.getString("FolderSelection.MB_TITLE_ERROR"), JOptionPane.ERROR_MESSAGE); //$NON-NLS-1$ //$NON-NLS-2$
                return false;
            }
        }
		return false;
	}

    /**
     * @see WizardStep#onStepActivated(boolean)
     */
	@Override
	public void onStepActivated(boolean nextClicked) {
		panelSpace.setVisible(true);
		updateSize();
		String projectName = MainWindow.getPreference(MainWindow.APP_NAME) + " v" + MainWindow.getPreference(MainWindow.APP_VERSION); //$NON-NLS-1$
		if(MainWindow.getOpType()==OpType.INSTALL) {
			taMessage.setText(String.format(Messages.getString("FolderSelection.INSTALL_MSG"), projectName)); //$NON-NLS-1$
			if(tfFolder.getText().isEmpty()) {
				String installFolder = System.getProperty("user.home") + ("/Programs/"+MainWindow.getPreference(MainWindow.DST_FOLDER_NAME)).replace('/', File.separatorChar); //$NON-NLS-1$ //$NON-NLS-2$
				tfFolder.setText(installFolder);
				MainWindow.setPreference(MainWindow.DESTINATION_FOLDER, installFolder);
			}
			tfFolder.setEditable(true);
			btnBrowse.setEnabled(true);
		} else if(MainWindow.getOpType()==OpType.UPDATE) {
			taMessage.setText(String.format(Messages.getString("FolderSelection.UPDATE_MSG"), projectName)); //$NON-NLS-1$
			tfFolder.setText(Paths.get((String)MainWindow.getPreference(MainWindow.DESTINATION_FOLDER)).toString());
			tfFolder.setEditable(false);
			btnBrowse.setEnabled(false);
			// TODO : Update space required for update.
		} else if(MainWindow.getOpType()==OpType.RECOVER) {
			taMessage.setText(String.format(Messages.getString("FolderSelection.RECOVER_MSG"), projectName)); //$NON-NLS-1$
            tfFolder.setText(Paths.get((String)MainWindow.getPreference(MainWindow.DESTINATION_FOLDER)).toString());
            tfFolder.setEditable(true);
            btnBrowse.setEnabled(true);
		} else if(MainWindow.getOpType()==OpType.UNINSTALL) {
    		taMessage.setText(String.format(Messages.getString("FolderSelection.UNINSTALL_MSG"), projectName)); //$NON-NLS-1$
            tfFolder.setText(Paths.get(MainWindow.getPreference(MainWindow.DESTINATION_FOLDER).toString()).toString());
            tfFolder.setEditable(false);
            btnBrowse.setEnabled(false);
            MainWindow.getNextButton().setVisible(false);
            JButton installButton = MainWindow.getInstallButton();
            installButton.setText(Messages.getString("FolderSelection.BUTTON_UNINSTALL")); //$NON-NLS-1$
            installButton.setEnabled(true);
            installButton.setVisible(true);
            panelSpace.setVisible(false);
		}
	}

    /**
     * @see WizardStep#onStepDeactivated(boolean)
     */
	@Override
	public void onStepDeactivated(boolean nextClicked) {}

    /**
     * Updates the available and required space labels.
     */
	private void updateSize() {
		String folder = tfFolder.getText();
		if(!folder.isEmpty()) {
			long availabeSpace = Helper.getDriveFreeSpace(folder);
			MainWindow.setPreference(MainWindow.AVAILABLE_SPACE, availabeSpace);
			lblAvailable.setText(Helper.formatBytes(availabeSpace));
			if(MainWindow.getOpType() != OpType.UNINSTALL) {
				lblRequired.setText(Helper.formatBytes((int)MainWindow.getPreference(MainWindow.REQUIRED_SIZE)));
			}
		}
	}

	/**
	 * Create the panel.
	 */
	public FolderSelection() {
		taMessage = new JTextArea();
		taMessage.setFocusable(false);
		taMessage.setMargin(new Insets(1, 1, 1, 1));
		taMessage.setBorder(null);
		taMessage.setOpaque(false);
		taMessage.setFont(new Font("Tahoma", Font.PLAIN, 11)); //$NON-NLS-1$
		taMessage.setWrapStyleWord(true);
		taMessage.setLineWrap(true);
		taMessage.setEditable(false);
		taMessage.setText("Install or Update message..."); //$NON-NLS-1$

		tfFolder = new JTextField();
		tfFolder.setColumns(10);
		tfFolder.getDocument().addDocumentListener(new DocumentListener() {

			@Override
			public void insertUpdate(DocumentEvent e) { updateSize(); }

			@Override
			public void removeUpdate(DocumentEvent e) { updateSize(); }

			@Override
			public void changedUpdate(DocumentEvent e) { updateSize(); }

		});

		btnBrowse = new JButton(Messages.getString("FolderSelection.BUTTON_BROWSE_FOLDER")); //$NON-NLS-1$
		btnBrowse.addActionListener(new ActionListener() {
			/**
			 * Browse for destination folder selection
			 */
			public void actionPerformed(ActionEvent e) {
				JFileChooser fileChooser = new JFileChooser();
				fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				fileChooser.setAcceptAllFileFilterUsed(false);
				fileChooser.setDialogTitle(Messages.getString("FolderSelection.JFC_SEL_INSTALL_FOLDER")); //$NON-NLS-1$
				String path = Helper.moveUpToExistingParentDir(tfFolder.getText());
				fileChooser.setCurrentDirectory(new File(path!=null ? path : System.getProperty("user.home"))); //$NON-NLS-1$
				int result = fileChooser.showOpenDialog(MainWindow.getMainFrame());
				if (result == JFileChooser.APPROVE_OPTION) {
					File file = fileChooser.getSelectedFile();
					if(file.canWrite()) {
						String dstFolder = (String)MainWindow.getPreference(MainWindow.DST_FOLDER_NAME);
						String dstPath = file.getAbsolutePath();
                        if(!dstPath.endsWith(dstFolder))
                        	dstPath = dstPath + "/" + dstFolder; //$NON-NLS-1$
						tfFolder.setText(dstPath.replace('/', File.separatorChar).replace(":\\\\", ":\\")); //$NON-NLS-1$ //$NON-NLS-2$
						MainWindow.setPreference(MainWindow.DESTINATION_FOLDER, tfFolder.getText());
					} else JOptionPane.showMessageDialog(MainWindow.getMainFrame(), Messages.getString("FolderSelection.MB_MSG_NOT_WRITABLE_FOLDER"), Messages.getString("FolderSelection.MB_TITLE_ERROR"), JOptionPane.ERROR_MESSAGE); //$NON-NLS-1$ //$NON-NLS-2$
				}
			}
		});

		JLabel lblNewLabel = new JLabel(Messages.getString("FolderSelection.LBL_DST_FOLDER")); //$NON-NLS-1$

		panelSpace = new JPanel();
		GroupLayout groupLayout = new GroupLayout(this);
		groupLayout.setHorizontalGroup(
			groupLayout.createParallelGroup(Alignment.TRAILING)
				.addGroup(groupLayout.createSequentialGroup()
					.addGap(22)
					.addGroup(groupLayout.createParallelGroup(Alignment.TRAILING)
						.addGroup(Alignment.LEADING, groupLayout.createSequentialGroup()
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(lblNewLabel))
						.addComponent(taMessage, GroupLayout.DEFAULT_SIZE, 406, Short.MAX_VALUE)
						.addGroup(groupLayout.createSequentialGroup()
							.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
								.addGroup(groupLayout.createSequentialGroup()
									.addPreferredGap(ComponentPlacement.RELATED)
									.addComponent(panelSpace, GroupLayout.DEFAULT_SIZE, 321, Short.MAX_VALUE))
								.addComponent(tfFolder, GroupLayout.DEFAULT_SIZE, 321, Short.MAX_VALUE))
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(btnBrowse)))
					.addGap(22))
		);
		groupLayout.setVerticalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addGap(22)
					.addComponent(taMessage, GroupLayout.PREFERRED_SIZE, 69, GroupLayout.PREFERRED_SIZE)
					.addGap(16)
					.addComponent(lblNewLabel)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
						.addComponent(tfFolder, GroupLayout.PREFERRED_SIZE, 21, GroupLayout.PREFERRED_SIZE)
						.addComponent(btnBrowse))
					.addGap(18)
					.addComponent(panelSpace, GroupLayout.PREFERRED_SIZE, 48, GroupLayout.PREFERRED_SIZE)
					.addContainerGap(84, Short.MAX_VALUE))
		);

				JLabel lblNewLabel_1 = new JLabel(Messages.getString("FolderSelection.LBL_RQ_SPACE")); //$NON-NLS-1$

				lblRequired = new JLabel("0.00 XB"); //$NON-NLS-1$

				JLabel lblNewLabel_4 = new JLabel(Messages.getString("FolderSelection.LBL_AV_SPACE")); //$NON-NLS-1$

				lblAvailable = new JLabel("0.00 XB"); //$NON-NLS-1$
		GroupLayout gl_panelSpace = new GroupLayout(panelSpace);
		gl_panelSpace.setHorizontalGroup(
			gl_panelSpace.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panelSpace.createSequentialGroup()
					.addGap(1)
					.addGroup(gl_panelSpace.createParallelGroup(Alignment.LEADING)
						.addGroup(gl_panelSpace.createSequentialGroup()
							.addGap(1)
							.addComponent(lblNewLabel_1)
							.addGap(6)
							.addComponent(lblRequired))
						.addGroup(gl_panelSpace.createSequentialGroup()
							.addComponent(lblNewLabel_4)
							.addGap(6)
							.addComponent(lblAvailable)))
					.addContainerGap(30, Short.MAX_VALUE))
		);
		gl_panelSpace.setVerticalGroup(
			gl_panelSpace.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panelSpace.createSequentialGroup()
					.addGap(1)
					.addGroup(gl_panelSpace.createParallelGroup(Alignment.LEADING)
						.addComponent(lblNewLabel_1)
						.addComponent(lblRequired))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(gl_panelSpace.createParallelGroup(Alignment.LEADING)
						.addComponent(lblNewLabel_4)
						.addComponent(lblAvailable))
					.addContainerGap(38, Short.MAX_VALUE))
		);
		panelSpace.setLayout(gl_panelSpace);
		setLayout(groupLayout);

		Helper.enableRtlWhenNeeded(FolderSelection.this);
	}
}
