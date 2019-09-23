package com.wsterling.util;

import com.google.common.collect.Sets;
import com.wsterling.photos2.FolderParsers;

import java.io.FileFilter;
import java.util.Set;

public class FileFilters {

    static FileFilter all() {
        return file -> true;
    }

    public static final Set<String> photoExtensions = Sets.newHashSet("jpg", "png", "mov", "raw", "mts", "arw", "heic");

    public static FileFilter startsWithFilter(final String s) {
        return file -> file.getName().startsWith(s);
    }

    public static FileFilter notDirectory() {
        return file -> !file.isDirectory();
    }

    public static FileFilter dirFilter() {
        return file -> file.isDirectory();
    }

    public static final FileFilter photoFileFilter =
            file -> {
                if (file.getName().startsWith(".") || file.isDirectory()) {
                    return false;
                }
                String[] tokenedName = file.getName().split("\\.");
                if (tokenedName.length < 2) {
                    return false;
                }
                String ext = tokenedName[tokenedName.length-1].toLowerCase();
                if (photoExtensions.contains(ext)) {
                    return true;
                } else {
                    return false;
                }
            };

    public static FileFilter getPhotoFolderFilter() {
        return pathname -> {
            if (FolderParsers.getFolderBasedDate(pathname) != null) {
                return true;
            } else {
                return false;
            }
        };
    }
}
