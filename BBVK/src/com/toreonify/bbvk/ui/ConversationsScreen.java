package com.toreonify.bbvk.ui;

import org.json.me.JSONArray;
import org.json.me.JSONException;
import org.json.me.JSONObject;

import com.toreonify.bbvk.JSONSearch;
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

public class ConversationsScreen extends VKScreen implements ListFieldCallback, RequestingImage, RequestingApi {
	private ListField _conversationsList;	
	
	private JSONArray _conversations;
	private JSONArray _profiles;
	private EncodedImage[] _avatars;
	
	public ConversationsScreen() {
		super();
		setTitle("Conversations – loading...");

		_conversationsList = new ListField();
		_conversationsList.setCallback(this);
		_conversationsList.setRowHeight(50); // 50 = size of avatar
		
		add(_conversationsList);
		
		RequestedApi request = new RequestedApi();
		request.urlTemplate = ApiHelper.API_CONVERSATIONS_URL;
		request.callArgs = new Object[] {"0", "10", "all"};
		request.type = RequestedApi.OBJECT;
		
		ApiFetchThread.enqueue(request, this);
	}
	
	public void drawListRow(ListField listField, Graphics graphics, int index, int y, int width) {
		JSONObject selected = null;
		JSONObject peer = null;
		try {
			selected = (JSONObject) _conversations.get(index);
			JSONObject conversation = selected.getJSONObject("conversation");
			JSONObject lastMessage = selected.getJSONObject("last_message");
			
			int peerId = lastMessage.getInt("peer_id");
			peer = JSONSearch.byInt(_profiles, "id", peerId);
			
			if (peer != null) {
				String name = peer.getString("first_name") + " " + peer.getString("last_name");
				String messageText = lastMessage.getString("text");
				
				graphics.drawText(name, 54, y);
				graphics.drawText(messageText, 54, y + (16 + 8));	
			}			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
		graphics.drawText("?", 10, y + 10);
	
		if (_avatars[index] != null) {
			Bitmap bitmap = _avatars[index].getBitmap();
			graphics.drawBitmap(0, y, 50, 50, bitmap, 0, 0);
		} else {
			if (peer != null)
				try {
					RequestedImage avatar = new RequestedImage();
					avatar.tag = Integer.toString(index);
					avatar.url = peer.getString("photo_50");
					
					ImageFetchThread.enqueue(avatar, this);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
	}

	public Object get(ListField listField, int index) {
		try {
			return _conversations.getJSONObject(index);
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
			_conversationsList.invalidate(index);
		}
	}

	protected boolean navigationClick(int status, int time) {
		if ((status & KeypadListener.STATUS_FOUR_WAY) != 0) {
			int index = _conversationsList.getSelectedIndex();
			JSONObject peer = null;
			
//			try {
//				//friend = _friends.getJSONObject(index);
//			} catch (JSONException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
			
//			String id = null;
//			if (friend != null) {
//				try {
//					id = friend.getString("id");
//				} catch (JSONException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//			}
//			
//			if (id != null) {
//				_app.pushScreen(new ProfileScreen(id));
//			}
		}
		
		return true;
	}

	public void setResponse(RequestedApi info, Object result) {
		JSONObject conversationsInfo = null;
		
		if (result != null) {
			conversationsInfo = (JSONObject)result;
		}
		
		if (conversationsInfo != null) {
			try {
				final int count = conversationsInfo.getInt("count");
				
				_conversations = conversationsInfo.getJSONArray("items");
				_profiles = conversationsInfo.getJSONArray("profiles");
				
				_avatars = new EncodedImage[count];
				
				_app.invokeAndWait(new Runnable() {
					public void run() {
						_conversationsList.setSize(count);
						setTitle("Conversations – " + Integer.toString(count) + " total");					
					}
				});
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
