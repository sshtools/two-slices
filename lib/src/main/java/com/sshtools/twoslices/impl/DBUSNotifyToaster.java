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
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.freedesktop.dbus.annotations.DBusInterfaceName;
import org.freedesktop.dbus.connections.impl.DBusConnection;
import org.freedesktop.dbus.connections.impl.DBusConnectionBuilder;
import org.freedesktop.dbus.exceptions.DBusException;
import org.freedesktop.dbus.interfaces.DBusInterface;
import org.freedesktop.dbus.messages.DBusSignal;
import org.freedesktop.dbus.types.UInt32;
import org.freedesktop.dbus.types.Variant;

import com.sshtools.twoslices.AbstractToaster;
import com.sshtools.twoslices.Capability;
import com.sshtools.twoslices.Slice;
import com.sshtools.twoslices.ToastActionListener;
import com.sshtools.twoslices.ToastBuilder;
import com.sshtools.twoslices.ToastBuilder.ToastAction;
import com.sshtools.twoslices.Toaster;
import com.sshtools.twoslices.ToasterService;
import com.sshtools.twoslices.ToasterSettings;

import uk.co.bithatch.nativeimage.annotations.Proxy;
import uk.co.bithatch.nativeimage.annotations.Reflectable;
import uk.co.bithatch.nativeimage.annotations.TypeReflect;

/**
 * Implementation for linux that uses the DBUS notification service.
 */
public class DBUSNotifyToaster extends AbstractToaster {
	
	public static class Service implements ToasterService {
		@Override
		public Toaster create(ToasterSettings settings) {
			return new DBUSNotifyToaster(settings);
		}
	}
	
	@DBusInterfaceName("org.freedesktop.Notifications")
	@Proxy
	@Reflectable
	@TypeReflect(methods = true, classes = true)
	public interface Notifications extends DBusInterface {

		void CloseNotification(int id);

		String[] GetCapabilities();

		String[] GetServerInformation();

		UInt32 Notify(String appName, UInt32 replacesId, String appIcon, String summary, String body,
				List<String> actions, Map<String, Variant<?>> hints, int expireTimeout);

		@Reflectable
		@TypeReflect(methods = true, constructors = true)
		public class ActionInvoked extends DBusSignal {

			private UInt32 id;
			private String action;

			public ActionInvoked(String path, UInt32 id, String action) throws DBusException {
				super(path);
				this.id = id;
				this.action = action;
			}

		}

		@Reflectable
		@TypeReflect(methods = true, constructors = true)
		public class NotificationClosed extends DBusSignal {

			private UInt32 id;
			private UInt32 reason;

			public NotificationClosed(String path, UInt32 id, UInt32 reason) throws DBusException {
				super(path);
				this.id = id;
				this.reason = reason;
			}

			public UInt32 getReason() {
				return reason;
			}

		}

	}

	private DBusConnection conn;
	private Notifications notifications;
	private Map<UInt32, ActiveNotification> actives = new HashMap<>();

	class ActiveNotification implements Slice {
		List<ToastAction> actions;
		ToastAction defaultAction;
		UInt32 id;
		ToastActionListener closed;
		public boolean destroyed;
		Set<Path> tempImagePath = new LinkedHashSet<>();
		
		@Override
		public void close() throws IOException {
			if(!destroyed) {
				notifications.CloseNotification(id.intValue());
			}
		}
	}

	/**
	 * Constructor
	 * 
	 * @param configuration configuration
	 */
	public DBUSNotifyToaster(ToasterSettings configuration) {
		super(configuration);
		
		if(!System.getProperty("os.name", "").toLowerCase().contains("linux"))
			throw new UnsupportedOperationException();
		
		capabilities.addAll(Arrays.asList(Capability.ACTIONS, Capability.CLOSE, Capability.DEFAULT_ACTION, Capability.IMAGES));
		try {
			conn = DBusConnectionBuilder.forSessionBus().build();

			/* https://github.com/hypfvieh/dbus-java/issues/159 */
//			conn.changeThreadCount((byte)1);

			notifications = conn.getRemoteObject("org.freedesktop.Notifications", "/org/freedesktop/Notifications",
					Notifications.class);

			conn.addSigHandler(Notifications.ActionInvoked.class, notifications, (s) -> {
				ActiveNotification active = null;
				synchronized (actives) {
					active = actives.get(s.id);
					if (active != null) {
						actives.remove(s.id);
					}
				}
				if (active != null) {
					if(s.action.equals("default") && active.defaultAction != null) {
						active.defaultAction.listener().action();
					}
					else {
						for (ToastAction l : active.actions) {
							if (l.name().equals(s.action) && l.listener() != null) {
								l.listener().action();
							}
						}
					}
					if(active.closed != null) {
						active.closed.action();
					}
				}
			});

			conn.addSigHandler(Notifications.NotificationClosed.class, notifications, (s) -> {
				synchronized (actives) {
					ActiveNotification active = actives.get(s.id);
					if (active != null) {
						active.destroyed = true;
						actives.remove(s.id);
						for(var p : active.tempImagePath) {
							try {
								Files.delete(p);
							}
							catch(Exception e) {
							}
						}
					}
				}
			});

			Runtime.getRuntime().addShutdownHook(new Thread() {
				public void run() {
					try {
						conn.close();
					} catch (IOException e) {
					}
				}
			});
		} catch (DBusException dbe) {
			throw new UnsupportedOperationException(dbe);
		} catch (RuntimeException dbe) {
			throw new UnsupportedOperationException(dbe);
		}
	}

	@Override
	public Slice toast(ToastBuilder builder) {
		var args = new ArrayList<String>();
		Map<String, Variant<?>> hints = new HashMap<>();
		List<String> actions = new ArrayList<>();

		args.add("notify-send");
		var icon = builder.icon();
		var type = builder.type();
		var tempImagePaths = new LinkedHashSet<Path>();

		if (icon == null || icon.length() == 0) {
			switch (type) {
			case NONE:
				break;
			default:
				switch (type) {
				case INFO:
					icon = "dialog-information";
					break;
				default:
					icon = "dialog-" + type.name().toLowerCase();
					break;
				}
			}
		}
		else {
			icon = ensureImageLocalPath(icon, tempImagePaths).toAbsolutePath().toString();
		}
		var image = builder.image();
		if (image != null && image.length() > 0) {
			hints.put("image-path", new Variant<String>(ensureImageLocalPath(image, tempImagePaths).toAbsolutePath().toString()));
		}
		var toastActions = builder.actions();
		if(builder.defaultAction() != null) {
			actions.add("default");
			actions.add(builder.defaultAction().displayName());
		}
		for (var a : toastActions) {
			actions.add(a.name());
			actions.add(a.displayName());
		}
		var active = new ActiveNotification();
		active.tempImagePath.addAll(tempImagePaths);
		if (builder.timeout() == 0) {
			hints.put("urgency", new Variant<Byte>(Byte.valueOf((byte) 2)));
		}
		var timeout = (builder.timeout() == -1 ? configuration.getTimeout() : builder.timeout()) * 1000;
		
		active.id = notifications.Notify(configuration.getAppName(), new UInt32(0), icon == null ? "" : icon,
				builder.title() == null ? "" : builder.title(), builder.content() == null ? "" : builder.content(),
				actions, hints, timeout);
		active.actions = toastActions;
		active.closed = builder.closed();
		active.defaultAction = builder.defaultAction();
		
		this.actives.put(active.id, active);
		
		return active;
	}
	
	Path ensureImageLocalPath(String uriOrPath, Set<Path> tempFiles) {
		try {
			URL url = new URL(uriOrPath);
			var path = url.getPath();
			if(url.getProtocol().equals("file")) {
				return Path.of(path);
			}
			else {
				var idx = path.lastIndexOf('.');
				String ext = "img";
				if(idx > -1) {
					ext = path.substring(idx + 1);
				}
				var tempImagePath = Files.createTempFile("twoslices", "." + ext);
				try(var in = url.openStream()) {
					try(var out = Files.newOutputStream(tempImagePath)) {
						in.transferTo(out);
					}
				}
				tempImagePath.toFile().deleteOnExit();
				tempFiles.add(tempImagePath);
				return tempImagePath;
			}
		}
		catch(Exception e) {
			return Path.of(uriOrPath);
		}		
	}

}
