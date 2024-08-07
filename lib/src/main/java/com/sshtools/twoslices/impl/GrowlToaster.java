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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import com.sshtools.twoslices.AbstractToaster;
import com.sshtools.twoslices.Capability;
import com.sshtools.twoslices.Slice;
import com.sshtools.twoslices.ToastBuilder;
import com.sshtools.twoslices.ToastType;
import com.sshtools.twoslices.Toaster;
import com.sshtools.twoslices.ToasterException;
import com.sshtools.twoslices.ToasterService;
import com.sshtools.twoslices.ToasterSettings;

/**
 * Implementation of a {@link Toaster} when running on Mac OS X with Growl
 * installed. More recent versions of OS X do not have Growl by default, it is a
 * separate (paid) app.
 */
public class GrowlToaster extends AbstractToaster {
	
	public static class Service implements ToasterService {
		@Override
		public Toaster create(ToasterSettings settings) {
			return new GrowlToaster(settings);
		}
	}
	
	private static final String GROWL = "com.Growl.GrowlHelperApp";
	private ScriptEngine engine;
	private Map<String, File> resourceIcons = new HashMap<>();

	/**
	 * Constructor
	 * 
	 * @param configuration configuration
	 */
	public GrowlToaster(ToasterSettings configuration) {
		super(configuration);
        if(!System.getProperty("os.name").toLowerCase().contains("mac os")) {
            throw new UnsupportedOperationException();
        }
		capabilities.addAll(Arrays.asList(Capability.IMAGES));
		try {
			engine = new ScriptEngineManager().getEngineByName("AppleScript");
			if (engine == null)
				engine = new ScriptEngineManager().getEngineByName("AppleScriptEngine");
			if (engine != null) {
				StringBuilder script = new StringBuilder();
				script.append("tell application id \"");
				script.append(GROWL);
				script.append("\"\n");
				script.append("set the availableNames to ");
				script.append(array(ToastType.names()));
				script.append("\n");
				script.append("set the enabledNames to ");
				script.append(array(ToastType.names()));
				script.append("\n");
				script.append("register as application \"");
				script.append(escape(configuration.getAppName()));
				script.append("\" all notifications availableNames default notifications enabledNames\n");
				script.append("end tell");
				engine.eval(script.toString(), engine.getContext());
				if (canGrowl())
					return;
			}
		} catch (Exception e) {
		}
		throw new UnsupportedOperationException();
	}

	@Override
	public Slice toast(ToastBuilder builder) {
		var script = new StringBuilder();
		script.append("tell application id \"");
		script.append(GROWL);
		script.append("\"\n");
		script.append("notify with name \"");
		script.append(builder.type().name());
		script.append("\" title \"");
		script.append(escape(builder.title()));
		script.append("\" description \"");
		script.append(escape(builder.content()));
		script.append("\" application name \"");
		script.append(escape(configuration.getAppName()));
		var icon = builder.icon();
		if(icon == null || icon.length() == 0) {
			script.append("\" image from location \"file://");
			script.append(getFileForType(builder.type()).getAbsolutePath().toString());
		}
		else {
			script.append("\" image from location \"file://");
			script.append(new File(icon).getAbsolutePath());
		}
		script.append("\"\nend tell");
		try {
			engine.eval(script.toString(), engine.getContext());
		} catch (ScriptException e) {
			throw new ToasterException(String.format("Failed to show toast for %s: %s", builder.type(), builder.title()), e);
		}
		return Slice.defaultSlice();
	}
	
	private File getFileForType(ToastType type) {
		synchronized(resourceIcons) {
			var key = type == null ? "" : type.name();
			var v = resourceIcons.get(key);
			if(v == null) {
				try {
					var f = File.createTempFile("two-slices", ".png");
					f.deleteOnExit();
					try(InputStream in = getClass().getResourceAsStream("/images/" + (type == null ? "idle-48.png" : ("dialog-" + type.name().toLowerCase() + "-48.png")))) {
						try(FileOutputStream out = new FileOutputStream(f)) {
							byte[] buf = new byte[65536];
							int r;
							while( ( r = in.read(buf)) != -1) {
								out.write(buf, 0, r);
							}
							out.flush();
						}
					}
					resourceIcons.put(key, v = f);
				} catch (IOException e) {
					
				}
			}
			return v;
		}
	}

	private String array(String[] array) {
		var bui = new StringBuilder();
		bui.append("{");
		for (String s : array) {
			if (bui.length() > 1) {
				bui.append(", ");
			}
			bui.append("\"");
			bui.append(s);
			bui.append("\"");
		}
		bui.append("}");
		return bui.toString();
	}

	private String escape(String text) {
		return text.replace("\"", "\\\"");
	}

	private boolean canGrowl() {
		var script = new StringBuilder();
		script.append("tell application \"System Events\"\nreturn count of (every process whose bundle identifier is \"");
		script.append(GROWL);
		script.append("\") > 0\nend tell");
		try {
			return (Long) engine.eval(script.toString(), engine.getContext()) > 0;
		} catch (ScriptException e) {
			return false;
		}
	}
}
