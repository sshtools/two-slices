package com.sshtools.twoslices;

public abstract class AbstractToaster implements Toaster {
	
	protected ToasterSettings configuration;

	protected AbstractToaster(ToasterSettings configuration) {
		this.configuration = configuration;
	}

	@Override
	public void toast(ToastType type, String title, String content) {
		toast(type, null, title, content);
	}

	protected String textIcon(ToastType messageType) {
		switch (messageType) {
		case INFO:
			return "!";
		case WARNING:
			return "∆";
		case ERROR:
			return "☠";
		default:
			return "";
		}
	}
}
