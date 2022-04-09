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

/**
 * Default entry point providing two simple methods for displaying a
 * notification message. For more control over the notification system used,
 * consider using {@link ToasterFactory} directly.
 *
 * @see ToasterFactory
 */
public class Toast {

	/**
	 * Create a new {@link ToastBuilder}.
	 * @throws ToasterException if there is a serious unrecoverable error.
	 */
	public static ToastBuilder builder() {
		return new ToastBuilder();
	}
	
	/**
	 * Display an information message.
	 * 
	 * @param content content of message
	 * @param listeners optional array of listeners that will be invoked if the
	 *            toast is clicked. If listeners are not supported, the listeners
	 *            will be silently ignored.
	 * @return handle to message
	 * @throws ToasterException if there is a serious unrecoverable error.
	 */
	public static Slice toast(String content, ToastActionListener... listeners) {
		var builder = builder().content(content);
		for(var l : listeners) {
			builder.action(l.toString(), () -> l.action());
		}
		return builder.toast();
	}
	
	/**
	 * Display a notification message.
	 * 
	 * @param type type of message
	 * @param title title of message
	 * @param content content of message
	 * @param listeners optional array of listeners that will be invoked if the
	 *            toast is clicked. If listeners are not supported, the listeners
	 *            will be silently ignored.
	 * @return handle to message
	 * @throws ToasterException if there is a serious unrecoverable error.
	 */
	public static Slice toast(ToastType type, String title, String content, ToastActionListener... listeners) {
		var builder = builder().type(type).title(title).content(content);
		for(var l : listeners) {
			builder.action(l.toString(), () -> l.action());
		}
		return builder.toast();
	}

	/**
	 * Display a notification message.
	 * 
	 * @param type type of message
	 * @param icon icon hint
	 * @param title title of message
	 * @param content content of message
	 * @param listeners optional array of listeners that will be invoked if the
	 *            toast is clicked. If listeners are not supported, the listeners
	 *            will be silently ignored.
	 * @return handle to message
	 * @throws ToasterException if there is a serious unrecoverable error.
	 */
	public static Slice toast(ToastType type, String icon, String title, String content, ToastActionListener... listeners) {
		var builder = builder().type(type).title(title).icon(icon).content(content);
		for(var l : listeners) {
			builder.action(l.toString(), () -> l.action());
		}
		return builder.toast();
	}
}
