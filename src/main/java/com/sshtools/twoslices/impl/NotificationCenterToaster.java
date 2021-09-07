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

import com.sshtools.twoslices.AbstractToaster;
import com.sshtools.twoslices.ToastActionListener;
import com.sshtools.twoslices.ToastType;
import com.sshtools.twoslices.ToasterSettings;
import com.sun.jna.Platform;

import de.jangassen.jfa.foundation.Foundation;
import de.jangassen.jfa.foundation.ID;

public class NotificationCenterToaster extends AbstractToaster {

	public static class StringUtil {

		public static boolean isEmptyOrSpaces(String s) {
			return s == null || s.trim().equals("");
		}

		public static String notNullize(String s) {
			return s == null ? "" : s;
		}

		public static /* @NotNull */ String stripHtml(/* @NotNull */ String html, boolean convertBreaks) {
			return stripHtml(html, convertBreaks ? "\n\n" : null);
		}

		public static /* @NotNull */ String stripHtml(/* @NotNull */ String html, /* @Nullable */ String breaks) {
			if (breaks != null) {
				html = html.replaceAll("<br/?>", breaks);
			}

			return html.replaceAll("<(.|\n)*?>", "");
		}

	}

	public NotificationCenterToaster(ToasterSettings configuration) {
		super(configuration);
		if (!Platform.isMac())
			throw new UnsupportedOperationException();
		
		// Call to load class
		try {
			Foundation.isMainThread();
		}
		catch(Exception e) {
			throw new UnsupportedOperationException(e);
		}
		
		// TODO check mountain lion or higher
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				cleanupDeliveredNotifications();
			}
		});
	}

	@Override
	public void toast(ToastType type, String icon, String title, String content, ToastActionListener... listeners) {
		final ID notification = Foundation.invoke(Foundation.getObjcClass("NSUserNotification"), "new");
		Foundation.invoke(notification, "setTitle:",
				Foundation.nsString(StringUtil.stripHtml(title, true).replace("%", "%%")));
		Foundation.invoke(notification, "setInformativeText:",
				Foundation.nsString(StringUtil.stripHtml(content, true).replace("%", "%%")));
		final ID center = Foundation.invoke(Foundation.getObjcClass("NSUserNotificationCenter"),
				"defaultUserNotificationCenter");
		Foundation.invoke(center, "deliverNotification:", notification);
	}

	private static void cleanupDeliveredNotifications() {
		final ID center = Foundation.invoke(Foundation.getObjcClass("NSUserNotificationCenter"),
				"defaultUserNotificationCenter");
		Foundation.invoke(center, "removeAllDeliveredNotifications");
	}
}
