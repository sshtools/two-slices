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
	public static void toast(ToastType type, String title, String content) {
		ToasterFactory.factory().toaster().toast(type, title, content);
	}

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
	public static void toast(ToastType type, String icon, String title, String content) {
		ToasterFactory.factory().toaster().toast(type, icon, title, content);
	}
}
