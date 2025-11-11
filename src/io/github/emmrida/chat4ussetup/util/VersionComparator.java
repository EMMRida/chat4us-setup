/*
 * Copyright (c) 2025 El Mhadder Mohamed Rida. All rights reserved.
 * This code is licensed under the [MIT License](https://opensource.org/licenses/MIT).
 */
package io.github.emmrida.chat4ussetup.util;

import java.util.List;
import java.util.ArrayList;

/**
 * Version comparator class with support for semantic versioning, pre-release labels, and build metadata
 */
public class VersionComparator {

    /**
     * Advanced version comparison with support for:
     * - Semantic versioning (1.2.3)
     * - Pre-release labels (1.2.3-alpha, 1.2.3-beta)
     * - Build metadata (1.2.3+build123)
     * - Mixed alphanumeric versions (1.2.3-rc1)
     */
    public static int compareVersions(String version1, String version2) {
        return compareVersions(version1, version2, true);
    }

    public static int compareVersions(String version1, String version2, boolean strict) {
        if (version1 == null && version2 == null) return 0;
        if (version1 == null) return -1;
        if (version2 == null) return 1;

        // Normalize versions
        version1 = normalizeVersion(version1);
        version2 = normalizeVersion(version2);

        // Parse version components
        VersionComponents v1 = parseVersion(version1);
        VersionComponents v2 = parseVersion(version2);

        // Compare numeric parts
        int maxNumericParts = Math.max(v1.numericParts.size(), v2.numericParts.size());
        for (int i = 0; i < maxNumericParts; i++) {
            int num1 = getSafeNumericPart(v1.numericParts, i);
            int num2 = getSafeNumericPart(v2.numericParts, i);

            if (num1 < num2) return -1;
            if (num1 > num2) return 1;
        }

        // If numeric parts are equal, compare pre-release labels
        int preReleaseCompare = comparePreReleaseLabels(v1.preRelease, v2.preRelease);
        if (preReleaseCompare != 0) return preReleaseCompare;

        // If still equal, compare build metadata (usually ignored in comparisons)
        if (strict) {
            return compareBuildMetadata(v1.buildMetadata, v2.buildMetadata);
        }

        return 0;
    }

    /**
     * Parse version string into components
     */
    private static VersionComponents parseVersion(String version) {
        VersionComponents components = new VersionComponents();

        // Split version and build metadata
        String[] buildSplit = version.split("\\+", 2); //$NON-NLS-1$
        String versionWithoutBuild = buildSplit[0];
        if (buildSplit.length > 1) {
            components.buildMetadata = buildSplit[1];
        }

        // Split pre-release and main version
        String[] preReleaseSplit = versionWithoutBuild.split("-", 2); //$NON-NLS-1$
        String mainVersion = preReleaseSplit[0];
        if (preReleaseSplit.length > 1) {
            components.preRelease = preReleaseSplit[1];
        }

        // Parse numeric parts
        String[] numericStrings = mainVersion.split("\\."); //$NON-NLS-1$
        for (String numStr : numericStrings) {
            try {
                components.numericParts.add(Integer.parseInt(numStr));
            } catch (NumberFormatException e) {
                components.numericParts.add(0);
            }
        }

        return components;
    }

    /**
     * Compare pre-release labels according to semantic versioning rules
     */
    private static int comparePreReleaseLabels(String preRelease1, String preRelease2) {
        // If one has pre-release and the other doesn't, the one without is greater
        if (preRelease1 == null && preRelease2 == null) return 0;
        if (preRelease1 == null) return 1;  // 1.0.0 > 1.0.0-alpha
        if (preRelease2 == null) return -1; // 1.0.0-alpha < 1.0.0

        String[] labels1 = preRelease1.split("\\."); //$NON-NLS-1$
        String[] labels2 = preRelease2.split("\\."); //$NON-NLS-1$

        int maxLength = Math.max(labels1.length, labels2.length);

        for (int i = 0; i < maxLength; i++) {
            String label1 = getSafeLabel(labels1, i);
            String label2 = getSafeLabel(labels2, i);

            int result = comparePreReleaseLabel(label1, label2);
            if (result != 0) return result;
        }

        return 0;
    }

    /**
     * Compare individual pre-release labels
     */
    private static int comparePreReleaseLabel(String label1, String label2) {
        boolean isNumeric1 = label1.matches("\\d+"); //$NON-NLS-1$
        boolean isNumeric2 = label2.matches("\\d+"); //$NON-NLS-1$

        if (isNumeric1 && isNumeric2) {
            // Both numeric - compare as numbers
            int num1 = Integer.parseInt(label1);
            int num2 = Integer.parseInt(label2);
            return Integer.compare(num1, num2);
        } else if (isNumeric1) {
            // Numeric has lower precedence than non-numeric
            return -1;
        } else if (isNumeric2) {
            // Non-numeric has higher precedence than numeric
            return 1;
        } else {
            // Both non-numeric - compare lexically
            return label1.compareTo(label2);
        }
    }

    /**
     * Compare build metadata (usually doesn't affect version precedence)
     */
    private static int compareBuildMetadata(String build1, String build2) {
        if (build1 == null && build2 == null) return 0;
        if (build1 == null) return -1;
        if (build2 == null) return 1;

        return build1.compareTo(build2);
    }

    private static int getSafeNumericPart(List<Integer> parts, int index) {
        return index < parts.size() ? parts.get(index) : 0;
    }

    private static String getSafeLabel(String[] labels, int index) {
        return index < labels.length ? labels[index] : ""; //$NON-NLS-1$
    }

    /**
     * Normalize version string
     */
    private static String normalizeVersion(String version) {
        if (version == null) return ""; //$NON-NLS-1$

        // Remove leading/trailing whitespace
        version = version.trim();

        // Remove leading 'v' if present
        if (version.startsWith("v") || version.startsWith("V")) { //$NON-NLS-1$ //$NON-NLS-2$
            version = version.substring(1);
        }

        return version;
    }

    /**
     * Version components container class
     */
    private static class VersionComponents {
        List<Integer> numericParts = new ArrayList<>();
        String preRelease;
        String buildMetadata;
    }
}