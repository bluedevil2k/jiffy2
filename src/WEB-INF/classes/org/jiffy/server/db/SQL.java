package org.jiffy.server.db;

import java.util.Properties;

public class SQL extends Properties
{
	private static final long serialVersionUID = 4570177306973585230L;
	
	// @X-JVM-safe this member is ready only
	// @Thread-safe this member is read only
	private static SQL _sql;
	
	private SQL() {}

	public static void init() throws Exception 
	{
		if (_sql == null)
		{
			_sql = new SQL();
		}
		_sql.load(SQL.class.getResourceAsStream("/sql.properties"));
	}
}
