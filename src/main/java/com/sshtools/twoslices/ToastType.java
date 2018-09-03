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
 * Constants for the type of notification message.
 */
public enum ToastType {
	/**
	 * Error
	 */
	ERROR,
	/**
	 * Information
	 */
	INFO,
	/**
	 * No specific type
	 */
	NONE,
	/**
	 * Warning
	 */
	WARNING;
	/**
	 * Get all type names as a string array.
	 * 
	 * @return all type names as a string array
	 */
	public static String[] names() {
		ToastType[] v = values();
		String[] n = new String[v.length];
		for (int i = 0; i < v.length; i++)
			n[i] = v[i].name();
		return n;
	}

	public String description() {
		switch (this) {
		case ERROR:
			return "Error";
		case INFO:
			return "Information";
		case WARNING:
			return "Warning";
		case NONE:
			return "Notification";
		}
		// TODO Auto-generated method stub
		return null;
	}
}
