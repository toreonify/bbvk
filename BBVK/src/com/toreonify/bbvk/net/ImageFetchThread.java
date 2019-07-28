package com.toreonify.bbvk.net;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Vector;

import javax.microedition.io.HttpsConnection;
import net.rim.device.api.system.EncodedImage;


public class ImageFetchThread extends Thread 
{

    /**
     * Callback browser field.
     */
    private Requesting _requesting;
    
    /**
     * Images to retrieve.
     */
    private Vector _imageQueue;
    
    /**
     * True is all images have been enqueued.
     */
    private boolean _done;
    
    /**
     * Sync object.
     */
    private static Object _syncObject = new Object();
    
    /**
     * Secondary thread.
     */
    private static ImageFetchThread _currentThread;
    
    
    /**
     * Enqueues secondary resource for a browser field.
     * 
     * @param resource - resource to retrieve.
     * @param referrer - call back browsr field.
     */
    public static void enqueue(RequestedImage resource, Requesting referrer) 
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
                _currentThread = new ImageFetchThread();
                _currentThread.start();
            } 
            else 
            {
                // If thread alread is running, check that we are adding images for the same browser field.
                if (referrer != _currentThread._requesting) 
                {  
                    synchronized( _currentThread._imageQueue) 
                    {
                        // If the request is for a different browser field,
                        // clear old elements.
                        _currentThread._imageQueue.removeAllElements();
                    }
                }
            }   
            
            synchronized( _currentThread._imageQueue) 
            {
                _currentThread._imageQueue.addElement(resource);
            }
            
            _currentThread._requesting = referrer;
        }
    }
    
    /**
     * Constructor
     *
     */
    private ImageFetchThread() 
    {
        _imageQueue = new Vector();        
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
                    synchronized( _imageQueue ) 
                    {
                        if (_imageQueue.size() == 0) 
                        {
                            _currentThread = null;   
                            break;
                        }
                    }
                }
            }
            
            RequestedImage resource = null;
                              
            // Request next image.
            synchronized( _imageQueue ) 
            {
                if (_imageQueue.size() > 0) 
                {
                    resource = (RequestedImage)_imageQueue.elementAt(0);
                    _imageQueue.removeElementAt(0);
                }
            }
            
            if (resource != null) 
            {
                HttpsConnection connection = Utilities.makeConnection(resource.url, null, null, null);
                EncodedImage image = null;
                
                try {
        			ByteArrayOutputStream result = new ByteArrayOutputStream();
        			InputStream is = connection.openInputStream();
        			byte[] buffer = new byte[512];
        			int length;
        			while ((length = is.read(buffer)) != -1) {
        			    result.write(buffer, 0, length);
        			}
        			
        			image = EncodedImage.createEncodedImage(result.toByteArray(), 0, result.size());
        		}
        		catch (IOException e) {
        			// TODO can't get response 
        			
        			continue;
        		}
                
                // Signal to the browser field that resource is ready.
                if (_requesting != null) 
                {            
                    _requesting.setResponse(resource, image);
                }
            }
        }       
    }   
    
}
