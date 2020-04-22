package org.jiffy.server;

import io.javalin.Javalin;

public interface Routable 
{
	public abstract void createRoutes(Javalin app);
}
