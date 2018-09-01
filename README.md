# two-slices
Simple library for desktop notifications from Java on Windows, Mac OS X and Linux.

## Windows

Windows support is currently provided in the following order :-

 * SWT. If SWT is on the CLASSPATH, it's System Tray support and balloon tooltip will be used.
 * AWT. If no SWT is available, the built-in AWT System Tray support will be used. 

## Mac OS X

Mac OS X support will be provided in the following order :-

 * Growl. If Growl is available, it will be used.
 * If there is no growl, but osascript is an available command, the default Notification Centre will be used
 * SWT. If SWT is on the CLASSPATH, it's System Tray support and balloon tooltip will be used.
 * AWT. If no SWT is available, the built-in AWT System Tray support will be used.    
 
## Linux

Linux support will be provided in the following order :-

 * notify-send. If this is an available command, the default desktop notifications will be used
 * SWT. If SWT is on the CLASSPATH, it's System Tray support and balloon tooltip will be used.
 * AWT. If no SWT is available, the built-in AWT System Tray support will be used.

## Configuring your project

The library is available in Maven Central, so configure your project according to the
build system you use. For example, for Maven itself :-

* Note, currently only available on Snapshots repository so you'll need to add that first 
  until full release*

```xml
<dependencies>
	<dependency>
		<groupId>com.sshtools</groupId>
		<artifactId>two-slices</artifactId>
		<version>0.0.1-SNAPSHOT</version>
	</dependency>
</dependencies>
```

## Showing A Message

For the simplest use, call Toast.toast() :-

```
Toast.toast(ToastType.INFO, "Information", "Here is some information you cannot do without.");
```

