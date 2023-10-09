# two-slices
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.sshtools/two-slices/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.sshtools/two-slices)
[![javadoc](https://javadoc.io/badge2/com.sshtools/two-slices/javadoc.svg)](https://javadoc.io/doc/com.sshtools/two-slices)
![JPMS](https://img.shields.io/badge/JPMS-com.sshtools.twoslices-purple) 

Simple library for desktop notifications from Java on Windows, Mac OS X and Linux.
 
 * One-line of code to show a notification for most cases.
 * Tries to find the best implementation available to give the best looking and most integrated experience..
 * No hard dependencies, although can be augmented with AWT, Swing, SWT or JavaFX to provide further options.
 * Supports NONE, INFO, ERROR and WARNING notifications types, each with it's own icon.
 * Depending on provider, can supports custom icons, images and actions.
 * Can integrate with your existing system tray icon if required (SWT and Swing/AWT)
 * Cross platform and custom JavaFX and SWT popup notifications included.

## All Platforms

All platforms support Growls GNTP protocol. If you have have Growl for Mac OS X, Linux or Windows installed, running on listening on the default port, it will be used in preference to all platform specific notification systems.

All platforms can use one of the Java GUI toolkit specific implementations, such as JavaFX or SWT. If these toolkit libraries are available, those will be chosen if the experience is superior to the native implementation. 

Additionally on all platforms, if no notifier implementation can be found, the last resort fallback will be to display the messages on *System.out*.

In all cases, you can override the chosen provider using `ToasterSettings.setPreferredToasterClassName`, see below.

## Windows

![](src/web/images/windows-awt.png) ![](src/web/images/windows-swt.png)

Windows support is currently provided in the following order :-

 * JavaFX. If JavaFX and ControlsFX is on the CLASSPATH, [ControlsFX](http://controlsfx.com) based notification popups will be used.
 * SWT. If SWT is on the CLASSPATH, the custom SWT popup component will be used. The alternative System Tray based support and balloon tooltip can be used by explicitly requesting it.
 * AWT. If no SWT is available, the built-in AWT System Tray support will be used. 

## Mac OS X

![](src/web/images/osx-notification-centre.png) ![](src/web/images/osx-swt.png) 

Mac OS X support will be provided in the following order :-

 * If native notification centre support is available (Mountain Lion and above), it will be used first.
 * Growl (via AppleScript). If Growl via AppleScript is available, it will be used.
 * If there is no growl, but osascript is an available command, the default Notification Centre will be used
 * JavaFX. If JavaFX and ControlsFX is on the CLASSPATH, [ControlsFX](http://controlsfx.com) based notification 
 * SWT. If SWT is on the CLASSPATH, the custom SWT popup component will be used. The alternative System Tray based support and balloon tooltip can be used by explicitly requesting it.
 * AWT. If no SWT is available, the built-in AWT System Tray support will be used.    
 
## Linux

![](src/web/images/linux-notify.png) ![](src/web/images/linux-swt.png)

Linux support will be provided in the following order :-

 * If `dbus-java` is available, native DBus notifications will be used.
 * notify-send. If this is an available command, the default desktop notifications will be used
 * JavaFX. If JavaFX and ControlsFX is on the CLASSPATH, [ControlsFX](http://controlsfx.com) based notification 
 * SWT. If SWT is on the CLASSPATH, the custom SWT popup component will be used. The alternative System Tray based support and balloon tooltip can be used by explicitly requesting it.
 * AWT. If no SWT is available, the built-in AWT System Tray support will be used.

## Configuring your project

The library is available in Maven Central, so configure your project according to the
build system you use. For example, for Maven itself :-

```xml
	<dependencies>
		<dependency>
			<groupId>com.sshtools</groupId>
			<artifactId>two-slices</artifactId>
			<version>0.9.1</version>
		</dependency>
	</dependencies>
```

### Snapshots

*Development builds may be available in the snapshots repository *

```xml

	<repositories>
		<repository>
			<id>oss-snapshots</id>
			<url>https://oss.sonatype.org/content/repositories/snapshots</url>
			<snapshots>
				<enabled>true</enabled>
			</snapshots>
			<releases>
				<enabled>false</enabled>
			</releases>
		</repository>
	</repositories>
	
..

	<dependencies>
		<dependency>
			<groupId>com.sshtools</groupId>
			<artifactId>two-slices</artifactId>
			<version>0.9.2-SNAPSHOT</version>
		</dependency>
	</dependencies>
```

## Showing A Message

For the simplest use, call Toast.toast() :-

```java
Toast.toast(ToastType.INFO, "Information", "Here is some information you cannot do without.");
```

### With A Custom Icon

You can pass in the full path to an icon too (requires support for this in selected platform)

```java
Toast.toast(ToastType.INFO, "/usr/share/pixmaps/mypic.png", "Boo!", "See my image?");
```

## Using The Builder

An alternative way to build more complex messages is using `ToastBuilder`. 

```java
var builder = Toast.builder();
builder.title("My Title");
builder.content("Some content");

builder.toast();
```

### Closing

You can prematurely close messages if the toaster implementation supports it.

```java
var builder = Toast.builder();
builder.title("My Title");
builder.content("Some content");
var slice = builder.toast();

// ...

slice.close();

```

If you want to be notified when a message is closed, e.g. dismissed by the user. Set a listener on the builder.

```java
var builder = Toast.builder();
builder.content("Some content");
builder.closed(() -> {
    System.out.println("Message closed.");
});
builder.toast();

```

### Actions

If the toaster implementation supports them, *Actions* may be added. An action would usually be represented as a button on a notification message. 

There is a special action, the `defaultAction()`. This is usually invoked when the whole notification message is clicked, or it may simply be presented as another button.

Be aware different implementations have different restrictions on how many actions can be presented.

```java
	var builder = Toast.builder();
	builder.content("Some content");
	builder.action("Action 1", () -> System.out.println("Do Action 1"));
	builder.action("Action 2", () -> System.out.println("Do Action 2"));
	builder.defaultAction(System.out.println("Do Default Action"));
	builder.toast();
```

## Settings

Some settings may be provided to alter the behaviour of the toasters. These are only hints, and specific 
toasters can ignore any and all of them.  

```java
ToasterFactory.setSettings(new ToasterSettings().setAppName("My App Name"));
```

Some toaster implementations have additional hints that can be passed. These hints are only
generally only supported by individual toolkits. For example, to resize icons or images in 
the SWT implementation, you would do the following.

```java
ToasterSettings settings = new ToasterSettings();
settings.getHints().put(BasicToastHint.ICON_SIZE, 64);
ToasterFactory.setSettings(settings);
```

`BasicToastHint` contains a generic list of hints that a provider may or may not support. See
the class documentation for more detail.

Hints may also be set on individual toast, see `ToastBuilder.hints()`. 

### The Tray Icon Mode

Some implementations will require and/or show an icon in your system tray. This will be where the notification
messages are anchored too. You can set a hint as to how to treat this icon via the configuration.

```java
ToasterSettings settings = new ToasterSettings();
settings.setAppName("My App Name");
settings.setSystemTrayIconMode(SystemTrayIconMode.HIDDEN);
ToasterFactory.setSettings(settings);
```

The options for system tray icon mode are :-
 * HIDDEN. The icon will be hidden at all times. This may require the use of a transparent image depending on the platform.
 * SHOW_TOAST_TYPE_WHEN_ACTIVE. When active, the icon in the system tray will reflect the type of the current message that is being displayed.
 * SHOW_DEFAULT_WHEN_ACTIVE. When a message is shown, the default tray icon will be show (see `ToasterSettings.setDefaultImage()`).
 * SHW_DEFAULT_ALWAYS. The default image (see above) will always be visible as soon as the first notification message is sent.

*When you are providing a 'parent' tray item, the icon mode above may be ignored.*

### SWT

If you have an SWT application that already has an icon on the tray, you can re-use this for your notification
settings when the SWT notifier is used.

*For SWT, you must already be running an event loop (see SWT toolkit documentation). At the moment it is not possible to automatically start a loop mainly due to restrictions on OS X where SWT must be on the main thread.*

```java
TrayItem myTrayItem = .....  // this is the reference to your org.eclipse.swt.widgets.TrayItem
ToasterFactory.setSettings(new ToasterSettings().setParent(myTrayItem));
```

Then, whenever the SWT notifier is used, the balloon message will be anchored to your tray item.

### AWT

If you have a Swing/AWT application that already has an icon on the tray, you can re-use this for your notification
settings when the AWT notifier is used.

```java
TrayIcon myTrayIcon = .....  // this is the reference to your java.awt.TrayIcon
ToasterFactory.setSettings(new ToasterSettings().setParent(myTrayIcon));
```

Then, whenever the AWT notifier is used, the balloon message will be anchored to your tray item.

## Extending

You can add your own notifier implementations and customise the factory if you have any special requirements.

### Implementing a Toaster

Implement the `Toaster` interface. An abstract implementation `AbstractToaster` is provided for your convenience and should be used where possible. By convention, all Toasters take a `ToasterSettings` in their constructor. 

In the constructor of your implementation, you should test if this implementation is for the current enviroment. E.g. if you were writing an OS X specific notifier, then you would test for running on that platform and the availability of any libraries or external tools you might need. By convention, if the environment is not sufficient, an `UnsupportedOperationException` should be thrown.

#### Adding The Service

Before your new `Toaster` implementation can be used, you must provide a `ToasterService` implementation that creates it based on the given configuration. By convention, you place this as an inner class inside the Toaster implementation.

```java
public class MyToaster extends AbstractToaster {
	
	public static class Service implements ToasterService {
		@Override
		public Toaster create(ToasterSettings settings) {
			return new MyToaster(settings);
		}
	}

	public MyToaster(ToasterSettings configuration) {
		super(configuration);
	}

	@Override
	public Slice toast(ToastBuilder builder) {
		// TODO
	}
}
```

For Java to automatically find this service, you must add it's full class name, e.g. `com.mypackage.MyToaster$Service` to a file in `META-INF/services/com.sshtools.twoslices.ToasterService`, and/or add it to `module-info.java` using the appropriate syntax for Java services.

### Installing Your Own Factory

If you do not wish to use Java's `ServiceLoader` feature to locate toaster implementations, you can extend `ToasterFactory`, providing you own `toaster()` method.  This will be registered as the default factory the first time you instantiate it (so make sure you do this before ever asking for toast) :-

```java
new ToasterFactory() {
	@Override
	public Toaster toaster() {
		return new MyToaster(); 
	}
};
```

### Your Custom Toast

Just call `Toast.toast` as you normally would.
