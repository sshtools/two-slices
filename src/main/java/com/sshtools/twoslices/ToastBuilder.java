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

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Preferred method to create a new toaster notification. Use
 * {@link Toast#builder()} to create a new instance of a builder, set any
 * required attributes, then call {@link #toast()} to display the notification.
 */
public class ToastBuilder {

	/**
	 * Represents an action that may be invoked from a toaster notification. The
	 * underlying platform may support multiple actions (e.g. in the form of
	 * buttons), or a single action that is invoked by clicking on the message
	 * itself.
	 * <p>
	 * Some systems may support icons
	 *
	 */
	public static class ToastAction {
		private final String name;
		private String label;
		private String icon;
		private ToastActionListener listener;

		ToastAction(String name) {
			this(name, name);
		}

		ToastAction(String name, String label) {
			this(name, label, null);
		}

		ToastAction(String name, String label, ToastActionListener listener) {
			super();
			this.name = name;
			this.label = label;
			this.listener = listener;
		}

		/**
		 * Get the icon for this action. The actual semantics of this string may vary
		 * between platforms.
		 * 
		 * @return icon
		 */
		public String icon() {
			return icon;
		}

		/**
		 * Set the icon for this action. The actual semantics of this string may vary
		 * between platforms.
		 * 
		 * @param icon icon
		 */
		public ToastAction icon(String icon) {
			this.icon = icon;
			return this;
		}

		/**
		 * Set the label for this action.
		 * 
		 * @param label label
		 */
		public ToastAction label(String label) {
			this.label = label;
			return this;
		}

		/**
		 * Set the listener for this action.
		 * 
		 * @param listener listener
		 */
		public ToastAction listener(ToastActionListener listener) {
			this.listener = listener;
			return this;
		}

		/**
		 * Get the name of this action.
		 * 
		 * @return name
		 */
		public String name() {
			return name;
		}

		/**
		 * Get the label for this action.
		 * 
		 * @return name
		 */
		public String label() {
			return label;
		}

		/**
		 * Get the listener for this action.
		 * 
		 * @return listener
		 */
		public ToastActionListener listener() {
			return listener;
		}

		/**
		 * Get a display name, either the label for preference, or the required name.
		 * 
		 * @return display name
		 */
		public String displayName() {
			return label == null ? name : label;
		}

	}

	private ToastType type = ToastType.INFO;
	private String title;
	private String content;
	private String icon;
	private List<ToastAction> actions = new ArrayList<>();
	private ToastAction defaultAction;
	private Toaster toaster;
	private int timeout = -1;
	private String image;
	private ToastActionListener closed;
	
	/**
	 * Reset everything except the toaster so builder can be re-used easily.
	 * 
	 * @return this for chaining
	 */
	public ToastBuilder reset() {
		type = ToastType.INFO;
		title = null;
		content = null;
		actions.clear();
		icon = null;
		image = null;
		closed = null;
		return this;
	}

	/**
	 * Get the timeout (in seconds) for this message. If <code>-1</code>, the default from {@link ToasterSettings}
	 * is used. If <code>0</code>, the message should be persistent.
	 * 
	 * @return timeout in seconds or zero for persistent
	 */
	public int timeout() {
		return timeout;
	}

	/**
	 * Set the timeout (in secconds) for this message. If <code>-1</code>, the default from {@link ToasterSettings}
	 * is used. If <code>0</code>, the message should be persistent.
	 * 
	 * @param timeout timeout in seconds  or zero for persistent
	 * @return this for chaining 
	 */
	public ToastBuilder timeout(int timeout) {
		this.timeout = timeout;
		return this;
	}

	/**
	 * Get the toaster used to create the toast.
	 * 
	 * @return toaster
	 */
	public Toaster toaster() {
		return toaster;
	}

	/**
	 * Set the factory used to create the toast.
	 * 
	 * @param factory factory
	 * @return this for chaining
	 */
	public ToastBuilder toaster(Toaster toaster) {
		this.toaster = toaster;
		return this;
	}

	/**
	 * Get the type of toast.
	 * 
	 * @return type
	 */
	public ToastType type() {
		return type;
	}

	/**
	 * Set the type of toast.
	 * 
	 * @param type type
	 * @return this
	 */
	public ToastBuilder type(ToastType type) {
		this.type = type;
		return this;
	}

	/**
	 * Get the icon for this toast. All notification systems support absolute path names to
	 * file resources, as well as string representations of {@link URL}s. Some may
	 * support logical names, such as Freedesktop Icon Names like "dialog-info".
	 * 
	 * @return icon name or path
	 */
	public String icon() {
		return icon;
	}

	/**
	 * Set the icon for this toast. All notification systems support absolute path names to
	 * file resources, as well as string representations of {@link URL}s. Some may
	 * support logical names, such as Freedesktop Icon Names like "dialog-info".
	 * 
	 * @param icon icon name or path
	 * @return this for chaining
	 */
	public ToastBuilder icon(String icon) {
		this.icon = icon;
		return this;
	}

	/**
	 * Convenience method to set the icon for this toast using a URL. This can be
	 * used for example with class path resource URLs (when supported), internet
	 * URLs (when supported) or file URLs. 
	 * 
	 * @param icon icon name or path
	 * @return this for chaining
	 */
	public ToastBuilder icon(URL icon) {
		this.icon = icon == null ? null : icon.toString();
		return this;
	}

	/**
	 * Get the image for this toast. Some notification systems may support an additional
	 * image as well as an {@link #icon()}. All notification systems support absolute path names to
	 * file resources, as well as string representations of {@link URL}s. Some may
	 * support logical names, such as Freedesktop Icon Names like "dialog-info".
	 * 
	 * @return icon name or path
	 */
	public String image() {
		return image;
	}

	/**
	 * Set the image for this toast. Some notification systems may support an additional
	 * image as well as an {@link #icon()}. All notification systems support absolute path names to
	 * file resources, as well as string representations of {@link URL}s. Some may
	 * support logical names, such as Freedesktop Icon Names like "dialog-info".
	 * 
	 * @param icon icon name or path
	 * @return this for chaining
	 */
	public ToastBuilder image(String image) {
		this.image = image;
		return this;
	}

	/**
	 * Convenience method to set the image for this toast using a URL. Some notification systems may support an additional
	 * image as well as an {@link #icon()}. This can be
	 * used for example with class path resource URLs (when supported), internet
	 * URLs (when supported) or file URLs. 
	 * 
	 * @param icon icon name or path
	 * @return this for chaining
	 */
	public ToastBuilder image(URL image) {
		this.image = image.toString();
		return this;
	}
	
	/**
	 * Get this title for this toast.
	 * 
	 * @return title
	 */
	public String title() {
		return title;
	}

	/**
	 * Set this title for this toast.
	 * 
	 * @param title title
	 * @return this for chaining
	 */
	public ToastBuilder title(String title) {
		this.title = title;
		return this;
	}

	/**
	 * Get this content for this toast.
	 * 
	 * @return content
	 */
	public String content() {
		return content;
	}

	/**
	 * Set this content for this toast.
	 * 
	 * @param content content
	 * @return this for chaining
	 */
	public ToastBuilder content(String content) {
		this.content = content;
		return this;
	}

	/**
	 * Get the default action
	 * 
	 * @return action
	 */
	public ToastAction defaultAction() {
		return defaultAction;
	}

	/**
	 * Get an unmodifiable list of all actions.
	 * 
	 * @return actions
	 */
	public List<ToastAction> actions() {
		return Collections.unmodifiableList(actions);
	}

	/**
	 * Create a new empty action.
	 * 
	 * @return action
	 */
	public ToastAction newAction(String name) {
		ToastAction a = new ToastAction(name);
		actions.add(a);
		return a;
	}

	/**
	 * Create a new empty default action.
	 * 
	 * @return default action
	 */
	public ToastAction newDefaultAction() {
		return defaultAction = new ToastAction("default");
	}

	/**
	 * Convenience method to add a new named action with a label.
	 * 
	 * @param name name
	 * @return this for chaining
	 */
	public ToastBuilder defaultAction(String label) {
		newDefaultAction().label(label);
		return this;
	}

	/**
	 * Convenience method to add a new named action with a listener.
	 * 
	 * @param name name
	 * @param listener listener
	 * @return this for chaining
	 */
	public ToastBuilder defaultAction(ToastActionListener listener) {
		newDefaultAction().listener(listener);
		return this;
	}

	/**
	 * Convenience method to add a new named action with a listener.
	 * 
	 * @param name name
	 * @param listener listener
	 * @return this for chaining
	 */
	public ToastBuilder defafultAction(String label, ToastActionListener listener) {
		newDefaultAction().label(label).listener(listener);
		return this;
	}

	/**
	 * Convenience method to add a new named action.
	 * 
	 * @param name name
	 * @return this for chaining
	 */
	public ToastBuilder action(String name) {
		newAction(name);
		return this;
	}

	/**
	 * Convenience method to add a new named action with a label.
	 * 
	 * @param name name
	 * @return this for chaining
	 */
	public ToastBuilder action(String name, String label) {
		newAction(name).label(label);
		return this;
	}

	/**
	 * Convenience method to add a new named action with a listener.
	 * 
	 * @param name name
	 * @param listener listener
	 * @return this for chaining
	 */
	public ToastBuilder action(String name, ToastActionListener listener) {
		newAction(name).listener(listener);
		return this;
	}

	/**
	 * Convenience method to add a new named action with a label and a listener.
	 * 
	 * @param name name
	 * @param label label
	 * @param listener listener
	 * @return this for chaining
	 */
	public ToastBuilder action(String name, String label, ToastActionListener listener) {
		newAction(name).label(label).listener(listener);
		return this;
	}

	/**
	 * Get the listener invoked when the notification is closed.
	 * 
	 * @return closed listener
	 */
	public ToastActionListener closed() {
		return closed;
	}

	/**
	 * Set the listener for this action.
	 * 
	 * @param listener listener
	 */
	public ToastBuilder closed(ToastActionListener closed) {
		this.closed = closed;
		return this;
	}

	/**
	 * Trigger a new notification message based on the configuration in this
	 * builder.
	 */
	public Slice toast() {
		if(toaster == null) {
			return ToasterFactory.getFactory().toaster().toast(this);
		}
		else {
			return toaster.toast(this);
		}
	}
}
