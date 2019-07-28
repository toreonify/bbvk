package com.toreonify.bbvk.ui;

import com.toreonify.bbvk.api.Api;

import net.rim.device.api.system.Application;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.container.MainScreen;

public class VKScreen extends MainScreen {
	protected Api _api = Api.getInstance();
	protected UiApplication _app = UiApplication.getUiApplication();
}
