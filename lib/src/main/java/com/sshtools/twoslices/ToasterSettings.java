/**
 * Copyright © 2018 SSHTOOLS Limited (support@sshtools.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.sshtools.twoslices;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * Allows some configuration of notification messages and their behaviour. These
 * are only hints, providers have no obligation to support any or all of these.
 * 
 * @see ToasterFactory#setSettings(ToasterSettings)
 */
public class ToasterSettings {

	public enum SystemTrayIconMode {
		HIDDEN, ORIGINAL, SHOW_TOAST_TYPE_WHEN_ACTIVE, SHOW_DEFAULT_WHEN_ACTIVE, SHOW_DEFAULT_ALWAYS
	}

	public enum Position {
		TL, T, TR, CL, C, CR, BL, B, BR
	}

	private String appName = "TwoSlices";
	private Object parent;
	private int timeout = 10;
	private URL defaultImage;
	private SystemTrayIconMode systemTrayIconMode = SystemTrayIconMode.SHOW_DEFAULT_WHEN_ACTIVE;
	private Position position;
	private Map<ToastHint, Object> hints = new HashMap<>();
	private String preferredToasterClassName = System.getProperty("twoslices.preferred");
	@Deprecated
	private Map<String, Object> properties = new HashMap<>();

	public ToasterSettings() {
		try {
			defaultImage = getClass().getResource("/images/idle-48.png");
		} catch (Exception e) {
		}
	}

	/**
	 * Get the class name of the preferred toaster (when there are multiple
	 * available). Ordinarily, the order is set by two slices itself, preferring the
	 * most native implementation, followed by the best looking generic
	 * implementation (all depending on what libraries are available). Each
	 * implementation is tried to see if it works at all, the first one that does is
	 * used. This property can override that, i.e. if the specified toaster has all of its dependencies
	 * available, initialises OK, then it is used. Otherwise the first available is used.
	 * 
	 * @return preferred toaster
	 */
	public String getPreferredToasterClassName() {
		return preferredToasterClassName;
	}

	/**
	 * Set the class name of the preferred toaster (when there are multiple
	 * available). Ordinarily, the order is set by two slices itself, preferring the
	 * most native implementation, followed by the best looking generic
	 * implementation (all depending on what libraries are available). Each
	 * implementation is tried to see if it works at all, the first one that does is
	 * used. This property can override that, i.e. if the specified toaster has all of its dependencies
	 * available, initialises OK, then it is used. Otherwise the first available is used.
	 * 
	 * @param preferredToasterClassName preferred toaster
	 */
	public void setPreferredToasterClassName(String preferredToasterClassName) {
		this.preferredToasterClassName = preferredToasterClassName;
	}

	/**
	 * Get the generic properties. These are used to pass toaster specific
	 * configuration.
	 * 
	 * Deprecated. Use {@#getHints()}.
	 * 
	 * @return properties
	 */
	@Deprecated
	public Map<String, Object> getProperties() {
		return properties;
	}

	/**
	 * Get the generic hints. These are used to pass toaster specific
	 * configuration.You can also pass hints additional hints in individual
	 * toast. Those will override any hints here.
	 * 
	 * @return hints
	 */
	public Map<ToastHint, Object> getHints() {
		return hints;
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
	 * {@link BasicSWTToaster}, then you can provide a 'parent' that the notifier can use
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
	 * {@link BasicSWTToaster}, then you can provide a 'parent' that the notifier can use
	 * for positioning and other inheritable properties. In the case of SWT, this
	 * would be a {@link TrayItem} which the message can attach to.
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
	 * Set the default image that might be displayed depending on
	 * {@link #getSystemTrayIconMode()}. Some implementations require an item on the
	 * <i>System tray</i> to be able to show messages, and may even require the icon
	 * is permanently visible for proper operation (such as SWT).
	 * 
	 * @param defaultImage the location of the default image.
	 * @return this instance for chaining
	 * @see #getDefaultImage()
	 */
	public ToasterSettings setDefaultImage(URL defaultImage) {
		this.defaultImage = defaultImage;
		return this;
	}

	/**
	 * Get the default image that might be displayed depending on the . Some
	 * implementations require an item on the <i>System tray</i> to be able to show
	 * messages, and may even require the icon is permanently visible for proper
	 * operation (such as SWT).
	 * 
	 * @return the location of the default image.
	 * @see #setDefaultImage(URL)
	 * @see #getSystemTrayIconMode()
	 */
	public URL getDefaultImage() {
		return defaultImage;
	}

	/**
	 * Get the hint as to how to treat the system tray icon (if one is needed for
	 * the platform in use.)
	 * 
	 * @return system tray icon mode
	 * @see #getDefaultImage()
	 * @see #setSystemTrayIconMode(SystemTrayIconMode)
	 */
	public SystemTrayIconMode getSystemTrayIconMode() {
		return systemTrayIconMode;
	}

	/**
	 * Set the hint as to how to treat the system tray icon (if one is needed for
	 * the platform in use.)
	 * 
	 * @param systemTrayIconMode system tray icon mode
	 * @see #getDefaultImage()
	 * @see #getSystemTrayIconMode()
	 */
	public void setSystemTrayIconMode(SystemTrayIconMode systemTrayIconMode) {
		this.systemTrayIconMode = systemTrayIconMode;
	}

	/**
	 * Get the position hint (if supported). Will be automatically determined when
	 * <code>null</code> (the default).
	 * 
	 * @return position hint
	 */
	public Position getPosition() {
		return position;
	}

	/**
	 * Set the position hint (if supported). Will be automatically determined when
	 * <code>null</code> (the default).
	 * 
	 * @param position position hint
	 */
	public void setPosition(Position position) {
		this.position = position;
	}

}
