package org.jiffy.util;

import java.io.File;
import java.io.InputStream;

import org.apache.commons.lang3.StringUtils;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;

import io.javalin.http.UploadedFile;

public class CloudStorage
{
	public static final String S3 = "s3";
	public static final String WASABI = "wasabi";
	
	private static final String WASABI_END_POINT = "https://s3.wasabisys.com";
	
	private static AWSCredentials getCredentials() throws Exception
	{
	    String key = Jiffy.getValue("cloudStorageKey");
	    String secret = Jiffy.getValue("cloudStorageSecretKey");
	    
	    AWSCredentials creds = new BasicAWSCredentials(key, secret);
	    
	    return creds;
	}
	
	public static String uploadFile(File f, String folder, String filename) throws Exception
	{
	    String bucket = Jiffy.getValue("cloudStorageBucket");
	    
	    AmazonS3 s3 = new AmazonS3Client(getCredentials());
	    
	    if (StringUtils.equals(Jiffy.getValue("cloudStorageHost"), WASABI))
	    {
		    s3.setEndpoint(WASABI_END_POINT);
	    }
	    
	    S3Object obj = new S3Object();
	    
        s3.putObject(new PutObjectRequest(bucket, folder + "/" + filename, f));
        obj.close();

        return s3.getUrl(bucket, folder + "/" + filename).toString();
	}
	
	public static String uploadFile(UploadedFile f, String folder, String filename) throws Exception
	{
	    String bucket = Jiffy.getValue("cloudStorageBucket");
	    
	    AmazonS3 s3 = new AmazonS3Client(getCredentials());

	    if (StringUtils.equals(Jiffy.getValue("cloudStorageHost"), WASABI))
	    {
		    s3.setEndpoint(WASABI_END_POINT);
	    }
	    
	    S3Object obj = new S3Object();
	    
	    ObjectMetadata omd = new ObjectMetadata();
        omd.setContentType(f.getContentType());
        omd.setContentLength(f.getContentLength());
        omd.setHeader("filename", f.getFilename());
        
        obj.setObjectContent(f.getContent());
        s3.putObject(new PutObjectRequest(bucket, folder + "/" + filename, f.getContent(), omd));
        obj.close();

        return s3.getUrl(bucket, folder + "/" + filename).toString();
	}
	
	public static InputStream getFile(String folder, String filename) throws Exception
	{
	    String bucket = Jiffy.getValue("cloudStorageBucket");

	    AmazonS3 s3 = new AmazonS3Client(getCredentials());

	    if (StringUtils.equals(Jiffy.getValue("cloudStorageHost"), WASABI))
	    {
		    s3.setEndpoint(WASABI_END_POINT);
	    }
	    
		S3Object obj = s3.getObject(new GetObjectRequest(bucket, folder + "/" + filename));
		
		return obj.getObjectContent();
	}
	
	public static void deleteFile(String folder, String filename) throws Exception
	{
	    String bucket = Jiffy.getValue("cloudStorageBucket");

	    AmazonS3 s3 = new AmazonS3Client(getCredentials());

	    if (StringUtils.equals(Jiffy.getValue("cloudStorageHost"), WASABI))
	    {
		    s3.setEndpoint(WASABI_END_POINT);
	    }
	    
	    s3.deleteObject(new DeleteObjectRequest(bucket, folder + "/" + filename));	    
	}
	
}
