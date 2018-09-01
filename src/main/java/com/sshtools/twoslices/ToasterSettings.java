package com.sshtools.twoslices;

import java.net.URL;

import org.eclipse.swt.widgets.TrayItem;

import com.sshtools.twoslices.impl.SWTToaster;

/**
 * Allows some configuration of notification messages and their behaviour. These
 * are only hints, providers have no obligation to support any or all of these.
 * 
 * @see ToasterFactory#setSettings(ToasterSettings)
 */
public class ToasterSettings {
	private String appName = "TwoSlices";
	private Object parent;
	private int timeout = 10;
	private URL idleImage;

	public ToasterSettings() {
		try {
			idleImage = getClass().getResource("/images/idle-48.png");
		} catch (Exception e) {
		}
	}

	/**
	 * Get the application name hint. This may be used by some notifiers to
	 * display the sending applications name.
	 * 
	 * @return name
	 * @see #setAppName(String)
	 */
	public String getAppName() {
		return appName;
	}

	/**
	 * If you are using a specific {@link Toaster} implementation, such as
	 * {@link SWTToaster}, then you can provide a 'parent' that the notifier can
	 * use for positioning and other inheritable properties. In the case of SWT,
	 * this would be a {@link TrayItem} which the message can attach to.
	 * 
	 * @return parent
	 * @see #setParent(Object)
	 */
	public Object getParent() {
		return parent;
	}

	/**
	 * Get the timeout hint. When supported, the notification message will be
	 * visible for this long before being hidden.
	 * 
	 * @return timeout hint
	 * @see #setTimeout(int)
	 */
	public int getTimeout() {
		return timeout;
	}

	/**
	 * Set the application name hint. This may be used by some notifiers to
	 * display the sending applications name.
	 * 
	 * @param appName application name hint
	 * @return this instance for chaining
	 * @see #getAppName()
	 */
	public ToasterSettings setAppName(String appName) {
		this.appName = appName;
		return this;
	}

	/**
	 * If you are using a specific {@link Toaster} implementation, such as
	 * {@link SWTToaster}, then you can provide a 'parent' that the notifier can
	 * use for positioning and other inheritable properties. In the case of SWT,
	 * this would be a {@link TrayItem} which the message can attach to.
	 * 
	 * @param parent parent
	 * @return this instance for chaining
	 * @see #getParent()
	 */
	public ToasterSettings setParent(Object parent) {
		this.parent = parent;
		return this;
	}

	/**
	 * Set the timeout hint. When supported, the notification message will be
	 * visible for this long before being hidden.
	 * 
	 * @param timeout timeout hint
	 * @return this instance for chaining
	 * @see #getTimeout()
	 */
	public ToasterSettings setTimeout(int timeout) {
		this.timeout = timeout;
		return this;
	}

	/**
	 * Set the idle image that might be displayed when there are no messages.
	 * Some implementations require an item on the <i>System tray</i> to be
	 * able to show messages, and may even require the icon is permanently
	 * visible for proper operation (such as SWT).
	 * 
	 * @param idleImage the location of the idle image.
	 * @return this instance for chaining
	 * @see #getIdleImage()
	 */
	public ToasterSettings setIdleImage(URL idleImage) {
		this.idleImage = idleImage;
		return this;
	}

	/**
	 * Get the idle image that might be displayed when there are no messages.
	 * Some implementations require an item on the <i>System tray</i> to be
	 * able to show messages, and may even require the icon is permanently
	 * visible for proper operation (such as SWT).
	 * 
	 * @return the location of the idle image.
	 * @see #setIdleImage(URL)
	 */
	public URL getIdleImage() {
		return idleImage;
	}
}
