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
import net.rim.device.api.ui.Manager;
import net.rim.device.api.ui.component.BitmapField;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.component.ListField;
import net.rim.device.api.ui.component.ListFieldCallback;
import net.rim.device.api.ui.container.HorizontalFieldManager;
import net.rim.device.api.util.Arrays;
import net.rim.device.api.util.StringComparator;


public class AppScreen extends VKScreen implements ListFieldCallback, RequestingImage, RequestingApi {
	private HorizontalFieldManager _upperShelf;
	private ListField _actionsList;
		
	private BitmapField _avatar;
	private LabelField _name;
	
	private final String[] _items = {
			"Conversations",
			"Friends",
			"Exit"
	};
	
	public AppScreen() {
		super();
		setTitle("VK");

		_upperShelf = new HorizontalFieldManager();
		add(_upperShelf);
		
		_avatar = new BitmapField();
		_upperShelf.add(_avatar);
		
		_name = new LabelField("Loading...");
		_upperShelf.add(_name);
		
		_actionsList = new ListField();
		_actionsList.setCallback(this);
		_actionsList.setSize(_items.length);
		add(_actionsList);
		
		RequestedApi request = new RequestedApi();
		request.urlTemplate = ApiHelper.API_CURRENTUSER_URL;
		request.callArgs = new Object[] {"photo_50"};
		request.type = RequestedApi.ARRAY;
		
		ApiFetchThread.enqueue(request, this);
	}
	
	protected boolean navigationClick(int status, int time) {
		if ((status & KeypadListener.STATUS_FOUR_WAY) != 0) {
			switch (_actionsList.getSelectedIndex()) {
				case 0:
					_app.pushScreen(new ConversationsScreen());
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

	public void setResponse(RequestedImage info, EncodedImage result) {
		if (result != null) {
			final Bitmap bitmap = result.getBitmap();
			
			_app.invokeLater(new Runnable() {
				public void run() {
					_avatar.setBitmap(bitmap);
					
					Manager manager = _name.getManager();
					LabelField padded = new LabelField(_name.getText());
					
					padded.setPosition(8);
					manager.replace(_name, padded);
					
					_name = padded;
				}
			});
		}
	}

	public void setResponse(RequestedApi info, Object result) {
		JSONArray responseArray = (JSONArray) result;
		
		JSONObject profileInfo = null;
		if (responseArray != null) {
			try {
				profileInfo = responseArray.getJSONObject(0);
			}
			catch (JSONException e) {
				
			}
		}
		
		if (profileInfo != null) {
			try {
				RequestedImage avatar = new RequestedImage();
				avatar.tag = "avatar";
				avatar.url = profileInfo.getString("photo_50");
				
				ImageFetchThread.enqueue(avatar, this);
			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
				
			try {
				final String name = profileInfo.getString("first_name") + " " + profileInfo.getString("last_name");
				
				_app.invokeLater(new Runnable() {
					public void run() {
						_name.setText(name);
						_upperShelf.invalidate();
					}
				});
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
