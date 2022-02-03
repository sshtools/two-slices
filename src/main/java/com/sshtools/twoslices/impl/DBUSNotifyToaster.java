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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.freedesktop.dbus.annotations.DBusInterfaceName;
import org.freedesktop.dbus.connections.impl.DBusConnection;
import org.freedesktop.dbus.exceptions.DBusException;
import org.freedesktop.dbus.interfaces.DBusInterface;
import org.freedesktop.dbus.messages.DBusSignal;
import org.freedesktop.dbus.types.UInt32;
import org.freedesktop.dbus.types.Variant;

import com.sshtools.twoslices.AbstractToaster;
import com.sshtools.twoslices.ToastActionListener;
import com.sshtools.twoslices.ToastBuilder;
import com.sshtools.twoslices.ToastBuilder.ToastAction;
import com.sshtools.twoslices.ToasterSettings;

/**
 * Implementation for linux that uses the DBUS notification service.
 */
public class DBUSNotifyToaster extends AbstractToaster {
	@DBusInterfaceName("org.freedesktop.Notifications")
	public interface Notifications extends DBusInterface {

		void CloseNotification(int id);

		String[] GetCapabilities();

		String[] GetServerInformation();

		UInt32 Notify(String appName, UInt32 replacesId, String appIcon, String summary, String body,
				List<String> actions, Map<String, Variant<?>> hints, int expireTimeout);

		public class ActionInvoked extends DBusSignal {

			private UInt32 id;
			private String action;

			public ActionInvoked(String path, UInt32 id, String action) throws DBusException {
				super(path);
				this.id = id;
				this.action = action;
			}

		}

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

	class ActiveNotification {
		List<ToastAction> actions;
		UInt32 id;
		ToastActionListener closed;
	}

	/**
	 * Constructor
	 * 
	 * @param configuration configuration
	 */
	public DBUSNotifyToaster(ToasterSettings configuration) {
		super(configuration);
		try {
			conn = DBusConnection.getConnection(DBusConnection.DBusBusType.SESSION);

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
					for (ToastAction l : active.actions) {
						if (l.name().equals(s.action) && l.listener() != null) {
							l.listener().action();
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
						actives.remove(s.id);
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
	public void toast(ToastBuilder builder) {
		var args = new ArrayList<String>();
		Map<String, Variant<?>> hints = new HashMap<>();
		List<String> actions = new ArrayList<>();

		args.add("notify-send");
		var icon = builder.icon();
		var type = builder.type();

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
		var image = builder.image();
		if (image != null && image.length() > 0) {
			hints.put("image-path", new Variant<String>(image));
		}
		var toastActions = builder.actions();
		if (toastActions.size() > 0) {
			actions.add("default");
			actions.add(toastActions.get(0).displayName());
		}
		if (toastActions.size() > 1) {
			for (int i = 1; i < toastActions.size(); i++) {
				actions.add(toastActions.get(i).name());
				actions.add(toastActions.get(i).displayName());
			}
		}
		var active = new ActiveNotification();
		if (builder.timeout() == 0) {
			hints.put("urgency", new Variant<Byte>(Byte.valueOf((byte) 2)));
		}
		var timeout = (builder.timeout() == -1 ? configuration.getTimeout() : builder.timeout()) * 1000;
		
		active.id = notifications.Notify(configuration.getAppName(), new UInt32(0), icon == null ? "" : icon,
				builder.title() == null ? "" : builder.title(), builder.content() == null ? "" : builder.content(),
				actions, hints, timeout);
		active.actions = toastActions;
		active.closed = builder.closed();
		
		this.actives.put(active.id, active);
	}

}
