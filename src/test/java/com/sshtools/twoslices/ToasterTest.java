package com.sshtools.twoslices;

import org.junit.Test;

public class ToasterTest {
	@Test
	public void testToast() throws InterruptedException {
		Thread.sleep(5000);
		Toast.toast(ToastType.ERROR, "Test Error", "Some error");
		Thread.sleep(5000);
		Toast.toast(ToastType.INFO, "Test Info", "Some information");
		Thread.sleep(5000);
		Toast.toast(ToastType.WARNING, "Test Warning", "Some warning");
		Thread.sleep(15000);
		Toast.toast(ToastType.ERROR, "Another Error", "Another error after a longer wait");
		Thread.sleep(15000);
	}
}
