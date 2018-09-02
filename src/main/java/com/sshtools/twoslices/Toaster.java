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
 * Interface to be implemented by all OS specific providers of notification
 * messages. For clients in general, there is no need to directly use methods.
 * Instead use the convenience class {@link Toast}.
 */
public interface Toaster {
	/**
	 * Return if actions are supported. If so, a {@link ToastActionListener} may
	 * be provided to both
	 * {@link #toast(ToastType, String, String, ToastActionListener...)} and
	 * {@link #toast(ToastType, String, String, String, ToastActionListener...)}.
	 * If actions are not supported, the listeners will be silently ignored.
	 * 
	 * @return actions are supported
	 */
	boolean isActionsSupported();

	/**
	 * Display a notification message.
	 * 
	 * @param type type of message
	 * @param title title of message
	 * @param content content of message
	 * @param listeners optional array of listeners that will be invoked if the
	 *            toast is clicked. If actions are not supported, the listeners
	 *            will be silently ignored.
	 * @throws ToasterException if there is a serious unrecoverable error.
	 */
	void toast(ToastType type, String title, String content, ToastActionListener... listeners);

	/**
	 * Display a notification message.
	 * 
	 * @param type type of message
	 * @param icon icon hint
	 * @param title title of message
	 * @param content content of message
	 * @param listeners optional array of listeners that will be invoked if the
	 *            toast is clicked. If actions are not supported, the listeners
	 *            will be silently ignored.
	 * @throws ToasterException if there is a serious unrecoverable error.
	 */
	void toast(ToastType type, String icon, String title, String content, ToastActionListener... listeners);
}
