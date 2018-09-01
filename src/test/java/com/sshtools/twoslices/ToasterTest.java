package com.sshtools.twoslices;

import org.eclipse.swt.widgets.Display;
import org.junit.Test;

import com.sshtools.twoslices.impl.SWTToaster;

public class ToasterTest {

	private Thread swtThread;

//	@Test
//	public void testToast() throws InterruptedException {
//		Toast.toast(ToastType.NONE, "Test Title", "Test Content");
//		Thread.sleep(5000);
//		Toast.toast(ToastType.NONE, "Test Title 2", "Test Content 2");
//		Thread.sleep(5000);
//	}

	@Test
	public void testSWT() throws InterruptedException {
		startSWT();
		try {
			SWTToaster toaster = new SWTToaster(new ToasterConfiguration());
			toaster.toast(ToastType.NONE, "Test Title", "Test Content");
			Thread.sleep(5000);
			toaster.toast(ToastType.ERROR, "Test Error", "Some error");
			Thread.sleep(5000);
			toaster.toast(ToastType.INFO, "Test Info", "Some information");
			Thread.sleep(5000);
			toaster.toast(ToastType.WARNING, "Test Warning", "Some warning");
			Thread.sleep(15000);
			toaster.toast(ToastType.WARNING, "Another Error", "Another error after a longer wait");
			Thread.sleep(15000);
		} finally {
			stopSWT();
		}
	}

	void startSWT() {
		swtThread = new Thread() {
			@Override
			public void run() {
				Display display = Display.getDefault();
				try {
					while (true) {
						if (!display.readAndDispatch())
							display.sleep();
					}
				} finally {
					display.dispose();
				}
			}
		};
		swtThread.start();
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
		}
	}

	void stopSWT() {
		swtThread.interrupt();
		swtThread = null;
	}
}
