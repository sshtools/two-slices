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
package com.sshtools.twoslices.impl;

import java.io.IOException;

import com.sshtools.twoslices.AbstractToaster;
import com.sshtools.twoslices.Slice;
import com.sshtools.twoslices.ToastBuilder;
import com.sshtools.twoslices.Toaster;
import com.sshtools.twoslices.ToasterException;
import com.sshtools.twoslices.ToasterService;
import com.sshtools.twoslices.ToasterSettings;

/**
 * Implementation of a {@link Toaster} when running on Mac OS X with Growl
 * installed. More recent versions of OS X do not have Growl by default, it is a
 * separate (paid) app.
 */
public class OsXToaster extends AbstractToaster {
	
	public static class Service implements ToasterService {
		@Override
		public Toaster create(ToasterSettings settings) {
			return new OsXToaster(settings);
		}
	}
	
	public OsXToaster(ToasterSettings configuration) {
		super(configuration);
		var b = new ProcessBuilder("osascript", "-?");
		try {
			b.redirectErrorStream(true);
			var p = b.start();
			while ((p.getInputStream().read()) != -1)
				;
			if (p.waitFor() != 2 && p.exitValue() != 0)
				throw new IOException("Failed to find osascript.");
		} catch (IOException | InterruptedException ioe) {
			throw new UnsupportedOperationException(ioe);
		}
	}

	@Override
	public Slice toast(ToastBuilder builder) {
		var t = textIcon(builder.type());
		var script = new StringBuilder();
		script.append("display notification \"");
		script.append(escape(builder.content()));
		script.append("\" with title \"");
		script.append(escape(t.length() == 0 ? builder.title() : (t + " " + builder.title())));
		script.append("\"");
		var b = new ProcessBuilder("osascript", "-e", script.toString());
		try {
			b.redirectErrorStream(true);
			var p = b.start();
			while ((p.getInputStream().read()) != -1)
				;
			if (p.waitFor() != 0)
				throw new IOException("Failed to find osascript.");
		} catch (IOException | InterruptedException ioe) {
			throw new ToasterException(String.format("Failed to show toast for %s: %s", builder.type(), builder.title()), ioe);
		}
		return Slice.defaultSlice();
	}

	private String escape(String text) {
		return text.replace("\"", "\\\"");
	}
}
