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
package com.sshtools.twoslices.swt;

import org.eclipse.swt.widgets.Display;
import org.junit.Test;

import com.sshtools.twoslices.AbstractToasterTest;
import com.sshtools.twoslices.ToasterSettings;

public class SWTToasterTest extends AbstractToasterTest {
	@Test
	public void testSWT() {
		var display = Display.getDefault();
		new Thread() {
			public void run() {
				try {
					var settings = new ToasterSettings();
					testToaster(new SWTToaster(settings));
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					display.asyncExec(() -> display.dispose());
				}
			}
		}.start();
		while (!display.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
	}
	
	@Test
	public void testSWTScaledIcons() {
		var display = Display.getDefault();
		new Thread() {
			public void run() {
				try {
					var settings = new ToasterSettings();
					settings.getProperties().put(SWTToaster.ICON_SIZE, 64);
					settings.getProperties().put(SWTToaster.IMAGE_SIZE, 512);
					testToaster(new SWTToaster(settings));
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					display.asyncExec(() -> display.dispose());
				}
			}
		}.start();
		while (!display.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
	}
}
