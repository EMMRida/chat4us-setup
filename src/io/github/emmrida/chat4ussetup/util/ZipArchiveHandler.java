/*
 * Copyright (c) 2025 El Mhadder Mohamed Rida. All rights reserved.
 * This code is licensed under the [MIT License](https://opensource.org/licenses/MIT).
 */
package io.github.emmrida.chat4ussetup.util;

import java.io.*;
import java.nio.file.*;
import java.util.zip.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for handling zip archives
 */
public class ZipArchiveHandler {
	private final static int BUFFER_SIZE = 1024;

    private ProgressListener progressListener = null;

    private String zipFilePath;

    /**
     * Constructs a new ZipArchiveHandler with the specified zip file path
     * @param zipFilePath
     */
    public ZipArchiveHandler(String zipFilePath) {
        this.zipFilePath = zipFilePath;
    }

	/**
	 * Returns the zip file path
	 * @return The zip file path
	 */
    public String getZipFilePath() { return zipFilePath; }

    /**
     * Extracts the entire zip archive to the specified destination folder
     *
	 * @param destFolderPath The destination folder path
	 * @return true if successful
     */
    public boolean extractTo(String destFolderPath) {
        try {
            Path destPath = Paths.get(destFolderPath);
            Files.createDirectories(destPath);

            try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFilePath))) {
                ZipEntry entry;
                byte[] buffer = new byte[BUFFER_SIZE];

                while ((entry = zis.getNextEntry()) != null) {
                    Path filePath = destPath.resolve(entry.getName());

                    // Create parent directories if they don't exist
                    Files.createDirectories(filePath.getParent());

                    if (!entry.isDirectory()) {
                        try (FileOutputStream fos = new FileOutputStream(filePath.toFile())) {
                            int length;
                            if(progressListener != null)
                            	progressListener.onFileCreated(entry.getName());
                            while ((length = zis.read(buffer)) > 0) {
                                fos.write(buffer, 0, length);
								if(progressListener != null)
									progressListener.onProgress(length);
                            }
                        }
                    } else {
                        Files.createDirectories(filePath);
                    }
                    zis.closeEntry();
                }
            }
            return true;
        } catch (IOException e) {
            System.err.println(Messages.getString("ZipArchiveHandler.EX_EXTRACT_ZIP") + e.getMessage()); //$NON-NLS-1$
            return false;
        }
    }

    /**
     * Extracts a specific file from the zip archive
     *
     * @param fileInZip The file name in the zip archive
	 * @param destFilePath The destination file path
	 * @return true if successful
     */
    public boolean extractFile(String fileInZip, String destFilePath) {
        try {
            try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFilePath))) {
                ZipEntry entry;
                byte[] buffer = new byte[BUFFER_SIZE];

                while ((entry = zis.getNextEntry()) != null) {
                    if (entry.getName().equals(fileInZip)) {
                        try (FileOutputStream fos = new FileOutputStream(destFilePath)) {
                            int length;
                            if(progressListener != null)
                            	progressListener.onFileCreated(entry.getName());
                            while ((length = zis.read(buffer)) > 0) {
                                fos.write(buffer, 0, length);
								if(progressListener != null)
									progressListener.onProgress(length);
                            }
                        }
                        zis.closeEntry();
                        return true;
                    }
                    zis.closeEntry();
                }
            }
            System.err.println(Messages.getString("ZipArchiveHandler.EX_ZIP_FILE_NFOUND") + fileInZip); //$NON-NLS-1$
            return false;
        } catch (IOException e) {
            System.err.println(Messages.getString("ZipArchiveHandler.EX_ZIP_EXTRACT_FILE") + e.getMessage()); //$NON-NLS-1$
            return false;
        }
    }

    /**
     * Calculates total deflated (compressed) and extracted (uncompressed) sizes
     *
	 * @return SizeInfo object containing file count, compressed size, uncompressed size, and compression ratio
	 */
    public SizeInfo calculateSizes() {
        long compressedSize = 0;
        long uncompressedSize = 0;
        int fileCount = 0;
        try (ZipFile zipFile = new ZipFile(zipFilePath)) {
            var entries = zipFile.entries();

            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                if (!entry.isDirectory()) {
                    compressedSize += entry.getCompressedSize();
                    uncompressedSize += entry.getSize();
                    fileCount++;
                }
            }
        } catch (IOException e) {
            System.err.println(Messages.getString("ZipArchiveHandler.EX_ZIP_SIZES_CALCS") + e.getMessage()); //$NON-NLS-1$
        }

        return new SizeInfo(compressedSize, uncompressedSize, fileCount);
    }

    /**
     * Tests the zip archive for corruption by trying to read all entries
     *
	 * @return true if successful
     */
    public boolean testArchive() {
        try (ZipFile zipFile = new ZipFile(zipFilePath)) {
            var entries = zipFile.entries();

            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                try (InputStream is = zipFile.getInputStream(entry)) {
                	int length;
                    // Try to read the entry to test for corruption
                    byte[] buffer = new byte[BUFFER_SIZE];
                    while ((length = is.read(buffer)) > 0) {
                        // Just reading to test integrity
						if(progressListener != null)
							progressListener.onProgress(length);
                    }
                }
            }
            return true;
        } catch (IOException e) {
            System.err.println(Messages.getString("ZipArchiveHandler.EX_CORRUPTED_ZIP_FILE") + e.getMessage()); //$NON-NLS-1$
            return false;
        }
    }

    /**
     * Lists all files in the zip archive
     *
	 * @return List of file names in the archive
     */
    public List<String> listFiles() {
        List<String> fileList = new ArrayList<>();

        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFilePath))) {
            ZipEntry entry;

            while ((entry = zis.getNextEntry()) != null) {
                fileList.add(entry.getName());
                zis.closeEntry();
            }
        } catch (IOException e) {
            System.err.println(Messages.getString("ZipArchiveHandler.EX_ZIP_LISTING_FILES") + e.getMessage()); //$NON-NLS-1$
        }

        return fileList;
    }

    /**
     * Gets information about all entries in the zip file
     *
     * @return List of ZipEntryInfo objects
     */
    public List<ZipEntryInfo> getDetailedFileList() {
        List<ZipEntryInfo> entryList = new ArrayList<>();

        try (ZipFile zipFile = new ZipFile(zipFilePath)) {
            var entries = zipFile.entries();

            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                entryList.add(new ZipEntryInfo(
                    entry.getName(),
                    entry.getSize(),
                    entry.getCompressedSize(),
                    entry.getMethod(),
                    entry.getTime()
                ));
            }
        } catch (IOException e) {
            System.err.println(Messages.getString("ZipArchiveHandler.EX_GET_ZIP_FILES_LIST") + e.getMessage()); //$NON-NLS-1$
        }

        return entryList;
    }

    /**
     * Creates a new zip archive from a source folder
     *
	 * @return true if successful
     */
    public boolean createZip(String sourceFolderPath) {
        try {
            Path sourcePath = Paths.get(sourceFolderPath);
            try(ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFilePath))) {
                Files.walk(sourcePath)
                    .filter(path -> !Files.isDirectory(path))
                    .forEach(path -> {
                        try {
                            String zipEntryName = sourcePath.relativize(path).toString().replace("\\", "/"); //$NON-NLS-1$ //$NON-NLS-2$
                            ZipEntry entry = new ZipEntry(zipEntryName);
                            zos.putNextEntry(entry);
                            //Files.copy(path, zos);
                            if((progressListener != null))
								progressListener.onFileCreated(zipEntryName);
                            try(InputStream is = Files.newInputStream(path)) {
                            	int length;
	                            byte[] buffer = new byte[BUFFER_SIZE];
	                            while ((length = is.read(buffer)) > 0) {
	                            	zos.write(buffer, 0, length);
	                            	if(progressListener != null)
	                            		progressListener.onProgress(length);
	                            }
                            }
                            zos.closeEntry();
                        } catch (IOException e) {
                            throw new UncheckedIOException(e);
                        }
                    });
            }
            return true;
        } catch (IOException e) {
            System.err.println(Messages.getString("ZipArchiveHandler.EX_ZIP_FILE_CREATION") + e.getMessage()); //$NON-NLS-1$
            return false;
        }
    }

    /**
     * Sets the progress listener
     * @param listener A listener for progress updates.
     */
    public void setProgressListener(ProgressListener listener) {
		progressListener = listener;
	}

	// Interface for progress updates
    public static interface ProgressListener {
		void onProgress(int blockSize);
		void onFileCreated(String name);
    }

    // Inner class to hold size information
    public static class SizeInfo {
        public final long compressedSize;
        public final long uncompressedSize;
        public final int fileCount;
        public final double compressionRatio;

        public SizeInfo(long compressedSize, long uncompressedSize, int fileCount) {
            this.compressedSize = compressedSize;
            this.uncompressedSize = uncompressedSize;
            this.fileCount = fileCount;
            this.compressionRatio = uncompressedSize > 0 ?
                (1 - (double)compressedSize / uncompressedSize) * 100 : 0;
        }

        @Override
        public String toString() {
            return String.format(
                Messages.getString("ZipArchiveHandler.SIZE_INFO_TO_STRING"), //$NON-NLS-1$
                fileCount, compressedSize, uncompressedSize, compressionRatio
            );
        }
    }

    // Inner class to hold detailed zip entry information
    public static class ZipEntryInfo {
        public final String name;
        public final long size;
        public final long compressedSize;
        public final int compressionMethod;
        public final long lastModifiedTime;

        public ZipEntryInfo(String name, long size, long compressedSize, int compressionMethod, long lastModifiedTime) {
            this.name = name;
            this.size = size;
            this.compressedSize = compressedSize;
            this.compressionMethod = compressionMethod;
            this.lastModifiedTime = lastModifiedTime;
        }

        @Override
        public String toString() {
            return String.format(
                Messages.getString("ZipArchiveHandler.ZIP_ENTRY_INFO_TO_STRING"), //$NON-NLS-1$
                name, size, compressedSize, compressionMethod, lastModifiedTime
            );
        }
    }
}