/*
 * Copyright (c) 2025 El Mhadder Mohamed Rida. All rights reserved.
 * This code is licensed under the [MIT License](https://opensource.org/licenses/MIT).
 */
package io.github.emmrida.chat4ussetup.gui;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import javax.swing.JButton;
import javax.swing.JComponent;

import java.awt.FlowLayout;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.ImageIcon;

import java.awt.Dimension;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

import java.awt.Font;
import java.awt.Point;
import java.awt.Toolkit;

import javax.swing.LayoutStyle.ComponentPlacement;
import java.awt.Color;
import java.awt.Component;
import java.awt.CardLayout;
import javax.swing.border.MatteBorder;

import io.github.emmrida.chat4ussetup.util.Helper;
import io.github.emmrida.chat4ussetup.util.Messages;
import io.github.emmrida.chat4ussetup.util.Helper.KeyValue;

import javax.swing.UIManager;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * MainWindow class is the main window of the setup application.
 */
public class MainWindow {

	public static enum OpType {INTRO, INSTALL, UPDATE, RECOVER, UNINSTALL; }

	public static final String APP_NAME						= "app_name";		 // App name not jar name //$NON-NLS-1$
	public static final String APP_VERSION					= "app_version"; //$NON-NLS-1$
	public static final String LICENCE_FILE					= "licence_file"; //$NON-NLS-1$
	public static final String APP_ICON						= "app_icon"; //$NON-NLS-1$
	public static final String LINK_ICONS					= "link_icons"; // Icons comma separated list for desktop/system menu links icons //$NON-NLS-1$
	public static final String LINK_DESCRIPTION				= "link_description"; // Desktop/system menu links description //$NON-NLS-1$
	public static final String SOURCE_ARCHIVE				= "source_archive";	 // Archive source name //$NON-NLS-1$
    public static final String DESTINATION_FOLDER			= "destination_folder"; // Desination folder absolute path //$NON-NLS-1$
	public static final String DST_FOLDER_NAME				= "dst_folder_name"; // Desination folder name not path //$NON-NLS-1$
	public static final String FINAL_TASKS					= "final_tasks"; //$NON-NLS-1$
	public static final String APP_RUNNABLE					= "app_runnable";	 // Jar name //$NON-NLS-1$
	public static final String INSTALLED_APP_VERSION		= "installed_app_version"; //$NON-NLS-1$
	public static final String MINIMUM_VERSION_UPDATE		= "minimum_version_update"; // Minimum installed jar version to update //$NON-NLS-1$
	public static final String AVAILABLE_SPACE				= "available_space"; //$NON-NLS-1$
	public static final String REQUIRED_SIZE				= "required_size"; //$NON-NLS-1$
	public static final String CREATE_DESKTOP_ICON			= "create_desktop_icon"; //$NON-NLS-1$
	public static final String CREATE_START_MENU_ICON		= "create_start_menuIcon"; //$NON-NLS-1$
	public static final String CREATE_BACKUP_BEFORE_UPDATE	= "create_backup_before_update"; //$NON-NLS-1$
	public static final String ALL_SUCCESS					= "all_success"; // Installation, Update or Recovery success //$NON-NLS-1$


	private static MainWindow mainWindow;

	private int curStep;
	private OpType opType;
	private List<WizardStep> wzSteps;
	private Map<String, Object> wzPrefs;
	private Map<String, List<KeyValue>> wzUpdate;

	private JFrame frmSetup;
	private JButton btnBack;
	private JButton btnNext;
	private JButton btnInstall;
	private JButton btnCancel;
	private JLabel lblStepTitle;
	private JLabel lblStepDescription;
	private JPanel panelStep;
	private JLabel lblIcon;

	public static MainWindow getInstance() { return mainWindow; }
	public static JFrame getMainFrame() { return mainWindow.frmSetup; }
	public static void setTitle(String title) { mainWindow.lblStepTitle.setText(title); }
	public static void setDescription(String description) { mainWindow.lblStepDescription.setText(description); }

	public static int getCurrentStep() { return mainWindow.curStep; }
	public static OpType getOpType() { return mainWindow.opType; }

	public static JButton getBackButton() { return mainWindow.btnBack; }
	public static JButton getNextButton() { return mainWindow.btnNext; }
	public static JButton getInstallButton() { return mainWindow.btnInstall; }
	public static JButton getCancelButton() { return mainWindow.btnCancel; }

	public static void setPreference(String key, Object value) { mainWindow.wzPrefs.put(key, value); }

	/**
	 * Get the value of a preference by key.
	 * The preferences are loaded from the install.cfg file
	 * @param key The key of the preference
	 * @return The value of the preference, null if not found
	 */
	public static Object getPreference(String key) {
		return mainWindow.wzPrefs.get(key);
	}

	/**
     * Get the update entry set for the update wizard.
     * Each Entry is a list of KeyValue strings of an update section. See update.cfg file for details.
     * @return The update entry set.
     */
	public static Set<Entry<String, List<KeyValue>>> getUpdateEntrySet() {
		return mainWindow.wzUpdate.entrySet();
	}

	/**
	 * Change the application language
	 * @param lang Language code
	 */
	public static void onLanguageChanged(String lang) {
		Point p = mainWindow.frmSetup.getLocation();
		mainWindow.frmSetup.dispose();
		Locale.setDefault(Locale.forLanguageTag(lang));
		JComponent.setDefaultLocale(Locale.getDefault());
		mainWindow = new MainWindow();
        mainWindow.frmSetup.setLocation(p);
        mainWindow.frmSetup.setVisible(true);
	}

	/**
	 * Start the installation.
	 */
	public void startInstallation() {
		wzUpdate.entrySet();
		if(opType == OpType.INSTALL)
			return;
		int i = 0;
		Iterator<WizardStep> it = wzSteps.iterator();
		while(it.hasNext()) { // Remove all steps except the welcome and operation type selector
			if(i > 1) {
				it.remove();
				i++;
			}
			it.next();
		}
		int n = wzSteps.size();
		wzSteps.add(new LicenceAgreement());
		wzSteps.add(new FolderSelection());
		wzSteps.add(new TasksSelection());
		wzSteps.add(new ProcessProgress());
		wzSteps.add(new ProcessComplete());
		for(i = n; i < wzSteps.size(); i++)
			panelStep.add((Component)wzSteps.get(i), "step" + i); //$NON-NLS-1$
		opType = OpType.INSTALL;
	}

	/**
     * Start the update.
     */
	public void startUpdate() {
		if(opType == OpType.UPDATE)
			return;
		int i = 0;
		Iterator<WizardStep> it = wzSteps.iterator();
		while(it.hasNext()) { // Remove all steps except the welcome and operation type selector
			if(i > 1) {
				it.remove();
				i++;
			}
			it.next();
		}
		int n = wzSteps.size();
		wzSteps.add(new LicenceAgreement()); // In case when licence has changed
		wzSteps.add(new FolderSelection());
		wzSteps.add(new TasksSelection());
		wzSteps.add(new ProcessProgress());
		wzSteps.add(new ProcessComplete());
		for(i = n; i < wzSteps.size(); i++)
			panelStep.add((Component)wzSteps.get(i), "step" + i); //$NON-NLS-1$
		opType = OpType.UPDATE;
	}

	/**
     * Start the recovery.
     */
	public void startRecovery() {
		if(opType == OpType.RECOVER)
			return;
		int i = 0;
		Iterator<WizardStep> it = wzSteps.iterator();
		while(it.hasNext()) { // Remove all steps except the welcome and operation type selector
			if(i > 1) {
				it.remove();
				i++;
			}
			it.next();
		}
		int n = wzSteps.size();
		wzSteps.add(new FolderSelection());
		wzSteps.add(new TasksSelection());
		wzSteps.add(new ProcessProgress());
		wzSteps.add(new ProcessComplete());
		for(i = n; i < wzSteps.size(); i++)
			panelStep.add((Component)wzSteps.get(i), "step" + i); //$NON-NLS-1$
		opType = OpType.RECOVER;
	}

	/**
     * Start the uninstall.
     */
	public void startUninstall() {
		if(opType == OpType.UNINSTALL)
			return;
		int i = 0;
		Iterator<WizardStep> it = wzSteps.iterator();
		while(it.hasNext()) { // Remove all steps except the welcome and operation type selector
			if(i > 1) {
				it.remove();
				i++;
			}
			it.next();
		}
		int n = wzSteps.size();
		wzSteps.add(new FolderSelection());
		wzSteps.add(new ProcessProgress());
		wzSteps.add(new ProcessComplete());
		for(i = n; i < wzSteps.size(); i++)
			panelStep.add((Component)wzSteps.get(i), "step" + i); //$NON-NLS-1$
		opType = OpType.UNINSTALL;
	}

	/**
	 *  Load install.cfg
	 * @return true on success
	 */
	private boolean loadInstallConfig() {
		try(BufferedReader reader = new BufferedReader(new FileReader(new File("./setup/setup.cfg")))) { //$NON-NLS-1$
			String line;
			String[] parts;
			while((line = reader.readLine()) != null) {
				if(line.isBlank() || line.startsWith("#"))
					continue;
				parts = line.split("=", 2); //$NON-NLS-1$
				setPreference(parts[0].trim(), parts[1].trim());
			}
			return true;
		} catch (Exception ex) {
			JOptionPane.showMessageDialog(frmSetup, Messages.getString("MainWindow.LOAD_INSTALL_CFG_FAILED") + ex.getMessage(), Messages.getString("MainWindow.MB_TITLE_ERROR"), JOptionPane.ERROR_MESSAGE); //$NON-NLS-1$ //$NON-NLS-2$
		}
		return false;
	}

	/**
	 * Create the application.
	 */
	public MainWindow() {
		mainWindow = this;
		initialize();

		wzPrefs = new HashMap<>();
		wzUpdate = Helper.readConfigFile("./setup/update.cfg"); //$NON-NLS-1$
		if(wzUpdate != null) {
			List<KeyValue> versionSec = wzUpdate.get("version"); //$NON-NLS-1$
			for(KeyValue pair : versionSec) {
				if("minimum".equals(pair.key())) { //$NON-NLS-1$
					MainWindow.setPreference(MINIMUM_VERSION_UPDATE, pair.value());
					break;
				}
			}
		}
		if(!loadInstallConfig() || (wzUpdate == null)) {
			JOptionPane.showMessageDialog(null, Messages.getString("MainWindow.ERROR_LOADING_CONFIGS"), Messages.getString("MainWindow.MB_TITLE_ERROR"), JOptionPane.ERROR_MESSAGE); //$NON-NLS-1$ //$NON-NLS-2$
			frmSetup.dispose();
			return;
		}
		wzSteps = new ArrayList<>();
		wzSteps.add(new WelcomeStep());
		wzSteps.add(new OperationSelection());

		for(int i = 0; i < wzSteps.size(); i++)
			panelStep.add((Component)wzSteps.get(i), "step" + i); //$NON-NLS-1$

		curStep = 0;
		opType = OpType.INTRO;
		CardLayout cl = (CardLayout) panelStep.getLayout();
		cl.show(panelStep, "step" + curStep); //$NON-NLS-1$
		btnBack.setVisible(false);
		btnInstall.setVisible(false);
		WizardStep step = wzSteps.get(curStep);
		lblStepTitle.setText(step.getTitle());
		lblStepDescription.setText(step.getDescription());
		step.onStepActivated(false);

		frmSetup.setTitle(Messages.getString("MainWindow.TITLE_PREFIX") + (String)wzPrefs.get(APP_NAME) + " v" + (String)wzPrefs.get(APP_VERSION)); //$NON-NLS-1$ //$NON-NLS-2$
		lblIcon.setIcon(new ImageIcon(((String)wzPrefs.get(APP_ICON)).split(";")[0])); //$NON-NLS-1$
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frmSetup = new JFrame();
		frmSetup.setIconImage(Toolkit.getDefaultToolkit().getImage("./setup/setup.png")); //$NON-NLS-1$
		frmSetup.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				boolean finish = "finish".equals(btnCancel.getClientProperty("role")); //$NON-NLS-1$ //$NON-NLS-2$
				if(finish) {
					WizardStep step = wzSteps.get(curStep);
					step.onStepDeactivated(true);
					frmSetup.dispose();
				} else {
					int ret = JOptionPane.showConfirmDialog(frmSetup, Messages.getString("MainWindow.SETUP_CANCEL_CONFIRM_MSG"), Messages.getString("MainWindow.MB_TITLE_CONFIRM"), JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE); //$NON-NLS-1$ //$NON-NLS-2$
					if(ret == JOptionPane.YES_OPTION) {
						frmSetup.dispose();
						return;
					}
				}
				Toolkit.getDefaultToolkit().beep();
			}
		});
		frmSetup.setResizable(false);
		frmSetup.setSize(512, 384);
		frmSetup.setLocationRelativeTo(null);
		frmSetup.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

		JPanel panelNav = new JPanel();
		FlowLayout flowLayout = (FlowLayout) panelNav.getLayout();
		flowLayout.setHgap(1);
		flowLayout.setVgap(10);
		flowLayout.setAlignment(FlowLayout.TRAILING);
		frmSetup.getContentPane().add(panelNav, BorderLayout.SOUTH);

		btnBack = new JButton(Messages.getString("MainWindow.BUTTON_BACK")); //$NON-NLS-1$
		btnBack.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(curStep > 0) {
					WizardStep step = wzSteps.get(curStep);
					step.onStepDeactivated(false);
					CardLayout cl = (CardLayout) panelStep.getLayout();
					cl.show(panelStep, "step" + (--curStep)); //$NON-NLS-1$
					btnBack.setVisible(curStep > 0);
					btnNext.setVisible(curStep < wzSteps.size() - 1);
					btnInstall.setVisible(curStep == wzSteps.size() - 1);
					step = wzSteps.get(curStep);
					lblStepTitle.setText(step.getTitle());
					lblStepDescription.setText(step.getDescription());
					step.onStepActivated(false);
				}
			}
		});
		panelNav.add(btnBack);

		btnNext = new JButton(Messages.getString("MainWindow.BUTTON_NEXT")); //$NON-NLS-1$
		btnNext.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(curStep < wzSteps.size()) {
					WizardStep step = wzSteps.get(curStep);
					if(step.validateStep()) {
						step.onStepDeactivated(true);
						CardLayout cl = (CardLayout) panelStep.getLayout();
						cl.show(panelStep, "step" + (++curStep)); //$NON-NLS-1$
						btnBack.setVisible(curStep > 0);
						btnNext.setVisible(curStep < wzSteps.size() - 1);
						btnInstall.setVisible(curStep == wzSteps.size() - 1);
						step = wzSteps.get(curStep);
						lblStepTitle.setText(step.getTitle());
						lblStepDescription.setText(step.getDescription());
						step.onStepActivated(true);
					}
				}
			}
		});
		panelNav.add(btnNext);

		btnInstall = new JButton(Messages.getString("MainWindow.BUTTON_INSTALL")); //$NON-NLS-1$
		btnInstall.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				btnNext.doClick();
			}
		});
		panelNav.add(btnInstall);

		JSeparator separator_1 = new JSeparator();
		separator_1.setPreferredSize(new Dimension(5, 2));
		separator_1.setOrientation(SwingConstants.VERTICAL);
		panelNav.add(separator_1);

		btnCancel = new JButton(Messages.getString("MainWindow.BUTTON_CANCEL")); //$NON-NLS-1$
		btnCancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				boolean finish = "finish".equals(btnCancel.getClientProperty("role")); //$NON-NLS-1$ //$NON-NLS-2$
				if(finish) {
					WizardStep step = wzSteps.get(curStep);
					step.onStepDeactivated(true);
					frmSetup.dispose();
				} else { // Cancel
					int ret = JOptionPane.showConfirmDialog(frmSetup, Messages.getString("MainWindow.SETUP_CANCEL_CONFIRM_MSG"), Messages.getString("MainWindow.MB_TITLE_CONFIRM"), JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE); //$NON-NLS-1$ //$NON-NLS-2$
					if(ret == JOptionPane.YES_OPTION)
						frmSetup.dispose();
				}
			}
		});
		panelNav.add(btnCancel);

		JSeparator separator = new JSeparator();
		separator.setPreferredSize(new Dimension(10, 2));
		separator.setOrientation(SwingConstants.VERTICAL);
		panelNav.add(separator);

		JPanel panelHeader = new JPanel();
		panelHeader.setBackground(Color.WHITE);
		panelHeader.setPreferredSize(new Dimension(10, 64));
		frmSetup.getContentPane().add(panelHeader, BorderLayout.NORTH);

		lblStepTitle = new JLabel("Step Title"); //$NON-NLS-1$
		lblStepTitle.setFont(new Font("Tahoma", Font.BOLD, 12)); //$NON-NLS-1$

		lblStepDescription = new JLabel("Step description..."); //$NON-NLS-1$

		lblIcon = new JLabel(""); //$NON-NLS-1$
		GroupLayout gl_panelHeader = new GroupLayout(panelHeader);
		gl_panelHeader.setHorizontalGroup(
			gl_panelHeader.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panelHeader.createSequentialGroup()
					.addGap(22)
					.addGroup(gl_panelHeader.createParallelGroup(Alignment.LEADING)
						.addComponent(lblStepTitle)
						.addGroup(gl_panelHeader.createSequentialGroup()
							.addGap(22)
							.addComponent(lblStepDescription)))
					.addPreferredGap(ComponentPlacement.RELATED, 470, Short.MAX_VALUE)
					.addComponent(lblIcon, GroupLayout.PREFERRED_SIZE, 45, GroupLayout.PREFERRED_SIZE)
					.addGap(22))
		);
		gl_panelHeader.setVerticalGroup(
			gl_panelHeader.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panelHeader.createSequentialGroup()
					.addContainerGap()
					.addGroup(gl_panelHeader.createParallelGroup(Alignment.BASELINE)
						.addGroup(gl_panelHeader.createSequentialGroup()
							.addComponent(lblStepTitle)
							.addGap(10)
							.addComponent(lblStepDescription))
						.addComponent(lblIcon, GroupLayout.PREFERRED_SIZE, 41, GroupLayout.PREFERRED_SIZE))
					.addContainerGap(12, Short.MAX_VALUE))
		);
		panelHeader.setLayout(gl_panelHeader);

		panelStep = new JPanel();
		panelStep.setBorder(new MatteBorder(1, 0, 1, 0, (Color) UIManager.getColor("Button.shadow"))); //$NON-NLS-1$
		frmSetup.getContentPane().add(panelStep, BorderLayout.CENTER);
		panelStep.setLayout(new CardLayout(0, 0));

		Helper.enableRtlWhenNeeded(frmSetup);
	}

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
					mainWindow = new MainWindow();
                    mainWindow.frmSetup.setVisible(true);
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		});
	}
}
