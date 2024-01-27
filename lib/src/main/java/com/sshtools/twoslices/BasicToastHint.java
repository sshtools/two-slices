package com.sshtools.twoslices;

import java.util.List;

import com.sshtools.twoslices.impl.JavaFXToaster;
import com.sshtools.twoslices.impl.NotificationCenterToaster;

/**
 * Hints can provide addition configuration to specific toaster implementations.
 * Most hints are currently only supported by one or two implementations.
 */
public enum BasicToastHint implements ToastHint {

	/**
	 * Key hint for a CSS stylesheet URI to use. Should be a {@link String}.
	 * Supported by {@link JavaFXToaster}
	 */
	STYLESHEET,

	/**
	 * Key hint for a CSS stylesheet URI to use. Should be a {@link List} of
	 * {@link String}. Supported by {@link JavaFXToaster}
	 */
	STYLESHEETS,

	/**
	 * Key hint for a an instance of a {@link Function<ToastType, Node>}. This
	 * should generate a new node for a given toast type. Supported by
	 * {@link JavaFXToaster}
	 */
	TYPE_ICON_GENERATOR,
	/**
	 * Key hint for a CSS style to use. Should be a {@link String}. Supported by
	 * {@link JavaFXToaster}
	 */
	STYLE,
	/**
	 * Key hinting to use dark mode. Should be a {@link Boolean}. Supported by
	 * {@link JavaFXToaster}
	 */
	DARK,

	/**
	 * Key hinting of max number of stacked messages before they are collapsed.
	 * Should be a {@link Integer}. Supported by {@link JavaFXToaster}
	 */
	THRESHOLD,

	/**
	 * Key hinting of the text to use for collapsed messages. Should be a
	 * {@link String}. Supported by {@link JavaFXToaster}
	 */
	COLLAPSE_MESSAGE,

	/**
	 * Key hinting the toaster implementation should not use the textual name of the
	 * the {@link ToastType} if it does not support type icons. Supported by
	 * {@link NotificationCenterToaster}
	 */
	NO_TYPE_IN_TEXT,

	/**
	 * Key for hint for an offset between the edge of the screen (as decided by the
	 * {@Position}. Should be an {@link Integer}. Supported by SWT.
	 */
	OFFSET,

	/**
	 * Key for hint for the maximum size of image content. Should be an
	 * {@link Integer}. Any size lower than this will be also be scaled up depending
	 * on {@link SWTToaster#SCALE_UP}. Use a size of zero to prevent scaling
	 * entirely. Supported by SWT.
	 */
	IMAGE_SIZE,

	/**
	 * Key for hint for the maximum size of the icon. Should be an {@link Integer}.
	 * Any size lower than this will be also be scaled up depending on
	 * {@link SWTToaster#SCALE_UP}. Use a size of zero to prevent scaling entirely.
	 * Supported by SWT.
	 */
	ICON_SIZE,

	/**
	 * Key for hint to use animated window positioning. This is experimental, as
	 * there are some issues. Supported by SWT.
	 */
	ANIMATED,

	/**
	 * Key for hint to signal which monitor to use. Should be an {@link Integer}.
	 * Use -1 to indicate the primary monitor (the default). Supported by SWT.
	 */
	MONITOR;

	@Override
	public String toLegacyKey() {
		switch (this) {
		case TYPE_ICON_GENERATOR:
			return "typeIconGenerator";
		case COLLAPSE_MESSAGE:
			return "collapseMessage";
		case NO_TYPE_IN_TEXT:
			return "noTypeInText";
		case IMAGE_SIZE:
			return "imageSize";
		case ICON_SIZE:
			return "iconSize";
		default:
			return name().toLowerCase();
		}
	}
}
