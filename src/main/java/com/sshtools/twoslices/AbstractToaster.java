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
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public abstract class AbstractToaster implements Toaster {
	
	protected ToasterSettings configuration;
	protected final Set<Capability> capabilities = new LinkedHashSet<>();

	protected AbstractToaster(ToasterSettings configuration) {
		this.configuration = configuration;
	}

	@Override
	public final Set<Capability> capabilities() {
		return Collections.unmodifiableSet(capabilities);
	}

	protected <V> V getHint(ToastHint key) {
		return getHint(Collections.emptyMap(), key);
	}

	protected <V> V getHint(ToastHint key, V defaultValue) {
		return getHint(Collections.emptyMap(), key, defaultValue);
	}
	
	protected <V> V getHint(Map<ToastHint, Object> hints, ToastHint key) {
		return getHint(hints, key, null);
	}
	
	/**
	 * Get the value of a hint. 
	 * 
	 * @param <V> type
	 * @param key key
	 * @param defaultValue default value
	 * @return value
	 */
	@SuppressWarnings("unchecked")
	protected <V> V getHint(Map<ToastHint, Object> hints, ToastHint hint, V defaultValue) {
		if(hints.containsKey(hint)) {
			return (V)hints.get(hint);
		}
		if(configuration.getHints().containsKey(hint)) {
			return (V)configuration.getHints().get(hint);
		}
		if(configuration.getProperties().containsKey(hint)) {
			return (V)configuration.getProperties().get(hint);			
		}
		return defaultValue;
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
