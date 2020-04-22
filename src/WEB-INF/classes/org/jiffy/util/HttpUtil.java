package org.jiffy.util;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;

public class HttpUtil
{
	private static Logger logger = LogManager.getLogger();
	
	public static JSON post(String urlString, String data) throws Exception
	{
		HttpURLConnection connection = null;
		try
		{
			URL url = new URL(urlString);
			connection = (HttpURLConnection)url.openConnection();
			connection.setRequestMethod("POST");
			 
			connection.setUseCaches(false);
		    connection.setDoInput(true);
		    connection.setDoOutput(true);
	
		    //Send request
		    DataOutputStream wr = new DataOutputStream (connection.getOutputStream());
		    wr.writeBytes(data);
		    wr.flush();
		    wr.close();
	
		    //Get Response	
		    InputStream is = connection.getInputStream();
		    BufferedReader rd = new BufferedReader(new InputStreamReader(is));
		    String line;
		    StringBuffer response = new StringBuffer(); 
		    while((line = rd.readLine()) != null) {
		      response.append(line);
		      response.append('\r');
		    }
		    rd.close();
		    
		    ObjectMapper mapper = new ObjectMapper();
		    return JSON.toJSON(response.toString());
		    			
		}
		catch (Exception ex)
		{
			LogUtil.printErrorDetails(logger, ex);
			JSON json = new JSON();
			json.put("error", ex.getMessage());
			return json;
		}
		finally
		{
		    if (connection != null) 
		    {
		    	connection.disconnect(); 
		    }
		}
	}	

	public static String getURLContents(String website) throws Exception
	{
		URL url = new URL(website);
        URLConnection urlC = url.openConnection();
        BufferedReader in = new BufferedReader(new InputStreamReader(urlC.getInputStream(), "UTF-8"));
        String inputLine;
        StringBuilder a = new StringBuilder();
        while ((inputLine = in.readLine()) != null)
            a.append(inputLine);
        in.close();

        return a.toString();
	}
}
