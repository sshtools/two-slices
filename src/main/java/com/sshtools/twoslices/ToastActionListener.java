package com.sshtools.twoslices;

/**
 * Interface defining callback for the when an action is performed on a piece of
 * toast, i.e. the notification popup is clicked.
 */
public interface ToastActionListener {
	/**
	 * Called when action is invoked.
	 */
	void action();

	/**
	 * Return the text for the action (i.e. the ubtton text)
	 */
	default String getName() {
		return toString();
	}
}
