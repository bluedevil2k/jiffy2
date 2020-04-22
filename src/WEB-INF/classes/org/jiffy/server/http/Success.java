package org.jiffy.server.http;

public class Success
{
	public boolean isSuccess = true;
	public String message = "";
	public Object data = null;
	
	public Success() {}
	
	public Success(Object data)
	{
		this.data = data;
	}
}
