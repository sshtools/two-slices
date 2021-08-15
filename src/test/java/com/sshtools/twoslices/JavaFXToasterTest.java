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

import org.junit.Test;
import org.testfx.framework.junit.ApplicationTest;

import com.sshtools.twoslices.impl.JavaFXToaster;

public class JavaFXToasterTest extends ApplicationTest {
	@Test
	public void testJavaFX() throws InterruptedException {
		JavaFXToaster toaster = new JavaFXToaster(new ToasterSettings());
		toaster.toast(ToastType.NONE, "Test Title", "Test Content");
		try {
			Thread.sleep(5000);
			toaster.toast(ToastType.ERROR, "Test Error", "Some error");
			Thread.sleep(5000);
			toaster.toast(ToastType.ERROR, "Test Error", "Some error, with 2 actions", new ToastActionListener() {
				@Override
				public void action() {
					
				}
				
				@Override
				public String getName() {
					return "Ok";
				}
			}, new ToastActionListener() {
				@Override
				public void action() {
				}
				
				@Override
				public String getName() {
					return "Cancel";
				}
			});
			Thread.sleep(15000);
			toaster.toast(ToastType.INFO, "Test Info", "Some information");
			Thread.sleep(5000);
			toaster.toast(ToastType.WARNING, "Test Warning", "Some warning");
			Thread.sleep(15000);
			toaster.toast(ToastType.ERROR, "Another Error", "Another error after a longer wait");
			Thread.sleep(15000);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
