package com.sshtools.twoslices.impl;

import com.sshtools.twoslices.AbstractToaster;
import com.sshtools.twoslices.ToastType;
import com.sshtools.twoslices.ToasterSettings;

/**
 * Last resort implementation that outputs to the console.
 */
public class SysOutNotifier extends AbstractToaster {

	/**
	 * Constructor
	 * 
	 * @param configuration
	 *            configuration
	 */
	public SysOutNotifier(ToasterSettings configuration) {
		super(configuration);
	}

	@Override
	public void toast(ToastType type, String icon, String title, String content) {
		System.out.println(String.format("[%1s] %s - %s", textIcon(type), title, content));
	}

}
