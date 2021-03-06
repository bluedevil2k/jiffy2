package org.jiffy.util;

import org.apache.commons.lang3.CharUtils;
import org.apache.commons.lang3.StringUtils;
import org.jasypt.util.password.BasicPasswordEncryptor;
import org.jasypt.util.password.StrongPasswordEncryptor;

public final class PasswordUtil  {

	public static final String THREE_OF_FOUR = "three_of_four";
	public static final String ALPHA_NUMERIC = "alpha_numeric";
	public static final String ALPHA = "alpha";
	public static final String NUMERIC = "numeric";	
	public static final String UPPER = "upper";	
	public static final String LOWER = "lower";		
	public static final String ALL = "all";
	
	public static final String ACCEPTABLE_SYMBOLS = "!@#$%*";
	
    public static String encrypt(String plaintext) throws Exception  
    {
    	String encryptedPassword = "";
    	try
    	{
        	StrongPasswordEncryptor passwordEncryptor = new StrongPasswordEncryptor();
        	encryptedPassword = passwordEncryptor.encryptPassword(plaintext);
    	}
    	catch (Exception ex)
    	{
    		BasicPasswordEncryptor passwordEncryptor = new BasicPasswordEncryptor();
        	encryptedPassword = passwordEncryptor.encryptPassword(plaintext);	
    	}
    	
    	return encryptedPassword;
    }
    
    public static boolean isPasswordValid(String inputtedPassword, String encryptedStoredPassword) throws Exception
    {
    	try
    	{
        	StrongPasswordEncryptor passwordEncryptor = new StrongPasswordEncryptor();
        	return passwordEncryptor.checkPassword(inputtedPassword, encryptedStoredPassword);
    	}
    	catch (Exception ex)
    	{
    		BasicPasswordEncryptor passwordEncryptor = new BasicPasswordEncryptor();
        	return passwordEncryptor.checkPassword(inputtedPassword, encryptedStoredPassword);
    	}
    }
        
    public static boolean doesPasswordMeetRules(String rule, String password)
    {
		if (StringUtils.equals(rule,  THREE_OF_FOUR))
			return checkThreeOfFour(password);
		
    	for (int i=0; i<password.length(); i++)
    	{
    		char c = password.charAt(i);
			if (StringUtils.equals(rule, ALPHA_NUMERIC) && (c<=47 || (c>=58 && c<=64) || (c>=91 && c<=96)))
				return false;
			if (StringUtils.equals(rule, ALPHA) && (c<=64 || (c>=91 && c<=96)))
				return false;
			if (StringUtils.equals(rule, NUMERIC) && (c<=47 || c>=58))
				return false;
			if (StringUtils.equals(rule, UPPER) && (c<=64 || c>=91))
				return false;
			if (StringUtils.equals(rule, LOWER) && (c<=96 || c>=123))
				return false;
    	}
    	return true;
    }
    
	public static String autogeneratePassword(int length, String rule)
	{
		StringBuilder b = new StringBuilder("");
		while (b.length()<length)
		{
			int c = (int)(Math.random()*122);
			
			// no 0's or o's or O's
			if (c == 48 || c == 79 || c == 111)
				continue;
			
			// no 1's or l's or I's
			if (c == 49 || c == 108 || c == 73)
				continue;
			
			if (StringUtils.equals(rule, ALPHA_NUMERIC) && (c<=47 || (c>=58 && c<=64) || (c>=91 && c<=96)))
				continue;
			else if (StringUtils.equals(rule, ALPHA) && (c<=64 || (c>=91 && c<=96)))
				continue;
			else if (StringUtils.equals(rule, NUMERIC) && (c<=47 || c>=58))
				continue;
			else if (StringUtils.equals(rule, UPPER) && (c<=64 || c>=91))
				continue;
			else if (StringUtils.equals(rule, LOWER) && (c<=96 || c>=123))
				continue;
			else if (StringUtils.equals(rule,  THREE_OF_FOUR) && !( (c>47 && c<58) || (c>64 && c<91) || (c>96) || c==33 || c==64 || c==35 || c==36 || c==37 || c==42) )
				continue;
			b.append((char)c);
		}
		return b.toString();
	}
	
	private static boolean checkThreeOfFour(String password)
	{
		int count = 0;
		for (int i=0; i<password.length(); i++)
		{
			char c = password.charAt(i);
			if (CharUtils.isAsciiAlphaLower(c))
			{
				count++;
				break;
			}
		}
		for (int i=0; i<password.length(); i++)
		{
			char c = password.charAt(i);
			if (CharUtils.isAsciiAlphaUpper(c))
			{
				count++;
				break;
			}
		}
		for (int i=0; i<password.length(); i++)
		{
			char c = password.charAt(i);
			if (CharUtils.isAsciiNumeric(c))
			{
				count++;
				break;
			}
		}
		for (int i=0; i<password.length(); i++)
		{
			String c = "" + password.charAt(i);
			if (StringUtils.containsOnly(c, ACCEPTABLE_SYMBOLS))
			{
				count++;
				break;
			}
		}
		if (count >=3)
			return true;
		else
			return false;
	}

}



