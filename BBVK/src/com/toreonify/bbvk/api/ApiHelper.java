package com.toreonify.bbvk.api;

public class ApiHelper {
	public static final char PARAM_SEPARATOR = '$';
	
	public static final String API_TOKEN_URL = "https://oauth.vk.com/authorize?client_id=7065224&display=page&redirect_uri=blank.html&scope=friends,photos,status,offline,docs,groups,notifications&response_type=token&v=5.92";
	
	public static final String API_HOST = "https://api.vk.com/method/";
	
	// Account methods
	public static final String API_PROFILEINFO_URL = "account.getProfileInfo?v=5.92&access_token=$";
	// Messages methods	
	public static final String API_CONVERSATIONS_URL = "messages.getConversations?v=5.92&access_token=$&offset=$&count=$&filter=$&extended=0";
	// Friends methods
	public static final String API_FRIENDS_URL = "friends.get?v=5.92&access_token=$&offset=0&order=name&fields=photo_50,online";	
	// Users methods
	public static final String API_CURRENTUSER_URL = "users.get?v=5.92&access_token=$&fields=$";
	public static final String API_USER_URL = "users.get?v=5.92&access_token=$&user_ids=$&fields=$";
	
	public static String formatApiCall(String apiTemplate, Object[] args) {
		String result = apiTemplate;
		
		for (int i = 0; i < args.length; i++) {
			int param_idx = result.indexOf(PARAM_SEPARATOR);
			
			String before = result.substring(0, param_idx);
			String after = result.substring(param_idx + 1, result.length()); 
			
			result = before + args[i].toString() + after;
		}
		
		return API_HOST + result;
	}
}
