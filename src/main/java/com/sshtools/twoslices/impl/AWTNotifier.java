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
import com.sshtools.twoslices.ToastType;
import com.sshtools.twoslices.ToasterConfiguration;
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
	public AWTNotifier(ToasterConfiguration configuration) {
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
	public void toast(ToastType type, String icon, String title, String content) {
		final SystemTray tray = SystemTray.getSystemTray();
		EventQueue.invokeLater(() -> {
			try {
				if (trayIcon == null) {
					if (icon == null || icon.length() == 0)
						trayIcon = new TrayIcon(ImageIO.read(getClass().getResource("/images/dialog-"
								+ (type.equals(ToastType.NONE) ? ToastType.INFO : type).name().toLowerCase()
								+ "-48.png")).getScaledInstance(24, 24, Image.SCALE_SMOOTH), title);
					else
						trayIcon = new TrayIcon(ImageIO.read(new File(icon)), title);
					tray.add(trayIcon);
				} else {
					timer.interrupt();
				}
				trayIcon.displayMessage(title, content, TrayIcon.MessageType.valueOf(type.name()));
				timer = new Thread("AWTNotifierWait") {
					@Override
					public void run() {
						try {
							Thread.sleep(configuration.getTimeout() * 1000);
							timer = null;
							tray.remove(trayIcon);
							trayIcon = null;
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

	private boolean hasTray() {
		return SystemTray.isSupported();
	}
}
