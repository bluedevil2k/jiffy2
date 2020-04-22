package org.jiffy.server.db;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.commons.dbutils.handlers.MapHandler;
import org.apache.commons.dbutils.handlers.MapListHandler;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.tomcat.jdbc.pool.DataSource;
import org.apache.tomcat.jdbc.pool.PoolProperties;
import org.jiffy.server.db.annotations.DBColumn;
import org.jiffy.server.db.annotations.DBTable;
import org.jiffy.server.db.annotations.DBUniqueKey;
import org.jiffy.server.db.handlers.AnnotatedDataRowProcessor;
import org.jiffy.util.Constants;
import org.jiffy.util.Jiffy;
import org.jiffy.util.Util;

public class DB
{
	protected static Logger logger = LogManager.getLogger();

	// @X-JVM-safe this member is read-only after init()
	// @Thread-safe this member is read-only after init()
    private static DataSource dataSource = null;

    public static final String MYSQL = "mysql";
    public static final String MARIADB = "mariadb";
    public static final String POSTGRESQL = "postgresql";

    private static final String MYSQL_DRIVER = "com.mysql.cj.jdbc.Driver";
    private static final String MARIADB_DRIVER = "org.mariadb.jdbc.Driver";
    private static final String POSTGRESQL_DRIVER = "org.postgresql.Driver";
    
    private static volatile boolean isInitialized = false;
    
	// @X-JVM-safe this member is read-only after init()
	// @Thread-safe this member is read-only after init()
    private static String jdbcDriver = MYSQL_DRIVER;

	// @X-JVM-Synchronized only called by HtmlServlet.init()
	// @Synchronized
	public synchronized static void init() throws Exception
	{
		if (isInitialized)
			return;
		
		// Get all these values from the settings object
		String dbEngine = Jiffy.getValue("dbEngine");
		
		if (StringUtils.equals(dbEngine, MYSQL))
		{
			jdbcDriver = MYSQL_DRIVER; 
		}
		else if (StringUtils.equals(dbEngine, MARIADB))
		{
			jdbcDriver = MARIADB_DRIVER;
		}
		else if (StringUtils.equals(dbEngine, POSTGRESQL))
		{
			jdbcDriver = POSTGRESQL_DRIVER;
		}
		else if (StringUtils.equals(dbEngine, Constants.NONE))
		{
			return;
		}

		// db parameters
		String server = Jiffy.getValue("dbServer");
		String port = Jiffy.getValue("dbPort");
		String database = Jiffy.getValue("dbDatabase");
		String userName = Jiffy.getValue("dbUser");
		String password = Jiffy.getValue("dbPassword");
		
		String connectURI = "jdbc:" + dbEngine + "://" + server + ":" + port + "/" +  database;
		
		// create the pool properties to create the db pool
		PoolProperties p = new PoolProperties();
		
		// connection parameters
        p.setUrl(connectURI);
        p.setDriverClassName(jdbcDriver);
        p.setUsername(userName);
        p.setPassword(password);
        
        // pooling parameters
        p.setJmxEnabled(false);
        p.setTestWhileIdle(false);
        p.setTestOnBorrow(true);
        p.setValidationQuery("SELECT 1");
        p.setTestOnReturn(false);
        p.setValidationInterval(30000);
        p.setTimeBetweenEvictionRunsMillis(30000);
        p.setMaxActive(500);
        p.setInitialSize(20);
        p.setMaxWait(10000);
        p.setRemoveAbandonedTimeout(60);
        p.setMinEvictableIdleTimeMillis(30000);
        p.setMinIdle(10);
        p.setLogAbandoned(true);
        p.setRemoveAbandoned(true);
        
        // Create the pooled DataSource
        dataSource = new DataSource();
        dataSource.setPoolProperties(p);

        isInitialized = true;
   	}
	
    public static Connection getConnection() throws SQLException 
    {
    	return dataSource.getConnection();
    }
        
    public static void close(Connection conn) throws Exception
    {
    	DbUtils.close(conn);
    }
    
    public static void openTransaction(Connection conn) throws Exception
    {
        conn.setAutoCommit(false);
    }
    
    public static void committTransaction(Connection conn) throws Exception
    {
    	if (conn != null)
    	{
    		conn.commit();
    		conn.setAutoCommit(true);
    		DbUtils.close(conn);
    	}
    }
    
    public static void rollbackTransaction(Connection conn) throws Exception
    {
    	if (conn != null)
    	{
    		conn.rollback();
    		conn.setAutoCommit(true);
    		DbUtils.close(conn);
            logger.debug("*** Transaction rolled back *** ");
    	}
    }
        
    public static DBResult select(String sql, Object...args) throws Exception
    {
    	QueryRunner run = new QueryRunner();
		MapListHandler h = new MapListHandler();
		
		Connection conn = DB.getConnection();
		try
		{
			return new DBResult(run.query(conn, sql, h, args));
		}
		finally
		{
			DbUtils.close(conn);
		}
    }
	
	public static <T> T selectOne(Class<T> type) throws Exception
	{
		return selectOneWhere(type, "");
	}
	
	public static <T> T selectOne(Class<T> type, String sql, Object...args) throws Exception
	{
		QueryRunner run = new QueryRunner();
		ResultSetHandler<T> h = new BeanHandler<T>(type, new AnnotatedDataRowProcessor());
		
		Connection conn = DB.getConnection();
		try
		{
			T result = run.query(conn, sql, h, args);
			return result;		        
		} 
		finally 
		{
			DbUtils.close(conn);  
		}
	}
    
	public static <T> T selectOneWhere(Class<T> type, String clause, Object...args) throws Exception
	{
		QueryRunner run = new QueryRunner();
		ResultSetHandler<T> h = new BeanHandler<T>(type, new AnnotatedDataRowProcessor());
		
		Connection conn = DB.getConnection();
		try
		{
			if (!StringUtils.isEmpty(clause))
			{
				clause = " " + clause;
			}
			
			T result = run.query(conn, "SELECT * FROM " + getTableName(type) + clause, h, args);
			return result;		        
		} 
		finally 
		{
			DbUtils.close(conn);  
		}
	}
	
	public static <T, S extends ArrayList<T>> S selectAll(Class<S> listType) throws Exception
	{
		return selectAllWhere(listType, "");
	}
	
	public static <T, S extends ArrayList<T>> S selectAll(Class<S> listType, String sql, Object...args) throws Exception
	{
		Class<T> t = (Class<T>)((ParameterizedType)listType.getGenericSuperclass()).getActualTypeArguments()[0];

		QueryRunner run = new QueryRunner();
		ResultSetHandler<List<T>> h = new BeanListHandler<T>(t, new AnnotatedDataRowProcessor(listType));

		Connection conn = DB.getConnection();
		try
		{	
		    return (S)run.query(conn, sql, h, args);
		} 
		finally 
		{
		    DbUtils.close(conn);  
		}
	}
	
	public static <T, S extends ArrayList<T>> S selectAllWhere(Class<S> listType, String clause, Object...args) throws Exception
	{
		Class<T> t = (Class<T>)((ParameterizedType)listType.getGenericSuperclass()).getActualTypeArguments()[0];

		QueryRunner run = new QueryRunner();
		ResultSetHandler<List<T>> h = new BeanListHandler<T>(t, new AnnotatedDataRowProcessor(listType));

		Connection conn = DB.getConnection();
		try
		{			
			if (!StringUtils.isEmpty(clause))
			{
				clause = " " + clause;
			}
			
		    return (S)run.query(conn, "SELECT * FROM " + getTableName(t) + clause, h, args);
		} 
		finally 
		{
		    DbUtils.close(conn);  
		}
	}
	
	public static int update(String sql, Object...args) throws Exception
	{
		QueryRunner run = new QueryRunner();
		
		Connection conn = DB.getConnection();
		
		try
		{
		    return run.update(conn, sql, args);
		}
		finally 
		{		
			DbUtils.close(conn);  
		}
	}
	
	// Note - this doesn't return the new key generated on an insert
	public static <T> int update(Class<T> type, String sql, Object...args) throws Exception
	{
		QueryRunner run = new QueryRunner();
		
		Connection conn = DB.getConnection();
		
		try
		{
			String table = getTableName(type);
			sql = StringUtils.replace(sql, "@table@", table);
			
		    return run.update(conn, sql, args);
		}
		finally 
		{		
			DbUtils.close(conn);  
		}
	}
	
	public static <T> int updateWithConn(Class<T> type, String sql, Connection conn, Object...args) throws Exception
	{
		QueryRunner run = new QueryRunner();

		String table = getTableName(type);
		sql = StringUtils.replace(sql, "@table@", table);
		
	    return run.update(conn, sql, args);
	}

	/**
	 * Based on the Object passed in as a parameter, the DB will check if this object exists in the DB given the @DBUniqueKey fields
	 * <br>If it exists, it will update that entry in the table
	 * <br>If it doesn't exist, it wll insert the entry into the table and return the new id
	 * @param o
	 * @return
	 * @throws Exception
	 */
	public static int insertUpdate(Object o) throws Exception
	{
		return DB.insertUpdateWithConn(null, o);
	}

	/**
	 * Based on the Object passed in as a parameter, the DB will check if this object exists in the DB given the @DBUniqueKey fields
	 * <br>If it exists, it will update that entry in the table
	 * <br>If it doesn't exist, it wll insert the entry into the table and return the new id
	 * @conn The existing Connection to use in a transaction
	 * @param o
	 * @return
	 * @throws Exception
	 */
	public static int insertUpdateWithConn(Connection conn, Object o) throws Exception
	{
		String tableName = o.getClass().getAnnotation(DBTable.class).table();
		if (StringUtils.isEmpty(tableName))
		{
			tableName = o.getClass().getSimpleName();
		}
		tableName = Util.camelToUnderscore(tableName);

		String sql = "WHERE ";
		List<String> keys = new ArrayList();
		List<Object> args = new ArrayList();
		
		Field[] f = o.getClass().getFields();
		for (int i=0; i<f.length; i++)
		{
			Field field = f[i];

			if (field.isAnnotationPresent(DBUniqueKey.class))
			{
				sql += Util.camelToUnderscore(field.getName()) + "=? AND ";
				keys.add(Util.camelToUnderscore(field.getName()));
				args.add(field.get(o));
			}
		}
		
//		for (int i=0; i<uniqueKeys.length; i++)
//		{
//			sql += uniqueKeys[i] + "=? AND ";
//		}
		
		sql = sql.substring(0, sql.length()-4);
				
		//int count = DB.count(o.getClass(), sql, uniqueValues);
		int count = DB.count(o.getClass(), sql, args.toArray());
		
		if (count == 0)
		{
			return insertWithConn(conn, o);
		}
		else
		{
			return updateWithConn(conn, o, keys.stream().toArray(String[]::new), args.toArray());
			//return updateWithConn(conn, o, uniqueKeys, uniqueValues);
		}
	}
	
	/**
	 * Deletes a row from the database matching the "where" parameter
	 * @param type
	 * @param where
	 * @param args
	 * @throws Exception
	 */
	public static <T> void deleteWhere(Class<T> type, String where, Object...args) throws Exception
	{
		deleteWhereWithConn(null, type, where, args);
	}
	
	public static <T> void deleteWhereWithConn(Connection conn, Class<T> type, String where, Object...args) throws Exception
	{
		QueryRunner run = new QueryRunner();
		
		boolean createdConn = false;
		if (conn == null)
		{
			createdConn = true;
			conn = DB.getConnection();
		}
		
		String tableName = type.getAnnotation(DBTable.class).table();
		if (StringUtils.isEmpty(tableName))
		{
			tableName = type.getSimpleName();
		}
		tableName = Util.camelToUnderscore(tableName);
		
		String sql = "DELETE FROM " + tableName + " " + where;
		
		try
		{
		    run.update(conn, sql, args);
		}
		finally 
		{		
			if (createdConn)
			{
				DbUtils.close(conn);
			}
		}
	}

	/**
	 * Automatically updates this object in the database, reading all the columns marked with @DBColumn as updatable fields
	 * <br>This is usually not called directly, and the insertUpdate() function should instead be called
	 * @param o
	 * @param uniqueKeys
	 * @param uniqueValues
	 * @return
	 * @throws Exception
	 */
	public static int update(Object o, String[] uniqueKeys, Object[] uniqueValues) throws Exception
	{
		return DB.updateWithConn(null, o, uniqueKeys, uniqueValues);
	}	

	/**
	 * Automatically updates this object in the database, reading all the columns marked with @DBColumn as updatable fields
	 * <br>This is usually not called directly, and the insertUpdateWithConn() function should instead be called
	 * @param conn
	 * @param o
	 * @param uniqueKeys
	 * @param uniqueValues
	 * @return
	 * @throws Exception
	 */
	public static int updateWithConn(Connection conn, Object o, String[] uniqueKeys, Object[] uniqueValues) throws Exception
	{
		QueryRunner run = new QueryRunner();
		
		boolean createdConn = false;
		if (conn == null)
		{
			createdConn = true;
			conn = DB.getConnection();
		}
		
		try
		{
			String tableName = o.getClass().getAnnotation(DBTable.class).table();
			if (StringUtils.isEmpty(tableName))
			{
				tableName = o.getClass().getSimpleName();
			}
			tableName = Util.camelToUnderscore(tableName);

			int id = 0;
			
			Field[] f = o.getClass().getFields();
			
			String sql = "UPDATE " + tableName + " SET ";
			
			ArrayList<Object> params = new ArrayList<Object>();
		
			for (int i=0; i<f.length; i++)
			{
				Field field = f[i];

				if (field.isAnnotationPresent(DBColumn.class))
				{
					String columnName = field.getAnnotation(DBColumn.class).name();
					if (StringUtils.isEmpty(columnName))
					{
						String fieldName = field.getName();
						columnName = Util.camelToUnderscore(fieldName);
					}
					
					// if it's a unique column, skip it
					boolean shouldSkip = false;
					for (int c=0; c<uniqueKeys.length; c++)
					{
						String key = uniqueKeys[c];
						if (StringUtils.equals(key, columnName))
						{
							shouldSkip = true;
						}
					}
					if (shouldSkip)
					{
						continue;
					}
					
					if (StringUtils.equals(columnName, "id"))
					{
						id = field.getInt(o);
						continue;
					}
					
					if (field.getType() == String.class)
					{
						sql += columnName + "=?,";
						params.add(field.get(o));
					}
					else if (field.getType() == java.util.Date.class)
					{
						java.util.Date d = (java.util.Date)field.get(o);
						if (d != null)
						{
							sql += columnName + "=?,";
							params.add(field.get(o));
						}
						else
						{
							sql += columnName + "=?,";
							params.add(null);
						}
					}
//					else if (field.getType() == YearMonth.class)
//					{
//						YearMonth d = (YearMonth)field.get(o);
//						//java.sql.Date ts = new java.sql.Date(d.getYear(), d.getMonthValue(), 1);
//						sql += columnName + "=?,";
//						params.add(field.get(o));
//					}
					else if (field.getType() == Instant.class)
					{
						Instant d = (Instant)field.get(o);
						if (d != null)
						{
							sql += columnName + "=?,";
							params.add(Timestamp.from(d));
						}
						else
						{
							sql += columnName + "=?,";
							params.add(null);
						}
					}
					else
					{
						sql += columnName + "=?,";
						params.add(field.get(o));
					}
				}
			}
			sql = sql.substring(0, sql.length()-1);
			
			sql += " WHERE ";
			for (int c=0; c<uniqueKeys.length; c++)
			{
				String key = uniqueKeys[c];
				sql += key + "=? AND ";
				params.add(uniqueValues[c]);
			}

			sql = sql.substring(0, sql.length()-4);

			run.update(conn, sql, params.toArray());
			
			return id;
		}
		finally
		{
			if (createdConn)
			{
				DbUtils.close(conn);
			}
		}
	}

	/**
	 * Inserts an object into the database using the @DBColumn fields as the DB columns
	 * <br>This function is not usually called directly, but instead called by the insertUpdateWithConn()
	 * @param o
	 * @return
	 * @throws Exception
	 */
	public static int insert(Object o) throws Exception
	{
		return DB.insertWithConn(null, o);
	}

	/**
	 * Inserts an object into the database using the @DBColumn fields as the DB columns
	 * <br>This function is not usually called directly, but instead called by the insertUpdate()
	 * @param conn
	 * @param o
	 * @return
	 * @throws Exception
	 */
	public static int insertWithConn(Connection conn, Object o) throws Exception
	{
		QueryRunner run = new QueryRunner();

		boolean createdConn = false;
		if (conn == null)
		{
			createdConn = true;
			conn = DB.getConnection();
		}
		
		try
		{
			String tableName = o.getClass().getAnnotation(DBTable.class).table();
			if (StringUtils.isEmpty(tableName))
			{
				tableName = o.getClass().getSimpleName();
			}
			tableName = Util.camelToUnderscore(tableName);

			Field[] f = o.getClass().getFields();
			
			String sql = "INSERT INTO ";
			
			sql += tableName + " (";
			String values = "";
			ArrayList<Object> params = new ArrayList<Object>();
		
			for (int i=0; i<f.length; i++)
			{
				Field field = f[i];

				if (field.isAnnotationPresent(DBColumn.class))
				{
					String columnName = field.getAnnotation(DBColumn.class).name();
					if (StringUtils.isEmpty(columnName))
					{
						String fieldName = field.getName();
						columnName = Util.camelToUnderscore(fieldName);
					}
					
					if (StringUtils.equals(columnName, "id"))
					{
						continue;
					}
					
					sql += columnName + ",";
					if (field.getType() == String.class)
					{
						values += "?,";
						params.add(field.get(o));
					}
					else if (field.getType() == Instant.class)
					{
						if (field.get(o) != null)
						{
							Instant d = (Instant)field.get(o);
							values += "?,";
							params.add(Timestamp.from(d));
						}
						else
						{
							values += "?,";
							params.add(null);
						}
					}
//					else if (field.getType() == YearMonth.class)
//					{
//						YearMonth d = (YearMonth)field.get(o);
//						java.sql.Date ts = new java.sql.Date(d.getYear(), d.getMonthValue(), 1);
//						sql += columnName + "=?,";
//						params.add(field.get(o));
//					}
					else
					{
						values += "?,";
						params.add(field.get(o));
					}
				}
			}
			sql = sql.substring(0, sql.length()-1);
			values = values.substring(0, values.length()-1);
			
			sql = sql + ") VALUES (" + values + ")";
			
			Map<String, Object> map = run.insert(conn, sql, new MapHandler(), params.toArray());
			
			if (map == null || map.get("GENERATED_KEY") == null)
				return 0;
			
			int newId = 0;
			if (MYSQL_DRIVER.equals("com.mysql.cj.jdbc.Driver"))
			{
				newId = ((java.math.BigInteger)map.get("GENERATED_KEY")).intValue();
			}
			else
			{
				newId = ((Long)map.get("GENERATED_KEY")).intValue();
			}
			return newId;
		}
		finally
		{
			if (createdConn)
			{
				DbUtils.close(conn);
			}
		}
	}
		
	public static <T> int count(Class<T> type, String clause, Object... args) throws Exception
	{
		QueryRunner run = new QueryRunner();
		ResultSetHandler<Integer> h = new ResultSetHandler<Integer>() 
		{
		    public Integer handle(ResultSet rs) throws SQLException 
		    {
		    	if (rs != null && rs.next())
		    	{
		    		return rs.getInt("COUNT");
		    	}
		    	return 0;
		    }
		};
		
		Connection conn = DB.getConnection();
		try
		{
			if (!StringUtils.isEmpty(clause))
			{
				clause = " " + clause;
			}
			
		    return run.query(conn, new StringBuilder("SELECT COUNT(*) AS COUNT FROM ").append(getTableName(type)).append(clause).toString(), h, args);
		} 
		finally 
		{
		    DbUtils.close(conn);  
		}
	}
	
	public static <T> int countDistinct(Class<T> type, String fieldName, String clause, Object... args) throws Exception
	{
		QueryRunner run = new QueryRunner();
		ResultSetHandler<Integer> h = new ResultSetHandler<Integer>() 
		{
		    public Integer handle(ResultSet rs) throws SQLException 
		    {
		    	if (rs != null && rs.next())
		    	{
		    		return rs.getInt("COUNT");
		    	}
		    	return 0;
		    }
		};
		
		Connection conn = DB.getConnection();
		try
		{
			if (!StringUtils.isEmpty(clause))
			{
				clause = " " + clause;
			}
			
		    return run.query(conn, new StringBuilder("SELECT COUNT(DISTINCT(").append(fieldName).append(")) AS COUNT FROM ").append(getTableName(type)).append(clause).toString(), h, args);
		} 
		finally 
		{
		    DbUtils.close(conn);  
		}
	}
	
	public static <T> double sum(Class<T> type, String fieldName, String clause, Object...args) throws Exception
	{
		QueryRunner run = new QueryRunner();
		ResultSetHandler<Double> h = new ResultSetHandler<Double>() 
		{
		    public Double handle(ResultSet rs) throws SQLException 
		    {
		    	if (rs != null && rs.next())
		    	{
		    		return rs.getDouble("SUM");
		    	}
		    	return 0.0;
		    }
		};
		
		Connection conn = DB.getConnection();
		try
		{
			if (!StringUtils.isEmpty(clause))
			{
				clause = " " + clause;
			}
			
		    return run.query(conn, new StringBuilder("SELECT SUM(").append(fieldName).append(") AS SUM FROM ").append(getTableName(type)).append(clause).toString(), h, args);
		} 
		finally 
		{
		    DbUtils.close(conn);  
		}
	}
	
	public static <T> double max(Class<T> type, String fieldName, String clause, Object...args) throws Exception
	{
		QueryRunner run = new QueryRunner();
		ResultSetHandler<Double> h = new ResultSetHandler<Double>() 
		{
		    public Double handle(ResultSet rs) throws SQLException 
		    {
		    	if (rs != null && rs.next())
		    	{
		    		return rs.getDouble("MAX");
		    	}
		    	return 0.0;
		    }
		};
		
		Connection conn = DB.getConnection();
		try
		{
			if (!StringUtils.isEmpty(clause))
			{
				clause = " " + clause;
			}
			
		    return run.query(conn, new StringBuilder("SELECT MAX(").append(fieldName).append(") AS MAX FROM ").append(getTableName(type)).append(clause).toString(), h, args);
		} 
		finally 
		{
		    DbUtils.close(conn);  
		}
	}
	
	public static <T> double min(Class<T> type, String fieldName, String clause, Object...args) throws Exception
	{
		QueryRunner run = new QueryRunner();
		ResultSetHandler<Double> h = new ResultSetHandler<Double>() 
		{
		    public Double handle(ResultSet rs) throws SQLException 
		    {
		    	if (rs != null && rs.next())
		    	{
		    		return rs.getDouble("MIN");
		    	}
		    	return 0.0;
		    }
		};
		
		Connection conn = DB.getConnection();
		try
		{
			if (!StringUtils.isEmpty(clause))
			{
				clause = " " + clause;
			}

		    return run.query(conn, new StringBuilder("SELECT MIN(").append(fieldName).append(") AS MIN FROM ").append(getTableName(type)).append(clause).toString(), h, args);
		} 
		finally 
		{
		    DbUtils.close(conn);  
		}
	}
	
	public static <T> String getTableName(Class<T> type) throws Exception
	{
		String table = type.newInstance().getClass().getAnnotation(DBTable.class).table();
		
		if (StringUtils.isEmpty(table))
		{
			table = type.getSimpleName();
		}
		table = Util.camelToUnderscore(table);

		return table;
	}
	
}
