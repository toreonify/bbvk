package com.toreonify.bbvk.ui;

import org.json.me.JSONException;
import org.json.me.JSONObject;

import com.toreonify.bbvk.api.Api;
import com.toreonify.bbvk.api.ApiException;
import com.toreonify.bbvk.api.ApiHelper;

import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.container.MainScreen;

public class MessagesScreen extends MainScreen implements FieldChangeListener {
	private UiApplication _app;
	private Api _api;
	
	public MessagesScreen() {
		_app = UiApplication.getUiApplication();
		_api = Api.getInstance();
		
		setTitle("Messages");
		
		JSONObject conversations = null;
		try {
			conversations = _api.call(ApiHelper.API_CONVERSATIONS_URL, new Object[] {Integer.toString(0), Integer.toString(20), "all"});
		} catch (ApiException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if (conversations != null) {
			try {
				add(new LabelField(conversations.getJSONArray("items").toString()));
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			add(new LabelField("Failed to retreive messages, try again later."));
		}
	}

	public void fieldChanged(Field field, int context) {
		// TODO Auto-generated method stub
		
	}

}
