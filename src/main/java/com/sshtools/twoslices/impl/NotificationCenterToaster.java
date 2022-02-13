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
package com.sshtools.twoslices.impl;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Proxy;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.sshtools.twoslices.AbstractToaster;
import com.sshtools.twoslices.Capability;
import com.sshtools.twoslices.Slice;
import com.sshtools.twoslices.ToastBuilder;
import com.sshtools.twoslices.ToastBuilder.ToastAction;
import com.sshtools.twoslices.Toaster;
import com.sshtools.twoslices.ToasterService;
import com.sshtools.twoslices.ToasterSettings;
import com.sshtools.twoslices.Version;
import com.sun.jna.Callback;
import com.sun.jna.FromNativeContext;
import com.sun.jna.Function;
import com.sun.jna.Library;
import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.NativeLibrary;
import com.sun.jna.NativeLong;
import com.sun.jna.NativeMapped;
import com.sun.jna.Platform;
import com.sun.jna.Pointer;
import com.sun.jna.PointerType;
import com.sun.jna.Structure;
import com.sun.jna.ptr.PointerByReference;

/**
 * Implementation of a {@link Toaster} when running on Mac OS X on Mountain
 * Lion+. This will hopefully be the preferred method on modern OS X.
 * 
 * Based on
 * https://github.com/JetBrains/intellij-community/blob/master/platform/platform-impl/src/com/intellij/ui/MountainLionNotifications.java
 * 
 * Copyright 2000-2010 JetBrains s.r.o.
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
public class NotificationCenterToaster extends AbstractToaster {
	
	public static class Service implements ToasterService {
		@Override
		public Toaster create(ToasterSettings settings) {
			return new NotificationCenterToaster(settings);
		}
	}

	public static class StringUtil {

		public static boolean isEmptyOrSpaces(String s) {
			return s == null || s.trim().equals("");
		}

		public static String notNullize(String s) {
			return s == null ? "" : s;
		}

		public static /* @NotNull */ String stripHtml(/* @NotNull */ String html, boolean convertBreaks) {
			return stripHtml(html, convertBreaks ? "\n\n" : null);
		}

		public static /* @NotNull */ String stripHtml(/* @NotNull */ String html, /* @Nullable */ String breaks) {
			if (breaks != null) {
				html = html.replaceAll("<br/?>", breaks);
			}

			return html.replaceAll("<(.|\n)*?>", "");
		}

	}

	@SuppressWarnings("serial")
	public static class ID extends NativeLong {

		public ID() {
		}

		public ID(long peer) {
			super(peer);
		}

		public static final ID NIL = new ID(0L);

		public boolean booleanValue() {
			return intValue() != 0;
		}
	}

	public interface FoundationLibrary extends Library {
		void NSLog(Pointer pString, Object thing);

		ID NSFullUserName();

		ID objc_allocateClassPair(ID supercls, String name, int extraBytes);

		void objc_registerClassPair(ID cls);

		ID CFStringCreateWithBytes(Pointer allocator, byte[] bytes, int byteCount, int encoding,
				byte isExternalRepresentation);

		byte CFStringGetCString(ID theString, byte[] buffer, int bufferSize, int encoding);

		int CFStringGetLength(ID theString);

		long CFStringConvertNSStringEncodingToEncoding(long nsEncoding);

		ID CFStringConvertEncodingToIANACharSetName(long cfEncoding);

		long CFStringConvertIANACharSetNameToEncoding(ID encodingName);

		long CFStringConvertEncodingToNSStringEncoding(long cfEncoding);

		ID CGWindowListCreateImage(Foundation.NSRect screenBounds, int windowOption, ID windowID, int imageOption);

		void CFRetain(ID cfTypeRef);

		void CFRelease(ID cfTypeRef);

		int CFGetRetainCount(Pointer cfTypeRef);

		ID objc_getClass(String className);

		ID objc_getProtocol(String name);

		ID class_createInstance(ID pClass, int extraBytes);

		Pointer sel_registerName(String selectorName);

		ID class_replaceMethod(ID cls, Pointer selName, Callback impl, String types);

		ID objc_getMetaClass(String name);

		/**
		 * Note: Vararg version. Should only be used only for selectors with a single
		 * fixed argument followed by varargs.
		 */
		ID objc_msgSend(ID receiver, Pointer selector, Object firstArg, Object... args);

		boolean class_respondsToSelector(ID cls, Pointer selName);

		boolean class_addMethod(ID cls, Pointer selName, Callback imp, String types);

		boolean class_addMethod(ID cls, Pointer selName, ID imp, String types);

		boolean class_addProtocol(ID aClass, ID protocol);

		boolean class_isMetaClass(ID cls);

		ID NSStringFromSelector(Pointer selector);

		ID NSStringFromClass(ID aClass);

		Pointer objc_getClass(Pointer clazz);

		int kCFStringEncodingMacRoman = 0;
		int kCFStringEncodingWindowsLatin1 = 0x0500;
		int kCFStringEncodingISOLatin1 = 0x0201;
		int kCFStringEncodingNextStepLatin = 0x0B01;
		int kCFStringEncodingASCII = 0x0600;
		int kCFStringEncodingUnicode = 0x0100;
		int kCFStringEncodingUTF8 = 0x08000100;
		int kCFStringEncodingNonLossyASCII = 0x0BFF;

		int kCFStringEncodingUTF16 = 0x0100;
		int kCFStringEncodingUTF16BE = 0x10000100;
		int kCFStringEncodingUTF16LE = 0x14000100;
		int kCFStringEncodingUTF32 = 0x0c000100;
		int kCFStringEncodingUTF32BE = 0x18000100;
		int kCFStringEncodingUTF32LE = 0x1c000100;

		// https://developer.apple.com/library/mac/documentation/Carbon/Reference/CGWindow_Reference/Constants/Constants.html#//apple_ref/doc/constant_group/Window_List_Option_Constants
		int kCGWindowListOptionAll = 0;
		int kCGWindowListOptionOnScreenOnly = 1;
		int kCGWindowListOptionOnScreenAboveWindow = 2;
		int kCGWindowListOptionOnScreenBelowWindow = 4;
		int kCGWindowListOptionIncludingWindow = 8;
		int kCGWindowListExcludeDesktopElements = 16;

		// https://developer.apple.com/library/mac/documentation/Carbon/Reference/CGWindow_Reference/Constants/Constants.html#//apple_ref/doc/constant_group/Window_Image_Types
		int kCGWindowImageDefault = 0;
		int kCGWindowImageBoundsIgnoreFraming = 1;
		int kCGWindowImageShouldBeOpaque = 2;
		int kCGWindowImageOnlyShadows = 4;
		int kCGWindowImageBestResolution = 8;
		int kCGWindowImageNominalResolution = 16;

		// see enum NSBitmapImageFileType
		int NSBitmapImageFileTypeTIFF = 0;
		int NSBitmapImageFileTypeBMP = 1;
		int NSBitmapImageFileTypeGIF = 2;
		int NSBitmapImageFileTypeJPEG = 3;
		int NSBitmapImageFileTypePNG = 4;
		int NSBitmapImageFileTypeJPEG2000 = 5;
	}

	public static final class Foundation {

		public static void init() {
			/* fake method to init foundation */ }

		private Foundation() {
		}

		/**
		 * Get the ID of the NSClass with className
		 */
		public static ID getObjcClass(String className) {
			return myFoundationLibrary.objc_getClass(className);
		}

		public static ID getProtocol(String name) {
			return myFoundationLibrary.objc_getProtocol(name);
		}

		public static Pointer createSelector(String s) {
			return myFoundationLibrary.sel_registerName(s);
		}

		private static Object /* @NotNull */ [] prepInvoke(ID id, Pointer selector, Object[] args) {
			Object[] invokArgs = new Object[args.length + 2];
			invokArgs[0] = id;
			invokArgs[1] = selector;
			System.arraycopy(args, 0, invokArgs, 2, args.length);
			return invokArgs;
		}

		public static /* @NotNull */ ID invoke(final ID id, final Pointer selector, Object... args) {
			// objc_msgSend is called with the calling convention of the target method
			// on x86_64 this does not make a difference, but arm64 uses a different calling
			// convention for varargs
			// it is therefore important to not call objc_msgSend as a vararg function
			return new ID(myObjcMsgSend.invokeLong(prepInvoke(id, selector, args)));
		}

		/**
		 * Invokes the given vararg selector. Expects `NSArray arrayWithObjects:(id),
		 * ...` like signature, i.e. exactly one fixed argument, followed by varargs.
		 */
		public static ID invokeVarArg(final ID id, final Pointer selector, Object... args) {
			// c functions and objc methods have at least 1 fixed argument, we therefore
			// need to separate out the first argument
			return myFoundationLibrary.objc_msgSend(id, selector, args[0], Arrays.copyOfRange(args, 1, args.length));
		}

		public static ID invoke(final String cls, final String selector, Object... args) {
			return invoke(getObjcClass(cls), createSelector(selector), args);
		}

		public static ID invokeVarArg(final String cls, final String selector, Object... args) {
			return invokeVarArg(getObjcClass(cls), createSelector(selector), args);
		}

		public static ID safeInvoke(final String stringCls, final String stringSelector, Object... args) {
			ID cls = getObjcClass(stringCls);
			Pointer selector = createSelector(stringSelector);
			if (!invoke(cls, "respondsToSelector:", selector).booleanValue()) {
				throw new RuntimeException(String.format("Missing selector %s for %s", stringSelector, stringCls));
			}
			return invoke(cls, selector, args);
		}

		public static /* @NotNull */ ID invoke(final ID id, final String selector, Object... args) {
			return invoke(id, createSelector(selector), args);
		}

		public static double invoke_fpret(ID receiver, Pointer selector, Object... args) {
			return myObjcMsgSend.invokeDouble(prepInvoke(receiver, selector, args));
		}

		public static double invoke_fpret(ID receiver, String selector, Object... args) {
			return invoke_fpret(receiver, createSelector(selector), args);
		}

		public static boolean isNil(ID id) {
			return id == null || ID.NIL.equals(id);
		}

		public static ID safeInvoke(final ID id, final String stringSelector, Object... args) {
			Pointer selector = createSelector(stringSelector);
			if (!id.equals(ID.NIL) && !invoke(id, "respondsToSelector:", selector).booleanValue()) {
				throw new RuntimeException(String.format("Missing selector %s for %s", stringSelector,
						toStringViaUTF8(invoke(id, "description"))));
			}
			return invoke(id, selector, args);
		}

		public static ID allocateObjcClassPair(ID superCls, String name) {
			return myFoundationLibrary.objc_allocateClassPair(superCls, name, 0);
		}

		public static void registerObjcClassPair(ID cls) {
			myFoundationLibrary.objc_registerClassPair(cls);
		}

		public static boolean isClassRespondsToSelector(ID cls, Pointer selectorName) {
			return myFoundationLibrary.class_respondsToSelector(cls, selectorName);
		}

		/**
		 *
		 * @param cls          The class to which to add a method.
		 * @param selectorName A selector that specifies the name of the method being
		 *                     added.
		 * @param impl         A function which is the implementation of the new method.
		 *                     The function must take at least two arguments-self and
		 *                     _cmd.
		 * @param types        An array of characters that describe the types of the
		 *                     arguments to the method. See <a href=
		 *                     "https://developer.apple.com/library/IOs/documentation/Cocoa/Conceptual/ObjCRuntimeGuide/Articles/ocrtTypeEncodings.html#//apple_ref/doc/uid/TP40008048-CH100"></a>
		 * @return true if the method was added successfully, otherwise false (for
		 *         example, the class already contains a method implementation with that
		 *         name).
		 */
		public static boolean addMethod(ID cls, Pointer selectorName, Callback impl, String types) {
			return myFoundationLibrary.class_addMethod(cls, selectorName, impl, types);
		}

		public static boolean addProtocol(ID aClass, ID protocol) {
			return myFoundationLibrary.class_addProtocol(aClass, protocol);
		}

		public static boolean addMethodByID(ID cls, Pointer selectorName, ID impl, String types) {
			return myFoundationLibrary.class_addMethod(cls, selectorName, impl, types);
		}

		public static boolean isMetaClass(ID cls) {
			return myFoundationLibrary.class_isMetaClass(cls);
		}

		/* @Nullable */
		public static String stringFromSelector(Pointer selector) {
			ID id = myFoundationLibrary.NSStringFromSelector(selector);
			return ID.NIL.equals(id) ? null : toStringViaUTF8(id);
		}

		/* @Nullable */
		public static String stringFromClass(ID aClass) {
			ID id = myFoundationLibrary.NSStringFromClass(aClass);
			return ID.NIL.equals(id) ? null : toStringViaUTF8(id);
		}

		public static Pointer getClass(Pointer clazz) {
			return myFoundationLibrary.objc_getClass(clazz);
		}

		public static String fullUserName() {
			return toStringViaUTF8(myFoundationLibrary.NSFullUserName());
		}

		public static ID class_replaceMethod(ID cls, Pointer selector, Callback impl, String types) {
			return myFoundationLibrary.class_replaceMethod(cls, selector, impl, types);
		}

		public static ID getMetaClass(String className) {
			return myFoundationLibrary.objc_getMetaClass(className);
		}

		public static boolean isPackageAtPath(/* @NotNull */ final String path) {
			final ID workspace = invoke("NSWorkspace", "sharedWorkspace");
			final ID result = invoke(workspace, createSelector("isFilePackageAtPath:"), nsString(path));

			return result.booleanValue();
		}

		public static boolean isPackageAtPath(/* @NotNull */ final File file) {
			if (!file.isDirectory())
				return false;
			return isPackageAtPath(file.getPath());
		}

		private static final class NSString {
			private static final ID nsStringCls = getObjcClass("NSString");
			private static final Pointer stringSel = createSelector("string");
			private static final Pointer allocSel = createSelector("alloc");
			private static final Pointer autoreleaseSel = createSelector("autorelease");
			private static final Pointer initWithBytesLengthEncodingSel = createSelector(
					"initWithBytes:length:encoding:");
			private static final long nsEncodingUTF16LE = convertCFEncodingToNS(
					FoundationLibrary.kCFStringEncodingUTF16LE);

			/* @NotNull */
			public static ID create(/* @NotNull */ String s) {
				// Use a byte[] rather than letting jna do the String -> char* marshalling
				// itself.
				// Turns out about 10% quicker for long strings.
				if (s.isEmpty()) {
					return invoke(nsStringCls, stringSel);
				}

				byte[] utf16Bytes = s.getBytes(StandardCharsets.UTF_16LE);
				return invoke(invoke(invoke(nsStringCls, allocSel), initWithBytesLengthEncodingSel, utf16Bytes,
						utf16Bytes.length, nsEncodingUTF16LE), autoreleaseSel);
			}
		}

		/* @NotNull */
		public static ID nsString(/* @Nullable */ String s) {
			return s == null ? ID.NIL : NSString.create(s);
		}

		public static ID nsUUID(/* @NotNull */ UUID uuid) {
			return nsUUID(uuid.toString());
		}

		public static ID nsUUID(/* @NotNull */ String uuid) {
			return invoke(invoke(invoke("NSUUID", "alloc"), "initWithUUIDString:", nsString(uuid)), "autorelease");
		}

		/* @Nullable */
		public static String toStringViaUTF8(ID cfString) {
			if (ID.NIL.equals(cfString))
				return null;

			int lengthInChars = myFoundationLibrary.CFStringGetLength(cfString);
			int potentialLengthInBytes = 3 * lengthInChars + 1; // UTF8 fully escaped 16 bit chars, plus nul

			byte[] buffer = new byte[potentialLengthInBytes];
			byte ok = myFoundationLibrary.CFStringGetCString(cfString, buffer, buffer.length,
					FoundationLibrary.kCFStringEncodingUTF8);
			if (ok == 0)
				throw new RuntimeException("Could not convert string");
			return Native.toString(buffer);
		}

		public static /* @NlsSafe @Nullable */ String getNSErrorText(/* @Nullable */ ID error) {
			if (error == null || error.byteValue() == 0)
				return null;

			String description = toStringViaUTF8(invoke(error, "localizedDescription"));
			String recovery = toStringViaUTF8(invoke(error, "localizedRecoverySuggestion"));
			if (recovery != null)
				description += "\n" + recovery;
			return StringUtil.notNullize(description);
		}

		/* @Nullable */
		public static String getEncodingName(long nsStringEncoding) {
			long cfEncoding = myFoundationLibrary.CFStringConvertNSStringEncodingToEncoding(nsStringEncoding);
			ID pointer = myFoundationLibrary.CFStringConvertEncodingToIANACharSetName(cfEncoding);
			String name = toStringViaUTF8(pointer);
			if ("macintosh".equals(name))
				name = "MacRoman"; // JDK8 does not recognize IANA's "macintosh" alias
			return name;
		}

		public static long getEncodingCode(/* @Nullable */ String encodingName) {
			if (StringUtil.isEmptyOrSpaces(encodingName))
				return -1;

			ID converted = nsString(encodingName);
			long cfEncoding = myFoundationLibrary.CFStringConvertIANACharSetNameToEncoding(converted);

			ID restored = myFoundationLibrary.CFStringConvertEncodingToIANACharSetName(cfEncoding);
			if (ID.NIL.equals(restored))
				return -1;

			return convertCFEncodingToNS(cfEncoding);
		}

		private static long convertCFEncodingToNS(long cfEncoding) {
			return myFoundationLibrary.CFStringConvertEncodingToNSStringEncoding(cfEncoding) & 0xffffffffffL; // trim to
																												// C-type
																												// limits
		}

		public static void cfRetain(ID id) {
			myFoundationLibrary.CFRetain(id);
		}

		public static ID cgWindowListCreateImage(Foundation.NSRect screenBounds, int windowOption, ID windowID,
				int imageOption) {
			return myFoundationLibrary.CGWindowListCreateImage(screenBounds, windowOption, windowID, imageOption);
		}

		public static void cfRelease(ID... ids) {
			for (ID id : ids) {
				if (id != null) {
					myFoundationLibrary.CFRelease(id);
				}
			}
		}

		public static ID autorelease(ID id) {
			return invoke(id, "autorelease");
		}

		public static boolean isMainThread() {
			return invoke("NSThread", "isMainThread").booleanValue();
		}

		private static Callback ourRunnableCallback;
		private static final Map<String, RunnableInfo> ourMainThreadRunnables = new HashMap<>();
		private static long ourCurrentRunnableCount = 0;
		private static final Object RUNNABLE_LOCK = new Object();

		static class RunnableInfo {
			RunnableInfo(Runnable runnable, boolean useAutoreleasePool) {
				myRunnable = runnable;
				myUseAutoreleasePool = useAutoreleasePool;
			}

			Runnable myRunnable;
			boolean myUseAutoreleasePool;
		}

		public static void executeOnMainThread(final boolean withAutoreleasePool, final boolean waitUntilDone,
				final Runnable runnable) {
			String runnableCountString;
			synchronized (RUNNABLE_LOCK) {
				initRunnableSupport();

				runnableCountString = String.valueOf(++ourCurrentRunnableCount);
				ourMainThreadRunnables.put(runnableCountString, new RunnableInfo(runnable, withAutoreleasePool));
			}

			// fixme: Use Grand Central Dispatch instead?
			final ID ideaRunnable = getObjcClass("IdeaRunnable");
			final ID runnableObject = invoke(invoke(ideaRunnable, "alloc"), "init");
			final ID keyObject = invoke(nsString(runnableCountString), "retain");
			invoke(runnableObject, "performSelectorOnMainThread:withObject:waitUntilDone:", createSelector("run:"),
					keyObject, Boolean.valueOf(waitUntilDone));
			invoke(runnableObject, "release");
		}

		/**
		 * Registers idea runnable adapter class in ObjC runtime, if not registered yet.
		 * <p>
		 * Warning: NOT THREAD-SAFE! Must be called under lock. Danger of segmentation
		 * fault.
		 */
		private static void initRunnableSupport() {
			if (ourRunnableCallback == null) {
				final ID runnableClass = allocateObjcClassPair(getObjcClass("NSObject"), "IdeaRunnable");
				registerObjcClassPair(runnableClass);

				final Callback callback = new Callback() {
					@SuppressWarnings("UnusedDeclaration")
					public void callback(ID self, String selector, ID keyObject) {
						final String key = toStringViaUTF8(keyObject);
						invoke(keyObject, "release");

						RunnableInfo info;
						synchronized (RUNNABLE_LOCK) {
							info = ourMainThreadRunnables.remove(key);
						}

						if (info == null) {
							return;
						}

						ID pool = null;
						try {
							if (info.myUseAutoreleasePool) {
								pool = invoke("NSAutoreleasePool", "new");
							}

							info.myRunnable.run();
						} finally {
							if (pool != null) {
								invoke(pool, "release");
							}
						}
					}
				};
				if (!addMethod(runnableClass, createSelector("run:"), callback, "v@:*")) {
					throw new RuntimeException("Unable to add method to objective-c runnableClass class!");
				}
				ourRunnableCallback = callback;
			}
		}

		public static class NSDictionary {
			private final ID myDelegate;

			public NSDictionary(ID delegate) {
				myDelegate = delegate;
			}

			public ID get(ID key) {
				return invoke(myDelegate, "objectForKey:", key);
			}

			public ID get(String key) {
				return get(nsString(key));
			}

			public int count() {
				return invoke(myDelegate, "count").intValue();
			}

			public NSArray keys() {
				return new NSArray(invoke(myDelegate, "allKeys"));
			}

			/* @NotNull */
			public static Map<String, String> toStringMap(/* @Nullable */ ID delegate) {
				Map<String, String> result = new HashMap<>();
				if (isNil(delegate)) {
					return result;
				}

				NSDictionary dict = new NSDictionary(delegate);
				NSArray keys = dict.keys();

				for (int i = 0; i < keys.count(); i++) {
					String key = toStringViaUTF8(keys.at(i));
					String val = toStringViaUTF8(dict.get(key));
					result.put(key, val);
				}

				return result;
			}

			public static ID toStringDictionary(/* @NotNull */ Map<String, String> map) {
				ID dict = invoke("NSMutableDictionary", "dictionaryWithCapacity:", map.size());
				for (Map.Entry<String, String> entry : map.entrySet()) {
					invoke(dict, "setObject:forKey:", nsString(entry.getValue()), nsString(entry.getKey()));
				}
				return dict;
			}
		}

		public static class NSArray {
			private final ID myDelegate;

			public NSArray(ID delegate) {
				myDelegate = delegate;
			}

			public int count() {
				return invoke(myDelegate, "count").intValue();
			}

			public ID at(int index) {
				return invoke(myDelegate, "objectAtIndex:", index);
			}

			/* @NotNull */
			public List<ID> getList() {
				List<ID> result = new ArrayList<>();
				for (int i = 0; i < count(); i++) {
					result.add(at(i));
				}
				return result;
			}
		}

		public static class NSData {
			private final ID myDelegate;

			// delegate should not be nil
			public NSData(/* @NotNull */ ID delegate) {
				myDelegate = delegate;
			}

			public int length() {
				return invoke(myDelegate, "length").intValue();
			}

			public byte /* @NotNull */ [] bytes() {
				Pointer data = new Pointer(invoke(myDelegate, "bytes").longValue());
				return data.getByteArray(0, length());
			}

//			public /* @NotNull */ Image createImageFromBytes() {
//				return ImageLoader.loadFromBytes(bytes());
//			}
		}

		public static class NSAutoreleasePool {
			private final ID myDelegate;

			public NSAutoreleasePool() {
				myDelegate = invoke(invoke("NSAutoreleasePool", "alloc"), "init");
			}

			public void drain() {
				invoke(myDelegate, "drain");
			}
		}

		@Structure.FieldOrder({ "origin", "size" })
		public static class NSRect extends Structure implements Structure.ByValue {
			public NSPoint origin;
			public NSSize size;

			public NSRect(double x, double y, double w, double h) {
				origin = new NSPoint(x, y);
				size = new NSSize(w, h);
			}
		}

		@Structure.FieldOrder({ "x", "y" })
		public static class NSPoint extends Structure implements Structure.ByValue {
			public CGFloat x;
			public CGFloat y;

			@SuppressWarnings("UnusedDeclaration")
			public NSPoint() {
				this(0, 0);
			}

			public NSPoint(double x, double y) {
				this.x = new CGFloat(x);
				this.y = new CGFloat(y);
			}
		}

		@Structure.FieldOrder({ "width", "height" })
		public static class NSSize extends Structure implements Structure.ByValue {
			public CGFloat width;
			public CGFloat height;

			@SuppressWarnings("UnusedDeclaration")
			public NSSize() {
				this(0, 0);
			}

			public NSSize(double width, double height) {
				this.width = new CGFloat(width);
				this.height = new CGFloat(height);
			}
		}

		public static class CGFloat implements NativeMapped {
			private final double value;

			public CGFloat() {
				this(0);
			}

			public CGFloat(double d) {
				value = d;
			}

			@Override
			public Object fromNative(Object o, FromNativeContext fromNativeContext) {
				switch (Native.LONG_SIZE) {
				case 4:
					return new CGFloat((Float) o);
				case 8:
					return new CGFloat((Double) o);
				}
				throw new IllegalStateException();
			}

			@Override
			public Object toNative() {
				switch (Native.LONG_SIZE) {
				case 4:
					return (float) value;
				case 8:
					return value;
				}
				throw new IllegalStateException();
			}

			@Override
			public Class<?> nativeType() {
				switch (Native.LONG_SIZE) {
				case 4:
					return Float.class;
				case 8:
					return Double.class;
				}
				throw new IllegalStateException();
			}
		}

		public static ID fillArray(final Object[] a) {
			final ID result = invoke("NSMutableArray", "array");
			for (Object s : a) {
				invoke(result, "addObject:", convertType(s));
			}

			return result;
		}

		public static ID createDict(final String/* @NotNull */ [] keys, final Object /* @NotNull */ [] values) {
			final ID nsKeys = invokeVarArg("NSArray", "arrayWithObjects:", convertTypes(keys));
			final ID nsData = invokeVarArg("NSArray", "arrayWithObjects:", convertTypes(values));
			return invoke("NSDictionary", "dictionaryWithObjects:forKeys:", nsData, nsKeys);
		}

		/* @NotNull */
		public static PointerType createPointerReference() {
			PointerType reference = new PointerByReference(new Memory(Native.POINTER_SIZE));
			reference.getPointer().clear(Native.POINTER_SIZE);
			return reference;
		}

		/* @NotNull */
		public static ID castPointerToNSError(/* @NotNull */ PointerType pointerType) {
			return new ID(pointerType.getPointer().getLong(0));
		}

		public static Object[] convertTypes(Object /* @NotNull */ [] v) {
			final Object[] result = new Object[v.length + 1];
			for (int i = 0; i < v.length; i++) {
				result[i] = convertType(v[i]);
			}
			result[v.length] = ID.NIL;
			return result;
		}

		private static Object convertType(/* @NotNull */ Object o) {
			if (o instanceof Pointer || o instanceof ID) {
				return o;
			} else if (o instanceof String) {
				return nsString((String) o);
			} else {
				throw new IllegalArgumentException("Unsupported type! " + o.getClass());
			}
		}
	}

	private static final FoundationLibrary myFoundationLibrary;
	private static final Function myObjcMsgSend;

	static {
		if(!Platform.isMac())
			throw new UnsupportedOperationException();
		Version osVersion = new Version(System.getProperty("os.version"));
		Version minVersion = new Version("10.8.0"); // Mountain Lion
		if(osVersion.compareTo(minVersion) < 0) {
			throw new UnsupportedOperationException();
		}
		
		myFoundationLibrary = Native.load("Foundation", FoundationLibrary.class,
				Collections.singletonMap("jna.encoding", "UTF8"));
		NativeLibrary nativeLibrary = ((Library.Handler) Proxy.getInvocationHandler(myFoundationLibrary))
				.getNativeLibrary();
		myObjcMsgSend = nativeLibrary.getFunction("objc_msgSend");
	}

	public NotificationCenterToaster(ToasterSettings configuration) {
		super(configuration);
		capabilities.addAll(Arrays.asList(Capability.IMAGES, Capability.ACTIONS, Capability.CLOSE));
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				cleanupDeliveredNotifications();
			}
		});
	}

	@Override
	public Slice toast(ToastBuilder builder) {
		var notification = Foundation.invoke(Foundation.getObjcClass("NSUserNotification"), "new");
		Foundation.invoke(notification, "setTitle:",
				Foundation.nsString(StringUtil.stripHtml(builder.title(), true).replace("%", "%%")));
		Foundation.invoke(notification, "setInformativeText:",
				Foundation.nsString(StringUtil.stripHtml(builder.content(), true).replace("%", "%%")));
		
		var actions = builder.actions();
		if(actions.size() > 0) {
			Foundation.invoke(notification, "setHasActionButton:", true);	
			if(actions.size() > 1) {
				Foundation.invoke(notification, "setActionButtonTitle:",
						Foundation.nsString(actions.get(1).displayName()));
				Foundation.invoke(notification, "setOtherButtonTitle:",
						Foundation.nsString(actions.get(0).displayName()));
			}
			else {
				Foundation.invoke(notification, "setActionButtonTitle:",
						Foundation.nsString(actions.get(0).displayName()));	
			}
		}
		
		var center = Foundation.invoke(Foundation.getObjcClass("NSUserNotificationCenter"),
				"defaultUserNotificationCenter");
		Foundation.invoke(center, "deliverNotification:", notification);
		
		return new Slice() {
			@Override
			public void close() throws IOException {
				Foundation.invoke(notification, "removeDeliveredNotification:", notification);	
			}
		};
	}

	private static void cleanupDeliveredNotifications() {
		final ID center = Foundation.invoke(Foundation.getObjcClass("NSUserNotificationCenter"),
				"defaultUserNotificationCenter");
		Foundation.invoke(center, "removeAllDeliveredNotifications");
	}
}
