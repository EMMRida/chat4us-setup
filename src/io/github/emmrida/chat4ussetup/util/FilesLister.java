/*
 * Copyright (c) 2025 El Mhadder Mohamed Rida. All rights reserved.
 * This code is licensed under the [MIT License](https://opensource.org/licenses/MIT).
 */
package io.github.emmrida.chat4ussetup.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Utility class for listing files based on provided path strings.
 */
public class FilesLister {

    /**
     * Returns a list of files based on the provided path string.
     * Supports simple filenames, relative/absolute paths, and wildcard patterns.
     *
     * @param pathString the path string (can contain wildcards)
     * @return list of matching files
     */
    public static List<String> listFiles(String pathString) {
        List<String> result = new ArrayList<>();

        try {
            if (pathString.contains("*")) { //$NON-NLS-1$
                // Handle wildcard patterns
                result = handleWildcardSearch(pathString);
            } else {
                // Handle specific file/directory paths
                result = handleSpecificPath(pathString);
            }
        } catch (Exception e) {
            System.err.println(Messages.getString("FilesLister.EX_PATH_ERROR") + pathString + " - " + e.getMessage()); //$NON-NLS-1$ //$NON-NLS-2$
        }

        return result;
    }

    /**
     * Handles wildcard patterns in file paths
     *
     * @param pathString the path string (can contain wildcards)
     * @return list of matching files
     */
    private static List<String> handleWildcardSearch(String pathString) throws IOException {
        List<String> result = new ArrayList<>();

        // Convert to Path object
        Path basePath;
        String pattern;

        if (pathString.startsWith("/") || pathString.startsWith("\\")) { //$NON-NLS-1$ //$NON-NLS-2$
            // Absolute path with wildcard
            int lastSeparator = findLastSeparatorBeforeWildcard(pathString);
            String directoryPath = pathString.substring(0, lastSeparator);
            pattern = pathString.substring(lastSeparator + 1);
            basePath = Paths.get(directoryPath.isEmpty() ? "/" : directoryPath); //$NON-NLS-1$
        } else {
            // Relative path with wildcard
            int lastSeparator = findLastSeparatorBeforeWildcard(pathString);
            if (lastSeparator == -1) {
                // No directory specified, use current directory
                basePath = Paths.get("."); //$NON-NLS-1$
                pattern = pathString;
            } else {
                String directoryPath = pathString.substring(0, lastSeparator);
                pattern = pathString.substring(lastSeparator + 1);
                basePath = Paths.get(directoryPath);
            }
        }

        // Convert wildcard pattern to regex
        String regexPattern = convertWildcardToRegex(pattern);

        // Search for files matching the pattern
        try (Stream<Path> paths = Files.walk(basePath, 1)) {
            result = paths
                .filter(Files::isRegularFile)
                .filter(path -> path.getFileName().toString().matches(regexPattern))
                .map(Path::toString)
                .collect(Collectors.toList());
        }

        return result;
    }

    /**
     * Handles specific file or directory paths without wildcards
     *
     * @param pathString the path string (can contain wildcards)
     * @return list of matching files
     */
    private static List<String> handleSpecificPath(String pathString) throws IOException {
        List<String> result = new ArrayList<>();
        Path path = Paths.get(pathString);

        if (Files.exists(path)) {
            if (Files.isRegularFile(path)) {
                // Single file
                result.add(path.toString());
            } else if (Files.isDirectory(path)) {
                // Directory - list all files
                try (Stream<Path> paths = Files.list(path)) {
                    result = paths
                        .filter(Files::isRegularFile)
                        .map(Path::toString)
                        .collect(Collectors.toList());
                }
            }
        }

        return result;
    }

    /**
     * Finds the last directory separator before the first wildcard
     *
     * @param path the path string (can contain wildcards)
     * @return the index of the last directory separator
     */
    private static int findLastSeparatorBeforeWildcard(String path) {
        int firstWildcard = path.indexOf('*');
        String pathBeforeWildcard = path.substring(0, firstWildcard);

        int lastSlash = pathBeforeWildcard.lastIndexOf('/');
        int lastBackslash = pathBeforeWildcard.lastIndexOf('\\');

        return Math.max(lastSlash, lastBackslash);
    }

    /**
     * Converts wildcard pattern to regex pattern
     *
     * @param pattern the wildcard pattern
     * @return the regex pattern
     */
    private static String convertWildcardToRegex(String pattern) {
        // Escape regex special characters except * and ?
        String regex = pattern
            .replace(".", "\\.") //$NON-NLS-1$ //$NON-NLS-2$
            .replace("+", "\\+") //$NON-NLS-1$ //$NON-NLS-2$
            .replace("(", "\\(") //$NON-NLS-1$ //$NON-NLS-2$
            .replace(")", "\\)") //$NON-NLS-1$ //$NON-NLS-2$
            .replace("[", "\\[") //$NON-NLS-1$ //$NON-NLS-2$
            .replace("]", "\\]") //$NON-NLS-1$ //$NON-NLS-2$
            .replace("{", "\\{") //$NON-NLS-1$ //$NON-NLS-2$
            .replace("}", "\\}") //$NON-NLS-1$ //$NON-NLS-2$
            .replace("^", "\\^") //$NON-NLS-1$ //$NON-NLS-2$
            .replace("$", "\\$") //$NON-NLS-1$ //$NON-NLS-2$
            .replace("|", "\\|"); //$NON-NLS-1$ //$NON-NLS-2$

        // Convert wildcards to regex
        regex = regex.replace("*", ".*").replace("?", "."); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$

        return "^" + regex + "$"; //$NON-NLS-1$ //$NON-NLS-2$
    }

    /**
     * Alternative implementation using File class (simpler but less flexible)
     * @param pathString The path string (can contain wildcards)
     * @return list of matching files
     */
    public static List<String> listFilesAlternative(String pathString) {
        List<String> result = new ArrayList<>();

        File file = new File(pathString);

        if (file.exists()) {
            if (file.isFile()) {
                result.add(file.getAbsolutePath());
            } else if (file.isDirectory()) {
                // List all files in directory
                File[] files = file.listFiles();
                if (files != null) {
                    for (File f : files) {
                        if (f.isFile()) {
                            result.add(f.getAbsolutePath());
                        }
                    }
                }
            }
        } else if (pathString.contains("*")) { //$NON-NLS-1$
            // Handle wildcards using File class
            File parentDir;
            String pattern;

            if (pathString.contains("/") || pathString.contains("\\")) { //$NON-NLS-1$ //$NON-NLS-2$
                int lastSeparator = Math.max(
                    pathString.lastIndexOf('/'),
                    pathString.lastIndexOf('\\')
                );
                parentDir = new File(pathString.substring(0, lastSeparator));
                pattern = pathString.substring(lastSeparator + 1);
            } else {
                parentDir = new File("."); //$NON-NLS-1$
                pattern = pathString;
            }

            if (parentDir.exists() && parentDir.isDirectory()) {
                File[] files = parentDir.listFiles((dir, name) ->
                    name.matches(convertWildcardToRegex(pattern))
                );
                if (files != null) {
                    for (File f : files) {
                        if (f.isFile()) {
                            result.add(f.getAbsolutePath());
                        }
                    }
                }
            }
        }

        return result;
    }
}