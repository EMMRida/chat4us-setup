/*
 * Copyright (c) 2025 El Mhadder Mohamed Rida. All rights reserved.
 * This code is licensed under the [MIT License](https://opensource.org/licenses/MIT).
 */
package io.github.emmrida.chat4ussetup.util;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.attribute.PosixFilePermission;
import java.util.*;
import java.util.jar.*;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * Utility class to extract version from any supported file type
 */
public class CrossPlatformVersionReader {

    /**
     * Main method to extract version from any supported file type
     * For JAR files, returns app.version + '.' + build.number if both are found
     */
    public static String extractVersion(String filePath) {
        File file = new File(filePath);

        if (!file.exists()) {
            return Messages.getString("CrossPlatformVersionReader.LOG_FILE_NFOUND") + filePath; //$NON-NLS-1$
        }

        if (!file.canRead()) {
            return Messages.getString("CrossPlatformVersionReader.LOG_CANT_READ_FILE") + filePath; //$NON-NLS-1$
        }

        String fileName = file.getName().toLowerCase();

        try {
            if (fileName.endsWith(".jar")) { //$NON-NLS-1$
                return extractFromJar(filePath);
            } else if (fileName.endsWith(".exe") || fileName.endsWith(".dll") || //$NON-NLS-1$ //$NON-NLS-2$
                       fileName.endsWith(".msi") || fileName.endsWith(".com")) { //$NON-NLS-1$ //$NON-NLS-2$
                return extractFromWindowsExecutable(filePath);
            } else if (isUnixExecutable(file) || isMacAppBundle(filePath)) {
                return extractFromUnixExecutable(filePath);
            } else if (fileName.endsWith(".deb")) { //$NON-NLS-1$
                return extractFromDebPackage(filePath);
            } else if (fileName.endsWith(".rpm")) { //$NON-NLS-1$
                return extractFromRpmPackage(filePath);
            } else {
                return Messages.getString("CrossPlatformVersionReader.LOG_UNSUPPORTED_FILE_TYPE") + fileName; //$NON-NLS-1$
            }
        } catch (Exception e) {
            return Messages.getString("CrossPlatformVersionReader.EX_VERSION_EXTRACT_ERROR") + e.getMessage(); //$NON-NLS-1$
        }
    }

    /**
     * Extract version from JAR files - returns combined app.version + '.' + build.number
     */
    private static String extractFromJar(String jarPath) {
        JarVersionInfo versionInfo = extractAllJarVersionInfo(jarPath);

        if (versionInfo.hasCombinedVersion()) {
            return versionInfo.getCombinedVersion();
        } else if (versionInfo.hasAppVersion()) {
            return versionInfo.getAppVersion();
        } else if (versionInfo.hasBuildNumber()) {
            return "build." + versionInfo.getBuildNumber(); //$NON-NLS-1$
        } else {
            return Messages.getString("CrossPlatformVersionReader.LOG_VERSION_NFOUND"); //$NON-NLS-1$
        }
    }

    /**
     * Extract all version information from JAR
     */
    private static JarVersionInfo extractAllJarVersionInfo(String jarPath) {
        JarVersionInfo versionInfo = new JarVersionInfo();

        try (JarFile jarFile = new JarFile(jarPath)) {
            // 1. Check MANIFEST.MF
            extractFromManifest(jarFile, versionInfo);

            // 2. Check properties files
            extractFromPropertiesFiles(jarFile, versionInfo);

        } catch (IOException e) {
            // Ignore - return whatever we found
        }

        return versionInfo;
    }

    /**
     * Extract version from MANIFEST.MF
     */
    private static void extractFromManifest(JarFile jarFile, JarVersionInfo versionInfo) {
        try {
            Manifest manifest = jarFile.getManifest();
            if (manifest != null) {
                String[] versionAttributes = {
                    "Implementation-Version", "Specification-Version", //$NON-NLS-1$ //$NON-NLS-2$
                    "Bundle-Version", "Version" //$NON-NLS-1$ //$NON-NLS-2$
                };

                for (String attr : versionAttributes) {
                    String version = manifest.getMainAttributes().getValue(attr);
                    if (isValidVersion(version)) {
                        versionInfo.addVersionSource("MANIFEST-" + attr, version); //$NON-NLS-1$
                    }
                }
            }
        } catch (Exception e) {
            // Ignore - try other methods
        }
    }

    /**
     * Extract version from properties files in JAR
     */
    private static void extractFromPropertiesFiles(JarFile jarFile, JarVersionInfo versionInfo) {
        Enumeration<JarEntry> entries = jarFile.entries();

        while (entries.hasMoreElements()) {
            JarEntry entry = entries.nextElement();
            String entryName = entry.getName();

            if (entryName.toLowerCase().endsWith(".properties")) { //$NON-NLS-1$
                try (InputStream is = jarFile.getInputStream(entry)) {
                    Properties props = new Properties();
                    props.load(is);

                    // Check all properties for version patterns
                    for (String propName : props.stringPropertyNames()) {
                        String propValue = props.getProperty(propName);

                        // Look for app.version
                        if (isAppVersionProperty(propName, propValue)) {
                            versionInfo.setAppVersion(propValue);
                            versionInfo.addVersionSource(entryName + "::" + propName, propValue); //$NON-NLS-1$
                        }

                        // Look for build.number
                        if (isBuildNumberProperty(propName, propValue)) {
                            versionInfo.setBuildNumber(propValue);
                            versionInfo.addVersionSource(entryName + "::" + propName, propValue); //$NON-NLS-1$
                        }

                        // Also check for other version properties
                        if (isVersionProperty(propName, propValue) && !versionInfo.hasAppVersion()) {
                            versionInfo.addVersionSource(entryName + "::" + propName, propValue); //$NON-NLS-1$
                        }
                    }
                } catch (IOException e) {
                    // Ignore this properties file, continue with next
                }
            }
        }
    }

    /**
     * Get detailed version information from JAR file
     */
    public static Map<String, String> getDetailedJarVersionInfo(String jarPath) {
        JarVersionInfo versionInfo = extractAllJarVersionInfo(jarPath);
        return versionInfo.getAllVersionSources();
    }

    /**
     * Get the combined version (app.version + '.' + build.number)
     */
    public static String getCombinedVersion(String filePath) {
        if (filePath.toLowerCase().endsWith(".jar")) { //$NON-NLS-1$
            JarVersionInfo versionInfo = extractAllJarVersionInfo(filePath);
            if (versionInfo.hasCombinedVersion()) {
                return versionInfo.getCombinedVersion();
            }
        }
        return extractVersion(filePath);
    }

    /**
     * Get just the app version
     */
    public static String getAppVersion(String filePath) {
        if (filePath.toLowerCase().endsWith(".jar")) { //$NON-NLS-1$
            JarVersionInfo versionInfo = extractAllJarVersionInfo(filePath);
            if (versionInfo.hasAppVersion()) {
                return versionInfo.getAppVersion();
            }
        }
        return extractVersion(filePath);
    }

    /**
     * Get just the build number
     */
    public static String getBuildNumber(String filePath) {
        if (filePath.toLowerCase().endsWith(".jar")) { //$NON-NLS-1$
            JarVersionInfo versionInfo = extractAllJarVersionInfo(filePath);
            if (versionInfo.hasBuildNumber()) {
                return versionInfo.getBuildNumber();
            }
        }
        return Messages.getString("CrossPlatformVersionReader.LOG_BUILD_NUM_NFOUND"); //$NON-NLS-1$
    }

    /**
     * Property detection methods
     */
    private static boolean isAppVersionProperty(String propName, String propValue) {
        if (propValue == null || propValue.trim().isEmpty()) return false;

        String name = propName.toLowerCase();
        String value = propValue.trim();

        boolean isAppVersionName = name.equals("app.version") || //$NON-NLS-1$
                                  name.equals("version") || //$NON-NLS-1$
                                  name.equals("application.version") || //$NON-NLS-1$
                                  name.equals("project.version"); //$NON-NLS-1$

        return isAppVersionName && isValidVersion(value);
    }

    private static boolean isBuildNumberProperty(String propName, String propValue) {
        if (propValue == null || propValue.trim().isEmpty()) return false;

        String name = propName.toLowerCase();
        String value = propValue.trim();

        boolean isBuildNumberName = name.equals("build.number") || //$NON-NLS-1$
                                   name.equals("buildnumber") || //$NON-NLS-1$
                                   name.equals("build") || //$NON-NLS-1$
                                   name.equals("build.version") || //$NON-NLS-1$
                                   name.equals("buildnum"); //$NON-NLS-1$

        // Build number can be just digits or version-like
        boolean isBuildNumberValue = value.matches("^[0-9]+$") || //$NON-NLS-1$
                                    value.matches("^[0-9]+\\.[0-9]+$") || //$NON-NLS-1$
                                    isValidVersion(value);

        return isBuildNumberName && isBuildNumberValue;
    }

    private static boolean isVersionProperty(String propName, String propValue) {
        if (propValue == null || propValue.trim().isEmpty()) return false;

        String name = propName.toLowerCase();
        String value = propValue.trim();

        boolean isVersionName = name.contains("version"); //$NON-NLS-1$
        boolean isVersionValue = isValidVersion(value);

        return isVersionName && isVersionValue;
    }

    private static boolean isValidVersion(String version) {
        if (version == null || version.trim().isEmpty()) return false;
        return version.trim().matches("^[vV]?\\d+(\\.\\d+)*([.-]?[a-zA-Z0-9]+)*$"); //$NON-NLS-1$
    }

    /**
     * Container class for JAR version information
     */
    private static class JarVersionInfo {
        private String appVersion;
        private String buildNumber;
        private Map<String, String> versionSources = new LinkedHashMap<>();

        public void setAppVersion(String appVersion) {
            this.appVersion = cleanVersionString(appVersion);
        }

        public void setBuildNumber(String buildNumber) {
            this.buildNumber = cleanVersionString(buildNumber);
        }

        public void addVersionSource(String source, String version) {
            versionSources.put(source, version);
        }

        public boolean hasAppVersion() {
            return appVersion != null && !appVersion.isEmpty();
        }

        public boolean hasBuildNumber() {
            return buildNumber != null && !buildNumber.isEmpty();
        }

        public boolean hasCombinedVersion() {
            return hasAppVersion() && hasBuildNumber();
        }

        public String getAppVersion() {
            return appVersion;
        }

        public String getBuildNumber() {
            return buildNumber;
        }

        public String getCombinedVersion() {
            if (hasCombinedVersion()) {
                return appVersion + "." + buildNumber; //$NON-NLS-1$
            }
            return null;
        }

        public Map<String, String> getAllVersionSources() {
            return new LinkedHashMap<>(versionSources);
        }

        private String cleanVersionString(String version) {
            if (version == null) return null;

            version = version.trim();

            // Remove leading 'v' or 'V'
            if (version.startsWith("v") || version.startsWith("V")) { //$NON-NLS-1$ //$NON-NLS-2$
                version = version.substring(1);
            }

            return version;
        }
    }

    // The following methods remain the same as previous implementation
    // (Windows executable, Unix executable, macOS, Debian, RPM extraction)

    private static String extractFromWindowsExecutable(String filePath) {
        if (!isWindows()) {
            return Messages.getString("CrossPlatformVersionReader.LOG_WIN_REQUIRED_TO_EXTRACT_VERSION"); //$NON-NLS-1$
        }

        try {
            ProcessBuilder pb = new ProcessBuilder(
                "powershell", "-Command", //$NON-NLS-1$ //$NON-NLS-2$
                String.format("(Get-Item '%s').VersionInfo.FileVersion", //$NON-NLS-1$
                            filePath.replace("'", "''")) //$NON-NLS-1$ //$NON-NLS-2$
            );

            Process process = pb.start();
            String output = readProcessOutput(process).trim();
            process.waitFor();

            if (!output.isEmpty() && !output.contains("Exception") && isValidVersion(output)) { //$NON-NLS-1$
                return output;
            }

        } catch (Exception e) {
            return Messages.getString("CrossPlatformVersionReader.EX_WINEX_VERSION_READ_ERROR") + e.getMessage(); //$NON-NLS-1$
        }

        return Messages.getString("CrossPlatformVersionReader.LOG_VERSION_NFOUND_IN_WINEX"); //$NON-NLS-1$
    }

    private static String extractFromUnixExecutable(String filePath) {
        if (isWindows()) {
            return Messages.getString("CrossPlatformVersionReader.LOG_UNIX_REQUIRED_TO_EXTRACT_VERSION"); //$NON-NLS-1$
        }

        if (isMacAppBundle(filePath)) {
            return extractFromMacAppBundle(filePath);
        }

        try {
            ProcessBuilder pb = new ProcessBuilder("strings", filePath); //$NON-NLS-1$
            Process process = pb.start();
            String output = readProcessOutput(process);
            process.waitFor();

            List<String> potentialVersions = findVersionsInText(output);
            if (!potentialVersions.isEmpty()) {
                return potentialVersions.get(0);
            }

        } catch (Exception e) {
            return Messages.getString("CrossPlatformVersionReader.EX_READING_UNIXEX_VERSION") + e.getMessage(); //$NON-NLS-1$
        }

        return Messages.getString("CrossPlatformVersionReader.LOG_UNIXEX_VERSION_NFOUND"); //$NON-NLS-1$
    }

    private static String extractFromMacAppBundle(String appPath) {
        if (!isMacOS()) {
            return Messages.getString("CrossPlatformVersionReader.LOG_MACOSREQUIRED_TO_EXTRACT_VERSION"); //$NON-NLS-1$
        }

        try {
            File appFile = new File(appPath);
            File infoPlist;

            if (appPath.endsWith(".app")) { //$NON-NLS-1$
                infoPlist = new File(appFile, "Contents/Info.plist"); //$NON-NLS-1$
            } else {
                infoPlist = new File(appPath);
            }

            if (infoPlist.exists()) {
                ProcessBuilder pb = new ProcessBuilder("plutil", "-p", infoPlist.getAbsolutePath()); //$NON-NLS-1$ //$NON-NLS-2$
                Process process = pb.start();
                String output = readProcessOutput(process);
                process.waitFor();

                Pattern pattern = Pattern.compile("CFBundleShortVersionString\"\\s*=>\\s*\"([^\"]+)\""); //$NON-NLS-1$
                Matcher matcher = pattern.matcher(output);
                if (matcher.find()) {
                    return matcher.group(1);
                }
            }

        } catch (Exception e) {
            return Messages.getString("CrossPlatformVersionReader.EX_MACOSEX_READ_APP_BUNDLE") + e.getMessage(); //$NON-NLS-1$
        }

        return Messages.getString("CrossPlatformVersionReader.LOG_MACOSEX_APP_BUNDLE_VERSION_NFOUND"); //$NON-NLS-1$
    }

    private static String extractFromDebPackage(String filePath) {
        if (!isLinux()) return Messages.getString("CrossPlatformVersionReader.LOG_DEBIAN_PKG_EXTRACT_REQUIRES_LINUX"); //$NON-NLS-1$
        return Messages.getString("CrossPlatformVersionReader.LOG_DEBIAN_PKG_VERSION_NFOUND"); //$NON-NLS-1$
    }

    private static String extractFromRpmPackage(String filePath) {
        if (!isLinux()) return Messages.getString("CrossPlatformVersionReader.LOG_RPM_PKG_EXTRACT_REQUIRES_LINUX"); //$NON-NLS-1$
        return Messages.getString("CrossPlatformVersionReader.LOG_RPM_PKG_VERSION_NFOUND"); //$NON-NLS-1$
    }

    private static String readProcessOutput(Process process) throws IOException {
        try (InputStream inputStream = process.getInputStream();
             InputStream errorStream = process.getErrorStream()) {

            String output = new String(inputStream.readAllBytes());
            String error = new String(errorStream.readAllBytes());

            if (!error.isEmpty()) {
                System.err.println(Messages.getString("CrossPlatformVersionReader.LOG_PROCESS_ERROR") + error); //$NON-NLS-1$
            }

            return output;
        }
    }

    private static List<String> findVersionsInText(String text) {
        List<String> versions = new ArrayList<>();
        Pattern pattern = Pattern.compile("\\b([0-9]+\\.[0-9]+(\\.[0-9]+)*([.-]?[a-zA-Z0-9]+)*)\\b"); //$NON-NLS-1$
        Matcher matcher = pattern.matcher(text);

        while (matcher.find()) {
            String version = matcher.group(1);
            if (isValidVersion(version) && version.length() > 2) {
                versions.add(version);
            }
        }

        return versions;
    }

    // Platform detection methods (same as before)
    private static boolean isUnixExecutable(File file) {
        if (!isWindows()) {
            try {
                Set<PosixFilePermission> permissions = Files.getPosixFilePermissions(file.toPath());
                return permissions.contains(PosixFilePermission.OWNER_EXECUTE) ||
                       permissions.contains(PosixFilePermission.GROUP_EXECUTE) ||
                       permissions.contains(PosixFilePermission.OTHERS_EXECUTE);
            } catch (IOException e) {
                return false;
            }
        }
        return false;
    }

    private static boolean isMacAppBundle(String path) {
        if (isMacOS()) {
            File file = new File(path);
            if (path.endsWith(".app") && file.isDirectory()) { //$NON-NLS-1$
                return new File(file, "Contents/Info.plist").exists(); //$NON-NLS-1$
            }
        }
        return false;
    }

    public static boolean isWindows() {
        return System.getProperty("os.name").toLowerCase().contains("windows"); //$NON-NLS-1$ //$NON-NLS-2$
    }

    public static boolean isMacOS() {
        return System.getProperty("os.name").toLowerCase().contains("mac"); //$NON-NLS-1$ //$NON-NLS-2$
    }

    public static boolean isLinux() {
        return System.getProperty("os.name").toLowerCase().contains("linux"); //$NON-NLS-1$ //$NON-NLS-2$
    }
}