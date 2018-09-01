package com.sshtools.twoslices;

/**
 * Exception thrown on serious error in the toaster system.
 */
@SuppressWarnings("serial")
public class ToasterException extends RuntimeException {

	/**
	 * Constructor
	 */
	public ToasterException() {
		super();
	}

	/**
	 * Constructor.
	 * 
	 * @param message
	 *            message
	 */
	public ToasterException(String message) {
		super(message);
	}

	/**
	 * Constructor.
	 * 
	 * @param message
	 *            message
	 * @param cause
	 *            cause
	 */
	public ToasterException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructor.
	 * 
	 * @param cause
	 *            cause
	 */
	public ToasterException(Throwable cause) {
		super(cause);
	}

}
