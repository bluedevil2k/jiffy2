package org.jiffy.util;

import java.io.InputStream;
import java.util.Collection;

import eu.medsea.mimeutil.MimeType;
import eu.medsea.mimeutil.MimeUtil;

public class FileUtil 
{
	public static String getMimeType(InputStream is) throws Exception
	{
		MimeUtil.registerMimeDetector("eu.medsea.mimeutil.detector.MagicMimeMimeDetector");
		MimeUtil.registerMimeDetector("eu.medsea.mimeutil.detector.ExtensionMimeDetector");
		MimeUtil.registerMimeDetector("eu.medsea.mimeutil.detector.WindowsRegistryMimeDetector");
		
		Collection<?> mimes = MimeUtil.getMimeTypes(is);
	
		if (mimes.isEmpty())
		{
			return "";
		}
		else
		{
			MimeType m = mimes.toArray(new MimeType[mimes.size()])[0];
			return m.getMediaType();
		}
		
	}
}
