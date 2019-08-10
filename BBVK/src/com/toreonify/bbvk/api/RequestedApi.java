package com.toreonify.bbvk.api;

public class RequestedApi {
	public static final int ARRAY = 0;
	public static final int OBJECT = 1;
	
	public Object tag;
	public int type;
	public String urlTemplate;
	public Object[] callArgs;
}
