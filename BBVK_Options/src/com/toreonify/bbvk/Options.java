package com.toreonify.bbvk;

import net.rim.blackberry.api.options.*;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.*;
import net.rim.device.api.ui.container.*;
import net.rim.device.api.system.*;
import net.rim.device.api.util.*;

/**
 * A simple library class to demonstrate the use of the options facilities.
 */
public final class Options implements OptionsProvider, FieldChangeListener
{
    // Members --------------------------------------------------------------
    private OptionsData _data;
    private String _title;
    private ButtonField _clearOptions;
    
    // Statics --------------------------------------------------------------
    private static Options _instance;

    // Constructors ---------------------------------------------------------
    private Options()
    {
    }

    private Options(String title)
    {
        _title = title;
        _data = OptionsData.load();
    }

    public static void libMain(String[] args) 
    {
    	OptionsManager.registerOptionsProvider(getInstance());
    }
    // Methods --------------------------------------------------------------

    /**
     * Only allow one instance of this class.
     */
    public static Options getInstance() 
    {
        if (_instance == null) 
        {
            _instance = new Options("VK Client");
        }
        
        return _instance;
    }

    /**
     * Get the title for the option screen.
     * 
     * @see net.rim.blackberry.api.options.OptionsProvider#getTitle()
     */
    public String getTitle() 
    {
        return _title;
    }

    /**
     * Add our fields to the screen.
     * @see net.rim.blackberry.api.options.OptionsProvider#populateMainScreen(MainScreen)
     * 
     */
    public void populateMainScreen(MainScreen screen) 
    {
    	String message = "You are not authorized.";
        if (_data.getToken() != null && _data.getToken().length() > 0) {
        	message = "You are authorized!";
        }
        
        screen.add(new LabelField(message));
        
        _clearOptions = new ButtonField();
        _clearOptions.setLabel("Clear settings");
        _clearOptions.setChangeListener(this);
        screen.add(_clearOptions);
    }

    /**
     * Save our data
     * @see net.rim.blackberry.api.options.OptionsProvider#save()
     */
    public void save()
    {
        _data.commit();
    }

    /**
     * Retrieve the data.  Used by other applications to access the options data
     * for this provider.
     */
    public OptionsData getData() 
    {
        return _data;
    }

    // Inner classes ------------------------------------------------------------
    public static final class OptionsData implements Persistable
    {
        private static final long ID = 0xfaf0b5eb24dc1164L;
        private String _token;

        private OptionsData()
        {
        	_token = "";
        }

        public String getToken()
        {
            return _token;
        }

        public void setToken(String token)
        {
            _token = token;
        }

        public void commit()
        {
            PersistentObject.commit(this);
        }

        private static OptionsData load()
        {
            PersistentObject persist = PersistentStore.getPersistentObject( OptionsData.ID );
            
            synchronized( persist ) 
            {
                if( persist.getContents() == null ) 
                {
                    persist.setContents( new OptionsData() );
                    persist.commit();
                }
            }
            
            return (OptionsData)persist.getContents();
        }

    }

	public void fieldChanged(Field field, int context) {
		if (field.hashCode() == _clearOptions.hashCode()) {
			int answer = Dialog.ask(Dialog.D_YES_NO, "Are you sure?", Dialog.NO);
			
			if (answer == Dialog.YES) {
				_data.setToken("");
				_data.commit();
				UiApplication.getUiApplication().getActiveScreen().updateDisplay();
			}
		}
	}
}
