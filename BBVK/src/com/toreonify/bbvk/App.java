package com.toreonify.bbvk;

import com.toreonify.bbvk.Options.OptionsData;
import com.toreonify.bbvk.ui.AppScreen;
import com.toreonify.bbvk.ui.AuthenticationScreen;

import net.rim.device.api.ui.UiApplication;

public class App extends UiApplication {
	public static void main(String[] args) {
		new App().enterEventDispatcher();
	}
	
	public App() {
		Options options = Options.getInstance();
		String token = null;
		
		if (options == null) {
			// error
		} else {
			OptionsData data = options.getData();
			
			if (data != null) {
				token = data.getToken();
				
				if (token != null) {
					if (token.length() > 0) {
						pushScreen(new AppScreen());
					} else {
						pushScreen(new AuthenticationScreen());
					}
				} else {
					// error
				}
			} else {
				// error
			}
		}
	}

	
}
