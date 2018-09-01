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

import java.io.IOException;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolTip;
import org.eclipse.swt.widgets.TrayItem;

import com.sshtools.twoslices.AbstractToaster;
import com.sshtools.twoslices.ToastType;
import com.sshtools.twoslices.ToasterSettings;

/**
 * Fall-back notifier for when no native notifier can be found or used and the
 * SWT toolkit is available.
 * <p>
 * This implementation supports {@link ToasterSettings#getParent()} and
 * {@link ToasterSettings#setParent(Object)}. The parent object must be an
 * instance of {@link TrayItem}. When provided, this tray item will be used as
 * the parent of the balloon messages.
 */
public class SWTToaster extends AbstractToaster {
	/**
	 * On Linux Cinnamon (probably others?), if we don't wait for a short while
	 * after the tray item has been added for it to actually be shown on the
	 * desktop, the position of the balloon will be incorrect, so we have to
	 * wait for at least this amount of until the first notification message can
	 * be shown. Subsequent notifications will not need to do this. At no point
	 * though will the calling thread be held up, the message will just take a
	 * short while to appear.
	 */
	final static int STARTUP_WAIT = 3000;
	private TrayItem item;
	private Object lock = new Object();
	private boolean ready;
	private Shell shell;
	private long started;
	private Thread timer;
	private ToolTip tip;
	private Image lastImage;

	/**
	 * Constructor
	 * 
	 * @param configuration configuration
	 */
	public SWTToaster(ToasterSettings configuration) {
		super(configuration);
		try {
			Class.forName("org.eclipse.swt.widgets.Tray");
			if (!hasTray())
				throw new UnsupportedOperationException();
			started = System.currentTimeMillis();
			init();
		} catch (ClassNotFoundException cnfe) {
			throw new UnsupportedOperationException();
		}
	}

	@Override
	public void toast(ToastType type, String icon, String title, String content) {
		synchronized (lock) {
			Display display = Display.getDefault();
			if (!ready) {
				long now = System.currentTimeMillis();
				if (now < started + STARTUP_WAIT) {
					display.asyncExec(() -> display.timerExec((int) ((started + STARTUP_WAIT) - now), () -> {
						ready = true;
						toast(type, icon, title, content);
					}));
					return;
				}
			}
			display.asyncExec(() -> {
				synchronized (lock) {
					if (tip == null) {
						tip = new ToolTip(shell, SWT.BALLOON | SWT.ICON_INFORMATION);
						tip.setAutoHide(false);
					} else {
						timer.interrupt();
					}
					doShow(type, icon, title, content, display);
				}
			});
		}
	}

	protected boolean hasTray() {
		/*
		 * Check for the tray. This has to be done in the SWT thread. Being as
		 * we don't know if there is a dispatch thread running, we submit a task
		 * and wait a short while.
		 */
		Display d = Display.getDefault();
		try {
			return d != null && d.getSystemTray() != null;
		} catch (SWTException e) {
			final boolean[] result = new boolean[1];
			final Semaphore sem = new Semaphore(1);
			try {
				sem.acquire();
				try {
					d.asyncExec(() -> {
						result[0] = d != null && d.getSystemTray() != null;
						sem.release();
					});
					sem.tryAcquire(250, TimeUnit.MILLISECONDS);
				} finally {
					sem.release();
				}
			} catch (InterruptedException ie) {
			}
			return result[0];
		}
	}

	private void doShow(ToastType type, String icon, String title, String content, Display display) {
		tip.setMessage(content);
		if (configuration.getParent() != null && lastImage == null) {
			lastImage = item.getImage();
		}
		if (icon == null || icon.length() == 0)
			item.setImage(new Image(display, getClass().getResourceAsStream(
					"/images/dialog-" + (type.equals(ToastType.NONE) ? ToastType.INFO : type).name().toLowerCase() + "-48.png")));
		else
			item.setImage(new Image(display, icon));
		tip.setText(title);
		item.setToolTip(tip);
		tip.setVisible(true);
		item.setVisible(true);
		timer = new Thread("SWTNotifierWait") {
			@Override
			public void run() {
				try {
					Thread.sleep(configuration.getTimeout() * 1000);
					synchronized (lock) {
						ToolTip fTip = tip;
						display.asyncExec(() -> {
							fTip.dispose();
							if (configuration.getParent() == null) {
								if (configuration.getIdleImage() == null) {
									item.setVisible(false);
									ready = false;
								} else {
									try {
										item.setImage(new Image(display, configuration.getIdleImage().openStream()));
									} catch (IOException e) {
										item.setVisible(false);
										ready = false;
									}
								}
							} else {
								if (lastImage != null) {
									item.setImage(lastImage);
									lastImage = null;
								}
							}
						});
						tip = null;
					}
				} catch (InterruptedException ie) {
				}
			}
		};
		timer.start();
	}

	private void init() {
		Display display = Display.getDefault();
		display.syncExec(() -> {
			if (configuration.getParent() != null && configuration.getParent() instanceof TrayItem) {
				item = (TrayItem) configuration.getParent();
			} else {
				item = new TrayItem(display.getSystemTray(), SWT.NONE);
			}
			shell = new Shell(display, SWT.NONE);
		});
	}
}
