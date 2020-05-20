/* =======================================================
	Copyright 2020 - ePortfolium - Licensed under the
	Educational Community License, Version 2.0 (the "License"); you may
	not use this file except in compliance with the License. You may
	obtain a copy of the License at

	http://www.osedu.org/licenses/ECL-2.0

	Unless required by applicable law or agreed to in writing,
	software distributed under the License is distributed on an "AS IS"
	BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
	or implied. See the License for the specific language governing
	permissions and limitations under the License.
   ======================================================= */

package eportfolium.com.karuta.webapp.util;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

public class FileUtils{
  public static String getMimeType(String fileUrl)
    throws java.io.IOException, MalformedURLException
  {
    String type = null;
    URL u = new URL(fileUrl);
    URLConnection uc = null;
    uc = u.openConnection();
    type = uc.getContentType();
    return type;
  }

  public static String getMimeTypeFromExtension(String extension)
  {
	  String contentType = null;

	  if (extension.equals("au"))
		    contentType = "audio/basic";
		if (extension.equals("avi"))
		    contentType = "video/x-msvideo";
		if (extension.equals("bmp"))
		    contentType = "image/bmp";
		if (extension.equals("css"))
		    contentType = "text/css";
		if (extension.equals("doc") || extension.equals("dot"))
		    contentType = "application/msword";
		if (extension.equals("docx"))
		    contentType = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
		if (extension.equals("eps"))
		    contentType = "application/postscript";
		if (extension.equals("gif"))
		    contentType = "image/gif";
		if (extension.equals("html") || extension.equals("htm"))
		    contentType = "text/html";
		if (extension.equals("jpg") || extension.equals("jpeg"))
		    contentType = "image/jpeg";
		if (extension.equals("js"))
		    contentType = "application/x-javascript";
		if (extension.equals("mov") || extension.equals("qt"))
		    contentType = "video/quicktime";
		if (extension.equals("mp3"))
		    contentType = "audio/mpeg3";
		if (extension.equals("mpeg") || extension.equals("mpg"))
		    contentType = "video/mpeg";
		if (extension.equals("mpp"))
		    contentType = "application/vnd.ms-project";
		if (extension.equals("mtw"))
		    contentType = "application/octet-stream";
		if (extension.equals("pdf"))
		    contentType = "application/pdf";
		if (extension.equals("png"))
		    contentType = "image/png";
		if (extension.equals("ppt") || extension.equals("pot") || extension.equals("pps"))
		    contentType = "application/vnd.ms-powerpoint";
		if (extension.equals("pptx"))
		    contentType = "application/vnd.openxmlformats-officedocument.presentationml.presentation";
		if (extension.equals("ps"))
		    contentType = "application/postscript";
		if (extension.equals("ra"))
		    contentType = "audio/x-realaudio";
		if (extension.equals("ram") || extension.equals("rm"))
		    contentType = "audio/x-pn-realaudio";
		if (extension.equals("sql"))
		    contentType = "text/plain";
		if (extension.equals("swf"))
		    contentType = "application/x-shockwave-flash";
		if (extension.equals("tif"))
		    contentType = "image/tiff";
		if (extension.equals("txt") || extension.equals("text"))
		    contentType = "text/plain";
		if (extension.equals("vsd"))
		    contentType = "application/x-visio";
		if (extension.equals("wav"))
		    contentType = "audio/x-wav";
		if (extension.equals("xls"))
		    contentType = "application/vnd.ms-excel";
		if (extension.equals("xml"))
		    contentType = "application/xml";
		if (extension.equals("zip"))
		    contentType = "application/x-zip-compressed";
		if (extension.equals("java"))
		    contentType = "text/plain";

		return contentType;
  }


}