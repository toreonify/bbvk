package com.toreonify.bbvk.ui;

import com.toreonify.bbvk.Options;
import com.toreonify.bbvk.Options.OptionsData;
import com.toreonify.bbvk.api.ApiHelper;

import net.rim.blackberry.api.browser.Browser;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.ButtonField;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.component.TextField;
import net.rim.device.api.ui.container.MainScreen;

public class SimpleAuthenticationScreen extends MainScreen implements FieldChangeListener {

	private UiApplication _app;
	private TextField tokenField;
	private ButtonField browserButton, nextButton;
	
	public SimpleAuthenticationScreen() {
		_app = UiApplication.getUiApplication();
		
		tokenField = new TextField();
		browserButton = new ButtonField();
		nextButton = new ButtonField();
		
		setTitle("Authentication");
		add(new LabelField("Paste your token into field below:"));
		
		add(tokenField);
		
		add(new LabelField("To get the token, press button below:"));
		
		browserButton.setLabel("Open browser");
		browserButton.setChangeListener(this);
		add(browserButton);
		
		nextButton.setLabel("Next");
		nextButton.setChangeListener(this);
		add(nextButton);
	}

	public void fieldChanged(Field field, int context) {
		if (field.hashCode() == browserButton.hashCode()) {
			Browser.getDefaultSession().displayPage(ApiHelper.API_TOKEN_URL);
			return;
		}
		
		if (field.hashCode() == nextButton.hashCode()) {
			String token = tokenField.getText();
			if (token.length() > 0) {
				OptionsData data = Options.getInstance().getData();
				data.setToken(token);
				data.commit();
			}
			
			_app.popScreen(getScreen());
			_app.pushScreen(new AppScreen());
			return;
		}
	}
}
