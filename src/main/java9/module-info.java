module com.sshtools.twoslices {
	requires static java.desktop;
	requires static java.scripting;
	requires static org.freedesktop.dbus;
	requires static org.eclipse.swt.gtk.linux.x86_64;
	requires static javafx.controls;
	requires static javafx.graphics;
	requires static transitive org.controlsfx.controls;
	exports com.sshtools.twoslices;
}