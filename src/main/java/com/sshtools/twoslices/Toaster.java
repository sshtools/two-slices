package com.sshtools.twoslices;

/**
 * Interface to be implemented by all OS specific providers of notification
 * messages. For clients in general, there is no need to directly use methods.
 * Instead use the convenience class {@link Toast}.
 */
public interface Toaster {

	/**
	 * Display a notification message.
	 * 
	 * @param type
	 *            type of message
	 * @param title
	 *            title of message
	 * @param content
	 *            content of message
	 * @throws ToasterException
	 *             if there is a serious unrecoverable error.
	 */
	void toast(ToastType type, String title, String content);

	/**
	 * Display a notification message.
	 * 
	 * @param type
	 *            type of message
	 * @param icon
	 *            icon hint
	 * @param title
	 *            title of message
	 * @param content
	 *            content of message
	 * @throws ToasterException
	 *             if there is a serious unrecoverable error.
	 */
	void toast(ToastType type, String icon, String title, String content);
}
