package com.wsterling.photos2;

/**
public class MonthlyPhotoStorage implements PhotoStorage {

	private final File dir;
	private Multimap<String, ImageFile> hashedFiles = ArrayListMultimap.create();
	private Set<File> indexedFolders;
	private boolean verbose = true;
	private final boolean dryRun;

	public MonthlyPhotoStorage(File dir, boolean dryRun) {
		if (!dir.isDirectory()) {
			throw new RuntimeException("Error: not directory " + dir);
		}
		this.dir = dir;
		this.dryRun = dryRun;
		indexedFolders = Sets.newHashSet();
	}

	private void indexFolder(File folder) {

		long t1 = System.currentTimeMillis();
		if (!folder.exists()) {
			try {
				Files.createDirectory(folder.toPath());
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}

		if (!folder.isDirectory()) {
			throw new RuntimeException("Error: must be directory " + folder);
		}
		int cnt = 0;
		int misplaced = 0;
		for (File f : folder.listFiles(FileFilters.photoFileFilter)) {
			PhotoFile photoFile = PhotoFile.create(f);
			hashedFiles.put(photoFile.getHash(), photoFile);
			cnt++;
			if (isInWrongTargetFolder(photoFile)) {
				misplaced++;
			}
		}
		double elapsedSecs = ((double) (System.currentTimeMillis() - t1) / 1000);
		System.out.println("indexed " + folder + " " + cnt + " files, skipped " + misplaced + " misplaced files, took " + elapsedSecs + " secs");
		indexedFolders.add(folder);
	}

	public void indexTargetFolders() {
		indexFolders(targetFolderFilter);
	}

	public void indexFolders(FileFilter filter) {
		File[] subDirs = dir.listFiles(FileFilters.dirFilter());
		for (File subDir : subDirs) {
			if (filter.accept(subDir)) {
				indexFolder(subDir);
			}
		}
	}

	public boolean isInRightTargetFolder(PhotoFile photoFile) {
		return photoFile.getFile().getParentFile().equals(getTargetFolder(photoFile));
	}

	public boolean isInWrongTargetFolder(PhotoFile photoFile) {
		File parent = photoFile.getFile().getParentFile();
		if (targetFolderFilter.accept(parent) && !parent.equals(getTargetFolder(photoFile))) {
			return true;
		} else {
			return false;
		}
	}

	public File getTargetFolder(Photo photo) {
		LocalDate date = photo.getDateTime().toLocalDate();
		return new File(dir, getFolderForDate(date));
	}

	public static String getFolderForDate(LocalDate date) {
		String year = Integer.toString(date.getYear());
		String abbrev = DateUtils.monthToAbbrev(date.getMonthValue());
		return year + "." + abbrev;
	}

	public static FileFilter targetFolderFilter =
			folder -> {
				if (!folder.isDirectory()) return false;
				String subFolderName = folder.getName();
				String[] split = subFolderName.split("\\.");
				if (split.length != 2) {
					return false;
				}
				if (!StringUtils.isNumeric(split[0])) {
					return false;
				}
				int year = Integer.parseInt(split[0]);
				if (year < 1970 || year > 2050) {
					return false;
				}
				if (DateUtils.abbrevToMonth(split[1]) == -1) {
					return false;
				}
				return true;
			};

	public boolean anotherCopyExists(PhotoFile photoFile) {
		File targetFolder = getTargetFolder(photoFile);
		if (!indexedFolders.contains(targetFolder)) {
			if (!targetFolder.exists()) {
				targetFolder.mkdirs();
			}
			indexFolder(targetFolder);
		}
		for (PhotoFile otherFile: hashedFiles.get(photoFile.getHash())) {
			if (!otherFile.getFile().equals(photoFile.getFile())) {
				return true;
			}
		}
		return false;
	}

	private boolean isInThisFolder(File file) {
		return file.getParentFile().toString().startsWith(dir.toString());
	}

	@Override
	public void put(PhotoFile photoFile) {
		if (isInThisFolder(photoFile.getFile())) {
			throw new RuntimeException("Error : tried to put a file which is already in this folder, very bad!");
		}
		if (anotherCopyExists(photoFile)) {
			System.out.println("put found existing copy of file " + photoFile.getFile() + " so not putting new file");
			return;
		}
		File candidateFile = photoFile.getFile();
		File targetFile = new File(getTargetFolder(photoFile), photoFile.getFile().getName());
		if (targetFile.exists()) {
			targetFile = FileUtils.getAvailableAlternateFileName(targetFile);
		}

		try {
			if (!dryRun) {
				Files.copy(candidateFile.toPath(), targetFile.toPath(), StandardCopyOption.COPY_ATTRIBUTES);
				hashedFiles.put(photoFile.getHash(), PhotoFile.create(targetFile));
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		if (verbose) System.out.println("copied " + candidateFile + " to " + targetFile);
	}

	private void move(PhotoFile photoFile, File targetFolder) {
		if (!isInThisFolder(targetFolder)) {
			throw new RuntimeException("Error tried to move file to targetFolder that is not in this storage folder - " + targetFolder);
		}

		File srcFile = photoFile.getFile();
		File targetFile = new File(targetFolder, srcFile.getName());
		if (targetFile.exists()) {
			throw new RuntimeException("Error target file already exists - " + targetFile);
			//could increment file name if necessary but seems like a weird case
		}
		try {
			if (!dryRun) {
					Files.move(srcFile.toPath(), targetFile.toPath(), StandardCopyOption.ATOMIC_MOVE);
					hashedFiles.put(photoFile.getHash(), PhotoFile.create(targetFile));
				}
			} catch (IOException e) {
				throw new RuntimeException(e);
			}

	}

//	public void putWithMove(PhotoFile photoFile) {
//
//		if (!isInRightTargetFolder(photoFile)) {
//			throw new RuntimeException("Error tried to put/move a file that is not lost - " + photoFile.getFile());
//		}
//		File candidateFile = photoFile.getFile();
//
//		if (existsInTargetFolder(photoFile)) {
//			PhotoFile firstExisting = getPhotoFiles(photoFile).iterator().next();
//			if (verbose) System.out.println("already existsInTargetFolder based on hash, candidate: " + candidateFile + ", existing: " + firstExisting.getFile());
//			return;
//		} else {
//			File targetFile = new File(getTargetFolder(photoFile), photoFile.getFile().getName());
//			if (targetFile.exists()) {
//				targetFile = FileUtils.getAvailableAlternateFileName(targetFile);
//			}
//
//			try {
//				if (!dryRun) {
//					Files.move(candidateFile.toPath(), targetFile.toPath(), StandardCopyOption.ATOMIC_MOVE);
//					hashedFiles.put(photoFile.getHash(), PhotoFile.create(targetFile));
//				}
//			} catch (IOException e) {
//				throw new RuntimeException(e);
//			}
//			if (verbose) System.out.println("moved " + candidateFile + " to " + targetFile);
//		}
//	}

	public void findAndFixMisplacedFiles() {
		int correct = 0;
		int lost = 0;
		indexTargetFolders();
		for (PhotoFile photoFile : hashedFiles.values()) {
			File f = photoFile.getFile();
			if (isInWrongTargetFolder(photoFile)) {
				lost++;
				if (existsInTargetFolder(photoFile)) {
					System.out.println("deleting " + photoFile.getFile() +
							" because dupe already existsInTargetFolder in storage " + getPhotoFiles(photoFile).iterator().next().getFile());
					if (!dryRun) {
						f.delete();
					}
				} else {
					System.out.println("moving " + f + " into " + getTargetFolder(photoFile));
					if (!dryRun) {
						putWithMove(photoFile);
					}
				}
			} else {
				correct++;
			}
//			if ((lost + correct) % 1000 == 0) {
//				System.out.println(lost + " lost");
//				System.out.println(correct + " correct");
//			}
		}
		System.out.println(lost + " lost");
		System.out.println(correct + " correct");
	}

	public void findAndFixDuplicates() {
		int deleted = 0;
		for (String hash : hashedFiles.keySet()) {
			Collection<PhotoFile> files = hashedFiles.get(hash);
			if (files.size() > 1) {
				Iterator<PhotoFile> iterator = files.iterator();
				final PhotoFile first = iterator.next();
				while (iterator.hasNext()) {
					PhotoFile pf = iterator.next();
					System.out.println("deleting " + pf.getFile() + ", keeping original " + first.getFile());
					deleted++;
					if (!dryRun) {
						pf.getFile().delete();
					}
				}
			}
		}
		System.out.println(deleted + " deleted");
	}

	@Override
	public boolean contains(Photo photo) {
		File targetFolder = getTargetFolder(photo);
		if (!indexedFolders.contains(targetFolder)) {
			if (!targetFolder.exists()) {
				targetFolder.mkdirs();
			}
			indexFolder(targetFolder);
		}
		return hashedFiles.containsKey(photo.getHash());
	}

	public boolean contains(PhotoFile photoFile) {
		Collection<PhotoFile> filesForHash = hashedFiles.get(photoFile.getHash());
		if (filesForHash == null || filesForHash.size() == 0) {
			return false;
		}


	}

	@Override
	public Collection<PhotoFile> getPhotoFiles(Photo photo) {
		if (hashedFiles.containsKey(photo.getHash())) {
			return hashedFiles.get(photo.getHash());
		} else {
			throw new RuntimeException("Error tried to get a photo which doesn't exist in storage, hash: " + photo.getHash());
		}
	}

	public static void main(String[] args) {

		MonthlyPhotoStorage storage = new MonthlyPhotoStorage(new File("/Users/will/Dropbox/Photos"), true);
		storage.findAndFixMisplacedFiles();
	}
}
**/