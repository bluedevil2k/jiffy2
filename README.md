Jiffy 2
=====

Jiffy2 is a quick and easy Java Web App Framework for scalable applications.

The Basics
=====

Jiffy2 is a wrapper around the Javalin Java web framework.  Javalin wraps around an embedded Jetty web server, and Jiffy2 wraps Javalin by providing configuration options for the server start-up, a DB layer, and dozens of utility classes and methods for working with web applications and 3rd party tools like AWS, Twilio, YubiKey and Email services. 

Building Blocks of Jiffy2
-------
* Javalin web server.  A wonderful Java web framework that makes building an API server in Java extremely easy and fast.
* An extremely powerful and scalable DB class, utilizing built in connection pools, and scalable to tens of thousands of concurrent users.
* Closely tied together Model and DB layers, allowing for extremely simple DB->Model coding.  No JPA or XML configuration files, just standard SQL code.
* Works with MySQL, MariaDB and Postres databases

Database
=======

The database layer in Jiffy is designed to make it simple to code for, but also scalable to thousands of concurrent users. It was created to be much simpler to use than JPA for people
that are comfortable writing a little SQL code.

It utilizes connection pooling from Tomcat 7's DBPool class and database utility methods and abstractions from Apache's DBUtils class.  The end result is a DB utility class that handles
eveything you could want from your DB abstraction layer.

#### DB Examples

      // To get one User in the database
      DB.selectOne(UserData.class, "WHERE id=?", 1);

      // To get all the Users in the database
      DB.selectAll(UserList.class);
      
      // To count the users in the database
      DB.count(UserData.class);
      
      // To count the users with a distinct first name
      DB.countDistinct(UserData.class, "@firstName@");
      
      // It can also properly handle transactions
      try
      {
       DB.openTransaction();
       DB.commitTransaction();
      }
      catch (Exception ex)
      {
       DB.rollbackTransaction();
      }

Model
=====

The model layer is closely tied into the Database layer for a seamless integration just by using a few Annotations.

#### Model Examples

     @DBTable
     public class User
     {
      @DBUniqueKey
      public int id;

      @DBColumn
      public String username;
      
      @DBColumn(name="family_name")
      public String lastName;
      
      @DBHasOne
      public Address address;
      
      @DBHasMany
      public PhoneNumber phoneNumbers;
     }
     

Model & DB Integration
=====

With the Model classes annotated properly, the DB abstraction layer can use introspection and reflection to create and map all the database fields into the Java objects.  Additionally,
the integration has been set up to create an ArrayList of objects when more than 1 object is returned from the SQL.

#### Model/DB Example

     // define the class to store many User objects
     public class UserList extends ArrayList<User>
     
     // Use the DB class to populate a List of admin Users
     UserList admins = DB.selectAll(UserList.class, "WHERE @role@=?, "admin");


Jiffy Configuration Files
====
I've always hated in frameworks that they don't include a simple way to provide configuration settings, and a simple way to access them.  Rails is a good example of the right way to do it, which
uses global variables to provide access to configuration variables.  Jiffy tries to make this even easier.

Any configuration you place into one of the properties files will be accessible anywhere in the application.

Jiffy loads up 2 configuration files, 1)  jiffy.properties  2) env.properties with each property file overriding the settings already loaded.  This lets you nest
configurations and specify settings specific to only one deployment.

### Config Example

      // In jiffy.properties
      dbPassword = 
      
      // In the file named env.properties on your local machine
      dbPassword = abcdef
      
      // In the file named env.properties in production
      dbPassword = 8ah3F72h
      
The result of these configurations lets you set global variables, and environment-specific variables.  This lets you set variables for the application, while each developer can also specify
their own variables for their own environment.  


Other Features
========

Many Util classes for dealing with common Java issues - LogUtil, MathUtil, NumberUtil, PasswordUtil, PusherUtil, TimeUtil, TwilioUtil, ZipUtil
* Includes the Parallel utility, to run tasks in Parallel without using Java 8

