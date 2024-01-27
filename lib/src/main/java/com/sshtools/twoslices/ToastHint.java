package com.sshtools.twoslices;

/**
 * Interface to be implements by enums that provide <i>Hint Keys</i>, such as 
 * {@link BasicToastHint}.
 * <p>
 * This allows 3rd party toaster implementations to add their own hint keys and
 * still use the methods to set hints via the standard API. 
 */
public interface ToastHint {

	@Deprecated
	String toLegacyKey();

}
