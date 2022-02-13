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

public class AbstractToasterTest {
	public static void testToaster(Toaster toaster) throws InterruptedException {
		var builder = new ToastBuilder();
		builder.toaster(toaster);
//		builder.type(ToastType.NONE).title("Test Title").content("Test Content").toast();
//		Thread.sleep(5000);
//		builder.reset().type(ToastType.ERROR).title("Test Error").content("Some error").toast();
//		Thread.sleep(5000);
		builder.reset().type(ToastType.ERROR)
				.image(/*
						 * System.getProperty("user.dir") +
						 * "/src/test/resources/226520_flower-plant-garden.png"
						 */AbstractToasterTest.class.getResource("/226520_flower-plant-garden.png").toExternalForm()).title("Test Error").content("Some error, with 2 actions")
				.action("Ok", () -> System.out.println("OK!")).action("Cancel", () -> System.out.println("Cancel!"))
				.closed(() -> System.out.println("Closed!")).toast();
		Thread.sleep(15000);
		builder.reset().type(ToastType.INFO).title("Test Info").content("Some information").toast();
//		Thread.sleep(5000);
//		builder.reset().type(ToastType.WARNING).title("Test Warning").content("Some warning").toast();
//		Thread.sleep(15000);
//		builder.reset().type(ToastType.ERROR).title("Another Error").content("Another error after a longer wait")
//				.toast();
		Thread.sleep(15000);
	}
}
