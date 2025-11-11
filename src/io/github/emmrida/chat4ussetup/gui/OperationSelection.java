/*
 * Copyright (c) 2025 El Mhadder Mohamed Rida. All rights reserved.
 * This code is licensed under the [MIT License](https://opensource.org/licenses/MIT).
 */
package io.github.emmrida.chat4ussetup.gui;

import javax.swing.JPanel;

import io.github.emmrida.chat4ussetup.util.AppLinkCreator;
import io.github.emmrida.chat4ussetup.util.CrossPlatformVersionReader;
import io.github.emmrida.chat4ussetup.util.Helper;
import io.github.emmrida.chat4ussetup.util.Messages;
import io.github.emmrida.chat4ussetup.util.VersionComparator;
import io.github.emmrida.chat4ussetup.util.ZipArchiveHandler;
import io.github.emmrida.chat4ussetup.util.ZipArchiveHandler.ProgressListener;
import io.github.emmrida.chat4ussetup.util.ZipArchiveHandler.SizeInfo;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.JRadioButton;
import javax.swing.ButtonGroup;
import javax.swing.JProgressBar;
import javax.swing.JLabel;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

/**
 * OperationSelection class. Let the user choose the operation to perform:
 * install, update, recover or uninstall
 * @author El Mhadder Mohamed Rida
 */
public class OperationSelection extends JPanel implements WizardStep {
	private static final long serialVersionUID = 1L;

	private boolean archiveTested = false;
	private String origSourceArchive = null; // Save original installation archive path when the user selects the "Recover" option

	private final ButtonGroup buttonGroup = new ButtonGroup();
	private JRadioButton rbtnInstall;
	private JRadioButton rbtnUpdate;
	private JRadioButton rbtnRecover;
	private JPanel panelProgress;
	private JProgressBar progressBar;
	private JLabel lblSrcArchiveState;
	private JRadioButton rbtnUninstall;

    /**
     * @see WizarStep#getTitle()
     */
	@Override
	public String getTitle() {
		return Messages.getString("OperationSelection.TITLE_SETUP_TYPE"); //$NON-NLS-1$
	}

    /**
     * @see WizarStep#getDescription()
     */
	@Override
	public String getDescription() {
		return Messages.getString("OperationSelection.DESC_SETUP_TYPE"); //$NON-NLS-1$
	}

    /**
     * @see WizarStep#validateStep()
     */
	@Override
	public boolean validateStep() {
		boolean selected = rbtnInstall.isSelected() || rbtnUpdate.isSelected() || rbtnRecover.isSelected() || rbtnUninstall.isSelected();
		if(!selected) {
			JOptionPane.showMessageDialog(MainWindow.getMainFrame(), Messages.getString("OperationSelection.MB_MSG_SEL_OPTION"), Messages.getString("OperationSelection.MB_TITLE_INFORMATION"), JOptionPane.ERROR_MESSAGE); //$NON-NLS-1$ //$NON-NLS-2$
			return false;
		}
		if(!archiveTested) {
			JOptionPane.showMessageDialog(MainWindow.getMainFrame(), Messages.getString("OperationSelection.MB_MSG_SRC_ARC_TST_FAILURE"), Messages.getString("OperationSelection.MB_TITLE_INFORMATION"), JOptionPane.ERROR_MESSAGE); //$NON-NLS-1$ //$NON-NLS-2$
			return false;
		} else if(rbtnInstall.isSelected()) {
			MainWindow.getInstance().startInstallation();
			return true;
		} else if(rbtnUninstall.isSelected()) {
    		MainWindow.getInstance().startUninstall();
    		return true;
		} else if(rbtnUpdate.isSelected()) {
			MainWindow.getInstance().startUpdate();
			String curVersion = (String)MainWindow.getPreference(MainWindow.INSTALLED_APP_VERSION);
			String minVersion = (String)MainWindow.getPreference(MainWindow.MINIMUM_VERSION_UPDATE);
			if(minVersion != null && curVersion != null && VersionComparator.compareVersions(minVersion, curVersion, true) <= 0) {
				if(VersionComparator.compareVersions(curVersion, (String)MainWindow.getPreference(MainWindow.APP_VERSION), true) < 0) {
					MainWindow.getInstance().startUpdate();
					return true;
				} else {
					JOptionPane.showMessageDialog(MainWindow.getMainFrame(), Messages.getString("OperationSelection.MB_MSG_LATEST_UPDATE_FOUND"), Messages.getString("OperationSelection.MB_TITLE_INFORMATION"), JOptionPane.INFORMATION_MESSAGE); //$NON-NLS-1$ //$NON-NLS-2$
					return false;
				}
			} else {
				JOptionPane.showMessageDialog(MainWindow.getMainFrame(), Messages.getString("OperationSelection.MB_MSG_TOO_OLD_TO_UPDATE"), Messages.getString("OperationSelection.MB_TITLE_ERROR"), JOptionPane.INFORMATION_MESSAGE); //$NON-NLS-1$ //$NON-NLS-2$
				return false;
			}
		} else if(rbtnRecover.isSelected()) {
			int ret = JOptionPane.showConfirmDialog(MainWindow.getMainFrame(), Messages.getString("OperationSelection.MB_MSG_RECOVER_CONFIRM"), Messages.getString("OperationSelection.MB_TITLE_CONFIRM"), JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE); //$NON-NLS-1$ //$NON-NLS-2$
            if(ret == JOptionPane.YES_OPTION) {
            	MainWindow.getInstance().startRecovery();
            	return true;
            }
        }

		return false;
	}

    /**
     * @see WizarStep#onStepActivated(boolean)
     */
	@Override
	public void onStepActivated(boolean nextClicked) {
		if(lblSrcArchiveState.getClientProperty("text") == null) //$NON-NLS-1$
			lblSrcArchiveState.putClientProperty("text", lblSrcArchiveState.getText()); //$NON-NLS-1$
		if(origSourceArchive == null)
			origSourceArchive = (String)MainWindow.getPreference(MainWindow.SOURCE_ARCHIVE);
		MainWindow.getInstallButton().setVisible(false);
		MainWindow.getNextButton().setVisible(true);

		String linkFolder = AppLinkCreator.readAppParentFolder((String)MainWindow.getPreference(MainWindow.APP_NAME));
		if(linkFolder != null) {
			MainWindow.setPreference(MainWindow.DESTINATION_FOLDER, linkFolder);
		} else {
			rbtnUninstall.setEnabled(false);
			rbtnUpdate.setEnabled(false);
		}

	}

    /**
     * @see WizarStep#onStepDeactivated(boolean)
     */
	@Override
	public void onStepDeactivated(boolean nextClicked) { }

	/**
	 * Tests the archive for corruption and retrieves its uncompressed size
	 * @param zipFile The path to the archive
	 * @param onComplete Runnable to be executed when the test is complete
	 */
	private void testArchive(String zipFile, Runnable onComplete) {
		new Thread(()-> {
			SwingUtilities.invokeLater(() -> {
				rbtnInstall.setEnabled(false);
				rbtnUpdate.setEnabled(false);
				rbtnRecover.setEnabled(false);
				rbtnUninstall.setEnabled(false);
				panelProgress.setVisible(true);
				progressBar.setIndeterminate(true);
			});
			try {
				ZipArchiveHandler zip = new ZipArchiveHandler(zipFile);
				SizeInfo info = zip.calculateSizes();
				if(info.uncompressedSize <= 0) {
					SwingUtilities.invokeLater(() -> {
						String linkFolder = AppLinkCreator.readAppParentFolder((String)MainWindow.getPreference(MainWindow.APP_NAME));
						progressBar.setIndeterminate(false);
						rbtnInstall.setEnabled(true);
						rbtnUpdate.setEnabled(linkFolder != null);
						rbtnRecover.setEnabled(true);
						rbtnUninstall.setEnabled(linkFolder != null);
						lblSrcArchiveState.setText(lblSrcArchiveState.getClientProperty("text") + Messages.getString("OperationSelection.LBL_ARC_TST_FAILED")); //$NON-NLS-1$ //$NON-NLS-2$
						JOptionPane.showMessageDialog(MainWindow.getMainFrame(), Messages.getString("OperationSelection.MB_MSG_CORRUPTED_ARCHIVE"), Messages.getString("OperationSelection.MB_TITLE_ERROR"), JOptionPane.ERROR_MESSAGE); //$NON-NLS-1$ //$NON-NLS-2$
					});
					archiveTested = false;
					return;
				}
				MainWindow.setPreference(MainWindow.REQUIRED_SIZE, (int)info.uncompressedSize);
				SwingUtilities.invokeLater(() -> {
					progressBar.setIndeterminate(false);
					progressBar.setMaximum((int)info.uncompressedSize);
					progressBar.setValue(0);
				});
				zip.setProgressListener(new ProgressListener() {
					int progress = 0;
					@Override
					public void onProgress(int blockSize) {
						progress += blockSize;
						SwingUtilities.invokeLater(() -> progressBar.setValue(progress));
					}
					@Override
					public void onFileCreated(String name) { }
				});
				archiveTested = zip.testArchive();
				SwingUtilities.invokeLater(() -> {
					String linkFolder = AppLinkCreator.readAppParentFolder((String)MainWindow.getPreference(MainWindow.APP_NAME));
					progressBar.setMaximum(100);
					progressBar.setValue(progressBar.getMaximum());
					lblSrcArchiveState.setText(lblSrcArchiveState.getClientProperty("text").toString() + Messages.getString("OperationSelection.LBL_ARC_TST_OK")); //$NON-NLS-1$ //$NON-NLS-2$
					rbtnInstall.setEnabled(archiveTested);
					rbtnUpdate.setEnabled(archiveTested && linkFolder != null);
					rbtnRecover.setEnabled(true);
					rbtnUninstall.setEnabled(archiveTested && linkFolder != null);
				});

				String linkFolder = AppLinkCreator.readAppParentFolder((String)MainWindow.getPreference(MainWindow.APP_NAME));
				if(linkFolder != null)
					MainWindow.setPreference(MainWindow.DESTINATION_FOLDER, linkFolder);

				if(rbtnUpdate.isSelected() || rbtnRecover.isSelected()) {
					if(linkFolder == null) {
						String defaultFolder = Path.of((String)System.getProperty("user.home"), "Programs", (String)MainWindow.getPreference(MainWindow.DST_FOLDER_NAME)).toString(); //$NON-NLS-1$ //$NON-NLS-2$
						JOptionPane.showMessageDialog(MainWindow.getMainFrame(), String.format(Messages.getString("OperationSelection.MB_MSG_DST_FOLDER_NOT_FOUND"), defaultFolder), Messages.getString(Messages.getString("OperationSelection.1")), JOptionPane.WARNING_MESSAGE); //$NON-NLS-3$ //$NON-NLS-1$ //$NON-NLS-2$
                        linkFolder = defaultFolder;
					}
					if(linkFolder != null) {
						MainWindow.setPreference(MainWindow.DESTINATION_FOLDER, linkFolder);
						String curVersion = CrossPlatformVersionReader.extractVersion(linkFolder + File.separator + (String)MainWindow.getPreference(MainWindow.APP_RUNNABLE));
						MainWindow.setPreference(MainWindow.INSTALLED_APP_VERSION, curVersion);
					} else {
						SwingUtilities.invokeLater(() -> {
							JOptionPane.showMessageDialog(MainWindow.getMainFrame(), Messages.getString("OperationSelection.MB_MSG_INSTALL_FOLDER_NOT_DETECTED"), Messages.getString("OperationSelection.MB_TITLE_INFORMATION"), JOptionPane.ERROR_MESSAGE); //$NON-NLS-1$ //$NON-NLS-2$
							rbtnInstall.setSelected(true);
							rbtnUpdate.setEnabled(false);
                            rbtnRecover.setEnabled(false);
							rbtnUninstall.setEnabled(false);
						});
					}
				}
			} catch (Exception ex) {
				System.err.println(Messages.getString("OperationSelection.LOG_INSTALL_ARC_TST_FAILED") + ex.getMessage()); //$NON-NLS-1$
				lblSrcArchiveState.setText(lblSrcArchiveState.getClientProperty("text") + Messages.getString("OperationSelection.LBL_ARC_TST_FAILED")); //$NON-NLS-1$ //$NON-NLS-2$
			}
			while(!rbtnInstall.isEnabled())
				try { Thread.sleep(100); } catch (InterruptedException e) { e.printStackTrace(); }
			if(onComplete != null)
                onComplete.run();
		}).start();
	}

	/**
	 * Create the panel.
	 */
	public OperationSelection() {
		rbtnInstall = new JRadioButton(Messages.getString("OperationSelection.RBTN_INSTALL")); //$NON-NLS-1$
		rbtnInstall.addItemListener(e -> {
			if(((JRadioButton)e.getSource()).isSelected()) {
				MainWindow.setPreference(MainWindow.SOURCE_ARCHIVE, origSourceArchive);
				testArchive(origSourceArchive, () -> {
					if(archiveTested)
						SwingUtilities.invokeLater(() -> MainWindow.getMainFrame().setTitle(Messages.getString("OperationSelection.MWND_TITLE_PREFIX") + MainWindow.getPreference(MainWindow.APP_NAME) + " v" + MainWindow.getPreference(MainWindow.APP_VERSION))); //$NON-NLS-1$ //$NON-NLS-2$
				});
			}
		});
		buttonGroup.add(rbtnInstall);

		rbtnUpdate = new JRadioButton(Messages.getString("OperationSelection.RBTN_UPDATE")); //$NON-NLS-1$
		rbtnUpdate.addItemListener(e -> {
			if(((JRadioButton)e.getSource()).isSelected()) {
				MainWindow.setPreference(MainWindow.SOURCE_ARCHIVE, origSourceArchive);
				testArchive(origSourceArchive, () -> {
					if(archiveTested)
						SwingUtilities.invokeLater(() -> MainWindow.getMainFrame().setTitle(Messages.getString("OperationSelection.MWND_TITLE_PREFIX") + MainWindow.getPreference(MainWindow.APP_NAME) + " v" + MainWindow.getPreference(MainWindow.APP_VERSION))); //$NON-NLS-1$ //$NON-NLS-2$
				});
			}
		});
		buttonGroup.add(rbtnUpdate);

		rbtnRecover = new JRadioButton(Messages.getString("OperationSelection.RBTN_RECOVER")); //$NON-NLS-1$
        rbtnRecover.addItemListener(e -> {
            if(((JRadioButton)e.getSource()).isSelected()) {
                JFileChooser jfc = new JFileChooser();
                jfc.setFileSelectionMode(JFileChooser.FILES_ONLY);
                jfc.setAcceptAllFileFilterUsed(false);
                jfc.setFileFilter(new FileNameExtensionFilter(Messages.getString("OperationSelection.JFC_FILTER_TEXT_ZIP"), "zip")); //$NON-NLS-1$ //$NON-NLS-2$
                jfc.setDialogTitle(Messages.getString("OperationSelection.JFC_RECOVER_TITLE")); //$NON-NLS-1$
                int result = jfc.showOpenDialog(MainWindow.getMainFrame());
                if (result == JFileChooser.APPROVE_OPTION) {
                	String zipFile = jfc.getSelectedFile().getAbsolutePath();
                    MainWindow.setPreference(MainWindow.SOURCE_ARCHIVE, zipFile);
                    testArchive(zipFile, () -> {
	                    if(archiveTested) {
	                    	String appName = (String)MainWindow.getPreference(MainWindow.APP_RUNNABLE);
	                        List<String> files = new ZipArchiveHandler(zipFile).listFiles();
	                        for(String file : files) {
	                            if(file.endsWith(appName)) {
	                            	SwingUtilities.invokeLater(() -> MainWindow.getMainFrame().setTitle(Messages.getString("OperationSelection.MWND_TITLE_PREFIX") + MainWindow.getPreference(MainWindow.APP_NAME))); //$NON-NLS-1$
	                            	return;
	                            }
	                        }
	                        SwingUtilities.invokeLater(() -> {
	                        	JOptionPane.showMessageDialog(MainWindow.getMainFrame(), Messages.getString("OperationSelection.MB_MSG_INVALID_BKARC") + appName, Messages.getString("OperationSelection.MB_TITLE_ERROR"), JOptionPane.ERROR_MESSAGE); //$NON-NLS-1$ //$NON-NLS-2$
	                        	rbtnInstall.setSelected(true);
	                        });
	                    }
                    });
                } else {
                	archiveTested = false;
                	lblSrcArchiveState.setText(lblSrcArchiveState.getClientProperty("text") + Messages.getString("OperationSelection.LBL_ARC_TST_FAILED")); //$NON-NLS-1$ //$NON-NLS-2$
                }
            }
		});
		buttonGroup.add(rbtnRecover);

		panelProgress = new JPanel();
		panelProgress.setVisible(false);

		rbtnUninstall = new JRadioButton(Messages.getString("OperationSelection.RBTN_UNINSTALL")); //$NON-NLS-1$
		rbtnUninstall.addFocusListener(new FocusAdapter() {
			@Override
			public void focusGained(FocusEvent e) {
				panelProgress.setVisible(!rbtnUninstall.isSelected());
			}
			@Override
			public void focusLost(FocusEvent e) {
				panelProgress.setVisible(!rbtnUninstall.isSelected());
			}
		});
		rbtnUninstall.addActionListener(e -> {
			if(((JRadioButton)e.getSource()).isSelected()) {
				panelProgress.setVisible(false);
				archiveTested = true;
				String linkFolder = AppLinkCreator.readAppParentFolder((String)MainWindow.getPreference(MainWindow.APP_NAME));
				if(linkFolder != null)
					MainWindow.setPreference(MainWindow.DESTINATION_FOLDER, linkFolder);
				MainWindow.getMainFrame().setTitle(Messages.getString("OperationSelection.MWND_TITLE_PREFIX") + MainWindow.getPreference(MainWindow.APP_NAME)); //$NON-NLS-1$
			}
		});
		buttonGroup.add(rbtnUninstall);
		GroupLayout groupLayout = new GroupLayout(this);
		groupLayout.setHorizontalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addGap(22)
					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
						.addGroup(groupLayout.createSequentialGroup()
							.addComponent(rbtnUninstall)
							.addContainerGap())
						.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
							.addGroup(groupLayout.createSequentialGroup()
								.addComponent(panelProgress, GroupLayout.DEFAULT_SIZE, 406, Short.MAX_VALUE)
								.addGap(22))
							.addGroup(groupLayout.createSequentialGroup()
								.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
									.addComponent(rbtnInstall)
									.addComponent(rbtnUpdate)
									.addComponent(rbtnRecover))
								.addGap(193)))))
		);
		groupLayout.setVerticalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addGap(22)
					.addComponent(rbtnInstall)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(rbtnUpdate)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(rbtnRecover)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(rbtnUninstall)
					.addPreferredGap(ComponentPlacement.RELATED, 118, Short.MAX_VALUE)
					.addComponent(panelProgress, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
					.addGap(22))
		);

		lblSrcArchiveState = new JLabel(Messages.getString("OperationSelection.LBL_SRC_ARC_CHECKING")); //$NON-NLS-1$

		progressBar = new JProgressBar();
		progressBar.setStringPainted(true);
		GroupLayout gl_panelProgress = new GroupLayout(panelProgress);
		gl_panelProgress.setHorizontalGroup(
			gl_panelProgress.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panelProgress.createSequentialGroup()
					.addGap(3)
					.addGroup(gl_panelProgress.createParallelGroup(Alignment.LEADING)
						.addComponent(progressBar, GroupLayout.DEFAULT_SIZE, 400, Short.MAX_VALUE)
						.addComponent(lblSrcArchiveState))
					.addGap(3))
		);
		gl_panelProgress.setVerticalGroup(
			gl_panelProgress.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panelProgress.createSequentialGroup()
					.addGap(3)
					.addComponent(lblSrcArchiveState)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(progressBar, GroupLayout.DEFAULT_SIZE, 17, Short.MAX_VALUE)
					.addGap(6))
		);
		panelProgress.setLayout(gl_panelProgress);
		setLayout(groupLayout);

		Helper.enableRtlWhenNeeded(OperationSelection.this);
	}
}
