package com.sshtools.twoslices;

import org.eclipse.swt.widgets.Display;
import org.junit.Test;

import com.sshtools.twoslices.impl.SWTToaster;

public class SWTToasterTest {
	@Test
	public void testSWT() {
		Display display = Display.getDefault();
		new Thread() {
			public void run() {
				SWTToaster toaster = new SWTToaster(new ToasterSettings());
				toaster.toast(ToastType.NONE, "Test Title", "Test Content");
				try {
					Thread.sleep(5000);
					toaster.toast(ToastType.ERROR, "Test Error", "Some error");
					Thread.sleep(5000);
					toaster.toast(ToastType.INFO, "Test Info", "Some information");
					Thread.sleep(5000);
					toaster.toast(ToastType.WARNING, "Test Warning", "Some warning");
					Thread.sleep(15000);
					toaster.toast(ToastType.WARNING, "Another Error", "Another error after a longer wait");
					Thread.sleep(15000);
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
