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
package com.sshtools.twoslices.impl;

import java.awt.AWTException;
import java.awt.EventQueue;
import java.awt.Image;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import com.sshtools.twoslices.AbstractToaster;
import com.sshtools.twoslices.ToastActionListener;
import com.sshtools.twoslices.ToastType;
import com.sshtools.twoslices.ToasterSettings;
import com.sshtools.twoslices.ToasterSettings.SystemTrayIconMode;
import com.sshtools.twoslices.ToasterException;

/**
 * Fall-back notifier for when a native notification system cannot be located or
 * used, and AWT toolkit is available.
 */
public class AWTNotifier extends AbstractToaster {

	private Thread timer;
	private TrayIcon trayIcon;

	/**
	 * Constructor
	 * 
	 * @param configuration
	 *            configuration
	 */
	public AWTNotifier(ToasterSettings configuration) {
		super(configuration);
		try {
			Class.forName("java.awt.SystemTray");
			if (!hasTray())
				throw new UnsupportedOperationException();
		} catch (ClassNotFoundException cnfe) {
			throw new UnsupportedOperationException();
		}
	}

	@Override
	public void toast(ToastType type, String icon, String title, String content, ToastActionListener... listeners) {
		final SystemTray tray = SystemTray.getSystemTray();
		EventQueue.invokeLater(() -> {
			try {
				if (trayIcon == null) {
					if (configuration.getParent() != null) {
						trayIcon = (TrayIcon) configuration.getParent();
					} else if (icon == null || icon.length() == 0) {
						trayIcon = new TrayIcon(getPlatformImage(getTypeImage(type)), title);
						tray.add(trayIcon);
					}
					else {
						trayIcon = new TrayIcon(getPlatformImage(ImageIO.read(new File(icon))), title);
						tray.add(trayIcon);
					}
				} else {
					if (icon == null || icon.length() == 0) {
						trayIcon.setImage(getPlatformImage(getTypeImage(type)));
					} else
						trayIcon.setImage(getPlatformImage(ImageIO.read(new File(icon))));
					trayIcon.setToolTip(title);
					if(timer != null)
						timer.interrupt();
				}
				trayIcon.displayMessage(title, content, TrayIcon.MessageType.valueOf(type.name()));
				timer = new Thread("AWTNotifierWait") {
					@Override
					public void run() {
						try {
							Thread.sleep(configuration.getTimeout() * 1000);
							timer = null;
							if (configuration.getSystemTrayIconMode() != SystemTrayIconMode.SHOW_DEFAULT_ALWAYS) {
								if (configuration.getParent() == null) {
									tray.remove(trayIcon);
								}
								trayIcon = null;
							}
						} catch (InterruptedException ie) {
							// New one coming in
						}
					}
				};
				timer.start();
			} catch (IOException ioe) {
				throw new ToasterException(String.format("Failed to show toast for %s: %s", type, title), ioe);
			} catch (AWTException e) {
				throw new ToasterException(String.format("Failed to show toast for %s: %s", type, title), e);
			}
		});
	}

	private Image getPlatformImage(Image image) throws IOException {
		String osname = System.getProperty("os.name");
		int sz = 48;
		if (osname.toLowerCase().indexOf("windows") != -1)
			sz = 16;
		else if (osname.toLowerCase().indexOf("linux") != -1)
			sz = 24;
		return image.getScaledInstance(sz, sz, Image.SCALE_SMOOTH);
	}

	private Image getTypeImage(ToastType type) throws IOException {
		if (configuration.getSystemTrayIconMode() == SystemTrayIconMode.HIDDEN) {
			String osname = System.getProperty("os.name");
			if (osname.toLowerCase().indexOf("windows") != -1)
				return ImageIO.read(getClass().getResource("/images/blank-48.gif"));
			else
				return ImageIO.read(getClass().getResource("/images/blank-48.png"));
		} else if ((configuration.getSystemTrayIconMode() == SystemTrayIconMode.SHOW_DEFAULT_WHEN_ACTIVE
				|| configuration.getSystemTrayIconMode() == SystemTrayIconMode.SHOW_DEFAULT_ALWAYS)
				&& configuration.getDefaultImage() != null) {
			return ImageIO.read(configuration.getDefaultImage());
		} else
			return ImageIO.read(getClass().getResource("/images/dialog-"
					+ (type.equals(ToastType.NONE) ? ToastType.INFO : type).name().toLowerCase() + "-48.png"));
	}

	private boolean hasTray() {
		return SystemTray.isSupported();
	}
}
