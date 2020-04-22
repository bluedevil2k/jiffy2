package org.jiffy.util;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;

public class NumberUtil
{	
	public static DecimalFormat getSystemFormatter()
	{
		NumberFormat formatter = NumberFormat.getInstance(Locale.getDefault()); 
		return (DecimalFormat)formatter;
	}
		
	public static double parseDouble(String text, String format) throws Exception
	{		
		try
		{
			DecimalFormat formatter = getSystemFormatter();
			formatter.applyPattern(format);
			return formatter.parse(text).doubleValue();
		}
		catch (ParseException pe)
		{
			// now remove all the thousands delimiters and currency symbols
			// and just keep the negative sign and decimal point
			String work = "";
			for (int i=0; i<text.length(); i++)
			{
				char c = text.charAt(i);
				if (Character.isDigit(c) || c == '-')
					work += c;
			}
			return Double.parseDouble(work);
		}
	}
}
