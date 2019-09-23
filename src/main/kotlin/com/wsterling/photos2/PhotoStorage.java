package com.wsterling.photos2;

import java.util.Collection;

/**
 * Created by will on 6/2/15.
 */
public interface PhotoStorage {

    boolean contains(Photo photo);

    void put(PhotoFile photoFile);

    Collection<PhotoFile> getPhotoFiles(Photo photo);

}
