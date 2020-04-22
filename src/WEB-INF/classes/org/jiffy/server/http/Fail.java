package org.jiffy.server.http;

public class Fail
{
	public boolean isSuccess = false;
	public String message = "";
	public Object data = null;
	
	public Fail() {}
	
	public Fail(String errorMessage) 
	{
		this.message = errorMessage;
	}
	
	public Fail(String errorMessage, Object data)
	{
		this.message = errorMessage;
		this.data = data;
	}
}
