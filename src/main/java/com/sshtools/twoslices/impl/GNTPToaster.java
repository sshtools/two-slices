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

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Arrays;

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
 * Implementation for any platform that supports the Growl Network Transport
 * Protocol, GNTP.
 */
public class GNTPToaster extends AbstractToaster {
	
	public static class Service implements ToasterService {
		@Override
		public Toaster create(ToasterSettings settings) {
			return new GNTPToaster(settings);
		}
	}
	
	public final static int DEFAULT_PORT = 23053;

	/**
	 * Constructor
	 * 
	 * @param configuration configuration
	 */
	public GNTPToaster(ToasterSettings configuration) {
		super(configuration);
		capabilities.addAll(Arrays.asList(Capability.IMAGES));
		try {
			register();
		} catch (ToasterException te) {
			throw new UnsupportedOperationException(te);
		}
	}

	@Override
	public Slice toast(ToastBuilder builder) {
		try (Socket socket = new Socket(InetAddress.getLocalHost(), DEFAULT_PORT)) {
			var out = socket.getOutputStream();
			out.write(String.format("GNTP/1.0 %s %s\r\n", "NOTIFY", "NONE").getBytes("UTF-8"));
			out.write(String.format("Application-Name: %s\r\n", configuration.getAppName()).getBytes("UTF-8"));
			out.write(String.format("Notification-Name: %s\r\n", builder.type().name()).getBytes("UTF-8"));
			out.write(String.format("Notification-Title: %s\r\n", builder.title()).getBytes("UTF-8"));
			out.write(String.format("Notification-Text: %s\r\n", builder.content()).getBytes("UTF-8"));
			String icon = builder.icon();
			if (icon != null && icon.length() > 0) {
				out.write(String.format("Notification-Icon: file://%s\r\n", new File(icon).toURI().getRawPath()).getBytes("UTF-8"));
			}
			out.write("\r\n".getBytes("UTF-8"));
			out.flush();
			readResponse(socket.getInputStream());
			return Slice.defaultSlice();
		} catch (Exception e) {
			throw new ToasterException(e);
		}
	}

	private File getFileForType(ToastType type) throws IOException {
		File f = File.createTempFile("two-slices", ".png");
		f.deleteOnExit();
		try (InputStream in = getClass().getResourceAsStream(
				"/images/" + (type == null ? "idle-48.png" : ("dialog-" + type.name().toLowerCase() + "-48.png")))) {
			try (FileOutputStream out = new FileOutputStream(f)) {
				byte[] buf = new byte[65536];
				int r;
				while ((r = in.read(buf)) != -1) {
					out.write(buf, 0, r);
				}
				out.flush();
			}
		}
		return f;
	}

	private void readResponse(InputStream inputStream) throws IOException, EOFException {
		BufferedReader b = new BufferedReader(new InputStreamReader(inputStream));
		String r = b.readLine();
		if (r == null)
			throw new EOFException();
		else {
			int i = r.indexOf(' ');
			if (i == -1)
				throw new IOException("Unexpected response.");
			else {
				String ver = r.substring(0, i);
				if (!ver.startsWith("GNTP/"))
					throw new IOException("Unexpected response.");
				r = r.substring(i + 1);
				i = r.indexOf(' ');
				if (i == -1)
					throw new IOException("Unexpected response.");
				else {
					String type = r.substring(0, i);
					r = r.substring(i + 1);
					if (type.equals("-ERROR")) {
						throw new IOException(r);
					} else if (!type.equals("-OK")) {
						throw new IOException("Unexpected response.");
					}
				}
			}
		}
	}

	private void register() {
		try (Socket socket = new Socket(InetAddress.getLocalHost(), DEFAULT_PORT)) {
			OutputStream out = socket.getOutputStream();
			out.write(String.format("GNTP/1.0 %s %s\r\n", "REGISTER", "NONE").getBytes("UTF-8"));
			out.write(String.format("Application-Name: %s\r\n", configuration.getAppName()).getBytes("UTF-8"));
			if (configuration.getDefaultImage() != null)
				out.write(String.format("Application-Icon: %s\r\n", configuration.getDefaultImage().toString()).getBytes("UTF-8"));
			for (ToastType t : ToastType.values()) {
				out.write(String.format("\r\nNotification-Name: %s\r\n", t.name()).getBytes("UTF-8"));
				out.write(String.format("Notification-Display-Name: %s\r\n", t.description()).getBytes("UTF-8"));
				out.write("Notification-Enabled: True\r\n".getBytes("UTF-8"));
				if (t != ToastType.NONE)
					out.write(String.format("Notification-Icon: file://%s\r\n", getFileForType(t).toURI().getRawPath())
							.getBytes("UTF-8"));
			}
			out.write("\r\n".getBytes("UTF-8"));
			out.flush();
			readResponse(socket.getInputStream());
		} catch (Exception e) {
			throw new ToasterException(e);
		}
	}
}
