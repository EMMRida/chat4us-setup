/*
 * Copyright (c) 2025 El Mhadder Mohamed Rida. All rights reserved.
 * This code is licensed under the [MIT License](https://opensource.org/licenses/MIT).
 */
package io.github.emmrida.chat4ussetup.util;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;

public class ContentOpener {

    /**
     * Opens OR executes a file/url appropriately based on file type
     * @param uri Path/URL to open/execute
     * @return true if successful
     */
    public static boolean openOrExecute(String uri) {
    	if(uri.startsWith("http")) //$NON-NLS-1$
    		return openUrl(uri);

        File file = new File(uri);
        if (!file.exists()) {
            System.err.println(Messages.getString("ContentOpener.LOG_FILE_NEXIST") + uri); //$NON-NLS-1$
            return false;
        }

        // For executable files, use execution-specific approach
        if (isExecutableFile(file)) {
            return executeProgram(file);
        } else {
            // For non-executable files, open with default editor/viewer
            return openWithDefaultApplication(file);
        }
    }

    /**
     * Check if file is an executable type
     */
    private static boolean isExecutableFile(File file) {
        String fileName = file.getName().toLowerCase();
        return fileName.endsWith(".exe") || //$NON-NLS-1$
               fileName.endsWith(".jar") || //$NON-NLS-1$
               fileName.endsWith(".bat") || //$NON-NLS-1$
               fileName.endsWith(".cmd") || //$NON-NLS-1$
               fileName.endsWith(".sh") || //$NON-NLS-1$
               fileName.endsWith(".app"); // macOS application //$NON-NLS-1$
    }

    /**
     * Execute a program file with working directory set to its parent directory
     */
    private static boolean executeProgram(File file) {
        File workingDir = file.getParentFile();

        try {
            String os = System.getProperty("os.name").toLowerCase(); //$NON-NLS-1$
            Process process;
            ProcessBuilder processBuilder;

            if (file.getName().toLowerCase().endsWith(".jar")) { //$NON-NLS-1$
                // Execute JAR file with working directory
                processBuilder = new ProcessBuilder("java", "-jar", file.getAbsolutePath()); //$NON-NLS-1$ //$NON-NLS-2$
                processBuilder.directory(workingDir);
                process = processBuilder.start();

            } else if (os.contains("win")) { //$NON-NLS-1$
                // Windows - execute with working directory
                if (file.getName().toLowerCase().endsWith(".exe") || //$NON-NLS-1$
                    file.getName().toLowerCase().endsWith(".bat") || //$NON-NLS-1$
                    file.getName().toLowerCase().endsWith(".cmd")) { //$NON-NLS-1$

                    processBuilder = new ProcessBuilder("cmd", "/c", "start", "/D", workingDir.getAbsolutePath(), "\"\"", file.getAbsolutePath()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
                    processBuilder.directory(workingDir);
                    process = processBuilder.start();
                } else {
                    // Fallback for other file types on Windows
                    processBuilder = new ProcessBuilder("cmd", "/c", "start", "\"\"", file.getAbsolutePath()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
                    processBuilder.directory(workingDir);
                    process = processBuilder.start();
                }

            } else if (os.contains("mac")) { //$NON-NLS-1$
                // macOS - execute with working directory
                if (file.getName().toLowerCase().endsWith(".app")) { //$NON-NLS-1$
                    processBuilder = new ProcessBuilder("open", file.getAbsolutePath()); //$NON-NLS-1$
                    processBuilder.directory(workingDir);
                    process = processBuilder.start();
                } else {
                    // For scripts and other executables on macOS
                    processBuilder = new ProcessBuilder("open", "-a", "Terminal", file.getAbsolutePath()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                    processBuilder.directory(workingDir);
                    process = processBuilder.start();
                }

            } else {
                // Linux/Unix - make executable and run with working directory
                Runtime.getRuntime().exec(new String[]{"chmod", "+x", file.getAbsolutePath()}); //$NON-NLS-1$ //$NON-NLS-2$
                processBuilder = new ProcessBuilder(file.getAbsolutePath());
                processBuilder.directory(workingDir);
                process = processBuilder.start();
            }

            // Wait briefly to check if process started
            Thread.sleep(1000);
            return process.isAlive() || process.exitValue() == 0;

        } catch (IOException | InterruptedException e) {
            System.err.println(Messages.getString("ContentOpener.LOG_RUN_PROGRAM_ERROR") + e.getMessage()); //$NON-NLS-1$
            return false;
        }
    }

    /**
     * Alternative method using ProcessBuilder for more control over working directory
     */
    private static boolean executeProgramWithProcessBuilder(File file) {
        File workingDir = file.getParentFile();
        String os = System.getProperty("os.name").toLowerCase(); //$NON-NLS-1$

        try {
            ProcessBuilder processBuilder;

            if (file.getName().toLowerCase().endsWith(".jar")) { //$NON-NLS-1$
                // JAR file execution
                processBuilder = new ProcessBuilder("java", "-jar", file.getName()); //$NON-NLS-1$ //$NON-NLS-2$

            } else if (os.contains("win")) { //$NON-NLS-1$
                // Windows execution
                if (file.getName().toLowerCase().endsWith(".exe")) { //$NON-NLS-1$
                    processBuilder = new ProcessBuilder("cmd", "/c", "start", file.getName()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                } else if (file.getName().toLowerCase().endsWith(".bat") || file.getName().toLowerCase().endsWith(".cmd")) { //$NON-NLS-1$ //$NON-NLS-2$
                    processBuilder = new ProcessBuilder("cmd", "/c", file.getName()); //$NON-NLS-1$ //$NON-NLS-2$
                } else {
                    processBuilder = new ProcessBuilder(file.getName());
                }

            } else if (os.contains("mac")) { //$NON-NLS-1$
                // macOS execution
                if (file.getName().toLowerCase().endsWith(".app")) { //$NON-NLS-1$
                    processBuilder = new ProcessBuilder("open", file.getName()); //$NON-NLS-1$
                } else {
                    // Make script executable and run
                    Runtime.getRuntime().exec(new String[]{"chmod", "+x", file.getAbsolutePath()}); //$NON-NLS-1$ //$NON-NLS-2$
                    processBuilder = new ProcessBuilder("./" + file.getName()); //$NON-NLS-1$
                }

            } else {
                // Linux/Unix execution
                Runtime.getRuntime().exec(new String[]{"chmod", "+x", file.getAbsolutePath()}); //$NON-NLS-1$ //$NON-NLS-2$
                processBuilder = new ProcessBuilder("./" + file.getName()); //$NON-NLS-1$
            }

            // Set the working directory
            processBuilder.directory(workingDir);

            // Optional: redirect error stream
            processBuilder.redirectErrorStream(true);

            Process process = processBuilder.start();

            // Wait briefly to check if process started
            Thread.sleep(1000);
            return process.isAlive() || process.exitValue() == 0;

        } catch (IOException | InterruptedException e) {
            System.err.println(Messages.getString("ContentOpener.LOG_RUN_PROGRAM_ERROR") + e.getMessage()); //$NON-NLS-1$
            return false;
        }
    }

    /**
     * Open non-executable files with default application
     */
    private static boolean openWithDefaultApplication(File file) {
        if (Desktop.isDesktopSupported()) {
            Desktop desktop = Desktop.getDesktop();
            if (desktop.isSupported(Desktop.Action.OPEN)) {
                try {
                    desktop.open(file);
                    return true;
                } catch (IOException e) {
                    System.err.println(Messages.getString("ContentOpener.LOG_OPEN_FILE_ERROR") + e.getMessage()); //$NON-NLS-1$
                }
            }
        }
        return false;
    }

    /**
     * Enhanced version that gives you choice of execution method
     */
    public static boolean openOrExecuteFileEnhanced(String filePath, boolean useProcessBuilder) {
        File file = new File(filePath);

        if (!file.exists()) {
            System.err.println(Messages.getString("ContentOpener.LOG_FILE_NEXIST") + filePath); //$NON-NLS-1$
            return false;
        }

        if (isExecutableFile(file)) {
            if (useProcessBuilder) {
                return executeProgramWithProcessBuilder(file);
            } else {
                return executeProgram(file);
            }
        } else {
            return openWithDefaultApplication(file);
        }
    }

    /**
	 * Opens a URL in the default browser
	 */
    private static boolean openUrl(String url) {
		if (Desktop.isDesktopSupported()) {
			Desktop desktop = Desktop.getDesktop();
			if (desktop.isSupported(Desktop.Action.BROWSE)) {
				try {
					desktop.browse(new java.net.URI(url));
					return true;
				} catch (Exception ex) {
					System.err.println(Messages.getString("ContentOpener.LOG_OPEN_URL_ERROR") + ex.getMessage()); //$NON-NLS-1$
				}
			}
		}
		return false;
	}
}