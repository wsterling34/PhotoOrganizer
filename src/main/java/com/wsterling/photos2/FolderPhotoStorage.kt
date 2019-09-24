package com.wsterling.photos2

import com.google.common.collect.ArrayListMultimap
import com.google.common.collect.Sets
import com.wsterling.util.DateUtils
import com.wsterling.util.FileFilters
import com.wsterling.util.FileUtils
import com.wsterling.util.StringUtils
import java.io.File
import java.io.FileFilter
import java.io.IOException
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.time.LocalDate

class MonthlyPhotoStorage(private val dir: File, private val dryRun: Boolean) {

    private val hashedFiles = ArrayListMultimap.create<String, ImageFile>()
    private val indexedFolders: MutableSet<File>
    private val verbose = true

    init {
        if (!dir.isDirectory) {
            throw RuntimeException("Error: not directory $dir")
        }
        indexedFolders = Sets.newHashSet()
    }

    private fun indexFolder(folder: File) {

        val t1 = System.currentTimeMillis()

        if (!folder.exists()) {
            try {
                Files.createDirectory(folder.toPath())
            } catch (e: IOException) {
                throw RuntimeException(e)
            }
        }

        if (!folder.isDirectory) {
            throw RuntimeException("Error: must be directory $folder")
        }
        var cnt = 0
        var misplaced = 0
        for (f in folder.listFiles(FileFilters.photoFileFilter)!!) {

            val photoFile = ImageFile(f)
            hashedFiles.put(photoFile.getHash(), photoFile)
            cnt++
            if (isInWrongTargetFolder(photoFile)) {
                misplaced++
            }
        }
        val elapsedSecs = (System.currentTimeMillis() - t1).toDouble() / 1000
        if (cnt > 0) println("indexed $folder $cnt files, $misplaced misplaced files, took $elapsedSecs secs")
        indexedFolders.add(folder)
    }

    fun indexTargetFolders() {
        indexFolders(targetFolderFilter)
    }

    fun indexFolders(filter: FileFilter) {
        val subDirs = dir.listFiles(FileFilters.dirFilter())
        for (subDir in subDirs!!) {
            if (subDir.isDirectory() && filter.accept(subDir)) {
                indexFolder(subDir)
            }
        }
    }

    fun isInRightTargetFolder(imageFile: ImageFile): Boolean {
        return imageFile.file.parentFile == getTargetFolder(imageFile)
    }

    fun isInWrongTargetFolder(photoFile: ImageFile): Boolean {
        val parent = photoFile.file.parentFile
        return (targetFolderFilter.accept(parent) && parent != getTargetFolder(photoFile))
    }

    fun getTargetFolder(imageFile: ImageFile): File {
        val date = imageFile.getDateTime().toLocalDate()
        return File(dir, getFolderForDate(date))
    }

    fun anotherCopyExists(imageFile: ImageFile): Boolean {
        val targetFolder = getTargetFolder(imageFile)
        if (!indexedFolders.contains(targetFolder)) {
            if (!targetFolder.exists()) {
                targetFolder.mkdirs()
            }
            indexFolder(targetFolder)
        }
        for (otherFile in hashedFiles.get(imageFile.getHash())) {
            if (otherFile != imageFile) {
                if (verbose) println("another copy of ${imageFile.file} exists - ${otherFile.file}")
                return true
            } else {
                if (verbose) println("another copy of ${imageFile.file} doesn't exist")
            }
        }
        return false
    }

    fun isInThisFolder(file: File): Boolean {
        return file.parentFile.toString().startsWith(dir.toString())
    }

    fun putUnlessDuped(photoFile: ImageFile) {
        if (anotherCopyExists(photoFile)) {
            println("put found existing copy of file " + photoFile.file + " so not putting new file")
            return
        }
        put(photoFile)
    }

    fun put(photoFile: ImageFile) {
        if (isInThisFolder(photoFile.file)) {
            throw RuntimeException("Error : tried to put a file which is already in this folder, very bad!")
        }
        val candidateFile = photoFile.file
        var targetFile = File(getTargetFolder(photoFile), photoFile.file.name)
        if (targetFile.exists()) {
            targetFile = FileUtils.getAvailableAlternateFileName(targetFile)
        }

        try {
            if (!dryRun) {
                Files.copy(candidateFile.toPath(), targetFile.toPath(), StandardCopyOption.COPY_ATTRIBUTES)
                hashedFiles.put(photoFile.getHash(), ImageFile(targetFile))
                if (verbose) println("copied $candidateFile to $targetFile")
            }
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
    }

    private fun move(imageFile: ImageFile, targetFolder: File) {
        if (!hashedFiles.get(imageFile.getHash()).contains(imageFile)) {
            throw RuntimeException("Error tried to move file that is not in this storage folder - $imageFile")
        }

        val srcFile = imageFile.file
        val targetFile = File(targetFolder, srcFile.name)
        if (targetFile.exists()) {
            throw RuntimeException("Error target file already exists - $targetFile")
            //could increment file name if necessary but seems like a weird case
        }
        try {
            if (!dryRun) {
                Files.move(srcFile.toPath(), targetFile.toPath(), StandardCopyOption.ATOMIC_MOVE)
                val removed = hashedFiles.get(imageFile.getHash()).remove(imageFile)
                if (!removed) {
                    throw RuntimeException("Error couldn't find image file in hashedFiles - $imageFile")
                }
                hashedFiles.put(imageFile.getHash(), ImageFile(targetFile))
            }
            if (verbose) println("moved $srcFile to $targetFile")
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
    }

    private fun deleteImageFile(imageFile: ImageFile) {
        val removed = hashedFiles.get(imageFile.getHash()).remove(imageFile)
        if (!removed) {
            throw RuntimeException("Failed to remove image file - $imageFile")
        }
        imageFile.file.delete()
    }

    private fun deleteImageFileSafe(imageFile: ImageFile) {
        val imageFiles = hashedFiles.get(imageFile.getHash())
        if (imageFiles.size > 1) {
            val removed = imageFiles.remove(imageFile)
            if (!removed) {
                throw RuntimeException("Failed to remove image file - $imageFile")
            }
            imageFile.file.delete()
        } else {
            throw RuntimeException("Error tried to delete image file but there doesn't appear to be a duplicate")
        }
    }

    fun findAndFixMisplacedFiles() {
        var correct = 0
        var lost = 0
        indexTargetFolders()
        for (photoFile in hashedFiles.values().toList()) {
            val f = photoFile.file
            if (isInWrongTargetFolder(photoFile)) {
                lost++
                if (anotherCopyExists(photoFile)) {
                    println("deleting ${photoFile.file} because dupe already exists in TargetFolder in storage ")
                    if (!dryRun) {
                        deleteImageFile(photoFile)
                    }
                } else {
                    val targetFolder = getTargetFolder(photoFile)
                    println("moving $f into $targetFolder")
                    if (!dryRun) {
                        move(photoFile, targetFolder)
                    }
                }
            } else {
                correct++
            }
        }
        println("$lost lost")
        println("$correct correct")
    }

    fun findAndFixDuplicates() {
        var deleted = 0
        var total = 0
        for (hash in hashedFiles.keySet()) {
            val files = hashedFiles.get(hash)
            total += files.size
            if (files.size > 1) {
                val copyOf = files.toList()
                val keeper = pickKeeper(copyOf)
                copyOf.forEachIndexed { index, imageFile ->
                    if (index != keeper) {
                        println("deleting " + imageFile.file + ", keeping original " + copyOf[keeper].file)
                        deleted++
                        if (!dryRun) {
                            deleteImageFileSafe(imageFile)
                        }
                    }
                }
            }
        }
        println("$total total files found, $deleted deleted dupes")
    }

    private fun pickKeeper(imageFiles: List<ImageFile>): Int {
        imageFiles.forEachIndexed { index, imageFile ->
            if (isInRightTargetFolder(imageFile)) return index
        }
        imageFiles.forEachIndexed { index, imageFile ->
            if (!imageFile.file.toString().contains("sandy_cam"))
                return index
        }
        //just use first
        return 0
    }

    fun containsDuplicate(photoFile: ImageFile): Boolean {
        val filesForHash = hashedFiles.get(photoFile.getHash())
        if (filesForHash == null || filesForHash.size == 0) {
            return false
        }
        filesForHash.forEach {
            if (it != photoFile) return true
        }
        return false
    }
}

private fun getFolderForDate(date: LocalDate): String {
    val year = Integer.toString(date.year)
    val abbrev = DateUtils.monthToAbbrev(date.monthValue)
    return "$year.$abbrev"
}

private val targetFolderFilter = object: FileFilter {
    override fun accept(folder: File?): Boolean {
        if (folder == null) return false
        if (!folder.isDirectory) return false
        val subFolderName = folder.name
//        val split = subFolderName
        val split = subFolderName.split("\\.".toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray()
        if (split.size != 2) {
            return false
        }
        if (!StringUtils.isNumeric(split[0])) {
            return false
        }
        val year = Integer.parseInt(split[0])
        if (year < 1970 || year > 2050) {
            return false
        }
        if (DateUtils.abbrevToMonth(split[1]) == -1) {
            return false
        }
        return true
    }
}


fun main(args: Array<String>) {

    val storage = MonthlyPhotoStorage(File("/Users/will/Dropbox/Photos"), true)
//    val storage = MonthlyPhotoStorage(File("/Volumes/Photos"), true)
    storage.indexFolders(FileFilter {true})
    storage.findAndFixDuplicates()
}
