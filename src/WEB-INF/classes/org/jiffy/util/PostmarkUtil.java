package org.jiffy.util;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class PostmarkUtil
{
	private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
		
	public static boolean send(String to, String emailOnAccount, String replyToEmail, String fromName, String subject, String message) throws Exception
	{
		OkHttpClient client = new OkHttpClient();		
		
		JSON json = new JSON();
		json.put("From", fromName + " " + emailOnAccount);
		json.put("To", to);
		json.put("Subject", subject);
		json.put("TextBody", message);
		json.put("ReplyTo", replyToEmail);
		json.put("TrackOpens", true);
				
		RequestBody body = RequestBody.create(JSON, json.toString());
				
		Request request = new Request.Builder()
				.url("https://api.postmarkapp.com/email")
				.addHeader("Accept", "application/json")
				.addHeader("X-Postmark-Server-Token", Jiffy.getValue("postmarkAPI"))
				.post(body)
				.build();
		
		Response response = client.newCall(request).execute();
				
		if (response.body().string().indexOf("OK") > -1)
		{
			return true;
		}
		
		return false;
	}
}
