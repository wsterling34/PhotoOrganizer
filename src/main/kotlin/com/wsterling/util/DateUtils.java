package com.wsterling.util;

import java.time.LocalDate;

public class DateUtils {
	
	private static String[] months = new String[] {"jan", "feb", "mar", "apr", "may", "jun", "jul", "aug", "sep", "oct", "nov", "dec"};
	
	public static String monthToAbbrev(int month) {
		if (month > 12 || month < 1) {
			return null;
		} else {
			return months[month-1];
		}
	}
	
	public static int abbrevToMonth(String s) {
		
		for (int i=0; i<months.length; i++) {
			if (months[i].contentEquals(s)) {
				return i+1;
			}
		}
		return -1;
	}
	
	public static boolean isMonthAbbrev(String s) {
		return abbrevToMonth(s) != -1;
	}


	public static LocalDate getLocalDate(String yearStr, String monthStr, String dateStr) {

		if (!StringUtils.isNumeric(yearStr)) {
			return null;
		}
		int year = Integer.parseInt(yearStr);
		if (year > 2100 || year < 1900) {
			return null;
		}
		if (!StringUtils.isNumeric(monthStr)) {
			return null;
		}
		int month = Integer.parseInt(monthStr);
		if (month > 12 || month < 1) {
			return null;
		}
		if (!StringUtils.isNumeric(dateStr)) {
			return null;
		}
		int date = Integer.parseInt(dateStr);
		if (date < 1 || date > 31) {
			return null;
		}
		return LocalDate.of(year, month, date);
	}
}
