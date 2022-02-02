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
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolTip;
import org.eclipse.swt.widgets.TrayItem;

import com.sshtools.twoslices.AbstractToaster;
import com.sshtools.twoslices.ToastBuilder;
import com.sshtools.twoslices.ToastType;
import com.sshtools.twoslices.ToasterSettings;
import com.sshtools.twoslices.ToasterSettings.SystemTrayIconMode;

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
	private int lastSwtCode;

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
		} catch (ClassNotFoundException | NoClassDefFoundError cnfe) {
			throw new UnsupportedOperationException();
		}
	}

	@Override
	public void toast(ToastBuilder builder) {
		synchronized (lock) {
			Display display = Display.getDefault();
			if (!ready) {
				long now = System.currentTimeMillis();
				if (now < started + STARTUP_WAIT) {
					display.asyncExec(() -> display.timerExec((int) ((started + STARTUP_WAIT) - now), () -> {
						ready = true;
						toast(builder);
					}));
					return;
				}
			}
			display.asyncExec(() -> {
				synchronized (lock) {
					int swtCode = typeToSWTCode(builder.type());
					if (tip == null || swtCode != lastSwtCode) {
						if (tip != null)
							tip.dispose();
						tip = new ToolTip(shell, SWT.BALLOON | swtCode);
						lastSwtCode = swtCode;
						tip.setAutoHide(false);
					} else {
						timer.interrupt();
					}
					doShow(builder, display);
				}
			});
		}
	}

	private int typeToSWTCode(ToastType type) {
		switch (type) {
		case ERROR:
			return SWT.ICON_ERROR;
		case WARNING:
			return SWT.ICON_WARNING;
		case NONE:
			return SWT.NONE;
		default:
			return SWT.ICON_INFORMATION;
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
			if (d == null || d.getSystemTray() == null)
				return false;
		} catch (SWTException e) {
		}
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
		} catch (SWTException se) {
		}
		return result[0];
	}

	private void doShow(ToastBuilder builder, Display display) {
		tip.setMessage(builder.content());
		if (configuration.getParent() != null && lastImage == null) {
			lastImage = item.getImage();
		}
		String icon = builder.icon();
		if (icon == null || icon.length() == 0)
			try {
				item.setImage(getPlatformImage(getTypeImage(builder.type())));
			} catch (IOException e1) {
				try {
					item.setImage(getPlatformImage(getTypeImage(null)));
				} catch (IOException e) {
					// Give up
				}
			}
		else
			item.setImage(getPlatformImage(new Image(display, icon)));
		tip.setText(builder.title());
		item.setToolTip(tip);
		tip.setVisible(true);
		item.setVisible(true);
		timer = new Thread("SWTNotifierWait") {
			@Override
			public void run() {
				try {
					Thread.sleep((builder.timeout() == -1 ? configuration.getTimeout() : builder.timeout()) * 1000);
					synchronized (lock) {
						ToolTip fTip = tip;
						display.asyncExec(() -> {
							if (fTip != null)
								fTip.dispose();
							if (configuration.getParent() == null) {
								try {
									item.setImage(getPlatformImage(getTypeImage(null)));
								} catch (IOException e) {
									item.setVisible(false);
									ready = false;
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

	private Image getPlatformImage(Image image) {
		String osname = System.getProperty("os.name");
		int sz = 48;
		if (osname.toLowerCase().indexOf("windows") != -1)
			sz = 16;
		else if (osname.toLowerCase().indexOf("linux") != -1)
			sz = 24;
		ImageData data = image.getImageData();
		data = data.scaledTo(sz, sz);
		Image img = new Image(image.getDevice(), data, data);
		image.dispose();
		return img;
	}

	private Image getTypeImage(ToastType type) throws IOException {
		Display d = Display.getDefault();
		if (configuration.getSystemTrayIconMode() == SystemTrayIconMode.HIDDEN) {
			return new Image(d, getClass().getResourceAsStream("/images/blank-48.gif"));
		} else if (type == null || ((configuration.getSystemTrayIconMode() == SystemTrayIconMode.SHOW_DEFAULT_WHEN_ACTIVE
				|| configuration.getSystemTrayIconMode() == SystemTrayIconMode.SHOW_DEFAULT_ALWAYS)
				&& configuration.getDefaultImage() != null)) {
			return new Image(d, configuration.getDefaultImage().openStream());
		} else
			return new Image(d, getClass().getResourceAsStream(
					"/images/dialog-" + (type.equals(ToastType.NONE) ? ToastType.INFO : type).name().toLowerCase() + "-48.png"));
	}
}
