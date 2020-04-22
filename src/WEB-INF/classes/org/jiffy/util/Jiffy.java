package org.jiffy.util;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Jiffy extends Properties
{
	private static final long serialVersionUID = 4570177306973585230L;

	private static Logger logger = LogManager.getLogger();
	
	// @X-JVM-safe this member is ready only
	// @Thread-safe this member is read only
	private static Jiffy _jiffy;
	
	private Jiffy() 
	{
	}	
		
	public static synchronized void configure() throws Exception
	{
		if (_jiffy == null)
		{
			_jiffy = new Jiffy();
		}
		init("/jiffy.properties");
		init("/env.properties");
	}
	
	private static void init(String propName) throws Exception 
	{
		// try to load the properties if Jiffy is in a jar file
		try
		{
			System.out.println("Trying to load " + propName + " as an external file in the same folder as the JAR file");
			logger.debug("Trying to load " + propName + " as an external file in the same folder as the JAR file");
			File jarPath=new File(Jiffy.class.getProtectionDomain().getCodeSource().getLocation().getPath());
			String propertiesPath=jarPath.getParentFile().getAbsolutePath();
			_jiffy.load(new FileInputStream(propertiesPath+propName));
		}
		// if that fails, then we're just using it in development mode
		catch (Exception ex)
		{
			try
			{
				System.out.println("Now trying to load " + propName + " from inside the JAR in production, or from classpath if not in a JAR");
				logger.debug("Now trying to load " + propName + " from inside the JAR in production, or from classpath if not in a JAR");
				_jiffy.load(Jiffy.class.getResourceAsStream(propName));
			}
			catch (Exception ex2)
			{
				System.out.println("The properties file " + propName + " was not found and will be skipped");
				logger.debug("The properties file " + propName + " was not found and will be skipped");
			}
		}
	}
	
	public static String getValue(String key)
	{
		return _jiffy.getProperty(key);
	}
	
	public static int getInt(String key)
	{
		return Integer.parseInt(_jiffy.getProperty(key));
	}
	
	public static boolean getBool(String key)
	{
		return Boolean.parseBoolean(_jiffy.getProperty(key));
	}
}