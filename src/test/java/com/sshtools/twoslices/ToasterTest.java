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

public class ToasterTest {
	@Test
	public void testToast() throws InterruptedException {
//		Thread.sleep(5000);
//		Toast.toast(ToastType.ERROR, "Error",
//				"Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum");
//		Thread.sleep(15000);
//		Toast.toast(ToastType.INFO, "Test Info", "Some information");
//		Thread.sleep(5000);
//		Toast.toast(ToastType.WARNING, "Test Warning", "Some warning");
//		Thread.sleep(15000);
//		Toast.toast(ToastType.ERROR, "Another Error", "Another error after a longer wait");
//		Thread.sleep(15000);
		Toast.toast(ToastType.INFO, "Info", "Choose an action", new ToastActionListener() {
			@Override
			public void action() {
			}

			@Override
			public String getName() {
				return "An Action";
			}
		}, new ToastActionListener() {
			@Override
			public void action() {
			}

			@Override
			public String getName() {
				return "Anoither Action";
			}
		}, new ToastActionListener() {
			@Override
			public void action() {
			}

			@Override
			public String getName() {
				return "Third Action";
			}
		});
		Thread.sleep(15000);
	}
}
