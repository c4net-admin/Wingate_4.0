package com.maatayim.acceleradio.log;

import com.maatayim.acceleradio.utils.FormatException;


public class LogEntryBuilder {
	
	public static LogEntry build(String str) throws FormatException{
		if (str.startsWith(",")){
			return null;
		}
		str = str.trim();
		//String tmpstr = "L,1,0035,07,00,+41.22263,+665.82343,00,0000,";
		switch(str.split(",")[0].charAt(0)){
		case 'L':
			// tal 180405 fake location
			// return new Location("L,1,0035,07,00,+41.22263,+665.82343,00,0000,");
			return new Location(str);
		case 'I':
			return new Icon(str);
		case 'D':
			return new Delete(str);
		case 'T':
			return new Sms(str);
		case 'S':
			return new Log(str);				
		}
		
		//log str is wrongly formatted
		return null;
	}

}
