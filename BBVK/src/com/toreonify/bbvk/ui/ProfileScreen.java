package com.toreonify.bbvk.ui;

import org.json.me.JSONArray;
import org.json.me.JSONException;
import org.json.me.JSONObject;

import com.toreonify.bbvk.api.ApiException;
import com.toreonify.bbvk.api.ApiHelper;
import com.toreonify.bbvk.net.ImageFetchThread;
import com.toreonify.bbvk.net.RequestedImage;
import com.toreonify.bbvk.net.Requesting;

import net.rim.device.api.system.Bitmap;
import net.rim.device.api.system.EncodedImage;
import net.rim.device.api.ui.Manager;
import net.rim.device.api.ui.component.BitmapField;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.container.HorizontalFieldManager;
import net.rim.device.api.ui.container.VerticalFieldManager;

public class ProfileScreen extends VKScreen implements Requesting {
	private BitmapField _avatar;
	
	public ProfileScreen(String id) {
		super();
		setTitle("Profile");
		
		HorizontalFieldManager upperShelf = new HorizontalFieldManager();
		add(upperShelf);
		
		JSONArray responseArray = null;
		try {
			responseArray = _api.callArray(ApiHelper.API_USER_URL, new Object[] {id, "photo_50,sex,city,home_town,country,bdate,online,contacts,counters"});
		} catch (ApiException e) {
			
		}
		
		JSONObject profileInfo = null;
		if (responseArray != null) {
			try {
				profileInfo = responseArray.getJSONObject(0);
			}
			catch (JSONException e) {
				
			}
		}
		
		if (profileInfo != null) {
			_avatar = new BitmapField();
			upperShelf.add(_avatar);
			
			try {
				RequestedImage avatar = new RequestedImage();
				avatar.tag = "avatar";
				avatar.url = profileInfo.getString("photo_50");
				
				ImageFetchThread.enqueue(avatar, this);
			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		
		VerticalFieldManager infoColumn = new VerticalFieldManager();
		upperShelf.add(infoColumn);
		
		try {
			LabelField name = new LabelField(profileInfo.getString("first_name") + " " + profileInfo.getString("last_name"));
			name.setPosition(8);
			infoColumn.add(name);
			
			String status = "offline";
			
			if (profileInfo.getInt("online") == 1) {
				status = "online";
			}
			
			LabelField online = new LabelField(status);
			online.setPosition(8);
			infoColumn.add(online);
			
			if (!profileInfo.isNull("country")) {
				String locationString = profileInfo.getJSONObject("country").getString("title");
				
				if (!profileInfo.isNull("city")) {
					locationString += ", " + profileInfo.getJSONObject("city").getString("title");
				}
				
				if (!profileInfo.isNull("home_town")) {
					locationString += " (from " + profileInfo.getString("home_town") + ")";
				}
				
				LabelField location = new LabelField(locationString);
				location.setPosition(8);
				infoColumn.add(location);
			}
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}

	public void setResponse(RequestedImage info, EncodedImage result) {
		if (result != null) {
			final Bitmap bitmap = result.getBitmap();
			
			_app.invokeLater(new Runnable() {
				public void run() {
					_avatar.setBitmap(bitmap);
				}
			});
		}
	}
}
