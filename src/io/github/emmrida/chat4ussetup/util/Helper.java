/*
 * Copyright (c) 2025 El Mhadder Mohamed Rida. All rights reserved.
 * This code is licensed under the [MIT License](https://opensource.org/licenses/MIT).
 */
package io.github.emmrida.chat4ussetup.util;

import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.Container;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Helper class for various utility methods
 */
public class Helper {

    // Simple KeyValue record to hold key-value pairs
    public record KeyValue(String key, String value) {}

    /**
     * Enable RTL when needed.
     * @param container Container of the components to enable RTL.
     */
    public static void enableRtlWhenNeeded(Container container) {
    	if(!isRTL(Locale.getDefault()))
    		return;
        container.applyComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        for (Component component : container.getComponents())
            if (component instanceof Container)
            	enableRtlWhenNeeded((Container) component);
        container.revalidate();
        container.repaint();
    }

    /**
     * Checks whether a locale is RTL or not.
     * @param locale Locale to check.
     * @return true if RTL.
     */
    public static boolean isRTL(Locale locale) {
    	String name = locale.getDisplayName();
    	if(name.length() == 0)
    		return false;
        char firstChar = name.charAt(0);
        byte directionality = Character.getDirectionality(firstChar);
        return directionality == Character.DIRECTIONALITY_RIGHT_TO_LEFT ||
               directionality == Character.DIRECTIONALITY_RIGHT_TO_LEFT_ARABIC;
    }

    /**
     * Connects to a SQLite database.
     * @param dbPath Path to the SQLite database
     * @return Connection to the database
     */
    public static Connection connectToSqliteDb(String dbPath) {
        try {
            Class.forName("org.sqlite.JDBC"); //$NON-NLS-1$
            return java.sql.DriverManager.getConnection("jdbc:sqlite:" + dbPath); //$NON-NLS-1$
        } catch (Exception ex) {
            System.err.println(Messages.getString("Helper.EX_SQLITE_CON_ERROR") + ex.getMessage()); //$NON-NLS-1$
        }
        return null;
    }

    /**
     * Returns the current date in the format: yyyy-MM-dd_HH-mm-ss
     * @return The current date in the specified format
     */
    public static String getCurrentDate() {
		return new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date()); //$NON-NLS-1$
    }

    /**
     * Reads a configuration file and parses it into a map structure.
     *
     * @param filePath Path to the configuration file
     * @return Map where key is the section name, value is list of KeyValue pairs in that section
     * @throws IOException If file cannot be read
     */
    public static Map<String, List<KeyValue>> readConfigFile(String filePath) {
        String currentSection = null;
        List<KeyValue> currentList = null;
        Map<String, List<KeyValue>> configMap = new LinkedHashMap<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            int lineNumber = 0;

            while ((line = reader.readLine()) != null) {
                lineNumber++;
                String trimmedLine = line.trim();

                // Skip empty lines and comments
                if (trimmedLine.isEmpty() || trimmedLine.startsWith("#")) //$NON-NLS-1$
                    continue;

                // Check for section header: [section-name]
                if (trimmedLine.startsWith("[") && trimmedLine.endsWith("]")) { //$NON-NLS-1$ //$NON-NLS-2$
                    currentSection = trimmedLine.substring(1, trimmedLine.length() - 1).trim();
                    currentList = new ArrayList<>();
                    configMap.put(currentSection, currentList);
                    continue;
                }

                // Parse key:value pairs
                if (currentSection != null) {
                    int colonIndex = trimmedLine.indexOf(':');
                    if (colonIndex > 0) {
                        String key = trimmedLine.substring(0, colonIndex).trim();
                        String value = trimmedLine.substring(colonIndex + 1).trim();
                        currentList.add(new KeyValue(key, value));
                    } else currentList.add(new KeyValue(trimmedLine, "")); // Handle lines without colon (treat entire line as key with empty value) //$NON-NLS-1$
                } else System.err.println("Warning: Line " + lineNumber + " outside of any section: " + trimmedLine);
            }
        } catch (IOException ex) {
			System.err.println(Messages.getString("Helper.EX_CFG_FILE_READ_ERROR") + ex.getMessage()); //$NON-NLS-1$
		}

        return configMap;
    }

	/**
	 *
	 * @param path
	 * @return
	 */
	public static String moveUpToExistingParentDir(String path) {
		String[] parts = path.split(File.separator.replace("\\", "\\\\")); // Split the path into directory parts //$NON-NLS-1$ //$NON-NLS-2$
		for (int i = parts.length - 1; i >= 0; i--) {
		    String currentDir = Arrays.stream(parts, 0, i + 1).collect(Collectors.joining(File.separator));
		    if (new File(currentDir).exists()) {
		        return currentDir;
		    }
		}
		return null;
	}

	/**
	 * Create a directory path
	 * @param dirPath directory path to create
	 * @return true if successful
	 */
	public static boolean createDirectoryPath(String dirPath) {
	    try {
	        Path path = Paths.get(dirPath);
	        Files.createDirectories(path);
	        return true;
	    } catch (Exception e) {
	        System.err.println(Messages.getString("Helper.EX_DIR_CREATION_FAILURE") + e.getMessage()); //$NON-NLS-1$
	    }
	    return false;
	}

	/**
	 *
	 * @param absolutePath
	 * @return
	 */
	public static long getDriveFreeSpace(String absolutePath) {
		try {
			return new File(absolutePath).toPath().getRoot().toFile().getUsableSpace();
		} catch (Exception ex) {
			System.err.println(Messages.getString("Helper.EX_GET_DRV_FREE_SPACE_FAILURE") + ex.getMessage()); //$NON-NLS-1$
		}
		return 0;
	}

	/**
	 *
	 * @param bytes
	 * @return
	 */
	public static String formatBytes(long bytes) {
	    if (bytes == 0) return "0 B"; //$NON-NLS-1$
	    String[] units = {"B", "KB", "MB", "GB", "TB", "PB", "EB"}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$
	    int unitIndex = (int) (Math.log(Math.abs(bytes)) / Math.log(1024));
	    return String.format("%.1f %s", bytes / Math.pow(1024, unitIndex), units[unitIndex]); //$NON-NLS-1$
	}

	/**
	 *
	 * @param path
	 * @return
	 */
	public static boolean isValidPath(String path) {
	    try {
	        java.nio.file.Paths.get(path);
	        return true;
	    } catch (Exception ignore) {
	        return false;
	    }
	}

    /**
     * Deletes folder using Java NIO (more efficient)
     * @param folderPath Path to the folder to delete
     * @return true if successful, false otherwise
     */
    public static boolean deleteFolderTree(String folderPath) {
        Path path = Paths.get(folderPath);

        if (!Files.exists(path)) {
            System.out.println(Messages.getString("Helper.LOG_FOLDER_NEXIST") + folderPath); //$NON-NLS-1$
            return true;
        }

        if (!Files.isDirectory(path)) {
            System.out.println(Messages.getString("Helper.LOG_ISNOT_DIRECTORY") + folderPath); //$NON-NLS-1$
            return false;
        }

        try {
            // Walk the file tree and delete all files and directories
            Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    if (exc != null) {
                        throw exc;
                    }
                    Files.delete(dir);
                    return FileVisitResult.CONTINUE;
                }
            });
            return true;
        } catch (IOException e) {
            System.err.println(Messages.getString("Helper.EX_DEL_FOLDER_ERROR") + e.getMessage()); //$NON-NLS-1$
            return false;
        }
    }
}
