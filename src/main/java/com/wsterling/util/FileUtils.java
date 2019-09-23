package com.wsterling.util;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.primitives.Ints;

public class FileUtils {

    private static final int inputStreamBufferSize = 1024 * 1024;
    private static final int readByteArraySize = 8192;
    private static final String hashAlgorithm = "MD5";

    private static final MessageDigest md;

    static {
        try {
            md = MessageDigest.getInstance(hashAlgorithm);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("cannot initialize hash function", e);
        }
    }

    public static String hashFile(final File file) {
        return hashFile(file, Long.MAX_VALUE);
    }

    public static String hashFile(final File file, long maxBytesToHash)  {
        try {
            InputStream is = new BufferedInputStream(new FileInputStream(file), inputStreamBufferSize);
            byte[] bytes = new byte[readByteArraySize];
            int read;
            long totalRead = 0;
            while ((read = is.read(bytes)) != -1) {
                totalRead += read;
                if (totalRead > maxBytesToHash) {
                    int trunRead = read - ((int) (totalRead - maxBytesToHash));
                    md.update(bytes, 0, trunRead);
                    break;
                } else {
                    md.update(bytes, 0, read);
                }
            }
            byte[] hashBytes = md.digest();
            is.close();
            return BinaryUtils.encodeAsString(hashBytes);
        } catch(IOException e) {
            throw new RuntimeException("IO Exception with file " + file);
        }
    }

    public static File getAvailableAlternateFileName(File f) {
        String oldName = f.getName();
        int indexOfLastDot = oldName.lastIndexOf(".");

        String ext;
        String fileNameWithoutExt = "";

        if (indexOfLastDot == -1) {
            fileNameWithoutExt = oldName;
            ext = "";
        }
        else {
            fileNameWithoutExt = oldName.substring(0, indexOfLastDot);
            ext = oldName.substring(indexOfLastDot, oldName.length());
        }

        int len = fileNameWithoutExt.length();

        String newBaseFileName = fileNameWithoutExt;
        int versionNum = 1;

        if (fileNameWithoutExt.charAt(len-2) == '_') {
            Integer oldNum = Ints.tryParse(fileNameWithoutExt.substring(len-1));
            if (oldNum != null) {
                newBaseFileName = fileNameWithoutExt.substring(0, len-2);
                versionNum = oldNum + 1;
            }
        }
        else if (fileNameWithoutExt.charAt(len-3) == '_') {
            Integer oldNum = Ints.tryParse(fileNameWithoutExt.substring(len-2));
            if (oldNum != null) {
                newBaseFileName = fileNameWithoutExt.substring(0, len-3);
                versionNum = oldNum + 1;
            }
        }

        File testFile;
        do {
            testFile = new File(f.getParentFile(), newBaseFileName + "_" + (versionNum++) + ext);
        } while (testFile.exists());
        return testFile;
    }

    public static void safeCopyPreserveMeta(File src, File dest) {

        if (!src.exists()) {
            throw new RuntimeException("Error src does not exist " + src);
        }
        if (dest.exists()) {
            throw new RuntimeException("Error dest already existsInTargetFolder " + dest);
        }
        try {
            Files.copy(src.toPath(), dest.toPath(), StandardCopyOption.COPY_ATTRIBUTES);
            if (src.lastModified() != dest.lastModified()) {
                throw new RuntimeException("Error: modified time not equal");
            }
            BasicFileAttributes srcAttributes = Files.readAttributes(src.toPath(), BasicFileAttributes.class);
            BasicFileAttributes destAttributes = Files.readAttributes(dest.toPath(), BasicFileAttributes.class);
            if (!srcAttributes.creationTime().equals(destAttributes.creationTime())) {
                throw new RuntimeException("Error: creation times not equal " + src + " " + dest);
            }
        } catch (IOException e) {
            if (dest.exists()) {
                dest.delete();
            }
            throw new RuntimeException(e);
        }
    }

    public static void find(List<File> matches, FileFilter filter, File directory, boolean recursive) {
        int count = 0;
        for (File child : directory.listFiles()) {
            if (filter.accept(child)) {
                matches.add(child);
                count++;
            } else if (recursive && child.isDirectory()) {
                find(matches, filter, child, true);
            }
        }
        if (count > 0) {
            System.out.println(count + " files found in " + directory);
        }
    }

    public static Multimap<String, File> getHashedFileMap(List<File> files) {
        Multimap<String, File> hashedFiles = ArrayListMultimap.create();
        for (File f : files) {
            hashedFiles.put(hashFile(f), f);
        }
        return hashedFiles;
    }

    public static Multimap<String, File> getHashedPhotoFiles(File dir) {
        ArrayList<File> files = Lists.newArrayList();
        find(files, FileFilters.photoFileFilter, dir, true);
        return getHashedFileMap(files);
    }

    public static void findAndDeleteDupes(File folder, boolean dryRun) {
        //TODO just deletes the last one (of the dupes)...so if there are more than two of a given file, just one gets deleted
        if (!folder.exists()) {
            throw new RuntimeException("folder not found " + folder);
        }
        long t1 = System.nanoTime();
        Multimap<String, File> hashedFileMap = getHashedPhotoFiles(folder);
        long elapsedMicros = (System.nanoTime() - t1)/1000;
        System.out.println("total took " + elapsedMicros + " micros");
        int dupes = 0;
        for (String key : hashedFileMap.keySet()) {
            Collection<File> files = hashedFileMap.get(key);
            if (files.size() > 1) {
                dupes += (files.size() - 1);
                String printStr = "";
                File lastFile = null;
                for (File f : files) {
                    printStr += (f + " ");
//					if (lastFile == null || !lastFile.getParentFile().getName().contains("misc")) {
                    lastFile = f;
//					}
                }
                System.out.println(printStr);
                if (dryRun) {
                    System.out.println(" > would have deleted " + lastFile);
                } else {
                    lastFile.delete();
                    System.out.println(" > deleted " + lastFile);
                }
            }
        }
        System.out.println("total dupes " + dupes);
        System.out.println("total files " + hashedFileMap.values().size());
    }


    public static void main(String[] args) throws InterruptedException {

        File monthlyDir = new File("/Volumes/Macintosh HD-1/Users/sandy/Dropbox (Sterling)/Photos");
//		File monthlyDir = new File("/Users/will/Dropbox (Sterling)/Photos");
        findAndDeleteDupes(monthlyDir, true);
    }

}
