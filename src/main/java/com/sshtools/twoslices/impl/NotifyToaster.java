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
import java.util.ArrayList;

import com.sshtools.twoslices.AbstractToaster;
import com.sshtools.twoslices.Slice;
import com.sshtools.twoslices.ToastBuilder;
import com.sshtools.twoslices.Toaster;
import com.sshtools.twoslices.ToasterException;
import com.sshtools.twoslices.ToasterService;
import com.sshtools.twoslices.ToasterSettings;

/**
 * Implementation for linux that simply calls the <strong>notify-send</strong>
 * command, which must be installed.
 */
public class NotifyToaster extends AbstractToaster {
	
	public static class Service implements ToasterService {
		@Override
		public Toaster create(ToasterSettings settings) {
			return new NotifyToaster(settings);
		}
	}

	/**
	 * Constructor
	 * 
	 * @param configuration
	 *            configuration
	 */
	public NotifyToaster(ToasterSettings configuration) {
		super(configuration);
		var b = new ProcessBuilder("notify-send", "--help");
		try {
			b.redirectErrorStream(true);
			var p = b.start();
			while ((p.getInputStream().read()) != -1)
				;
			if (p.waitFor() != 0)
				throw new IOException("Failed to find notify-send.");
		} catch (IOException | InterruptedException ioe) {
			throw new UnsupportedOperationException(ioe);
		}
	}

	@Override
	public Slice toast(ToastBuilder builder) {
		var args = new ArrayList<String>();
		args.add("notify-send");
		var icon = builder.icon();
		var type = builder.type();
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
		args.add("-t");
		args.add(String.valueOf((builder.timeout() == -1 ? configuration.getTimeout() : builder.timeout() ) * 1000));
		args.add(builder.title());
		args.add(builder.content());
		try {
			Process p = new ProcessBuilder(args).redirectErrorStream(true).start();
			while ((p.getInputStream().read()) != -1)
				;
			if (p.waitFor() != 0)
				throw new ToasterException(String.format("Failed to show toast for %s: %s", type, builder.title()));
		} catch (IOException | InterruptedException ioe) {
			throw new ToasterException(String.format("Failed to show toast for %s: %s", type, builder.title()), ioe);
		}
		return Slice.defaultSlice();
	}

}
