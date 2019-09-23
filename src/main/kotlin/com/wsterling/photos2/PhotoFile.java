package com.wsterling.photos2;

import com.wsterling.util.FileFilters;
import com.wsterling.util.FileUtils;
import org.apache.commons.imaging.Imaging;
import org.apache.commons.imaging.common.ImageMetadata;
import org.apache.commons.imaging.formats.jpeg.JpegImageMetadata;
import org.apache.commons.imaging.formats.tiff.TiffField;
import org.apache.commons.imaging.formats.tiff.TiffImageMetadata;
import org.apache.commons.imaging.formats.tiff.constants.TiffTagConstants;

import java.io.File;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class PhotoFile implements Photo {

	private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy:MM:dd HH:mm:ss");
	private final File file;
	private String hash = null;
	private LocalDateTime dateTime;

	private PhotoFile(File file) {
		this.file = file;
	}

	public File getFile() {
		return file;
	}
	
	public LocalDateTime getModDateTime() {
		Instant instant = Instant.ofEpochMilli(file.lastModified());
		LocalDateTime ldt = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
		return ldt;
	}

	public LocalDateTime getDateTime() {

		if (dateTime == null) {
			try {
				final ImageMetadata metadata;
				metadata = Imaging.getMetadata(file);
				if ((metadata instanceof JpegImageMetadata)) {
					final JpegImageMetadata jpegMetadata = (JpegImageMetadata) metadata;
					final TiffField timeField = jpegMetadata.findEXIFValueWithExactMatch(TiffTagConstants.TIFF_TAG_DATE_TIME);
					final String strDate = timeField.getStringValue();
					dateTime = LocalDateTime.parse(strDate, formatter);
				} else if (metadata instanceof TiffImageMetadata) {
					final TiffImageMetadata tiffMetadata = (TiffImageMetadata) metadata;
					final TiffField timeField = tiffMetadata.findField(TiffTagConstants.TIFF_TAG_DATE_TIME);
					final String strDate = timeField.getStringValue();
					dateTime = LocalDateTime.parse(strDate, formatter);
				}
			} catch (Exception e) {
//				System.out.println("couldn't find metadata " + file);
			}

			if (dateTime == null) {
				dateTime = getModDateTime();
			}
		}
		return dateTime;
	}

	public static PhotoFile create(File file) {
		if (!FileFilters.photoFileFilter.accept(file)) {
			return null;
		}
		//TODO this isn't great, probably want to keep the PhotoFiles natively
		return new PhotoFile(file);
	}

	public String getHash() {
		if (hash == null) {
			hash = FileUtils.hashFile(file);
		}
		return hash;
	}

}
