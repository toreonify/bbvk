package com.toreonify.bbvk.ui;

import org.json.me.JSONException;
import org.json.me.JSONObject;

import com.toreonify.bbvk.api.Api;
import com.toreonify.bbvk.api.ApiException;
import com.toreonify.bbvk.api.ApiHelper;

import net.rim.device.api.system.KeypadListener;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.component.ListField;
import net.rim.device.api.ui.component.ListFieldCallback;
import net.rim.device.api.ui.container.MainScreen;
import net.rim.device.api.util.Arrays;
import net.rim.device.api.util.StringComparator;


public class AppScreen extends MainScreen implements ListFieldCallback {
	private UiApplication _app;
	private Api _api;
	private ListField _actionsList;	
	
	private final String[] _items = {
			"Messages",
			"Friends",
			"Exit"
	};
	
	public AppScreen() {
		_app = UiApplication.getUiApplication();
		_api = Api.getInstance();
		
		setTitle("VK");

		JSONObject profileInfo = null;
		try {
			profileInfo = _api.call(ApiHelper.API_PROFILEINFO_URL, null);
		} catch (ApiException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		try {
			add(new LabelField(profileInfo.getString("first_name") + " " + profileInfo.getString("last_name")));
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		_actionsList = new ListField();
		_actionsList.setCallback(this);
		_actionsList.setSize(_items.length);
		add(_actionsList);
	}
	
	protected boolean navigationClick(int status, int time) {
		if ((status & KeypadListener.STATUS_FOUR_WAY) != 0) {
			switch (_actionsList.getSelectedIndex()) {
				case 0:
					_app.pushScreen(new MessagesScreen());
					break;
				case 1:
					_app.pushScreen(new FriendsScreen());
					break;
				case 2:
					System.exit(0);
					break;
				default:
			}
		}
		
		return true;
	}

	public void drawListRow(ListField listField, Graphics graphics, int index, int y, int width) {
		graphics.drawText(_items[index], 0, y);
	}

	public Object get(ListField listField, int index) {
		return _items[index];
	}

	public int getPreferredWidth(ListField listField) {
		return 16;
	}

	public int indexOfList(ListField listField, String prefix, int start) {
		return Arrays.binarySearch(_items, prefix, StringComparator.getInstance(true), start, _items.length);
	}
}
