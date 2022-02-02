package com.sshtools.twoslices;

import org.junit.Test;

public class PersistentToastTest {
	@Test
	public void testToast() throws InterruptedException {
		Toast.builder().title("Test").content("Test Content").timeout(0).action("DA1", () -> System.out.println("DA1")).action("A2", () -> System.out.println("DA2")).toast();
		Thread.sleep(1000000);
	}
}
