package com.wsterling.photos2;

import com.google.common.collect.Lists;
import com.wsterling.util.FileFilters;
import com.wsterling.util.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PhotoOrganizer {

	public static List<File> findLegacyPhotoFolders(File folder) {
		ArrayList<File> ret = Lists.newArrayList();
		FileUtils.find(ret, FileFilters.getPhotoFolderFilter(), folder, true);
		System.out.println("found " + ret.size() + " legacy folders");
		return ret;
	}

	public static List<PhotoFile> getHashedFilesInLegacyFolders(File folder) {

		List<File> subFolders = findLegacyPhotoFolders(folder);
		List<PhotoFile> ret = Lists.newArrayList();
		for (File subFolder : subFolders) {
			ArrayList<File> files = Lists.newArrayList();
			FileUtils.find(files, FileFilters.photoFileFilter, subFolder, true);
			for (File candidate : files) {
				ret.add(PhotoFile.create(candidate));
			}
		}
		return ret;
	}

	public static List<PhotoFile> getAllHashedPhotoFiles(File folder) {

		ArrayList<File> files = Lists.newArrayList();
		FileUtils.find(files, FileFilters.photoFileFilter, folder, true);
		ArrayList<PhotoFile> photoFiles = Lists.newArrayList();
		for (File f : files) {
			photoFiles.add(PhotoFile.create(f));
		}
		return photoFiles;
	}

	public static void deleteIfInStorage(List<PhotoFile> photoFiles, PhotoStorage photoStorage) {
		for (PhotoFile pf : photoFiles) {
			if (photoStorage.contains(pf)) {
				System.out.println("deleting " + pf.getFile());
				pf.getFile().delete();
			}
		}
	}

	public static void main(String[] args) throws IOException {

//		File userDir = new File("/Users/will/");
////		File userDir = new File("/Volumes/Macintosh HD-1/Users/sandy");
//		File monthlyDir = new File(userDir, "Dropbox (Personal)/Photos");
//		File cameraUploadDir = new File(userDir, "Dropbox (Personal)/Camera Uploads/");
//
//		organizeCameraUploads(cameraUploadDir, monthlyDir);
	}
}