package com.sshtools.twoslices.impl;

import java.io.IOException;

import com.sshtools.twoslices.AbstractToaster;
import com.sshtools.twoslices.ToastType;
import com.sshtools.twoslices.Toaster;
import com.sshtools.twoslices.ToasterConfiguration;
import com.sshtools.twoslices.ToasterException;

/**
 * Implementation of a {@link Toaster} when running on Mac OS X with Growl
 * installed. More recent versions of OS X do not have Growl by default, it is a
 * separate (paid) app.
 */
public class OsXToaster extends AbstractToaster {
	public OsXToaster(ToasterConfiguration configuration) {
		super(configuration);
		ProcessBuilder b = new ProcessBuilder("osascript", "-?");
		try {
			b.redirectErrorStream(true);
			Process p = b.start();
			while ((p.getInputStream().read()) != -1)
				;
			if (p.waitFor() != 2 && p.exitValue() != 0)
				throw new IOException("Failed to find osascript.");
		} catch (IOException | InterruptedException ioe) {
			throw new UnsupportedOperationException(ioe);
		}
	}

	@Override
	public void toast(ToastType type, String icon, String title, String content) {
		String t = textIcon(type);
		StringBuilder script = new StringBuilder();
		script.append("display notification \"");
		script.append(escape(content));
		script.append("\" with title \"");
		script.append(escape(t.length() == 0 ? title : (t + " " + title)));
		script.append("\"");
		ProcessBuilder b = new ProcessBuilder("osascript", "-e", script.toString());
		try {
			b.redirectErrorStream(true);
			Process p = b.start();
			while ((p.getInputStream().read()) != -1)
				;
			if (p.waitFor() != 0)
				throw new IOException("Failed to find osascript.");
		} catch (IOException | InterruptedException ioe) {
			throw new ToasterException(String.format("Failed to show toast for %s: %s", type, title), ioe);
		}
	}

	private String escape(String text) {
		return text.replace("\"", "\\\"");
	}
}
