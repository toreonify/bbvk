package com.toreonify.bbvk.api;

import java.util.Vector;

public class ApiFetchThread extends Thread 
{
    private RequestingApi _requesting;
    
    private Vector _apiQueue;
    
    private boolean _done;
    
    private static Object _syncObject = new Object();
    
    private static ApiFetchThread _currentThread;
        
    public static void enqueue(RequestedApi resource, RequestingApi referrer) 
    {
        if (resource == null) 
        {
            return;
        }
        
        synchronized( _syncObject ) 
        {
            
            // Create new thread.
            if (_currentThread == null) 
            {
                _currentThread = new ApiFetchThread();
                _currentThread.start();
            } 
            else 
            {
                // If thread alread is running, check that we are adding images for the same browser field.
                if (referrer != _currentThread._requesting) 
                {  
                    synchronized( _currentThread._apiQueue) 
                    {
                        // If the request is for a different browser field,
                        // clear old elements.
                        _currentThread._apiQueue.removeAllElements();
                    }
                }
            }   
            
            synchronized( _currentThread._apiQueue) 
            {
                _currentThread._apiQueue.addElement(resource);
            }
            
            _currentThread._requesting = referrer;
        }
    }
    
    /**
     * Constructor
     *
     */
    private ApiFetchThread() 
    {
        _apiQueue = new Vector();        
    }
    
    /**
     * Indicate that all images have been enqueued for this browser field.
     */
    public static void doneAddingImages() 
    {
        synchronized( _syncObject ) 
        {
            if (_currentThread != null) 
            {
                _currentThread._done = true;
            }
        }
    }
    
    public void run() 
    {
        while (true) 
        {
            if (_done) 
            {
                // Check if we are done requesting images.
                synchronized( _syncObject ) 
                {
                    synchronized( _apiQueue ) 
                    {
                        if (_apiQueue.size() == 0) 
                        {
                            _currentThread = null;   
                            break;
                        }
                    }
                }
            }
            
            RequestedApi resource = null;
                              
            // Request next image.
            synchronized( _apiQueue ) 
            {
                if (_apiQueue.size() > 0) 
                {
                    resource = (RequestedApi)_apiQueue.elementAt(0);
                    _apiQueue.removeElementAt(0);
                }
            }
            
            if (resource != null) 
            {
                Object result = null;
                
                if (resource.type == RequestedApi.OBJECT) {
                	try {
						result = Api.getInstance().call(resource.urlTemplate, resource.callArgs);
					} catch (ApiException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
                } else {
                	try {
						result = Api.getInstance().callArray(resource.urlTemplate, resource.callArgs);
					} catch (ApiException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
                }
                
                // Signal to the browser field that resource is ready.
                if (_requesting != null) 
                {            
                    _requesting.setResponse(resource, result);
                }
            }
        }       
    }   
    
}