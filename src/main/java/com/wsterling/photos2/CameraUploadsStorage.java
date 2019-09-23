package com.wsterling.photos2;

import com.google.common.collect.Lists;
import com.wsterling.util.DateUtils;
import com.wsterling.util.FileFilters;

import java.io.File;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by will on 6/2/15.
 */
public class CameraUploadsStorage implements PhotoStorage {

    private final File dir;

    public CameraUploadsStorage(File f) {
        dir = f;
    }

    @Override
    public boolean contains(Photo photo) {
        return false;
    }

    @Override
    public void put(PhotoFile photoFile) {

    }

    @Override
    public Collection<PhotoFile> getPhotoFiles(Photo photo) {
        return null;
    }


    public static List<PhotoFile> getPhotoFilesInCameraUploads(File folder) {

        if (!folder.getName().contentEquals("Camera Uploads")) {
            throw new RuntimeException("Error expecting folder of different name " + folder);
        }
        ArrayList<PhotoFile> photoFiles = Lists.newArrayList();
        int nullCount = 0;
        for (File f : folder.listFiles(FileFilters.photoFileFilter)) {
            PhotoFile photoFile = PhotoFile.create(f);
            if (photoFile != null) {
                photoFiles.add(photoFile);
            } else {
                nullCount++;
            }
        }
        System.out.println("null count: " + nullCount);
        System.out.println("return count: " + photoFiles.size());
        return photoFiles;
    }


//    public static void organizeCameraUploads(File cameraUploadDir, File monthlyDir) {
//
//        MonthlyPhotoStorage photoStorage = new MonthlyPhotoStorage(monthlyDir, true);
//        List<PhotoFile> cameraUploadFiles = getPhotoFilesInCameraUploads(cameraUploadDir);
//        System.out.println("found " + cameraUploadFiles.size() + " in camera uploads");
//        int i = 0;
//        int deleted = 0;
//        for (PhotoFile photoFile : cameraUploadFiles) {
//            if (!photoStorage.existsInTargetFolder(photoFile)) {
//                photoStorage.putWithMove(photoFile);
//                if (++i % 100 == 0) {
//                    System.out.println("added " + i);
//                }
//            } {
//                if (photoStorage.contains(photoFile) && photoFile.getFile().exists()) {
//                    photoFile.getFile().delete();
//                    if (++deleted % 100 == 0) {
//                        System.out.println("deleted " + deleted);
//                    }
//                }
//            }
//        }
//        System.out.println("done organizing camera uploads, added " + i + ", deleted " + deleted);
//    }

    public static LocalDate parseDateForCameraUpload(File file) {
        String fn = file.getName();
        if (fn.length() < 10) {
            return null;
        } else {
            return DateUtils.getLocalDate(fn.substring(0, 4), fn.substring(5, 7), fn.substring(8, 10));
        }
    }
}
