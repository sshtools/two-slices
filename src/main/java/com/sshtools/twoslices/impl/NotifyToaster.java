package com.sshtools.twoslices.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.sshtools.twoslices.AbstractToaster;
import com.sshtools.twoslices.ToastType;
import com.sshtools.twoslices.ToasterSettings;
import com.sshtools.twoslices.ToasterException;

/**
 * Implementation for linux that simply calls the <strong>notify-send</strong>
 * command, which must be installed.
 */
public class NotifyToaster extends AbstractToaster {

	/**
	 * Constructor
	 * 
	 * @param configuration
	 *            configuration
	 */
	public NotifyToaster(ToasterSettings configuration) {
		super(configuration);
		ProcessBuilder b = new ProcessBuilder("notify-send", "--help");
		try {
			b.redirectErrorStream(true);
			Process p = b.start();
			while ((p.getInputStream().read()) != -1)
				;
			if (p.waitFor() != 0)
				throw new IOException("Failed to find notify-send.");
		} catch (IOException | InterruptedException ioe) {
			throw new UnsupportedOperationException(ioe);
		}
	}

	@Override
	public void toast(ToastType type, String icon, String title, String content) {
		List<String> args = new ArrayList<String>();
		args.add("notify-send");
		if (icon == null || icon.length() == 0) {
			switch (type) {
			case NONE:
				break;
			default:
				args.add("-i");
				switch(type) {
				case INFO:
					args.add("dialog-information");
					break;
				default:
					args.add("dialog-" + type.name().toLowerCase());
					break;
				}
			}
		} else {
			args.add("-i");
			args.add(icon);
		}
		args.add(title);
		args.add(content);
		try {
			Process p = new ProcessBuilder(args).redirectErrorStream(true).start();
			while ((p.getInputStream().read()) != -1)
				;
			if (p.waitFor() != 0)
				throw new ToasterException(String.format("Failed to show toast for %s: %s", type, title));
		} catch (IOException | InterruptedException ioe) {
			throw new ToasterException(String.format("Failed to show toast for %s: %s", type, title), ioe);
		}
	}

}
