package org.jiffy.util;

import io.javalin.http.Context;

public class RecaptchaUtil
{
	public static boolean isValidV2(Context ctx) throws Exception
	{
		String recaptcha = ctx.queryParam("g-recaptcha-response");
		String re_url = "https://www.google.com/recaptcha/api/siteverify";
		String data = "response=" + recaptcha + "&secret=" + Jiffy.getValue("recaptchaPrivate");
		JSON json = HttpUtil.post(re_url, data);
		return Boolean.parseBoolean(json.get("success").toString());
	}
}
