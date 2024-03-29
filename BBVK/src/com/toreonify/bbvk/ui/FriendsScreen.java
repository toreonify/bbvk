package com.toreonify.bbvk.ui;

import org.json.me.JSONArray;
import org.json.me.JSONException;
import org.json.me.JSONObject;

import com.toreonify.bbvk.api.ApiFetchThread;
import com.toreonify.bbvk.api.ApiHelper;
import com.toreonify.bbvk.api.RequestedApi;
import com.toreonify.bbvk.api.RequestingApi;
import com.toreonify.bbvk.net.ImageFetchThread;
import com.toreonify.bbvk.net.RequestedImage;
import com.toreonify.bbvk.net.RequestingImage;

import net.rim.device.api.system.Bitmap;
import net.rim.device.api.system.EncodedImage;
import net.rim.device.api.system.KeypadListener;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.component.ListField;
import net.rim.device.api.ui.component.ListFieldCallback;

public class FriendsScreen extends VKScreen implements ListFieldCallback, RequestingImage, RequestingApi {
	private ListField _friendsList;	
	
	private JSONArray _friends;
	private EncodedImage[] _avatars;
	
	public FriendsScreen() {
		super();
		setTitle("Friends – loading...");

		_friendsList = new ListField();
		_friendsList.setCallback(this);
		_friendsList.setRowHeight(50); // 50 = size of avatar
		
		add(_friendsList);
		
		RequestedApi request = new RequestedApi();
		request.urlTemplate = ApiHelper.API_FRIENDS_URL;
		request.callArgs = null;
		request.type = RequestedApi.OBJECT;
		
		ApiFetchThread.enqueue(request, this);
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

	protected boolean navigationClick(int status, int time) {
		if ((status & KeypadListener.STATUS_FOUR_WAY) != 0) {
			int index = _friendsList.getSelectedIndex();
			JSONObject friend = null;
			
			try {
				friend = _friends.getJSONObject(index);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			String id = null;
			if (friend != null) {
				try {
					id = friend.getString("id");
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			if (id != null) {
				_app.pushScreen(new ProfileScreen(id));
			}
		}
		
		return true;
	}

	public void setResponse(RequestedApi info, Object result) {
		if (result != null) {
			JSONObject friendsInfo = (JSONObject) result;
			
			try {
				final int count = friendsInfo.getInt("count");
				final JSONArray items = friendsInfo.getJSONArray("items");
				
				_app.invokeAndWait(new Runnable() {
					public void run() {
						_friendsList.setSize(count);
						_friends = items;
						_avatars = new EncodedImage[count];
						
						setTitle("Friends – " + Integer.toString(count) + " total");	
					}
				});
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
