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
}
