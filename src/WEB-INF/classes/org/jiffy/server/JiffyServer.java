package org.jiffy.server;

import java.io.File;
import java.lang.reflect.Method;
import java.net.InetAddress;

import javax.servlet.ServletException;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.jiffy.server.db.DB;
import org.jiffy.server.db.SQL;
import org.jiffy.util.Jiffy;
import org.jiffy.util.LogUtil;

import io.javalin.Javalin;
import io.javalin.http.staticfiles.Location;

public class JiffyServer
{
	private static final long serialVersionUID = 1452921980630171399L;
	
	private static Logger logger = LogManager.getLogger();

	public static void main(String...arg) throws Exception
	{
		try
		{
			System.out.println("*****************************");
			System.out.println("*** Jiffy Starting ***");
			System.out.println("*****************************");
			logger.info("");
			logger.info("");
			logger.info("******************************************");
			logger.info("");
			logger.info("     Jiffy Starting");
			logger.info("");
			logger.info("******************************************");
			
			Jiffy.configure();
			logger.info("***** Jiffy Configured");
			
			DB.init();
			SQL.init();
			logger.info("***** DB Initialized");
			
			String startupMethod = Jiffy.getValue("startupMethod");
			if (!StringUtils.isEmpty(startupMethod))
			{
				logger.info("***** Startup Call Initialized");
				Class c = Class.forName(startupMethod);
				Method m = c.getMethod("startUp");
				m.invoke(null);
			}
			
			////////////////////
			//
			//  Configuring the Javalin server
			//
			////////////////////
			logger.info("***** Javalin Server Being Configured");
			Javalin app = Javalin.create(config -> {
				
				//config.enableCorsForOrigin("*");
				config.enableCorsForAllOrigins();
				config.defaultContentType = "application/json";

				logger.info("***** Javalin Setting Up Logging");
//				if (StringUtils.isNotEmpty(Jiffy.getValue("requestLogger")))
//				{
//					try
//					{
//						RequestLogger rl = (RequestLogger)Class.forName(Jiffy.getValue("requestLogger")).newInstance();
//						config.requestLogger((ctx, ms) -> {
//					        rl.handle(ctx, ms);
//					    });
//					}
//					catch (Exception ex)
//					{
//						LogUtil.printErrorDetails(logger, ex);
//					}
//				}
				
				
				////////////////////
				//
				//  When Javalin is acting as the webserver
				//
				////////////////////

				logger.info("***** Javalin Enabling WebServer Settings");
				if (StringUtils.isEmpty(Jiffy.getValue("serverStaticPath")) == false)
				{
					config.addStaticFiles(Jiffy.getValue("serverStaticPath"), Location.EXTERNAL);
				}
				if (StringUtils.isEmpty(Jiffy.getValue("serverSinglePageFile")) == false)
				{
					System.out.println("Adding Single Page Root = " + Jiffy.getValue("serverSinglePageFile"));
					config.addSinglePageRoot(Jiffy.getValue("serverSinglePagePath"), Jiffy.getValue("serverSinglePageFile"), Location.EXTERNAL);
				}

				
				////////////////////
				//
				//  Whether to turn SSL on
				//
				////////////////////

				if (StringUtils.isEmpty(Jiffy.getValue("serverIsSSL")) == false)
				{
					config.enforceSsl = Jiffy.getBool("serverIsSSL");
				}
				
				config.server(() -> {
					Server server = new Server();
					
					ServerConnector connector = new ServerConnector(server);
//					connector.setPort(80);
//					connector.setHost("64.251.22.70");
					
					if (StringUtils.isEmpty(Jiffy.getValue("serverHost")) == false)
					{
						connector.setHost(Jiffy.getValue("serverHost"));
					}
					else
					{
						connector.setHost("0.0.0.0");
					}
					
					
					if (Jiffy.getInt("serverPort") != 0)
					{
						connector.setPort(Jiffy.getInt("serverPort"));
					}
//					else
//					{
//						connector.setPort(7070);
//					}
					
					// set up the SSL connection and certificates
					if (StringUtils.isEmpty(Jiffy.getValue("serverIsSSL")) == false && Jiffy.getBool("serverIsSSL") == true)
					{
						logger.info("***** Javalin Configuring SSL");
						
						ServerConnector sslConnector = new ServerConnector(server, getSslContextFactory());
		                sslConnector.setPort(Jiffy.getInt("sslPort"));
						if (StringUtils.isEmpty(Jiffy.getValue("serverHost")) == false)
						{
							sslConnector.setHost(Jiffy.getValue("serverHost"));
						}
		                
		                server.setConnectors(new Connector[]{sslConnector, connector});
					}
					// set up the non-ssl connector
					else
					{
						server.setConnectors(new Connector[]{connector});
					}
					return server;
				});
			});
			
			app.before(ctx -> ctx.header("Access-Control-Allow-Credentials", "true"));

			logger.info("***** Starting the Javalin Server ");
			app.start();

			logger.info("***** Routes Initialized");
			Routable routable = (Routable)Class.forName(Jiffy.getValue("routeFile")).newInstance();
			routable.createRoutes(app);	
	
			logger.info("***** " + Jiffy.getValue("version"));
			logger.info("***** Running on IP Address " + InetAddress.getLocalHost());

			logger.info("***** Jiffy Started Successfully *****");
		}
		catch (Exception ex)
		{
			System.out.println("^^^^  ERROR STARTING JIFFY SERVER   ^^^^");
			logger.info("^^^^  ERROR STARTING JIFFY SERVER   ^^^^");
			System.out.println(ex.getMessage());
			LogUtil.printErrorDetails(logger, ex);
            throw new ServletException(ex.getMessage());
		}
	}
	
	private static SslContextFactory getSslContextFactory() {
        SslContextFactory sslContextFactory = new SslContextFactory();
        
        File jarPath = new File(Jiffy.class.getProtectionDomain().getCodeSource().getLocation().getPath());
		String propertiesPath = jarPath.getParentFile().getAbsolutePath();
		String keyFile = propertiesPath + Jiffy.getValue("sslCertificateName");

		logger.info("***** Loading certificate " + keyFile);
		
        sslContextFactory.setKeyStorePath(keyFile);
        sslContextFactory.setKeyStorePassword(Jiffy.getValue("sslCertificatePass"));
        sslContextFactory.setExcludeProtocols("SSL_RSA_WITH_DES_CBC_SHA",
                "SSL_DHE_RSA_WITH_DES_CBC_SHA", "SSL_DHE_DSS_WITH_DES_CBC_SHA",
                "SSL_RSA_EXPORT_WITH_RC4_40_MD5",
                "SSL_RSA_EXPORT_WITH_DES40_CBC_SHA",
                "SSL_DHE_RSA_EXPORT_WITH_DES40_CBC_SHA",
                "SSL_DHE_DSS_EXPORT_WITH_DES40_CBC_SHA");
        
        return sslContextFactory;
    }
	
}
