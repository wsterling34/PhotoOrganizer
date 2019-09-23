package com.wsterling.photos2

import java.io.File

fun main() {

    val storage = MonthlyPhotoStorage(File("/Users/will/Dropbox/Photos"), true)
//    storage.indexFolders(FileFilter { true })
//    storage.findAndFixDuplicates()
    println(storage.isInThisFolder(File("/Users/will/Dropbox/Photos/2004.apr")))
    println(storage.isInThisFolder(File("/Users/will")))
    println(storage.isInThisFolder(File("/Users/will/Music")))
}