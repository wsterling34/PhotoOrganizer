package com.wsterling.photos2;

import org.apache.commons.imaging.formats.jpeg.JpegImageMetadata;
import org.apache.commons.imaging.formats.tiff.TiffField;
import org.apache.commons.imaging.formats.tiff.taginfos.TagInfo;

import java.io.File;

/**
 * Created by will on 6/2/15.
 */
public class ImageScratch {

    private static void printTagValue(final JpegImageMetadata jpegMetadata,
                                      final TagInfo tagInfo) {
        final TiffField field = jpegMetadata.findEXIFValueWithExactMatch(tagInfo);
        if (field == null) {
            System.out.println(tagInfo.name + ": " + "Not Found.");
        } else {
            System.out.println(tagInfo.name + ": "
                    + field.getValueDescription());
        }
    }

    private static void scratch() {

        File userDir = new File("/Users/will/Dropbox (Personal)");
        File monthlyDir = new File(userDir, "Photos");
        File uploadDir = new File(userDir, "Camera Uploads");

        File f1 = new File(monthlyDir, "2010.jan/IMG_0298.JPG");
        File f2 = new File(monthlyDir, "2014.may/2014-05-16 19.01.23.jpg");
        File f3 = new File(uploadDir, "2012-04-14 15.53.10.jpg");

        MonthlyPhotoStorage monthlyStorage = new MonthlyPhotoStorage(monthlyDir, true);
//        monthlyStorage.findAndFixDuplicates();

//		System.out.println(monthlyStorage.contains(PhotoFile.create(f1)));
//		System.out.println(monthlyStorage.contains(PhotoFile.create(f2)));
//		System.out.println(monthlyStorage.contains(PhotoFile.create(f3)));

    }

    public static void main(String[] args) throws Exception {


    }
}
