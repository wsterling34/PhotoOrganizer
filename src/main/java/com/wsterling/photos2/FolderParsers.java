package com.wsterling.photos2;

import com.wsterling.util.DateUtils;
import com.wsterling.util.StringUtils;

import java.io.File;
import java.time.LocalDate;

public class FolderParsers {

	public static LocalDate parseApertureFolder(File folder) {
		//TODO
		String folderName = folder.getName();
		if (folderName.length() < 11 || folderName.length() > 15)
			return null;
		
		String[] splitName = folderName.split(" ");
		if (splitName.length != 3) 
			return null;
		
		int month = DateUtils.abbrevToMonth(splitName[0].toLowerCase());
		if (month == -1)
			return null;
		
		Integer date = Integer.parseInt(splitName[1].substring(0, splitName[1].length() - 1));
		if (date == null) 
			return null;
		
		Integer year = Integer.parseInt(splitName[2].substring(0, 4));
		
		if (year == null || year > 2050 || year < 1900) return null;
		//TODO add logic to test that inner date info matches outer date info
		return LocalDate.of(year, month, date);
	}
	
	public static LocalDate parseSimpleByDate1(File folder) {
		
		String fn = folder.getName();
		if (fn.length() != 10)
			return null;
		String[] tk = fn.split("-");
		if (tk.length < 3)
			return null;
		return DateUtils.getLocalDate(tk[0], tk[1], tk[2]);
	}

	public static LocalDate parseSimpleByDate2(File folder) {

		String folderName = folder.getName();
		if (folderName.length() != 10 || !folderName.substring(4,5).contentEquals("-") || !folderName.substring(7,8).contentEquals("-")) {
			return null;
		}
		return DateUtils.getLocalDate(folderName.substring(0, 4), folderName.substring(5, 7), folderName.substring(8, 10));
	}
	
	public static LocalDate parseAperture1(File folder) {
		
		String fn = folder.getName();
		if (fn.length() < 11)
			return null;

		String[] tk = fn.split(" ");
		if (tk.length < 3)
			return null;

		String yrToken = tk[2];
		if (yrToken.length() < 4)
			return null;
		String year = yrToken.substring(0, 4);
		
		if (tk[1].length() < 2)
			return null;
		String date = tk[1].substring(0, tk[1].length()-1);
		String monthAbbrev = tk[0].toLowerCase();	
		int month = DateUtils.abbrevToMonth(monthAbbrev);

		if (month == -1) {
			return null;
		} else {
			return DateUtils.getLocalDate(year, Integer.toString(month), date);
		}

	}

	public static LocalDate parseAperture2(File folder) {
		
		String fn = folder.getName();
		if (fn.length() != 15)
			return null;

		String year = fn.substring(0,4);
		String month = fn.substring(4,6);
		String date = fn.substring(6,8);

		if (!fn.substring(8,9).contentEquals("-"))
			return null;

		if (!StringUtils.isNumeric(fn.substring(9, 15)))
			return null;

		File dateDir = folder.getParentFile();
		if (!dateDir.getName().contentEquals(date))
			return null;

		File monthDir = dateDir.getParentFile();
		if (!month.contentEquals(monthDir.getName()))
			return null;

		File yearDir = monthDir.getParentFile();
		if (!year.contentEquals(yearDir.getName()))
			return null;

		return DateUtils.getLocalDate(year, month, date);
	}

	public static LocalDate parseAperture3(File folder) {

		String date = folder.getName();
		File monthDir = folder.getParentFile();
		String month = monthDir.getName();
		File yearDir = monthDir.getParentFile();
		String year = yearDir.getName();
		return DateUtils.getLocalDate(year, month, date);
	}

	public static LocalDate getFolderBasedDate(File folder) {
		
		LocalDate localDate = null;
		
		localDate = parseSimpleByDate1(folder);
		if (localDate != null) {
			return localDate;
		}
		
		localDate = parseAperture1(folder);
		if (localDate != null) {
			return localDate;
		}
		
		localDate = parseAperture2(folder);
		if (localDate != null) {
			return localDate;
		}
		
		localDate = parseAperture3(folder);
		if (localDate != null) {
			return localDate;
		}
		
		localDate = parseApertureFolder(folder);
		if (localDate != null) {
			return localDate;
		}
		
		localDate = parseSimpleByDate2(folder);
		if (localDate != null) {
			return localDate;
		}
		return null;
	}
	
}
