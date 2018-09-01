package com.sshtools.twoslices;

import java.net.URL;

import org.eclipse.swt.widgets.TrayItem;

import com.sshtools.twoslices.impl.SWTToaster;

/**
 * Allows some configuration of notification messages and their behaviour. These
 * are only hints, providers have no obligation to support any or all of these.
 * 
 * @see ToasterFactory#setConfiguration(ToasterConfiguration)
 */
public class ToasterConfiguration {

	private String appName = "TwoSlices";
	private Object parent;
	private int timeout = 10;
	private URL idleImage;
	
	public ToasterConfiguration() {
		try {
			idleImage = getClass().getResource("/images/idle-48.png");
		}
		catch(Exception e) {
		}
	}

	/**
	 * Get the application name hint. This may be used by some notifiers to display
	 * the sending applications name.
	 * 
	 * @return name
	 * @see #setAppName(String)
	 */
	public String getAppName() {
		return appName;
	}

	/**
	 * If you are using a specific {@link Toaster} implementation, such as
	 * {@link SWTToaster}, then you can provide a 'parent' that the notifier can use
	 * for positioning and other inheritable properties. In the case of SWT, this
	 * would be a {@link TrayItem} which the message can attach to.
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
	 * Set the application name hint. This may be used by some notifiers to display
	 * the sending applications name.
	 * 
	 * @param name
	 *            application name hint
	 * @see #getAppName()
	 */
	public void setAppName(String appName) {
		this.appName = appName;
	}

	/**
	 * If you are using a specific {@link Toaster} implementation, such as
	 * {@link SWTToaster}, then you can provide a 'parent' that the notifier can use
	 * for positioning and other inheritable properties. In the case of SWT, this
	 * would be a {@link TrayItem} which the message can attach to.
	 * 
	 * @param parent
	 *            parent
	 * @see #getParent()
	 */
	public void setParent(Object parent) {
		this.parent = parent;
	}

	/**
	 * Set the timeout hint. When supported, the notification message will be
	 * visible for this long before being hidden.
	 * 
	 * @param timeout
	 *            timeout hint
	 * @see #getTimeout()
	 */
	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	/**
	 * Set the idle image that might be displayed when there are no messages. Some
	 * implementations require an item on the <em>System tray<em> to be able to show
	 * messages, and may even require the icon is permanently visible for proper
	 * operation (such as SWT).
	 * 
	 * @param idleImage
	 *            the location of the idle image.
	 * @see #getIdleImage()
	 */
	public void setIdleImage(URL idleImage) {
		this.idleImage = idleImage;
	}

	/**
	 * Get the idle image that might be displayed when there are no messages. Some
	 * implementations require an item on the <em>System tray<em> to be able to show
	 * messages, and may even require the icon is permanently visible for proper
	 * operation (such as SWT).
	 * 
	 * @return the location of the idle image.
	 * @see #setIdleImage(URL)
	 */
	public URL getIdleImage() {
		return idleImage;
	}
}
