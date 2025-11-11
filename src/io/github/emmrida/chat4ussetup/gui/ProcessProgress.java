/*
 * Copyright (c) 2025 El Mhadder Mohamed Rida. All rights reserved.
 * This code is licensed under the [MIT License](https://opensource.org/licenses/MIT).
 */
package io.github.emmrida.chat4ussetup.gui;

import javax.swing.JPanel;
import io.github.emmrida.chat4ussetup.gui.MainWindow.OpType;
import io.github.emmrida.chat4ussetup.util.AppLinkCreator;
import io.github.emmrida.chat4ussetup.util.FilesLister;
import io.github.emmrida.chat4ussetup.util.Helper;
import io.github.emmrida.chat4ussetup.util.Messages;
import io.github.emmrida.chat4ussetup.util.Helper.KeyValue;
import io.github.emmrida.chat4ussetup.util.XMLEditor;
import io.github.emmrida.chat4ussetup.util.ZipArchiveHandler;
import io.github.emmrida.chat4ussetup.util.ZipArchiveHandler.ProgressListener;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.SwingUtilities;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import java.awt.Font;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

/**
 * ProcessProgress class. This class is used to show the progress of setup process.
 * @author El Mhadder Mohamed Rida
 */
public class ProcessProgress extends JPanel implements WizardStep {
	private static final long serialVersionUID = 1L;

	private JTextArea textArea;
	private JProgressBar progressBar;
	private JLabel lblProgress;

	/**
	 * @see WizarStep#getTitle()
	 */
	@Override
	public String getTitle() {
		if(MainWindow.getOpType()==OpType.INSTALL) {
			return Messages.getString("ProcessProgress.TITLE_INSTALLING"); //$NON-NLS-1$
		} else if(MainWindow.getOpType()==OpType.UPDATE) {
			return Messages.getString("ProcessProgress.TITLE_UPDATING"); //$NON-NLS-1$
		} else if(MainWindow.getOpType()==OpType.RECOVER) {
            return Messages.getString("ProcessProgress.TITLE_RECOVERING"); //$NON-NLS-1$
        } else if(MainWindow.getOpType()==OpType.UNINSTALL) {
            return Messages.getString("ProcessProgress.TITLE_UNINSTALLING"); //$NON-NLS-1$
        } else return ""; //$NON-NLS-1$
	}

    /**
     * @see WizarStep#getDescription()
     */
	@Override
	public String getDescription() {
		if(MainWindow.getOpType()==OpType.INSTALL) {
			return Messages.getString("ProcessProgress.DESC_INSTALLING"); //$NON-NLS-1$
        } else if(MainWindow.getOpType()==OpType.UPDATE) {
        	return Messages.getString("ProcessProgress.DESC_UPDATING"); //$NON-NLS-1$
		} else if(MainWindow.getOpType()==OpType.RECOVER) {
			return Messages.getString("ProcessProgress.DESC_RECOVERING"); //$NON-NLS-1$
        } else if(MainWindow.getOpType()==OpType.UNINSTALL) {
            return Messages.getString("ProcessProgress.DESC_UNINSTALLING"); //$NON-NLS-1$
		} else return ""; //$NON-NLS-1$
	}

    /**
     * @see WizarStep#validateStep()
     */
	@Override
	public boolean validateStep() {
		if(null == MainWindow.getPreference(MainWindow.ALL_SUCCESS)) {
			String msg = null;
			if(MainWindow.getOpType()==OpType.INSTALL) {
				msg = Messages.getString("ProcessProgress.MB_MSG_WAIT_INSTALLING"); //$NON-NLS-2$
			} else if(MainWindow.getOpType()==OpType.UPDATE) {
				msg = Messages.getString("ProcessProgress.MB_MSG_WAIT_UPDATING"); //$NON-NLS-1$
			} else if(MainWindow.getOpType()==OpType.RECOVER) {
	            msg = Messages.getString("ProcessProgress.MB_MSG_WAIT_RECOVERING"); //$NON-NLS-1$
	        } else if(MainWindow.getOpType()==OpType.UNINSTALL) {
	            msg = Messages.getString("ProcessProgress.MB_MSG_WAIT_UNINSTALLING"); //$NON-NLS-2$
	        }
			if(msg != null) {
				JOptionPane.showMessageDialog(MainWindow.getMainFrame(), msg, Messages.getString("ProcessProgress.MB_TITLE_INFORMATION"), JOptionPane.INFORMATION_MESSAGE); //$NON-NLS-1$
				return false;
			}
		}
		return true;
	}

    /**
     * @see WizarStep#onStepActivated(boolean)
     */
	@Override
	public void onStepActivated(boolean nextClicked) {
		MainWindow.getNextButton().setVisible(false);
		MainWindow.getBackButton().setVisible(false);
		MainWindow.getCancelButton().setEnabled(false);
		if(MainWindow.getOpType()==OpType.INSTALL) {
			lblProgress.setText(Messages.getString("ProcessProgress.LBL_INSTALL_PROGRESS")); //$NON-NLS-1$
			processInstallation();
		} else if(MainWindow.getOpType()==OpType.UPDATE) {
            lblProgress.setText(Messages.getString("ProcessProgress.LBL_UPDATE_PROGRESS")); //$NON-NLS-1$
			processUpdate();
		} else if(MainWindow.getOpType()==OpType.RECOVER) {
            lblProgress.setText(Messages.getString("ProcessProgress.LBL_RECOVER_PROGRESS")); //$NON-NLS-1$
            processRecovery();
        } else if(MainWindow.getOpType()==OpType.UNINSTALL) {
        	lblProgress.setText(Messages.getString("ProcessProgress.LBL_UNINSTALL_PROGRESS")); //$NON-NLS-1$
        	processUninstall();
        }
	}

    /**
     * @see WizarStep#onStepDeactivated(boolean)
     */
	@Override
	public void onStepDeactivated(boolean nextClicked) { }

	/**
	 * Process the uninstall of current installation
	 */
	private void processUninstall() {
		final String uninstallFolder = (String)MainWindow.getPreference(MainWindow.DESTINATION_FOLDER);
		progressBar.setValue(0);
		progressBar.setMaximum(100);
		progressBar.setIndeterminate(true);
		textArea.setText(""); //$NON-NLS-1$
		new Thread(() -> {
			SwingUtilities.invokeLater(() -> progressBar.setIndeterminate(true));
			try {
				if(Files.exists(Paths.get(uninstallFolder))) {
					String appName = (String)MainWindow.getPreference(MainWindow.APP_NAME);
					appendLog(Messages.getString("ProcessProgress.LOG_INSTALL_FOLDER_DELETING") + uninstallFolder); //$NON-NLS-1$
					Helper.deleteFolderTree(uninstallFolder);
					appendLog(Messages.getString("ProcessProgress.LOG_DESKTOP_LINK_REMOVING")); //$NON-NLS-1$
					AppLinkCreator.removeDesktopLink(appName);
					appendLog(Messages.getString("ProcessProgress.LOG_SYSMNU_LINK_REMOVING")); //$NON-NLS-1$
					AppLinkCreator.removeSystemMenuLink(appName);
					MainWindow.setPreference(MainWindow.ALL_SUCCESS, true);
					SwingUtilities.invokeLater(() -> {
						textArea.append(Messages.getString("ProcessProgress.LOG_UNINSTALL_SUCCESS")); //$NON-NLS-1$
						textArea.setCaretPosition(textArea.getDocument().getLength());
						progressBar.setIndeterminate(false);
						progressBar.setValue(100);
						MainWindow.getNextButton().setVisible(true);
					});
				}
				while(!MainWindow.getNextButton().isVisible())
					try { Thread.sleep(100); } catch (InterruptedException ignored) { }
				Files.writeString(Paths.get(uninstallFolder, "./uninstall.log"), textArea.getText());
			} catch (Exception ex) {
				MainWindow.setPreference(MainWindow.ALL_SUCCESS, false);
				SwingUtilities.invokeLater(() -> {
					appendLog(Messages.getString("ProcessProgress.LOG_UNINSTALL_ERROR") + ex.getMessage() + "\n"); //$NON-NLS-1$ //$NON-NLS-2$
					MainWindow.setDescription(Messages.getString("ProcessProgress.DESC_UNINSTALL_FAILURE")); //$NON-NLS-1$
    				MainWindow.getNextButton().setVisible(true);
    				MainWindow.getNextButton().setEnabled(true);
				});
				while(!MainWindow.getNextButton().isVisible())
					try { Thread.sleep(100); } catch (InterruptedException ignored) { }
				try {
					Files.writeString(Paths.get(uninstallFolder, "./uninstall.log"), textArea.getText());
				} catch (Exception ignored) { }
            }
		}).start();
    }

	/**
     * Process the recovery of a backed up previous version
     */
	private void processRecovery() {
		final String recoveryFolder = (String)MainWindow.getPreference(MainWindow.DESTINATION_FOLDER);
		progressBar.setValue(0);
		progressBar.setMaximum((int)MainWindow.getPreference(MainWindow.REQUIRED_SIZE));
		progressBar.setIndeterminate(true);
		textArea.setText(""); //$NON-NLS-1$
		new Thread(() -> {
			try {
				if(Files.exists(Paths.get(recoveryFolder))) {
					appendLog(Messages.getString("ProcessProgress.LOG_INSTALL_FOLDER_DELETING") + recoveryFolder); //$NON-NLS-2$
					Helper.deleteFolderTree(recoveryFolder);
				}
				if(Helper.createDirectoryPath(recoveryFolder)) {
					appendLog(Messages.getString("ProcessProgress.LOG_RECOVER_FOLDER_CREATED") + recoveryFolder); //$NON-NLS-1$ //$NON-NLS-2$
					ZipArchiveHandler zip = new ZipArchiveHandler((String)MainWindow.getPreference(MainWindow.SOURCE_ARCHIVE));
					zip.setProgressListener(new ProgressListener() {
						private int progress = 0;
						@Override
						public void onProgress(int size) {
							progress += size;
							SwingUtilities.invokeLater(() -> progressBar.setValue(progress));
						}

						@Override
						public void onFileCreated(String name) {
							appendLog(Messages.getString("ProcessProgress.LOG_COPYING_FILE") + name); //$NON-NLS-2$
						}
					});
					if(zip.extractTo(recoveryFolder)) {
						createAppLinks(recoveryFolder);
						SwingUtilities.invokeLater(() -> {
							textArea.append(Messages.getString("ProcessProgress.LOG_RECOVERY_COMPLETE")); //$NON-NLS-1$
							textArea.setCaretPosition(textArea.getDocument().getLength());
							MainWindow.setDescription(Messages.getString("ProcessProgress.DESC_RECOVERY_COMPLETE")); //$NON-NLS-1$
							progressBar.setIndeterminate(false);
							MainWindow.getNextButton().setVisible(true);
						});
						MainWindow.setPreference(MainWindow.ALL_SUCCESS, true);
					} else {
						SwingUtilities.invokeLater(() -> {
							textArea.append(Messages.getString("ProcessProgress.LOG_RECOVERY_FAILURE")); //$NON-NLS-1$
							textArea.setCaretPosition(textArea.getDocument().getLength());
							MainWindow.setDescription(Messages.getString("ProcessProgress.DESC_RECOVERY_FAILURE")); //$NON-NLS-1$
							MainWindow.getNextButton().setVisible(true);
						});
						MainWindow.setPreference(MainWindow.ALL_SUCCESS, false);
					}
				}
			} catch (Exception ex) {
				MainWindow.setPreference(MainWindow.ALL_SUCCESS, false);
				SwingUtilities.invokeLater(() -> {
					appendLog(Messages.getString("ProcessProgress.LOG_RECOVERY_ERROR") + ex.getMessage() + "\n"); //$NON-NLS-1$ //$NON-NLS-2$
					MainWindow.setDescription(Messages.getString("ProcessProgress.DESC_RECOVERY_FAILURE")); //$NON-NLS-1$
    				MainWindow.getNextButton().setVisible(true);
    				MainWindow.getNextButton().setEnabled(true);
				});
			}
			while(!MainWindow.getNextButton().isVisible())
				try { Thread.sleep(100); } catch (Exception ignore) { }
			try {
				Files.writeString(Paths.get(recoveryFolder, "recovery.log"), textArea.getText()); //$NON-NLS-1$
			} catch (Exception ignore) { }
		}).start();

	}

	/**
	 * Process the update: Copy files, update DB, etc
	 */
	private void processUpdate() {
		final String updateFolder = (String)MainWindow.getPreference(MainWindow.DESTINATION_FOLDER);
		progressBar.setValue(0);
		progressBar.setMaximum((int)MainWindow.getPreference(MainWindow.REQUIRED_SIZE));
		progressBar.setIndeterminate(true);
		textArea.setText(""); //$NON-NLS-1$
		new Thread(() -> {
			try {
				// Make the backup if selected
				Boolean backup = (Boolean)MainWindow.getPreference(MainWindow.CREATE_BACKUP_BEFORE_UPDATE);
				if(backup != null && backup) {
					try {
						SwingUtilities.invokeLater(() -> lblProgress.setText(Messages.getString("ProcessProgress.LBL_BACKUP_PROGRESS"))); //$NON-NLS-1$
                        String appName = (String)MainWindow.getPreference(MainWindow.APP_NAME);
						final Path backupFile = Paths.get(System.getProperty("user.home"), "backup-" + appName + "-" + Helper.getCurrentDate() + ".zip"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
						long[] backupSize = new long[] { 0 };
						appendLog(Messages.getString("ProcessProgress.LOG_CREATING_BACKUP") + backupFile); //$NON-NLS-1$
		                Files.walk(Paths.get(updateFolder))
		                    .filter(path -> !Files.isDirectory(path))
		                    .forEach(path -> {
		                    	backupSize[0] += path.toFile().length();
		                    });
		                SwingUtilities.invokeLater(() -> {
		                	progressBar.setIndeterminate(false);
		                	progressBar.setMaximum((int)backupSize[0]);
		                	progressBar.setValue(0);
		                });
		                ZipArchiveHandler zip = new ZipArchiveHandler(backupFile.toString());
		                zip.setProgressListener(new ProgressListener() {
		                	private int progress = 0;
							@Override
							public void onProgress(int blockSize) {
								progress += blockSize;
								SwingUtilities.invokeLater(() -> progressBar.setValue(progress));
							}
							@Override
							public void onFileCreated(String name) {
								appendLog(Messages.getString("ProcessProgress.LOG_FILE_ADDING") + name); //$NON-NLS-1$
							}
		                });
		                zip.createZip(Paths.get(updateFolder).toString());
		                appendLog(Messages.getString("ProcessProgress.LOG_BACKUP_SUCCESS")); //$NON-NLS-1$
					} catch (Exception ex) {
						SwingUtilities.invokeLater(() -> {
							textArea.append(Messages.getString("ProcessProgress.LOG_BACKUP_ERROR") + ex.getMessage() + "\n"); //$NON-NLS-1$ //$NON-NLS-2$
							textArea.setCaretPosition(textArea.getDocument().getLength());
							MainWindow.setDescription(Messages.getString("ProcessProgress.DESC_UPDATE_FAILURE")); //$NON-NLS-1$
							MainWindow.setPreference(MainWindow.ALL_SUCCESS, false);
							MainWindow.getNextButton().setVisible(true);
							MainWindow.getNextButton().setEnabled(true);
						});
						while(!MainWindow.getNextButton().isVisible())
							try { Thread.sleep(100); } catch (Exception ignore) { }
						try {
							Files.writeString(Paths.get((String)MainWindow.getPreference(MainWindow.DESTINATION_FOLDER), "update.log"), textArea.getText()); //$NON-NLS-1$
						} catch (Exception ignore) { }
						return;
					}
				}

				SwingUtilities.invokeLater(() -> {
    				progressBar.setIndeterminate(true);
    				progressBar.setValue(0);
    				lblProgress.setText(Messages.getString("ProcessProgress.LBL_UPDATE_PROGRESS")); //$NON-NLS-1$
    				textArea.append(Messages.getString("ProcessProgress.LOG_UPDATE_STARTING")); //$NON-NLS-1$
    				textArea.setCaretPosition(textArea.getDocument().getLength());
				});

				boolean updateSuccess = true;
				for(Entry<String, List<KeyValue>> e : MainWindow.getUpdateEntrySet()) {
    				String section = e.getKey();
    				List<KeyValue> tasks = e.getValue();
    				if("version".equals(section)) { //$NON-NLS-1$
        				continue; // Already done
    				} else if(section.startsWith("sqlite:")) { //$NON-NLS-1$
    					if(!processSqliteTasks(section, tasks)) {
    						updateSuccess = false;
    						break;
    					}
    				} else if(section.startsWith("pair:")) { //$NON-NLS-1$
        				if(!processPairTasks(section, tasks)) {
    						updateSuccess = false;
    						break;
    					}
    				} else if(section.startsWith("xml:")) { //$NON-NLS-1$
        				if(!processXmlTasks(section, tasks)) {
    						updateSuccess = false;
    						break;
    					}
    				} else if(section.equals("folders")) { //$NON-NLS-1$
        				if(!processFoldersTasks(tasks)) {
    						updateSuccess = false;
    						break;
    					}
    				} else if(section.equals("files")) { //$NON-NLS-1$
        				if(!processFilesTasks(tasks)) {
    						updateSuccess = false;
    						break;
    					}
    				} else if(section.equals("cleanup")) { //$NON-NLS-1$
        				if(!processCleanupTasks(tasks)) {
    						updateSuccess = false;
    						break;
    					}
    				} else System.err.println(Messages.getString("ProcessProgress.LOG_UNK_UPDATE_SECTION") + section); //$NON-NLS-1$
				}

				createAppLinks(updateFolder);

				final boolean updateSuccessFinal = updateSuccess;
				SwingUtilities.invokeLater(() -> {
    				progressBar.setIndeterminate(false);
    				progressBar.setValue(progressBar.getMaximum());
    				if(updateSuccessFinal) {
	    				textArea.append(Messages.getString("ProcessProgress.LOG_UPDATE_SUCCESS")); //$NON-NLS-1$
	    				textArea.setCaretPosition(textArea.getDocument().getLength());
	    				MainWindow.setDescription(Messages.getString("ProcessProgress.DESC_UPDATE_COMPLETED")); //$NON-NLS-1$
	    				MainWindow.setPreference(MainWindow.ALL_SUCCESS, true);
    				} else {
    					textArea.append(Messages.getString("ProcessProgress.LOG_UPDATE_FAILED")); //$NON-NLS-1$
    					textArea.setCaretPosition(textArea.getDocument().getLength());
    					MainWindow.setDescription(Messages.getString("ProcessProgress.DESC_UPDATE_FAILURE")); //$NON-NLS-1$
    					MainWindow.setPreference(MainWindow.ALL_SUCCESS, false);
    				}
    				MainWindow.getNextButton().setVisible(true);
    				MainWindow.getNextButton().setEnabled(true);
				});
			} catch (Exception ex) {
				MainWindow.setPreference(MainWindow.ALL_SUCCESS, false);
				SwingUtilities.invokeLater(() -> {
					appendLog(Messages.getString("ProcessProgress.LOG_UPDATE_ERROR") + ex.getMessage() + "\n"); //$NON-NLS-1$ //$NON-NLS-2$
					MainWindow.setDescription(Messages.getString("ProcessProgress.DESC_UPDATE_FAILURE")); //$NON-NLS-1$
    				MainWindow.getNextButton().setVisible(true);
    				MainWindow.getNextButton().setEnabled(true);
				});
			}
			while(!MainWindow.getNextButton().isVisible())
				try { Thread.sleep(100); } catch (Exception ignore) { }
			try {
				Files.writeString(Paths.get(updateFolder, "update.log"), textArea.getText()); //$NON-NLS-1$
			} catch (Exception ignore) { }
		}).start();
	}

	/**
	 * Create app links for current installation
	 * @param dstFolder Installation folder
	 * @return True if success
	 */
	private void createAppLinks(String dstFolder) {
		String lng = Locale.getDefault().getLanguage();
		String appDesc = (String)MainWindow.getPreference(MainWindow.LINK_DESCRIPTION + "_" + lng);
        if(appDesc == null)
            appDesc = (String)MainWindow.getPreference(MainWindow.LINK_DESCRIPTION);

		AppLinkCreator.LinkConfig config = null;
		String appName = (String)MainWindow.getPreference(MainWindow.APP_NAME);
		String appRunnable = (String)MainWindow.getPreference(MainWindow.APP_RUNNABLE);
		String icons = (String)MainWindow.getPreference(MainWindow.LINK_ICONS);
		if(icons != null) {
			config = AppLinkCreator.LinkConfig.create();
			Path path;
			for(String icon : icons.split(";")) { //$NON-NLS-1$
				path = Path.of(dstFolder, icon);
				if(Files.exists(path)) {
					config.withIcon(path.toAbsolutePath().toString());
				} else appendLog(Messages.getString("ProcessProgress.LOG_LINK_ICON_NOT_FOUND") + icon); //$NON-NLS-1$
			}
			if(appDesc != null)
				config.setDescription(appDesc);
		}
		Object createLink = MainWindow.getPreference(MainWindow.CREATE_DESKTOP_ICON);
		if(createLink != null && (boolean)createLink) {
			boolean success = false;
			appendLog(Messages.getString("ProcessProgress.LOG_DESKTOP_LINK_CREATING") + appRunnable); //$NON-NLS-1$
			if(config == null) {
				 success = AppLinkCreator.createDesktopLink(Paths.get(dstFolder, appRunnable).toString(), appName);
			} else success = AppLinkCreator.createDesktopLink(Paths.get(dstFolder, appRunnable).toString(), appName, config).isSuccess();
			if(!success)
				appendLog(Messages.getString("ProcessProgress.LOG_DESKTOP_LINK_CREATING_ERROR") + appRunnable); //$NON-NLS-1$
		}
		createLink = MainWindow.getPreference(MainWindow.CREATE_START_MENU_ICON);
		if(createLink != null && (boolean)createLink) {
			boolean success = false;
			appendLog(Messages.getString("ProcessProgress.LOG_SYSMNU_LINK_CREATING") + appRunnable); //$NON-NLS-1$
            if(config == null) {
            	success = AppLinkCreator.createSystemMenuLink(Paths.get(dstFolder, appRunnable).toString(), appName);
            } else success = AppLinkCreator.createSystemMenuLink(Paths.get(dstFolder, appRunnable).toString(), appName, config).isSuccess();
            if(!success)
                appendLog(Messages.getString("ProcessProgress.LOG_SYSMNU_LINK_CREATING_ERROR") + appRunnable); //$NON-NLS-1$
		}
	}

    /**
     * Cleanup tasks: Delete files and folders
     * @param tasks The tasks to process
     * @return True if successful
     */
	private boolean processCleanupTasks(List<KeyValue> tasks) {
        if(tasks != null && tasks.size() > 0) {
        	try {
	            for(KeyValue task : tasks) {
	                String op = task.key();
	                Path path = Paths.get((String)MainWindow.getPreference(MainWindow.DESTINATION_FOLDER), task.value());
                    if("remove".equals(op)) { //$NON-NLS-1$
    	                if(Files.isDirectory(path)) {
    	                    appendLog(Messages.getString("ProcessProgress.LOG_FOLDER_REMOVING") + path.toString()); //$NON-NLS-1$
    	                    Helper.deleteFolderTree(path.toString());
    	                } else if(Files.isRegularFile(path)) {
        	                appendLog(Messages.getString("ProcessProgress.LOG_FILE_REMOVING") + path.toString()); //$NON-NLS-1$
    	                    Files.delete(path);
    	                }
	                } else System.err.println(Messages.getString("ProcessProgress.LOG_UNK_CLEANUP_OPERTION") + op); //$NON-NLS-1$
	            }
                return true;
        	} catch (Exception ex) {
            	appendLog(Messages.getString("ProcessProgress.LOG_CLEANUP_ERROR") + ex.getMessage()); //$NON-NLS-1$
        	}
        }
		return false;
	}

	/**
     * Copy/remove files
     * @param tasks The tasks to process
     */
	private boolean processFilesTasks(List<KeyValue> tasks) {
        if(tasks != null && tasks.size() > 0) {
        	SwingUtilities.invokeLater(() -> {
        		progressBar.setIndeterminate(false);
        		progressBar.setMaximum((int)MainWindow.getPreference(MainWindow.REQUIRED_SIZE));
        		progressBar.setValue(0);
        	});
        	try {
	            String baseDir = (String)MainWindow.getPreference(MainWindow.DESTINATION_FOLDER);
	            ZipArchiveHandler zip = new ZipArchiveHandler((String)MainWindow.getPreference(MainWindow.SOURCE_ARCHIVE));
	            zip.setProgressListener(new ProgressListener() {
	            	int bytesExtracted = 0;
					@Override
					public void onProgress(int blockSize) {
	                    SwingUtilities.invokeLater(() -> {
	                        bytesExtracted += blockSize;
	                        progressBar.setValue(bytesExtracted);
	                    });
					}
					@Override
					public void onFileCreated(String name) { }
	            });
	            for(KeyValue task : tasks) {
	            	String op = task.key();
	            	String[] paths = task.value().split(":>", 2); //$NON-NLS-1$
	            	Path path = Paths.get(baseDir, paths[1]);
	            	if("copy".equals(op)) { //$NON-NLS-1$
	            		appendLog(Messages.getString("ProcessProgress.LOG_FILE_UPDATING") + path.toString()); //$NON-NLS-1$
	                	boolean b = zip.extractFile(paths[0], path.toString());
	                	if(!b) {
	                		appendLog(Messages.getString("ProcessProgress.LOG_UPDATE_FAILURE")); //$NON-NLS-1$
	                		return false;
	                	}
	            	} else if("remove".equals(op)) { //$NON-NLS-1$
	                	if(Files.exists(path)) {
	                    	appendLog(Messages.getString("ProcessProgress.LOG_FILE_REMOVING") + path); //$NON-NLS-1$
	                    	Files.delete(path);
	                	}
	            	} else System.err.println(Messages.getString("ProcessProgress.LOG_UNK_FILE_OPERATION") + op); //$NON-NLS-1$
	            }
	            return true;
            } catch (Exception ex) {
                appendLog(Messages.getString("ProcessProgress.LOG_FILES_UPDATE_ERROR") + ex.getMessage()); //$NON-NLS-1$
            }
        }
    	return false;
	}

	/**
     * Copy/remove folders
     * @param tasks The tasks to process
     */
	private boolean processFoldersTasks(List<KeyValue> tasks) {
		if(tasks != null && tasks.size() > 0) {
			String baseDir = (String)MainWindow.getPreference(MainWindow.DESTINATION_FOLDER);
			try {
	    		for(KeyValue task : tasks) {
	        		String op = task.key();
	        		String path = task.value();
            		Path folder = Paths.get(baseDir, path);
	        		if("add".equals(op)) { //$NON-NLS-1$
	            		if(Files.notExists(folder)) {
	            			appendLog(Messages.getString("ProcessProgress.LOG_FOLDER_CREATING") + folder.toString()); //$NON-NLS-1$
	                		Files.createDirectories(folder);
	            		}
	        		} else if("remove".equals(op)) { //$NON-NLS-1$
                		if(Files.exists(folder)) {
                			appendLog(Messages.getString("ProcessProgress.LOG_FOLDER_REMOVING") + folder.toString()); //$NON-NLS-1$
                			Helper.deleteFolderTree(folder.toString());
                		}
	        		} else System.err.println(Messages.getString("ProcessProgress.LOG_UNK_FOLDER_OPERATION") + op); //$NON-NLS-1$
	    		}
	    		return true;
            } catch (Exception ex) {
                appendLog(Messages.getString("ProcessProgress.LOG_FOLDER_UPDATE_ERROR") + ex.getMessage()); //$NON-NLS-1$
            }
		}
		return false;
	}

	/**
     * Process XML tasks: Add/Remove/Update nodes in files.
     * @param tasks The tasks to process
     */
	private boolean processXmlTasks(String section, List<KeyValue> tasks) {
        if(tasks != null && tasks.size() > 0) {
            Path baseDir = Path.of((String)MainWindow.getPreference(MainWindow.DESTINATION_FOLDER));
            try {
            	List<String> files = FilesLister.listFiles(baseDir.toString() + File.separator + section.split(":", 2)[1]); //$NON-NLS-1$
        		XMLEditor xmlEditor = new XMLEditor();
            	for(String file : files) {
            		xmlEditor.openFile(file);
            		for(KeyValue task : tasks) {
            			String op = task.key();
            			if("remove".equals(op)) { //$NON-NLS-1$
            				xmlEditor.removeElement(task.value());
            			} else {
                			String[] parts = task.value().split("=", 2); //$NON-NLS-1$
	            			String xmlPath = parts[0].trim();
	            			parts = parts[1].split(">"); // Index 0 is the xml tag value, other parts is the xml attribute=value pairs //$NON-NLS-1$
	            			String xmlValue = parts[0].trim();
                			Map<String, String> xmlAttributes = new HashMap<>();
                			for(int i = 1; i < parts.length; i++) {
                				String[] attr = parts[i].split("=", 2); //$NON-NLS-1$
                				xmlAttributes.put(attr[0].trim(), attr[1].trim());
                			}
                			if("add".equals(op)) { //$NON-NLS-1$
                				xmlEditor.addElement(xmlPath, xmlValue, xmlAttributes);
                			} else if("update".equals(op)) { //$NON-NLS-1$
                				xmlEditor.editElement(xmlPath, xmlValue);
                				if(xmlAttributes.size() > 0)
                					xmlEditor.editAttributes(xmlPath, xmlAttributes);
                			} else System.err.println(Messages.getString("ProcessProgress.LOG_UNK_XML_OPERATION") + op); //$NON-NLS-1$
            			}
            		}
            		xmlEditor.save();
            	}
            	return true;
            } catch (Exception ex) {
                appendLog(Messages.getString("ProcessProgress.LOG_XML_UPDATE_ERROR") + ex.getMessage()); //$NON-NLS-1$
            }
        }
		return false;
	}

    /**
     * Process Pair tasks: Add/Remove/Update key/value pairs in files.
     * @param tasks The tasks to process
     */
	private boolean processPairTasks(String section, List<KeyValue> tasks) {
        if(tasks != null && tasks.size() > 0) {
            Path baseDir = Path.of((String)MainWindow.getPreference(MainWindow.DESTINATION_FOLDER));
            try {
            	String sectionFile = baseDir.toString() + File.separator + section.split(":", 2)[1]; //$NON-NLS-1$
            	List<String> files = FilesLister.listFiles(sectionFile);
            	if(files.isEmpty())
            		files.add(sectionFile);
            	for(String file : files) {
            		List<String> lines;
            		if(Files.exists(Paths.get(file))) {
            			lines = Files.readAllLines(Paths.get(file));
            		} else {
            			appendLog(Messages.getString("ProcessProgress.LOG_FILE_CREATING") + file); //$NON-NLS-1$
            			lines = new ArrayList<>(); // Let the file be created if it doesn't exist
            		}
            		for(KeyValue task : tasks) {
                		String op = task.key();
                		if("add".equals(op)) { //$NON-NLS-1$
                			lines.add(task.value());
                		} else if("remove".equals(op)) { //$NON-NLS-1$
                			Iterator<String> it = lines.iterator();
                			while(it.hasNext()) {
                    			String line = it.next();
                    			if(line.startsWith(task.value())) {
                        			String[] parts = line.split("=", 2); //$NON-NLS-1$
                        			if(parts[0].trim().equals(task.value()))
                        				it.remove();
                    			}
                			}
                		} else if("update".equals(op)) { //$NON-NLS-1$
                			for(int i = 0; i < lines.size(); i++) {
                    			String line = lines.get(i);
                    			String[] updateParts = task.value().split("=", 2); //$NON-NLS-1$
                    			String[] lineParts = line.split("=", 2); //$NON-NLS-1$
                    			if(lineParts[0].trim().equals(updateParts[0].trim())) {
                    				lines.set(i, updateParts[0].trim() + "=" + updateParts[1].trim()); //$NON-NLS-1$
                    			}
                			}
                		} else System.err.println(Messages.getString("ProcessProgress.LOG_UNK_PAIR_OPERAITON") + op); //$NON-NLS-1$
            		}
            		Files.write(Paths.get(file), lines);
            	}
            	return true;
            } catch (Exception ex) {
                appendLog(Messages.getString("ProcessProgress.LOG_UPDATE_PAIR_ERROR") + ex.getMessage()); //$NON-NLS-1$
            }
        }
		return false;
	}

	/**
     * Process SQLite tasks: Execute SQL statements in databases.
     * @param tasks The tasks to process
     */
	private boolean processSqliteTasks(String section, List<KeyValue> tasks) {
		if(tasks != null && tasks.size() > 0) {
    		String baseDir = (String)MainWindow.getPreference(MainWindow.DESTINATION_FOLDER);
    		try {
    			List<String> dbFiles = FilesLister.listFiles(Paths.get(baseDir, section.split(":", 2)[1]).toString()); //$NON-NLS-1$
    			for(String dbFile : dbFiles) {
            		try(Connection con = Helper.connectToSqliteDb(dbFile)) {
                		appendLog(Messages.getString("ProcessProgress.LOG_DB_UPDATING") + dbFile); //$NON-NLS-1$
	            		for(KeyValue task : tasks) {
	                		String op = task.key();
	                		String query = task.value();
	                   		if("sql".equals(op)) {
	                   			try(PreparedStatement stmt = con.prepareStatement(query)) {
	                   				stmt.executeUpdate();
	                   			}
	                   		}
                		}
            		}
    			}
    			return true;
    		} catch (Exception ex) {
    			appendLog(Messages.getString("ProcessProgress.LOG_SQLITE_UPDATE_ERROR") + ex.getMessage()); //$NON-NLS-1$
    		}
		}
        return false;
	}

	/**
	 * Append text to the log
	 * @param text The text to append
	 */
	private void appendLog(String text) {
		SwingUtilities.invokeLater(() -> {
			textArea.append(text + "\n"); //$NON-NLS-1$
			textArea.setCaretPosition(textArea.getDocument().getLength());
		});
	}

	/**
	 * Process the installation: Copy files, create DB, etc
	 */
	private void processInstallation() {
		final String installFolder = (String)MainWindow.getPreference(MainWindow.DESTINATION_FOLDER);
		progressBar.setValue(0);
		progressBar.setMaximum((int)MainWindow.getPreference(MainWindow.REQUIRED_SIZE));
		textArea.setText(""); //$NON-NLS-1$
		new Thread(() -> {
			try {
				if(Files.exists(Paths.get(installFolder))) {
					appendLog(Messages.getString("ProcessProgress.LOG_INSTALL_FOLDER_DELETING") + installFolder); //$NON-NLS-2$
					Helper.deleteFolderTree(installFolder);
				}
				if(Helper.createDirectoryPath(installFolder)) {
					appendLog(Messages.getString("ProcessProgress.LOG_INSTALL_FOLDER_CREATED") + installFolder); //$NON-NLS-2$
					ZipArchiveHandler zip = new ZipArchiveHandler((String)MainWindow.getPreference(MainWindow.SOURCE_ARCHIVE));
					zip.setProgressListener(new ProgressListener() {
						private int progress = 0;
						@Override
						public void onProgress(int size) {
							progress += size;
							SwingUtilities.invokeLater(() -> progressBar.setValue(progress));
						}

						@Override
						public void onFileCreated(String name) {
							appendLog(Messages.getString("ProcessProgress.LOG_FILE_COPYING") + name);//$NON-NLS-2$
						}
					});
					if(zip.extractTo(installFolder)) {
						createAppLinks(installFolder);
						SwingUtilities.invokeLater(() -> {
							textArea.append(Messages.getString("ProcessProgress.LOG_INSTALL_COMPLETED")); //$NON-NLS-1$
							textArea.setCaretPosition(textArea.getDocument().getLength());
							MainWindow.setDescription(Messages.getString("ProcessProgress.DESC_INSTALL_COMPLETED")); //$NON-NLS-1$
							MainWindow.getNextButton().setVisible(true);
						});
						MainWindow.setPreference(MainWindow.ALL_SUCCESS, true);
					} else {
						SwingUtilities.invokeLater(() -> {
							textArea.append(Messages.getString("ProcessProgress.LOG_INSTALL_FAILURE")); //$NON-NLS-1$
							textArea.setCaretPosition(textArea.getDocument().getLength());
							MainWindow.setDescription(Messages.getString("ProcessProgress.DESC_INSTALL_FAILURE")); //$NON-NLS-1$
							MainWindow.getNextButton().setVisible(true);
						});
						MainWindow.setPreference(MainWindow.ALL_SUCCESS, false);
					}
				}
			} catch (Exception ex) {
				MainWindow.setPreference(MainWindow.ALL_SUCCESS, false);
				SwingUtilities.invokeLater(() -> {
					textArea.append(Messages.getString("ProcessProgress.LOG_INSTALL_ERROR") + ex.getMessage() + "\n"); //$NON-NLS-1$ //$NON-NLS-2$
					textArea.setCaretPosition(textArea.getDocument().getLength());
					MainWindow.setDescription(Messages.getString("ProcessProgress.LOG_INSTALL_FAILED")); //$NON-NLS-1$
				});
			}
			while(!MainWindow.getNextButton().isVisible())
				try { Thread.sleep(100); } catch (Exception ignore) { }
			try {
				Files.writeString(Paths.get(installFolder, "install.log"), textArea.getText()); //$NON-NLS-1$
			} catch (Exception ignore) { }
		}).start();
	}

	/**
	 * Create the panel.
	 */
	public ProcessProgress() {
		lblProgress = new JLabel("? Progress: "); //$NON-NLS-1$

		progressBar = new JProgressBar();
		progressBar.setStringPainted(true);
		progressBar.setValue(20);

		JScrollPane scrollPane = new JScrollPane();
		GroupLayout groupLayout = new GroupLayout(this);
		groupLayout.setHorizontalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(Alignment.TRAILING, groupLayout.createSequentialGroup()
					.addGap(22)
					.addGroup(groupLayout.createParallelGroup(Alignment.TRAILING)
						.addComponent(scrollPane, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 430, Short.MAX_VALUE)
						.addComponent(progressBar, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 430, Short.MAX_VALUE)
						.addComponent(lblProgress, Alignment.LEADING))
					.addGap(22))
		);
		groupLayout.setVerticalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addGap(22)
					.addComponent(lblProgress)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(progressBar, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(scrollPane, GroupLayout.DEFAULT_SIZE, 238, Short.MAX_VALUE)
					.addGap(22))
		);

		textArea = new JTextArea();
		textArea.setFont(new Font("Tahoma", Font.PLAIN, 11)); //$NON-NLS-1$
		textArea.setOpaque(false);
		textArea.setEditable(false);
		scrollPane.setViewportView(textArea);
		setLayout(groupLayout);

		Helper.enableRtlWhenNeeded(ProcessProgress.this);
	}
}
