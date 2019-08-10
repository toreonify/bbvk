package com.toreonify.bbvk;

import org.json.me.JSONArray;
import org.json.me.JSONException;
import org.json.me.JSONObject;

public class JSONSearch {
	public static JSONObject byObject(JSONArray array, String key, Object value) {
		for (int i = 0; i < array.length(); i++) {
			JSONObject current;
			try {
				current = array.getJSONObject(i);
				
				if (current != null && current.has(key)) {
					if (current.get(key) == value) {
						return current;
					}
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		return null;
	}

	public static JSONObject byInt(JSONArray array, String key, int value) {
		for (int i = 0; i < array.length(); i++) {
			JSONObject current;
			try {
				current = array.getJSONObject(i);
				
				if (current != null && current.has(key)) {
					if (current.getInt(key) == value) {
						return current;
					}
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		return null;
	}
}
