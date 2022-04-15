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

import org.eclipse.swt.widgets.Display;
import org.junit.Test;

import com.sshtools.twoslices.impl.BasicSWTToaster;
import com.sshtools.twoslices.impl.SWTToaster;

public class PersistentToastTest {
	@Test
	public void testToast() throws InterruptedException {
		
		var display = Display.getDefault();
		new Thread() {
			public void run() {
				try {
					var b = Toast.builder();
					//ToasterFactory.getSettings().setPreferredToasterClassName("com.sshtools.twoslices.impl.SWTToaster");
					b.title("Test").content("Test Content").timeout(0).action("DA1", () -> System.out.println("DA1")).action("A2", () -> System.out.println("DA2")).toast();
					Thread.sleep(1000000);	
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
