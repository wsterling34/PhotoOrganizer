package com.wsterling.photos2;

import java.time.LocalDateTime;

/**
 * Created by will on 6/2/15.
 */
public interface Photo {

    String getHash();

    LocalDateTime getDateTime();

}
