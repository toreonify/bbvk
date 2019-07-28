package com.toreonify.bbvk.ui;

import java.io.IOException;
import java.util.Hashtable;

import javax.microedition.io.HttpConnection;
import javax.microedition.io.HttpsConnection;

import com.toreonify.bbvk.Options;
import com.toreonify.bbvk.Options.OptionsData;
import com.toreonify.bbvk.api.ApiHelper;
import com.toreonify.bbvk.net.SecondaryResourceFetchThread;
import com.toreonify.bbvk.net.Utilities;

import net.rim.device.api.browser.field.*;
import net.rim.device.api.io.http.HttpHeaders;
import net.rim.device.api.system.Application;
import net.rim.device.api.ui.*;
import net.rim.device.api.ui.component.Status;
import net.rim.device.api.ui.container.MainScreen;
import net.rim.device.api.util.StringUtilities;
import net.rim.device.api.system.*;

final public class AuthenticationScreen extends MainScreen implements RenderingApplication 
{
    private static final String REFERER = "referer";   
    private static final String API_CODE_PARAM = "access_token=";
 
    private UiApplication _app;
    private BrowserContentManager _browserContentManager;
    private HttpsConnection  _currentConnection;
    private Hashtable _cookies = new Hashtable();
    
    public AuthenticationScreen() 
    {      
    	this._app = UiApplication.getUiApplication();

    	_browserContentManager = new BrowserContentManager(Field.FIELD_TOP); 

        add(_browserContentManager);
        
        PrimaryResourceFetchThread thread = new PrimaryResourceFetchThread(ApiHelper.API_TOKEN_URL, null, null, null, this, _cookies);
        thread.start(); 
    }
          
    public void processConnection(HttpsConnection connection, Event e) 
    {
        if (_currentConnection != null) 
        {
            try 
            {
                _currentConnection.close();
            } 
            catch (IOException e1) 
            {
            }
        }
        
        _currentConnection = connection;
        
        final String url = _currentConnection.getURL();
        
        if (url.startsWith("https://oauth.vk.com/blank.html")) {        	
        	int beginIndex = url.indexOf(API_CODE_PARAM);
        	int end = url.length() + 1;
        	
        	if (url.indexOf('&', beginIndex) != -1)
        		end = url.indexOf('&');
        	
        	final String token = url.substring(beginIndex + API_CODE_PARAM.length(), end);

        	OptionsData optionsData = Options.getInstance().getData();
        	optionsData.setToken(token);
        	optionsData.commit();
        	
        	_app.invokeAndWait(new Runnable() {

				public void run() {
		        	_app.popScreen(getScreen());
					_app.pushScreen(new AppScreen());
				}
        		
        	});

			return;
        }
        
		try {
		   int i = 0;
		   String headerValue = "";
		   while ((headerValue = _currentConnection.getHeaderField(i)) != null) 
		   {         
		   	String headerKey = _currentConnection.getHeaderFieldKey(i);
		   	
		   	if (StringUtilities.compareToIgnoreCase(headerKey, "Set-Cookie") == 0) {
		   		String cookieKey = headerValue.substring(0, headerValue.indexOf('='));
		   		
		   		int end = headerValue.length();
		   		if (headerValue.indexOf(';') != -1)
		   				end = headerValue.indexOf(';');
		   		
		   		String cookieValue = headerValue.substring(headerValue.indexOf('=') + 1, end + 1);
		   				
		   		_cookies.put(cookieKey, cookieValue);
		   	}
		   	
		   	i++;
		   }   
		}
		catch (Exception ex) {
		   
		}
        
		try 
		{
			_browserContentManager.setContent(connection, this, e);
		} 
		finally 
		{
			SecondaryResourceFetchThread.doneAddingImages();
		}  
    }    

    /**
     * @see net.rim.device.api.browser.RenderingApplication#eventOccurred(net.rim.device.api.browser.Event)
     */
    public Object eventOccurred(Event event) 
    {
        int eventId = event.getUID();

        switch (eventId) 
        {
            case Event.EVENT_URL_REQUESTED : 
            {            	
                UrlRequestedEvent urlRequestedEvent = (UrlRequestedEvent) event;                    
                PrimaryResourceFetchThread thread = new PrimaryResourceFetchThread(urlRequestedEvent.getURL(),
                                                                                         urlRequestedEvent.getHeaders(), 
                                                                                         urlRequestedEvent.getPostData(),
                                                                                         event, this, _cookies);
                thread.start();
    
                break;

            } 
            case Event.EVENT_BROWSER_CONTENT_CHANGED: 
            {                
                BrowserContentChangedEvent browserContentChangedEvent = (BrowserContentChangedEvent) event; 
            
                if (browserContentChangedEvent.getSource() instanceof BrowserContent) 
                { 
                    BrowserContent browserField = (BrowserContent) browserContentChangedEvent.getSource(); 
                    String newTitle = browserField.getTitle();
                    if (newTitle != null) 
                    {
                        synchronized (_app.getAppEventLock()) 
                        { 
                            setTitle(newTitle);
                        }                                               
                    }                                       
                }                   

                break;                

            } 
            case Event.EVENT_REDIRECT : 
            {
                RedirectEvent e = (RedirectEvent) event;
                String referrer = e.getSourceURL();
                
                switch (e.getType()) 
                {  
                    case RedirectEvent.TYPE_SINGLE_FRAME_REDIRECT :
                        Application.getApplication().invokeAndWait(new Runnable() 
                        {
                            public void run() 
                            {
                                Status.show("You are being redirected to a different page...");
                            }
                        });
                    
                    break;
                    
                    case RedirectEvent.TYPE_JAVASCRIPT :
                        break;
                    
                    case RedirectEvent.TYPE_META :
                        // MSIE and Mozilla don't send a Referer for META Refresh.
                        referrer = null;     
                        break;
                    
                    case RedirectEvent.TYPE_300_REDIRECT :
                        // MSIE, Mozilla, and Opera all send the original
                        // request's Referer as the Referer for the new
                        // request.
                        Object eventSource = e.getSource();
                        if (eventSource instanceof HttpsConnection) 
                        {
                            referrer = ((HttpsConnection)eventSource).getRequestProperty(REFERER);
                        }
                        
                        break;
                    }
	            
            		HttpHeaders originalHeaders = new HttpHeaders();
                    originalHeaders.setProperty(REFERER, referrer);
                    
                    PrimaryResourceFetchThread thread = new PrimaryResourceFetchThread(e.getLocation(), originalHeaders, null, event, this, _cookies);
                    thread.start();
                    break;

            } 
            case Event.EVENT_CLOSE :
                break;

            case Event.EVENT_SET_HEADER :        // No cache support.
            case Event.EVENT_SET_HTTP_COOKIE :   // No cookie support.
            case Event.EVENT_HISTORY :           // No history support.
            case Event.EVENT_EXECUTING_SCRIPT :  // No progress bar is supported.
            case Event.EVENT_FULL_WINDOW :       // No full window support.
            case Event.EVENT_STOP :              // No stop loading support.
            default :
        }

        return null;
    }

    /**
     * @see net.rim.device.api.browser.RenderingApplication#getAvailableHeight(net.rim.device.api.browser.BrowserContent)
     */
    public int getAvailableHeight(BrowserContent browserField) 
    {
        return Display.getHeight();
    }

    /**
     * @see net.rim.device.api.browser.RenderingApplication#getAvailableWidth(net.rim.device.api.browser.BrowserContent)
     */
    public int getAvailableWidth(BrowserContent browserField) 
    {
        return Display.getWidth();
    }

    /**
     * @see net.rim.device.api.browser.RenderingApplication#getHistoryPosition(net.rim.device.api.browser.BrowserContent)
     */
    public int getHistoryPosition(BrowserContent browserField) 
    {
        return 0;
    }
    

    /**
     * @see net.rim.device.api.browser.RenderingApplication#getHTTPCookie(java.lang.String)
     */
    public String getHTTPCookie(String url) 
    {
        return null;
    }

    /**
     * @see net.rim.device.api.browser.RenderingApplication#getResource(net.rim.device.api.browser.RequestedResource,
     *      net.rim.device.api.browser.BrowserContent)
     */
    public HttpConnection getResource( RequestedResource resource, BrowserContent referrer) 
    {
        if (resource == null) 
        {
            return null;
        }

        // Check if this is cache-only request.
        if (resource.isCacheOnly()) 
        {
            // No cache support.
            return null;
        }

        String url = resource.getUrl();

        if (url == null) 
        {
            return null;
        }

        // If referrer is null we must return the connection.
        if (referrer == null) 
        {
            HttpsConnection connection = Utilities.makeConnection(resource.getUrl(), resource.getRequestHeaders(), null, _cookies);
            
            return connection;
        } 
        else 
        {
            // If referrer is provided we can set up the connection on a separate thread.
            SecondaryResourceFetchThread.enqueue(resource, referrer);
        }

        return null;
    }

    /**
     * @see net.rim.device.api.browser.RenderingApplication#invokeRunnable(java.lang.Runnable)
     */
    public void invokeRunnable(Runnable runnable) 
    {       
        (new Thread(runnable)).start();
    }    
}

class PrimaryResourceFetchThread extends Thread 
{
    
    private AuthenticationScreen _application;
    private Event _event;
    private byte[] _postData;
    private HttpHeaders _requestHeaders;
    private String _url;
    private Hashtable _cookies;
    
    PrimaryResourceFetchThread(String url, HttpHeaders requestHeaders, byte[] postData, 
                                  Event event, AuthenticationScreen application, Hashtable cookies) 
    {
        _url = url;
        _requestHeaders = requestHeaders;
        _postData = postData;
        _application = application;
        _event = event;
        _cookies = cookies;
    }

    public void run() 
    {
        HttpsConnection connection = Utilities.makeConnection(_url, _requestHeaders, _postData, _cookies);
        _application.processConnection(connection, _event);        
    }
}

