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
package com.sshtools.twoslices;

import java.net.URL;
import java.nio.file.Paths;

public abstract class AbstractToaster implements Toaster {
	
	protected ToasterSettings configuration;

	protected AbstractToaster(ToasterSettings configuration) {
		this.configuration = configuration;
	}

	@Override
	public boolean isActionsSupported() {
		return false;
	}
	
	protected static String ensureURL(String pathOrURL) {
		try {
			new URL(pathOrURL);
			return pathOrURL;
		}
		catch(Exception e) {
			return Paths.get(pathOrURL).toUri().toString();
		}
	}

	protected String textIcon(ToastType messageType) {
		switch (messageType) {
		case INFO:
			return "!";
		case WARNING:
			return "∆";
		case ERROR:
			return "☠";
		default:
			return "";
		}
	}
}
