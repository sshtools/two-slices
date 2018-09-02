# two-slices
Simple library for desktop notifications from Java on Windows, Mac OS X and Linux.

## Windows

![](src/web/images/windows-awt.png) ![](src/web/images/windows-swt.png)

Windows support is currently provided in the following order :-

 * SWT. If SWT is on the CLASSPATH, its System Tray support and balloon tooltip will be used.
 * AWT. If no SWT is available, the built-in AWT System Tray support will be used. 

## Mac OS X

![](src/web/images/osx-notification-centre.png) ![](src/web/images/osx-swt.png) 

Mac OS X support will be provided in the following order :-

 * Growl. If Growl is available, it will be used.
 * If there is no growl, but osascript is an available command, the default Notification Centre will be used
 * SWT. If SWT is on the CLASSPATH, its System Tray support and balloon tooltip will be used.
 * AWT. If no SWT is available, the built-in AWT System Tray support will be used.    
 
## Linux

![](src/web/images/linux-notify.png) ![](src/web/images/linux-swt.png)

Linux support will be provided in the following order :-

 * notify-send. If this is an available command, the default desktop notifications will be used
 * SWT. If SWT is on the CLASSPATH, its System Tray support and balloon tooltip will be used.
 * AWT. If no SWT is available, the built-in AWT System Tray support will be used.

## Configuring your project

The library is available in Maven Central, so configure your project according to the
build system you use. For example, for Maven itself :-

*Note, currently only available on Snapshots repository so you'll need to add that first until full release*

```xml

..

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
			<version>0.0.1-SNAPSHOT</version>
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

## Settings

Some settings may be provided to alter the behaviour of the toasters. These are only hints, and specific 
toasters can ignore any and all of them.  

```java
ToasterFactory.setSettings(new ToasterSettings().setAppName("My App Name"));
```

### SWT

If you have an SWT application that already has an icon on the tray, you can re-use this for your notification
settings when the SWT notifier is used.

*For SWT, you must already be running an event loop (see SWT toolkit documentation). At the moment it is not possible to automatically start a loop mainly due to restrictions on OS X where SWT must be on the main thread.*

```java
TrayItem myTrayItem = .....  // this is the reference to your tray item
ToasterFactory.setSettings(new ToasterSettings().setParent(myTrayItem));
```

Then, whenever the SWT notifier is used, the balloon message will be anchored to your tray item.

## Extending

You can add your own notifier implementations and customise the factory if you have any special requirements.

### Installing Your Own Factory

Simply extend `ToasterFactory`, providing you own `toaster()` method.  This will be registered as the default factory the first time you instantiate it (so make sure you do this before ever asking for toast) :-

```java
new ToasterFactory() {
	@Override
	public Toaster toaster() {
		return new MyToaster(); 
	}
};
```

There is a Default implementation of a Toaster Factory you might consider, `ToasterFactory.DefaultToasterFactory`. For example to add support for another platform (if you actually do this please consider contributing to this project!) :-

```java
new ToasterFactory.DefaultToasterFactory() {
	@Override
	public Toaster toaster() {
		if(System.getProperty("os.name").equals("SomeOtherPlatform"))
			return new MyToaster();
		else
			return super.toaster(); 
	}
};
```



### Implementing a Toaster

Implement the `Toaster` interface. An abstract implementation `AbstractToaster` is provided for your convenience and should be used where possible. By convention, all Toasters take a `ToasterSettings` in their constructor. 

In the constructor of your implementation, you should test if this implementation is for the current enviroment. E.g. if you were writing an OS X specific notifier, then you would test for running on that platform and the availability of any libraries or external tools you might need. By convention, if the environment is not sufficient, an `UnsupportedOperationException` should be thrown.

### Your Custom Toast

Just call `Toast.toast` as you normally would.