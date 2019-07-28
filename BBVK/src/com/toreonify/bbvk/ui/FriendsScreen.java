package com.toreonify.bbvk.ui;

import org.json.me.JSONArray;
import org.json.me.JSONException;
import org.json.me.JSONObject;

import com.toreonify.bbvk.api.Api;
import com.toreonify.bbvk.api.ApiException;
import com.toreonify.bbvk.api.ApiHelper;
import com.toreonify.bbvk.net.ImageFetchThread;
import com.toreonify.bbvk.net.RequestedImage;
import com.toreonify.bbvk.net.Requesting;

import net.rim.device.api.system.Bitmap;
import net.rim.device.api.system.EncodedImage;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.ListField;
import net.rim.device.api.ui.component.ListFieldCallback;
import net.rim.device.api.ui.container.MainScreen;

public class FriendsScreen extends MainScreen implements ListFieldCallback, Requesting {
	private UiApplication _app;
	private Api _api;
	private ListField _friendsList;	
	
	private JSONArray _friends;
	private EncodedImage[] _avatars;
	
	public FriendsScreen() {
		_app = UiApplication.getUiApplication();
		_api = Api.getInstance();
		
		setTitle("Friends");

		JSONObject friendsInfo = null;
		try {
			friendsInfo = _api.call(ApiHelper.API_FRIENDS_URL, null);
		} catch (ApiException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		_friendsList = new ListField();
		_friendsList.setCallback(this);
		_friendsList.setRowHeight(50); // 50 = size of avatar
		
		try {
			int count = friendsInfo.getInt("count");
			
			_friendsList.setSize(count);
			_friends = friendsInfo.getJSONArray("items");
			
			_avatars = new EncodedImage[count];
			
			setTitle("Friends – " + Integer.toString(count) + " total");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		add(_friendsList);
	}
	
	public void drawListRow(ListField listField, Graphics graphics, int index, int y, int width) {
		JSONObject selected = null;
		try {
			selected = (JSONObject) _friends.get(index);
			String name = selected.getString("first_name") + " " + selected.getString("last_name");
			String status = "offline";
			
			if (selected.getInt("online") == 1) {
				status = "online";
			}
			
			graphics.drawText(name, 54, y);
			graphics.drawText(status, 54, y + (16 + 8));
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
		graphics.drawText("?", 10, y + 10);
	
		if (_avatars[index] != null) {
			Bitmap bitmap = _avatars[index].getBitmap();
			graphics.drawBitmap(0, y, 50, 50, bitmap, 0, 0);
		} else {
			if (selected != null)
				try {
					RequestedImage avatar = new RequestedImage();
					avatar.tag = Integer.toString(index);
					avatar.url = selected.getString("photo_50");
					
					ImageFetchThread.enqueue(avatar, this);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
	}

	public Object get(ListField listField, int index) {
		try {
			return _friends.getJSONObject(index);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			
			return null;
		}
	}

	public int getPreferredWidth(ListField listField) {
		return 16;
	}

	public int indexOfList(ListField listField, String prefix, int start) {
		return -1;
	}

	public void setResponse(RequestedImage info, EncodedImage result) {
		final int index = Integer.parseInt((String)info.tag);
		if (result != null) {
			synchronized (_avatars) {
				_avatars[index] = result;
			}
			_friendsList.invalidate(index);
		}
	}

}
