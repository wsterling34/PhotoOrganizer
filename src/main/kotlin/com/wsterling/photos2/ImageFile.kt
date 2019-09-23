package com.wsterling.photos2

import com.wsterling.util.FileUtils
import org.apache.commons.imaging.Imaging
import org.apache.commons.imaging.common.ImageMetadata
import org.apache.commons.imaging.formats.jpeg.JpegImageMetadata
import org.apache.commons.imaging.formats.tiff.TiffImageMetadata
import org.apache.commons.imaging.formats.tiff.constants.TiffTagConstants
import java.io.File
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val formatter = DateTimeFormatter.ofPattern("yyyy:MM:dd HH:mm:ss")

private fun getModDateTime(file: File): LocalDateTime {
    val instant = Instant.ofEpochMilli(file.lastModified())
    return LocalDateTime.ofInstant(instant, ZoneId.systemDefault())
}

data class ImageFile(val file: File) {

    private var dateTime: LocalDateTime? = null
    private var cashedHash: String? = null

    fun getDateTime(): LocalDateTime {

        if (dateTime == null) {
            try {
                val metadata: ImageMetadata
                metadata = Imaging.getMetadata(file)
                if (metadata is JpegImageMetadata) {
                    val timeField = metadata.findEXIFValueWithExactMatch(TiffTagConstants.TIFF_TAG_DATE_TIME)
                    val strDate = timeField!!.stringValue
                    dateTime = LocalDateTime.parse(strDate!!, formatter)
                } else if (metadata is TiffImageMetadata) {
                    val timeField = metadata.findField(TiffTagConstants.TIFF_TAG_DATE_TIME)
                    val strDate = timeField.stringValue
                    dateTime = LocalDateTime.parse(strDate!!, formatter)
                }
            } catch (e: Exception) {
                //				System.out.println("couldn't find metadata " + file);
            }

            if (dateTime == null) {
                dateTime = getModDateTime(file)
            }
        }
        return dateTime!!
    }

    fun getHash(): String {
        if (cashedHash == null) {
            println("WARNING using incompete hash for speed, not suitable if any changes will be made!")
            cashedHash = FileUtils.hashFile(file, 100000)
        }
        return cashedHash!!
    }
}

fun main() {

    val f1 = File("/Users/will/Dropbox/Photos/2001/baby1.JPG")

    val if1 = ImageFile(f1)
    println(if1.getHash())

    val f2 = File("/Users/will/Dropbox/Photos/2001/baby1.JPG")
    val if2 = ImageFile(f2)
    println(if1 == if2)
}