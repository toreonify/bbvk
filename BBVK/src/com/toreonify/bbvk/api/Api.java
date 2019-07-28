package com.toreonify.bbvk.api;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.microedition.io.HttpsConnection;

import org.json.me.JSONException;
import org.json.me.JSONObject;

import com.toreonify.bbvk.Options;
import com.toreonify.bbvk.Options.OptionsData;
import com.toreonify.bbvk.net.Utilities;

public class Api {
	private static Api _instance;
	private static String _token;
	
	private Api() {
		OptionsData data = Options.getInstance().getData();
		
		_token = data.getToken();
	}
	
	public static Api getInstance() {
		if (_instance == null) {
			_instance = new Api();
		}
		
		return _instance;
	}
	
	public JSONObject call(String urlTemplate, Object[] callArgs) throws ApiException {
		Object[] urlArgs;
		if (callArgs == null) {
			urlArgs = new Object[] {_token};
		} else {
			urlArgs = new Object[callArgs.length + 1];
			urlArgs[0] = _token;
			
			System.arraycopy(callArgs, 0, urlArgs, 1, callArgs.length);
		}
		
		String profileInfo = ApiHelper.formatApiCall(urlTemplate, urlArgs);	
		String response = "";
		HttpsConnection apiConnection = Utilities.makeConnection(profileInfo, null, null, null);
		
		try {
			ByteArrayOutputStream result = new ByteArrayOutputStream();
			InputStream is = apiConnection.openInputStream();
			byte[] buffer = new byte[4096];
			int length;
			while ((length = is.read(buffer)) != -1) {
			    result.write(buffer, 0, length);
			}
			response = result.toString();
		}
		catch (IOException e) {
			// TODO can't get response 
			throw new ApiException();
		}
		
		JSONObject jsonObject = null;
		
		try {
			jsonObject = new JSONObject(response);
			jsonObject = jsonObject.getJSONObject("response");
		} catch (JSONException e) {
			// TODO invalid call, parse error
			throw new ApiException();
		}
		
		return jsonObject;
	}
}
