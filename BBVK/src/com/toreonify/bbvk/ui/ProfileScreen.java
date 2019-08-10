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
import net.rim.device.api.ui.component.BitmapField;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.container.HorizontalFieldManager;
import net.rim.device.api.ui.container.VerticalFieldManager;
import net.rim.device.api.ui.container.MainScreen;

public class ProfileScreen extends VKScreen implements RequestingImage, RequestingApi {
	private BitmapField _avatar;
	private VerticalFieldManager _infoColumn;
	
	public ProfileScreen(String id) {
		super();
		setTitle("Profile – loading...");
		
		HorizontalFieldManager upperShelf = new HorizontalFieldManager();
		add(upperShelf);
		
		_avatar = new BitmapField();
		upperShelf.add(_avatar);
		
		_infoColumn = new VerticalFieldManager();
		upperShelf.add(_infoColumn);
		
		RequestedApi request = new RequestedApi();
		request.urlTemplate = ApiHelper.API_USER_URL;
		request.callArgs = new Object[] {id, "photo_50,sex,city,home_town,country,bdate,online,contacts,counters"};
		request.type = RequestedApi.ARRAY;
		
		ApiFetchThread.enqueue(request, this);		
	}

	public void setResponse(RequestedImage info, EncodedImage result) {
		if (result != null) {
			final Bitmap bitmap = result.getBitmap();
			
			_app.invokeLater(new Runnable() {
				public void run() {
					_avatar.setBitmap(bitmap);
					((MainScreen)_app.getActiveScreen()).setTitle("Profile");
				}
			});
		}
	}

	public void setResponse(RequestedApi info, Object result) {
		if (result != null) {
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
				
				final JSONObject finalProfileInfo = profileInfo;
				_app.invokeAndWait(new Runnable() {
					public void run() {
						try {
							LabelField name = new LabelField(finalProfileInfo.getString("first_name") + " " + finalProfileInfo.getString("last_name"));
							name.setPosition(8);
							_infoColumn.add(name);
							
							String status = "offline";
							
							if (finalProfileInfo.getInt("online") == 1) {
								status = "online";
							}
							
							LabelField online = new LabelField(status);
							online.setPosition(8);
							_infoColumn.add(online);
							
							if (!finalProfileInfo.isNull("country")) {
								String locationString = finalProfileInfo.getJSONObject("country").getString("title");
								
								if (!finalProfileInfo.isNull("city")) {
									locationString += ", " + finalProfileInfo.getJSONObject("city").getString("title");
								}
								
								if (!finalProfileInfo.isNull("home_town")) {
									locationString += " (from " + finalProfileInfo.getString("home_town") + ")";
								}
								
								LabelField location = new LabelField(locationString);
								location.setPosition(8);
								_infoColumn.add(location);
							}
							
						} catch (JSONException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				});
				
			}
			
			
		}
	}
}
