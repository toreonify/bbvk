package com.toreonify.bbvk.net;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.Hashtable;

import javax.microedition.io.Connector;
import javax.microedition.io.HttpConnection;
import javax.microedition.io.HttpsConnection;

import net.rim.device.api.io.http.HttpHeaders;
import net.rim.device.api.io.http.HttpProtocolConstants;
import net.rim.device.api.util.StringUtilities;

public class Utilities 
{
    
    public static HttpsConnection makeConnection(String url, HttpHeaders requestHeaders, byte[] postData, Hashtable cookies) 
    {
        HttpsConnection conn = null;
        OutputStream out = null;
        
        try 
        {
            conn = (HttpsConnection) Connector.open(url);           

            if (requestHeaders != null) 
            {
                // From
                // http://www.w3.org/Protocols/rfc2616/rfc2616-sec15.html#sec15.1.3
                //
                // Clients SHOULD NOT include a Referer header field in a (non-secure) HTTP 
                // request if the referring page was transferred with a secure protocol.
                String referer = requestHeaders.getPropertyValue("referer");
                boolean sendReferrer = true;
                
                if (referer != null && StringUtilities.startsWithIgnoreCase(referer, "https:") && !StringUtilities.startsWithIgnoreCase(url, "https:")) 
                {
                    sendReferrer = false;
                }
                
                int size = requestHeaders.size();
                for (int i = 0; i < size;) 
                {                    
                    String header = requestHeaders.getPropertyKey(i);
                    
                    // Remove referer header if needed.
                    if ( !sendReferrer && header.equals("referer")) 
                    {
                        requestHeaders.removeProperty(i);
                        --size;
                        continue;
                    }
                    
                    String value = requestHeaders.getPropertyValue(i++);
                    if (value != null) 
                    {
                        conn.setRequestProperty( header, value);
                    }
                }
                
                if (cookies != null) {
	                String cookieString = "";
	                Enumeration cookieEnum = cookies.keys();
	                
	                while (cookieEnum.hasMoreElements()) {
	                	String key = (String) cookieEnum.nextElement();
	                	cookieString += key + "=" + (String)cookies.get(key);
	                	
	                	if (cookieEnum.hasMoreElements()) {
	                		cookieString += "; ";
	                	}
	                }
	                
	                if (cookieString.length() > 0) {
	                	conn.setRequestProperty("Cookie", cookieString);
	                }
                }
            }                          
            
            if (postData == null) 
            {
                conn.setRequestMethod(HttpConnection.GET);
            } 
            else 
            {
                conn.setRequestMethod(HttpConnection.POST);
                conn.setRequestProperty(HttpProtocolConstants.HEADER_CONTENT_LENGTH, String.valueOf(postData.length));
                
                out = conn.openOutputStream();
                out.write(postData);
            }
            
        }
        catch (IOException e1) 
        {
        } 
        finally 
        {
            if (out != null) 
            {
                try 
                {
                    out.close();
                } 
                catch (IOException e2) 
                {
                }
            }
        }    
        
        return conn;
    }

}
