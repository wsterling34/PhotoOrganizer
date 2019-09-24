package com.wsterling.photos2

import com.drew.imaging.ImageMetadataReader
import com.drew.imaging.ImageProcessingException
import java.io.File
import com.drew.metadata.exif.ExifSubIFDDirectory


fun main() {

  val testDir = File("/Users/will/Dropbox/Photos/test")

  testDir.listFiles().forEach { file ->
    try {
      val metadata = ImageMetadataReader.readMetadata(file)
      println(file)
      metadata.directories.forEach {dir ->
        println(dir)
        dir.tags.forEach {
          println("  " + it)
        }
      }
    } catch (e: ImageProcessingException) {
      println("$file $e")
    }
  }
}
