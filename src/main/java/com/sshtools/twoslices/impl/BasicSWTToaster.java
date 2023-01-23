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
import java.util.Arrays;
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
import com.sshtools.twoslices.Capability;
import com.sshtools.twoslices.Slice;
import com.sshtools.twoslices.ToastBuilder;
import com.sshtools.twoslices.ToastType;
import com.sshtools.twoslices.Toaster;
import com.sshtools.twoslices.ToasterService;
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
public class BasicSWTToaster extends AbstractToaster {
	
	public static class Service implements ToasterService {
		@Override
		public Toaster create(ToasterSettings settings) {
			return new BasicSWTToaster(settings);
		}
	}
	
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
	
	class SWTSlice implements Slice {
		
		private Display display;
		private boolean isClosed = false;
		
		private final String icon;
		private final ToastType type;
		private final String title;
		private final String content;
		private final int timeout;
		
		SWTSlice(ToastBuilder builder) {
			type = builder.type();
			icon = builder.icon();
			title = builder.title();
			content = builder.content();
			timeout = builder.timeout();
		}

		@Override
		public void close() throws IOException {
			synchronized (lock) {
				if(isClosed)
					return;
				isClosed = true;
				var fTip = tip;
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
		}
		
	}

	/**
	 * Constructor
	 * 
	 * @param configuration configuration
	 */
	public BasicSWTToaster(ToasterSettings configuration) {
		super(configuration);
		try {
			Class.forName("org.eclipse.swt.widgets.Tray");
			if (!hasTray())
				throw new UnsupportedOperationException();
			started = System.currentTimeMillis();
			capabilities.addAll(Arrays.asList(Capability.IMAGES, Capability.CLOSE));
			init();
		} catch (ClassNotFoundException | NoClassDefFoundError cnfe) {
			throw new UnsupportedOperationException();
		}
	}

	@Override
	public Slice toast(ToastBuilder builder) {
		return doToast(new SWTSlice(builder));
	}

	protected Slice doToast(SWTSlice slice) {
		synchronized (lock) {
			var display = Display.getDefault();
			if (!ready) {
				var now = System.currentTimeMillis();
				if (now < started + STARTUP_WAIT) {
					display.asyncExec(() -> display.timerExec((int) ((started + STARTUP_WAIT) - now), () -> {
						ready = true;
						doToast(slice);
					}));
					return slice;
				}
			}
			display.asyncExec(() -> {
				synchronized (lock) {
					var swtCode = typeToSWTCode(slice.type);
					if (tip == null || swtCode != lastSwtCode) {
						if (tip != null)
							tip.dispose();
						tip = new ToolTip(shell, SWT.BALLOON | swtCode);
						lastSwtCode = swtCode;
						tip.setAutoHide(false);
					} else {
						timer.interrupt();
					}
					doShow(display, slice);
				}
			});
			return slice;
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
		var d = Display.getDefault();
		try {
			if (d == null || d.getSystemTray() == null)
				return false;
			/* Alway in dispatch thread */
			return true;
		} catch (SWTException e) {
		}
		final var result = new boolean[1];
		final var sem = new Semaphore(1);
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

	private void doShow(Display display, SWTSlice slice) {
		slice.display = display;
		tip.setMessage(slice.content);
		var icon = slice.icon;
		if(configuration.getSystemTrayIconMode() != SystemTrayIconMode.ORIGINAL) {
			if (configuration.getParent() != null && lastImage == null) {
				lastImage = item.getImage();
			}
			if (icon == null || icon.length() == 0)
				try {
					item.setImage(getPlatformImage(getTypeImage(slice.type)));
				} catch (IOException e1) {
					try {
						item.setImage(getPlatformImage(getTypeImage(null)));
					} catch (IOException e) {
						// Give up
					}
				}
			else
				item.setImage(getPlatformImage(new Image(display, icon)));
			item.setToolTip(tip);
		}
		tip.setText(slice.title);
		tip.setVisible(true);
		item.setVisible(true);
		if(timer != null) {
			timer.interrupt();
		}
		timer = new Thread("SWTNotifierWait") {
			@Override
			public void run() {
				try {
					Thread.sleep((slice.timeout == -1 ? configuration.getTimeout() : slice.timeout) * 1000);
					slice.close();
				} catch (Exception ie) {
				} finally {
					slice.isClosed = true;
				}
			}
		};
		timer.start();
	}

	private void init() {
		var display = Display.getDefault();
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
		var osname = System.getProperty("os.name");
		int sz = 48;
		if (osname.toLowerCase().indexOf("windows") != -1)
			sz = 16;
		else if (osname.toLowerCase().indexOf("linux") != -1)
			sz = 24;
		var data = image.getImageData();
		data = data.scaledTo(sz, sz);
		var img = new Image(image.getDevice(), data, data);
		image.dispose();
		return img;
	}

	private Image getTypeImage(ToastType type) throws IOException {
		var d = Display.getDefault();
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
