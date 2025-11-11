/*
 * Copyright (c) 2025 El Mhadder Mohamed Rida. All rights reserved.
 * This code is licensed under the [MIT License](https://opensource.org/licenses/MIT).
 */
package io.github.emmrida.chat4ussetup.util;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.Locale;
import java.util.List;

/**
 * Cross-platform application link creator
 * Supports Windows, Linux, and macOS with proper icon handling
 */
public class AppLinkCreator {

    public enum OperatingSystem {
        WINDOWS, LINUX, MAC, UNKNOWN;

        private static final OperatingSystem CURRENT_OS = detectOS();

        private static OperatingSystem detectOS() {
            String os = System.getProperty("os.name").toLowerCase(Locale.ROOT); //$NON-NLS-1$
            if (os.contains("windows")) return WINDOWS; //$NON-NLS-1$
            if (os.contains("mac")) return MAC; //$NON-NLS-1$
            if (os.contains("nix") || os.contains("nux") || os.contains("aix")) return LINUX; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            return UNKNOWN;
        }

        public static OperatingSystem getCurrent() { return CURRENT_OS; }
    }

    /**
     * Configuration for link creation with platform-specific icon support
     */
    public static class LinkConfig {
        private String description;
        private String[] arguments;
        private boolean terminalApp;
        private String workingDirectory;

        // Platform-specific icon paths
        private String windowsIconPath;  // .ico file
        private String linuxIconPath;    // .png, .svg, or xpm file
        private String macIconPath;      // .icns file

        public LinkConfig() {}

        // Platform-specific icon getters and setters
        public String getWindowsIconPath() { return windowsIconPath; }
        public void setWindowsIconPath(String windowsIconPath) { this.windowsIconPath = windowsIconPath; }

        public String getLinuxIconPath() { return linuxIconPath; }
        public void setLinuxIconPath(String linuxIconPath) { this.linuxIconPath = linuxIconPath; }

        public String getMacIconPath() { return macIconPath; }
        public void setMacIconPath(String macIconPath) { this.macIconPath = macIconPath; }

        // General icon setter that tries to be smart about platform detection
        public void setIconPath(String iconPath) {
            if (iconPath == null) return;

            String lowerPath = iconPath.toLowerCase();
            if (lowerPath.endsWith(".ico")) { //$NON-NLS-1$
                this.windowsIconPath = iconPath;
            } else if (lowerPath.endsWith(".png") || lowerPath.endsWith(".svg") || lowerPath.endsWith(".xpm")) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                this.linuxIconPath = iconPath;
            } else if (lowerPath.endsWith(".icns")) { //$NON-NLS-1$
                this.macIconPath = iconPath;
            } else {
                // Default to setting all platforms if format is unknown
                this.windowsIconPath = iconPath;
                this.linuxIconPath = iconPath;
                this.macIconPath = iconPath;
            }
        }

        public String getIconPath() {
            OperatingSystem os = OperatingSystem.getCurrent();
            switch (os) {
                case WINDOWS: return windowsIconPath;
                case LINUX: return linuxIconPath;
                case MAC: return macIconPath;
                default: return null;
            }
        }

        // Other getters and setters
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public String[] getArguments() { return arguments; }
        public void setArguments(String[] arguments) { this.arguments = arguments; }

        public boolean isTerminalApp() { return terminalApp; }
        public void setTerminalApp(boolean terminalApp) { this.terminalApp = terminalApp; }

        public String getWorkingDirectory() { return workingDirectory; }
        public void setWorkingDirectory(String workingDirectory) { this.workingDirectory = workingDirectory; }

        // Builder-style methods for fluent configuration
        public LinkConfig withDescription(String description) {
            this.description = description;
            return this;
        }

        public LinkConfig withArguments(String... arguments) {
            this.arguments = arguments;
            return this;
        }

        public LinkConfig withTerminalApp(boolean terminalApp) {
            this.terminalApp = terminalApp;
            return this;
        }

        public LinkConfig withWorkingDirectory(String workingDirectory) {
            this.workingDirectory = workingDirectory;
            return this;
        }

        public LinkConfig withWindowsIcon(String iconPath) {
            this.windowsIconPath = iconPath;
            return this;
        }

        public LinkConfig withLinuxIcon(String iconPath) {
            this.linuxIconPath = iconPath;
            return this;
        }

        public LinkConfig withMacIcon(String iconPath) {
            this.macIconPath = iconPath;
            return this;
        }

        public LinkConfig withIcon(String iconPath) {
            setIconPath(iconPath);
            return this;
        }

        public static LinkConfig create() {
            return new LinkConfig();
        }

        public static LinkConfig defaultConfig() {
            return new LinkConfig();
        }
    }

    /**
     * Result of link creation operation
     */
    public static class LinkCreationResult {
        private final boolean success;
        private final String message;
        private final Path linkPath;

        public LinkCreationResult(boolean success, String message, Path linkPath) {
            this.success = success;
            this.message = message;
            this.linkPath = linkPath;
        }

        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public Path getLinkPath() { return linkPath; }

        public static LinkCreationResult success(String message, Path linkPath) {
            return new LinkCreationResult(true, message, linkPath);
        }

        public static LinkCreationResult failure(String message) {
            return new LinkCreationResult(false, message, null);
        }
    }

    /**
     * Creates a desktop link/shortcut to an application
     * @param appPath Path to the application (JAR, EXE, etc.)
     * @param linkName Name for the link (without extension)
     * @return true if successful, false otherwise
     */
    public static boolean createDesktopLink(String appPath, String linkName) {
        return createLink(appPath, linkName, getDesktopDirectory(), LinkConfig.defaultConfig());
    }

    /**
     * Creates a desktop link/shortcut to an application with configuration
     * @param appPath Path to the application (JAR, EXE, etc.)
     * @param linkName Name for the link (without extension)
     * @param config Configuration for the link
     * @return LinkCreationResult with detailed information
     */
    public static LinkCreationResult createDesktopLink(String appPath, String linkName, LinkConfig config) {
        return createLinkWithResult(appPath, linkName, getDesktopDirectory(), config);
    }

    /**
     * Creates a system menu link to an application
     * @param appPath Path to the application (JAR, EXE, etc.)
     * @param linkName Name for the link (without extension)
     * @return true if successful, false otherwise
     */
    public static boolean createSystemMenuLink(String appPath, String linkName) {
        return createLink(appPath, linkName, getSystemMenuDirectory(), LinkConfig.defaultConfig());
    }

    /**
     * Creates a system menu link to an application with configuration
     * @param appPath Path to the application (JAR, EXE, etc.)
     * @param linkName Name for the link (without extension)
     * @param config Configuration for the link
     * @return LinkCreationResult with detailed information
     */
    public static LinkCreationResult createSystemMenuLink(String appPath, String linkName, LinkConfig config) {
        return createLinkWithResult(appPath, linkName, getSystemMenuDirectory(), config);
    }

    /**
     * Removes a desktop link/shortcut
     * @param linkName Name of the link (without extension)
     * @return true if successful, false otherwise
     */
    public static boolean removeDesktopLink(String linkName) {
        Path linkPath = getLinkPath(linkName, true);
        if (linkPath != null && Files.exists(linkPath)) {
            try {
                Files.delete(linkPath);
                System.out.println(Messages.getString("AppLinkCreator.LOG_DESKTOP_LINK_REMOVED") + linkPath); //$NON-NLS-1$
                return true;
            } catch (IOException ex) {
                System.out.println(Messages.getString("AppLinkCreator.LOG_DESKTOP_LINK_REMOVE_ERROR") + linkPath + "\n" + ex.getMessage()); //$NON-NLS-1$ //$NON-NLS-2$
            }
        } else {
            System.out.println(Messages.getString("AppLinkCreator.LOG_DESKTOP_LINK_NOT_FOUND") + linkName); //$NON-NLS-1$
        }
        return false;
    }

    /**
     * Removes a system menu link
     * @param linkName Name of the link (without extension)
     * @return true if successful, false otherwise
     */
    public static boolean removeSystemMenuLink(String linkName) {
        Path linkPath = getLinkPath(linkName, false);
        if (linkPath != null && Files.exists(linkPath)) {
            try {
                Files.delete(linkPath);
                System.out.println(Messages.getString("AppLinkCreator.LOG_SYSMNU_LINK_REMOVED") + linkPath); //$NON-NLS-1$
                return true;
            } catch (IOException ex) {
                System.out.println(Messages.getString("AppLinkCreator.LOG_SYSMNU_LINK_DELETE_ERROR") + linkPath + "\n" + ex.getMessage()); //$NON-NLS-1$ //$NON-NLS-2$
            }
        } else {
            System.out.println(Messages.getString("AppLinkCreator.LOG_SYSMNU_LINK_NOT_FOUND") + linkName); //$NON-NLS-1$
        }
        return false;
    }

    /**
     * Reads the application parent folder from an existing desktop or system menu link
     * @param linkName Name of the link (without extension)
     * @return Path to the application's parent folder, or null if not found
     */
    public static String readAppParentFolder(String linkName) {
        Path desktopLink = getLinkPath(linkName, true);
        Path systemMenuLink = getLinkPath(linkName, false);

        Path searchDir = null;
        if (desktopLink != null && Files.exists(desktopLink)) {
            searchDir = desktopLink.getParent();
        } else if (systemMenuLink != null && Files.exists(systemMenuLink)) {
            searchDir = systemMenuLink.getParent();
        }

        if (searchDir != null && Files.exists(searchDir)) {
            try {
                switch (OperatingSystem.getCurrent()) {
                    case WINDOWS:
                        return readWindowsLinkFolder(linkName, searchDir);
                    case LINUX:
                        return readLinuxDesktopEntryFolder(linkName, searchDir);
                    case MAC:
                        return readMacAppFolder(linkName, searchDir);
                    default:
                        System.out.println(Messages.getString("AppLinkCreator.LOG_UNSUPPORTED_OS") + OperatingSystem.getCurrent()); //$NON-NLS-1$
                        return null;
                }
            } catch (Exception ex) {
                System.out.println(Messages.getString("AppLinkCreator.LOG_LINK_READ_FAILED") + linkName + "\n" + ex.getMessage()); //$NON-NLS-1$ //$NON-NLS-2$
                return null;
            }
        }
        System.out.println(Messages.getString("AppLinkCreator.LOG_SEARCH_FOLDER_NEXIST") + linkName); //$NON-NLS-1$
        return null;
    }

    /**
     * Checks if a desktop link exists
     * @param linkName Name of the link (without extension)
     * @return true if the link exists, false otherwise
     */
    public static boolean desktopLinkExists(String linkName) {
        Path linkPath = getLinkPath(linkName, true);
        return linkPath != null && Files.exists(linkPath);
    }

    /**
     * Checks if a system menu link exists
     * @param linkName Name of the link (without extension)
     * @return true if the link exists, false otherwise
     */
    public static boolean systemMenuLinkExists(String linkName) {
        Path linkPath = getLinkPath(linkName, false);
        return linkPath != null && Files.exists(linkPath);
    }

    /**
     * Gets the actual path of a created link
     * @param linkName Name of the link (without extension)
     * @param isDesktop true for desktop link, false for system menu link
     * @return Path to the link file, or null if not applicable
     */
    public static Path getLinkPath(String linkName, boolean isDesktop) {
        Path targetDir = isDesktop ? getDesktopDirectory() : getSystemMenuDirectory();
        if (targetDir != null) {
            switch (OperatingSystem.getCurrent()) {
                case WINDOWS:
                    return targetDir.resolve(linkName + ".lnk"); //$NON-NLS-1$
                case LINUX:
                    return targetDir.resolve(linkName + ".desktop"); //$NON-NLS-1$
                case MAC:
                    return targetDir.resolve(linkName + ".app"); //$NON-NLS-1$
                default:
                    return null;
            }
        }
        return null;
    }

    /**
     * Gets the current operating system
     */
    public static OperatingSystem getCurrentOperatingSystem() {
        return OperatingSystem.getCurrent();
    }

    /**
     * Utility method to get icon requirements for each platform
     */
    public static String getIconRequirements() {
        return Messages.getString("AppLinkCreator.WIN_ICON_REQUIREMENTS") + //$NON-NLS-1$
               Messages.getString("AppLinkCreator.LINUX_ICON_REQUIREMENTS") + //$NON-NLS-1$
               Messages.getString("AppLinkCreator.MACOS_ICON_REQUIREMENTS"); //$NON-NLS-1$
    }

    // Private implementation methods

    private static boolean createLink(String appPath, String linkName, Path targetDir, LinkConfig config) {
        LinkCreationResult result = createLinkWithResult(appPath, linkName, targetDir, config);
        return result.isSuccess();
    }

    private static LinkCreationResult createLinkWithResult(String appPath, String linkName, Path targetDir, LinkConfig config) {
        try {
            validateInputs(appPath, linkName, targetDir);

            File appFile = new File(appPath);
            if (!appFile.exists()) {
                String message = Messages.getString("AppLinkCreator.LOG_APP_NEXIST") + appPath; //$NON-NLS-1$
                System.out.println(message);
                return LinkCreationResult.failure(message);
            }

            Path workingDir = config.getWorkingDirectory() != null ?
                Paths.get(config.getWorkingDirectory()) :
                appFile.getParentFile().toPath();

            Path linkPath;
            switch (OperatingSystem.getCurrent()) {
                case WINDOWS:
                    linkPath = createWindowsLink(appPath, linkName, targetDir, workingDir, config);
                    break;
                case LINUX:
                    linkPath = createLinuxDesktopEntry(appPath, linkName, targetDir, workingDir, config);
                    break;
                case MAC:
                    linkPath = createMacAppLink(appPath, linkName, targetDir, workingDir, config);
                    break;
                default:
                    String message = Messages.getString("AppLinkCreator.LOG_UNSUPPORTED_OS") + OperatingSystem.getCurrent(); //$NON-NLS-1$
                    System.out.println(message);
                    return LinkCreationResult.failure(message);
            }

            if (linkPath != null && Files.exists(linkPath)) {
                String message = Messages.getString("AppLinkCreator.LOG_LINK_CREATED_SUCCESS") + linkPath; //$NON-NLS-1$
                System.out.println(message);
                return LinkCreationResult.success(message, linkPath);
            } else {
                String message = Messages.getString("AppLinkCreator.LOG_LINK_CREATION_FAILURE") + linkName; //$NON-NLS-1$
                System.out.println(message);
                return LinkCreationResult.failure(message);
            }
        } catch (Exception e) {
            String message = Messages.getString("AppLinkCreator.LOG_LINK_CREATION_FAILURE") + e.getMessage(); //$NON-NLS-1$
            System.out.println(message);
            return LinkCreationResult.failure(message);
        }
    }

    private static void validateInputs(String appPath, String linkName, Path targetDir) {
        if (appPath == null || appPath.trim().isEmpty()) {
            throw new IllegalArgumentException(Messages.getString("AppLinkCreator.EX_NULL_EMPTY_PATH")); //$NON-NLS-1$
        }
        if (linkName == null || linkName.trim().isEmpty()) {
            throw new IllegalArgumentException(Messages.getString("AppLinkCreator.EX_NULL_EMPTY_NAME")); //$NON-NLS-1$
        }
        if (targetDir == null) {
            throw new IllegalArgumentException(Messages.getString("AppLinkCreator.EX_NULL_TARGET_DIR")); //$NON-NLS-1$
        }

        // Sanitize link name to prevent path traversal
        if (linkName.contains("..") || linkName.contains("/") || linkName.contains("\\")) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            throw new IllegalArgumentException(Messages.getString("AppLinkCreator.EX_LINK_NAME_INVALID") + linkName); //$NON-NLS-1$
        }
    }

    private static Path createWindowsLink(String appPath, String linkName, Path targetDir, Path workingDir, LinkConfig config) {
        Path tempScript = null;
        try {
            Path linkPath = targetDir.resolve(linkName + ".lnk"); //$NON-NLS-1$

            // Build icon configuration
            String iconConfig = ""; //$NON-NLS-1$
            if (config.getWindowsIconPath() != null) {
                String iconPath = config.getWindowsIconPath();
                if (iconPath.toLowerCase().endsWith(".ico")) { //$NON-NLS-1$
                    iconConfig = String.format("link.IconLocation = \"%s\"\n", //$NON-NLS-1$
                        iconPath.replace("\\", "\\\\")); //$NON-NLS-1$ //$NON-NLS-2$
                } else {
                    System.out.println(Messages.getString("AppLinkCreator.LOG_WIN_ICON_MUSTBE_ICO") + iconPath); //$NON-NLS-1$
                }
            }

            // Using VBScript to create Windows shortcut
            String vbsScript = String.format(
                "Set ws = CreateObject(\"WScript.Shell\")\n" + //$NON-NLS-1$
                "Set link = ws.CreateShortcut(\"%s\")\n" + //$NON-NLS-1$
                "link.TargetPath = \"%s\"\n" + //$NON-NLS-1$
                "link.WorkingDirectory = \"%s\"\n" + //$NON-NLS-1$
                "link.Arguments = \"%s\"\n" + //$NON-NLS-1$
                "%s" + // Icon configuration //$NON-NLS-1$
                "link.Description = \"%s\"\n" + //$NON-NLS-1$
                "link.Save", //$NON-NLS-1$
                linkPath.toString(),
                getWindowsTargetPath(appPath),
                workingDir.toString(),
                getWindowsArguments(appPath, config).replace("\\\\", "\\"), // FIXED: Added appPath parameter //$NON-NLS-1$ //$NON-NLS-2$
                iconConfig.replace("\\\\", "\\"), //$NON-NLS-1$ //$NON-NLS-2$
                config.getDescription()
            );

            // Write VBS script to temp file
            tempScript = Files.createTempFile("create_shortcut", ".vbs"); //$NON-NLS-1$ //$NON-NLS-2$
            Files.write(tempScript, vbsScript.getBytes(StandardCharsets.UTF_8));

            // Execute VBS script using ProcessBuilder
            ProcessBuilder processBuilder = new ProcessBuilder("wscript", tempScript.toString()) //$NON-NLS-1$
                .directory(workingDir.toFile());

            Process process = processBuilder.start();
            int exitCode = process.waitFor();

            if (exitCode == 0 && Files.exists(linkPath)) {
                return linkPath;
            } else {
                System.out.println(Messages.getString("AppLinkCreator.LOG_WIN_LINK_CREATION_FAILURE_XC") + exitCode); //$NON-NLS-1$
                return null;
            }

        } catch (Exception ex) {
            System.out.println(Messages.getString("AppLinkCreator.LOG_WIN_LINK_CREATION_FAILURE") + ex.getMessage()); //$NON-NLS-1$
            return null;
        } finally {
            if (tempScript != null) {
                try {
                    Files.deleteIfExists(tempScript);
                } catch (IOException e) {
                    System.out.println(Messages.getString("AppLinkCreator.LOG_TMP_SCRIPT_DELETE_FAILURE") + e.getMessage()); //$NON-NLS-1$
                }
            }
        }
    }
    private static Path createLinuxDesktopEntry(String appPath, String linkName, Path targetDir, Path workingDir, LinkConfig config) {
        try {
            Path desktopFile = targetDir.resolve(linkName + ".desktop"); //$NON-NLS-1$

            // Build icon line
            String iconLine = ""; //$NON-NLS-1$
            if (config.getLinuxIconPath() != null) {
                String iconPath = config.getLinuxIconPath();
                if (new File(iconPath).exists()) {
                    // Use absolute path if file exists
                    iconLine = "Icon=" + iconPath + "\n"; //$NON-NLS-1$ //$NON-NLS-2$
                } else {
                    // Otherwise assume it's a system icon name
                    iconLine = "Icon=" + iconPath + "\n"; //$NON-NLS-1$ //$NON-NLS-2$
                }
            }

            // Build categories line (common for Linux desktop entries)
            String categories = "Utility;"; //$NON-NLS-1$
            if (config.getDescription() != null && config.getDescription().toLowerCase().contains("game")) { //$NON-NLS-1$
                categories = "Game;"; //$NON-NLS-1$
            }

            String desktopEntry = String.format(
                "[Desktop Entry]\n" + //$NON-NLS-1$
                "Version=1.0\n" + //$NON-NLS-1$
                "Type=Application\n" + //$NON-NLS-1$
                "Name=%s\n" + //$NON-NLS-1$
                "Exec=%s\n" + //$NON-NLS-1$
                "Path=%s\n" + //$NON-NLS-1$
                "Terminal=%s\n" + //$NON-NLS-1$
                "StartupNotify=true\n" + //$NON-NLS-1$
                "Categories=%s\n" + //$NON-NLS-1$
                "%s" + // Description //$NON-NLS-1$
                "%s",  // Icon //$NON-NLS-1$
                linkName,
                getLinuxExecCommand(appPath, config),
                workingDir.toString(),
                config.isTerminalApp() ? "true" : "false", //$NON-NLS-1$ //$NON-NLS-2$
                categories,
                config.getDescription() != null ? "Comment=" + config.getDescription() + "\n" : "", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                iconLine
            );

            Files.write(desktopFile, desktopEntry.getBytes(StandardCharsets.UTF_8));

            // Make the desktop entry executable
            desktopFile.toFile().setExecutable(true);

            // Update desktop database (optional but recommended)
            updateDesktopDatabase();

            return desktopFile;

        } catch (Exception ex) {
            System.out.println(Messages.getString("AppLinkCreator.LOG_LINUX_DESKTOP_ENTRY_ERROR") + ex.getMessage()); //$NON-NLS-1$
            return null;
        }
    }

    private static Path createMacAppLink(String appPath, String linkName, Path targetDir, Path workingDir, LinkConfig config) {
        try {
            // For macOS, we create an app bundle that runs the command
            String appContent = String.format(
                "#!/bin/bash\n" + //$NON-NLS-1$
                "cd \"%s\"\n" + //$NON-NLS-1$
                "%s\n", //$NON-NLS-1$
                workingDir.toString().replace("\"", "\\\""), //$NON-NLS-1$ //$NON-NLS-2$
                getMacExecCommand(appPath, config).replace("\"", "\\\"") //$NON-NLS-1$ //$NON-NLS-2$
            );

            Path appDir = targetDir.resolve(linkName + ".app"); //$NON-NLS-1$
            Path contentsDir = appDir.resolve("Contents"); //$NON-NLS-1$
            Path macosDir = contentsDir.resolve("MacOS"); //$NON-NLS-1$
            Path resourcesDir = contentsDir.resolve("Resources"); //$NON-NLS-1$
            Path infoPlist = contentsDir.resolve("Info.plist"); //$NON-NLS-1$

            Files.createDirectories(macosDir);
            Files.createDirectories(resourcesDir);

            // Create executable script
            Path executable = macosDir.resolve(linkName);
            Files.write(executable, appContent.getBytes(StandardCharsets.UTF_8));
            executable.toFile().setExecutable(true);

            // Handle macOS icon
            if (config.getMacIconPath() != null) {
                String iconPath = config.getMacIconPath();
                if (iconPath.toLowerCase().endsWith(".icns")) { //$NON-NLS-1$
                    try {
                        Path iconFile = Paths.get(iconPath);
                        if (Files.exists(iconFile)) {
                            Path destIcon = resourcesDir.resolve(linkName + ".icns"); //$NON-NLS-1$
                            Files.copy(iconFile, destIcon, StandardCopyOption.REPLACE_EXISTING);
                        }
                    } catch (Exception e) {
                        System.out.println(Messages.getString("AppLinkCreator.LOG_MACOS_ICON_COPY_ERROR") + e.getMessage()); //$NON-NLS-1$
                    }
                } else {
                    System.out.println(Messages.getString("AppLinkCreator.LOG_MACOS_ICON_INVALID") + iconPath); //$NON-NLS-1$
                }
            }

            // Create Info.plist with icon reference if available
            String iconPlistEntry = ""; //$NON-NLS-1$
            if (config.getMacIconPath() != null && Files.exists(resourcesDir.resolve(linkName + ".icns"))) { //$NON-NLS-1$
                iconPlistEntry = "    <key>CFBundleIconFile</key>\n    <string>" + linkName + ".icns</string>\n"; //$NON-NLS-1$ //$NON-NLS-2$
            }

            String plistContent = String.format(
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + //$NON-NLS-1$
                "<!DOCTYPE plist PUBLIC \"-//Apple//DTD PLIST 1.0//EN\" \"http://www.apple.com/DTDs/PropertyList-1.0.dtd\">\n" + //$NON-NLS-1$
                "<plist version=\"1.0\">\n" + //$NON-NLS-1$
                "<dict>\n" + //$NON-NLS-1$
                "    <key>CFBundleExecutable</key>\n" + //$NON-NLS-1$
                "    <string>%s</string>\n" + //$NON-NLS-1$
                "    <key>CFBundleIdentifier</key>\n" + //$NON-NLS-1$
                "    <string>com.example.%s</string>\n" + //$NON-NLS-1$
                "    <key>CFBundleName</key>\n" + //$NON-NLS-1$
                "    <string>%s</string>\n" + //$NON-NLS-1$
                "    <key>CFBundleVersion</key>\n" + //$NON-NLS-1$
                "    <string>1.0</string>\n" + //$NON-NLS-1$
                "    <key>CFBundleShortVersionString</key>\n" + //$NON-NLS-1$
                "    <string>1.0</string>\n" + //$NON-NLS-1$
                "    <key>CFBundleGetInfoString</key>\n" + //$NON-NLS-1$
                "    <string>%s</string>\n" + //$NON-NLS-1$
                "%s" + // Icon entry //$NON-NLS-1$
                "</dict>\n" + //$NON-NLS-1$
                "</plist>", //$NON-NLS-1$
                linkName,
                linkName.toLowerCase().replace(" ", "-"), //$NON-NLS-1$ //$NON-NLS-2$
                linkName,
                config.getDescription() != null ? config.getDescription() : linkName,
                iconPlistEntry
            );

            Files.write(infoPlist, plistContent.getBytes(StandardCharsets.UTF_8));

            return appDir;

        } catch (Exception ex) {
            System.out.println(Messages.getString("AppLinkCreator.LOG_MACOS_APP_CREATION_FAILURE") + ex.getMessage()); //$NON-NLS-1$
            return null;
        }
    }

    private static String readWindowsLinkFolder(String linkName, Path searchDir) {
        Path tempScript = null;
        try {
            Path linkPath = searchDir.resolve(linkName + ".lnk"); //$NON-NLS-1$
            if (!Files.exists(linkPath)) {
                System.out.println(Messages.getString("AppLinkCreator.LOG_LINK_FILE_NOT_FOUND") + linkPath); //$NON-NLS-1$
                return null;
            }

            // Use VBScript to read Windows shortcut properties
            String vbsScript = String.format(
                "Set ws = CreateObject(\"WScript.Shell\")\n" + //$NON-NLS-1$
                "Set link = ws.CreateShortcut(\"%s\")\n" + //$NON-NLS-1$
                "WScript.StdOut.Write link.WorkingDirectory\n", //$NON-NLS-1$
                linkPath.toString().replace("\\", "/") //$NON-NLS-1$ //$NON-NLS-2$
            );

            tempScript = Files.createTempFile("read_shortcut", ".vbs"); //$NON-NLS-1$ //$NON-NLS-2$
            Files.write(tempScript, vbsScript.getBytes(StandardCharsets.UTF_8));

            Process process = new ProcessBuilder("wscript", tempScript.toString()).start(); //$NON-NLS-1$

            // Read the output
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String workingDir = reader.readLine();

            int exitCode = process.waitFor();

            if (exitCode == 0 && workingDir != null && !workingDir.trim().isEmpty()) {
                return workingDir.trim();
            } else {
                System.out.println(Messages.getString("AppLinkCreator.LOG_WIN_LINK_READ_FAILURE_XC") + exitCode); //$NON-NLS-1$
            }

        } catch (Exception ex) {
            System.out.println(Messages.getString("AppLinkCreator.LOG_WIN_LINK_READ_FAILURE") + ex.getMessage()); //$NON-NLS-1$
        } finally {
            if (tempScript != null) {
                try {
                    Files.deleteIfExists(tempScript);
                } catch (IOException e) {
                    System.out.println(Messages.getString("AppLinkCreator.LOG_TMP_SCRIPT_DELETE_FAILURE") + e.getMessage()); //$NON-NLS-1$
                }
            }
        }
        return null;
    }

    private static String readLinuxDesktopEntryFolder(String linkName, Path searchDir) {
        try {
            Path desktopFile = searchDir.resolve(linkName + ".desktop"); //$NON-NLS-1$
            if (!Files.exists(desktopFile)) {
                System.out.println(Messages.getString("AppLinkCreator.LOG_DESKTOP_FILE_NOT_FOUND") + desktopFile); //$NON-NLS-1$
                return null;
            }

            List<String> lines = Files.readAllLines(desktopFile, StandardCharsets.UTF_8);
            for (String line : lines) {
                if (line.startsWith("Path=")) { //$NON-NLS-1$
                    return line.substring(5).trim();
                }
            }

            System.out.println(Messages.getString("AppLinkCreator.LOG_PATH_NOT_FOUND_IN_DESKTOP") + desktopFile); //$NON-NLS-1$

        } catch (Exception ex) {
            System.out.println(Messages.getString("AppLinkCreator.LOG_LINUX_DESKTOP_ENTRY_FAILURE") + ex.getMessage()); //$NON-NLS-1$
        }
        return null;
    }

    private static String readMacAppFolder(String linkName, Path searchDir) {
        try {
            Path appDir = searchDir.resolve(linkName + ".app"); //$NON-NLS-1$
            if (!Files.exists(appDir)) {
                System.out.println(Messages.getString("AppLinkCreator.LOG_MACAPP_NOT_FOUND") + appDir); //$NON-NLS-1$
                return null;
            }

            Path executable = appDir.resolve("Contents/MacOS/" + linkName); //$NON-NLS-1$
            if (!Files.exists(executable)) {
                System.out.println(Messages.getString("AppLinkCreator.LOG_EXECUTABLE_NOT_FOUND") + executable); //$NON-NLS-1$
                return null;
            }

            List<String> lines = Files.readAllLines(executable, StandardCharsets.UTF_8);
            for (String line : lines) {
                if (line.startsWith("cd ")) { //$NON-NLS-1$
                    // Extract the path from "cd /path/to/dir"
                    String pathLine = line.substring(3).trim();
                    // Remove quotes if present
                    if (pathLine.startsWith("\"") && pathLine.endsWith("\"")) { //$NON-NLS-1$ //$NON-NLS-2$
                        pathLine = pathLine.substring(1, pathLine.length() - 1);
                    }
                    return pathLine;
                }
            }

            System.out.println(Messages.getString("AppLinkCreator.LOG_MAC_WD_NOT_FOUND") + executable); //$NON-NLS-1$

        } catch (Exception ex) {
            System.out.println(Messages.getString("AppLinkCreator.LOG_MACAPP_READ_FAILURE") + ex.getMessage()); //$NON-NLS-1$
        }
        return null;
    }

    private static void updateDesktopDatabase() {
        if (OperatingSystem.getCurrent() != OperatingSystem.LINUX) return;

        try {
            // Try to update desktop database for new applications
            String[] updateCommands = {
                "update-desktop-database", //$NON-NLS-1$
                "xdg-desktop-menu forceupdate" //$NON-NLS-1$
            };

            for (String command : updateCommands) {
                try {
                    Process process = new ProcessBuilder("sh", "-c", "which " + command.split(" ")[0]) //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
                        .start();
                    if (process.waitFor() == 0) {
                        new ProcessBuilder(command.split(" ")) //$NON-NLS-1$
                            .start()
                            .waitFor();
                        System.out.println(Messages.getString("AppLinkCreator.LOG_UPATED_DESKTOP_DB_WITH") + command); //$NON-NLS-1$
                        break;
                    }
                } catch (Exception ignored) {
                    // Ignore - command might not be available
                }
            }
        } catch (Exception e) {
            System.out.println(Messages.getString("AppLinkCreator.LOG_UPDATE_DESKTOP_DB_FAILURE") + e.getMessage()); //$NON-NLS-1$
        }
    }

    // Helper methods for command building
    private static String getWindowsTargetPath(String appPath) {
        if (appPath.toLowerCase().endsWith(".jar")) { //$NON-NLS-1$
        	String javaHome = System.getProperty("java.home"); //$NON-NLS-1$
        	return Path.of(javaHome, "bin", "javaw.exe").toString(); //$NON-NLS-1$ //$NON-NLS-2$
        }
        return appPath;
    }

    /**
     * Builds Windows command line arguments for the shortcut
     * @param appPath Path to the application
     * @param config Link configuration
     * @return Formatted arguments string
     */
    private static String getWindowsArguments(String appPath, LinkConfig config) {
        StringBuilder args = new StringBuilder();

        // For JAR files, we need to include the -jar parameter
        if (appPath.toLowerCase().endsWith(".jar")) { //$NON-NLS-1$
            args.append("-jar \"\"").append(appPath).append("\"\""); //$NON-NLS-1$ //$NON-NLS-2$
        }

        // Add any additional arguments from config
        if (config.getArguments() != null && config.getArguments().length > 0) {
            if (args.length() > 0) args.append(" "); //$NON-NLS-1$
            args.append(String.join(" ", config.getArguments())); //$NON-NLS-1$
        }

        // Escape backslashes for VBScript
        return args.toString().replace("\\", "\\\\"); //$NON-NLS-1$ //$NON-NLS-2$
    }

    private static String getLinuxExecCommand(String appPath, LinkConfig config) {
        StringBuilder command = new StringBuilder();
        if (appPath.toLowerCase().endsWith(".jar")) { //$NON-NLS-1$
            command.append("java -jar \"").append(appPath).append("\""); //$NON-NLS-1$ //$NON-NLS-2$
        } else {
            command.append("\"").append(appPath).append("\""); //$NON-NLS-1$ //$NON-NLS-2$
        }

        if (config.getArguments() != null && config.getArguments().length > 0) {
            command.append(" ").append(String.join(" ", config.getArguments())); //$NON-NLS-1$ //$NON-NLS-2$
        }

        return command.toString();
    }

    private static String getMacExecCommand(String appPath, LinkConfig config) {
        StringBuilder command = new StringBuilder();
        if (appPath.toLowerCase().endsWith(".jar")) { //$NON-NLS-1$
            command.append("java -jar \"").append(appPath).append("\""); //$NON-NLS-1$ //$NON-NLS-2$
        } else {
            command.append("open \"").append(appPath).append("\""); //$NON-NLS-1$ //$NON-NLS-2$
        }

        if (config.getArguments() != null && config.getArguments().length > 0) {
            command.append(" ").append(String.join(" ", config.getArguments())); //$NON-NLS-1$ //$NON-NLS-2$
        }

        return command.toString();
    }

    private static Path getDesktopDirectory() {
        switch (OperatingSystem.getCurrent()) {
            case WINDOWS:
                return Paths.get(System.getenv("USERPROFILE"), "Desktop"); //$NON-NLS-1$ //$NON-NLS-2$
            case LINUX:
            case MAC:
                return Paths.get(System.getProperty("user.home"), "Desktop"); //$NON-NLS-1$ //$NON-NLS-2$
            default:
                return Paths.get(System.getProperty("user.home")); //$NON-NLS-1$
        }
    }

    private static Path getSystemMenuDirectory() {
        switch (OperatingSystem.getCurrent()) {
            case WINDOWS:
                String appData = System.getenv("APPDATA"); //$NON-NLS-1$
                return Paths.get(appData, "Microsoft", "Windows", "Start Menu", "Programs"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
            case LINUX:
                return Paths.get(System.getProperty("user.home"), ".local", "share", "applications"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
            case MAC:
                return Paths.get("/Applications"); //$NON-NLS-1$
            default:
                return null;
        }
    }

    public static boolean isWindows() {
        return OperatingSystem.getCurrent() == OperatingSystem.WINDOWS;
    }

    public static boolean isLinux() {
        return OperatingSystem.getCurrent() == OperatingSystem.LINUX;
    }

    public static boolean isMac() {
        return OperatingSystem.getCurrent() == OperatingSystem.MAC;
    }
}