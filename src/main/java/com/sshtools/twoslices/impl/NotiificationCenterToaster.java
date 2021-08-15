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
import com.sshtools.twoslices.Toaster;
import com.sshtools.twoslices.ToasterSettings;

/**
 * Implementation of a {@link Toaster} when running on Mac OS X Mountain Lion or
 * above with the Notification Center.
 */
public class NotiificationCenterToaster extends AbstractToaster {
	public NotiificationCenterToaster(ToasterSettings configuration) {
		super(configuration);
		throw new UnsupportedOperationException();
	}

	@Override
	public void toast(ToastType type, String icon, String title, String content, ToastActionListener... listeners) {
	}

//	public interface NSUserNotificationCenter extends NSObject {
//
//		public static final _Class CLASS = Rococoa.createClass("NSUserNotificationCenter", _Class.class);
//
//		public interface _Class extends NSClass {
//			NSUserNotificationCenter defaultUserNotificationCenter();
//		}
//
//		void scheduleNotification(NSUserNotification notification);
//
//		void deliverNotification(NSUserNotification notification);
//
//		void setDelegate(NSUserNotificationCenter delegate);
//
//		void removeAllDeliveredNotifications();
//
//	}
}
