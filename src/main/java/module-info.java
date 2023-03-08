/**
 * Copyright © 2018 SSHTOOLS Limited (support@sshtools.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
import com.sshtools.twoslices.ToasterService;

module com.sshtools.twoslices {
	requires static uk.co.bithatch.nativeimage.annotations;
	
	requires static java.desktop;
	requires static java.scripting;
	requires static org.freedesktop.dbus;

	/*
	 * TODO This is going to be tricky. SWT Maven artifact naming just is not
	 * compatible with JPMS. I think the only way to solve this is to split the SWT
	 * implementation into multiple modules. This would be annoying, as two-slices
	 * was supposed to be a simple single library.
	 * 
	 * While it seems we can add multiple modules here, any attempt to actually use
	 * them results in compiler errors.
	 */
	requires static org.eclipse.swt;
	requires static org.eclipse.swt.gtk.linux.x86_64;

	requires static javafx.controls;
	requires static javafx.graphics;
	requires static org.controlsfx.controls;
	requires static com.sun.jna;

	exports com.sshtools.twoslices;
	exports com.sshtools.twoslices.impl; // TODO temporary because access is needed to toaster specific constants
	//exports com.sshtools.twoslices.impl to org.freedesktop.dbus;

	uses ToasterService;

	provides ToasterService
			with com.sshtools.twoslices.impl.GNTPToaster.Service, 
			com.sshtools.twoslices.impl.GrowlToaster.Service, 
			com.sshtools.twoslices.impl.DBUSNotifyToaster.Service,
			com.sshtools.twoslices.impl.NotifyToaster.Service,
			com.sshtools.twoslices.impl.JavaFXToaster.Service,
			com.sshtools.twoslices.impl.SWTToaster.Service,
			com.sshtools.twoslices.impl.NotificationCenterToaster.Service,
			com.sshtools.twoslices.impl.OsXToaster.Service,  
			com.sshtools.twoslices.impl.BasicSWTToaster.Service,
			com.sshtools.twoslices.impl.AWTToaster.Service,
			com.sshtools.twoslices.impl.SysOutToaster.Service;
}