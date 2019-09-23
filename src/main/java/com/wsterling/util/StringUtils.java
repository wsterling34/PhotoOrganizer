package com.wsterling.util;

/**
 * Created by will on 6/2/15.
 */
public class StringUtils {


    public static boolean isNumeric(String str) {
        try {
            Double.parseDouble(str);
        } catch(NumberFormatException nfe) {
            return false;
        }
        return true;
    }
}
