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
package com.sshtools.twoslices;

import com.sshtools.twoslices.impl.AWTNotifier;
import com.sshtools.twoslices.impl.DBUSNotifyToaster;
import com.sshtools.twoslices.impl.GNTPToaster;
import com.sshtools.twoslices.impl.GrowlToaster;
import com.sshtools.twoslices.impl.JavaFXToaster;
import com.sshtools.twoslices.impl.NotifyToaster;
import com.sshtools.twoslices.impl.NotiificationCenterToaster;
import com.sshtools.twoslices.impl.OsXToaster;
import com.sshtools.twoslices.impl.SWTToaster;
import com.sshtools.twoslices.impl.SysOutNotifier;

/**
 * Responsible for creating a {@link Toaster} instance based on the current
 * operating environment.
 * <p>
 * The {@link Toaster} implementation is accessed through {@link #getFactory()},
 * which will lazily create an instance using the default algorithm, which tries
 * each one until a supported implementation is found. The default order is such
 * that the 'best' is chosen, which in general is the most native looking.
 * <p>
 * You can install you own factory by extending this class and instantiating
 * your custom class before any call to the {@link #getFactory()} method, either
 * directly or using the convenience {@link Toast} class.
 * <p>
 * Configuration hints can be provided to the toaster implementation via
 * {@link #setSettings(ToasterSettings)}.
 *
 */
public abstract class ToasterFactory {
	/**
	 * Default {@link ToasterFactory} that will lazily create a {@link Toaster}
	 * by trying all implementations until a supported one is found.
	 */
	public static class DefaultToasterFactory extends ToasterFactory {
		private Toaster instance;
		private Object lock = new Object();

		@Override
		public Toaster toaster() {
			synchronized (lock) {
				if (instance == null) {
					try {
						instance = new GNTPToaster(settings);
					} catch (UnsupportedOperationException uoe0) {
						try {
							instance = new DBUSNotifyToaster(settings);
						} catch (UnsupportedOperationException uoe1) {
							try {
								instance = new NotifyToaster(settings);
							} catch (UnsupportedOperationException uoe2) {
								try {
									instance = new GrowlToaster(settings);
								} catch (UnsupportedOperationException uoe3) {
									try {
										instance = new NotiificationCenterToaster(settings);
									}
									catch(UnsupportedOperationException uoe4) {
										try {
											instance = new OsXToaster(settings);
										} catch (UnsupportedOperationException uoe5) {
											try {
												instance = new JavaFXToaster(settings);
											}
											catch(NoClassDefFoundError | UnsupportedOperationException uoe6) {
												try {
													instance = new SWTToaster(settings);
												} catch (NoClassDefFoundError | UnsupportedOperationException uoe7) {
													try {
														instance = new AWTNotifier(settings);
													} catch (Exception uoe8) {
														instance = new SysOutNotifier(settings);
													}
												}
											}
										}
									}
								}
							}
						}
					}
				}
				return instance;
			}
		}
	}

	private static ToasterSettings settings = new ToasterSettings();
	private static ToasterFactory instance;
	private static Object lock = new Object();

	/**
	 * Get an instance of the toaster factory which is responsible for creating
	 * an appropriate {@link Toaster}.
	 * 
	 * @return toaster factory
	 */
	public static ToasterFactory getFactory() {
		synchronized (lock) {
			if (instance == null)
				new DefaultToasterFactory();
			return instance;
		}
	}

	/**
	 * Set an instance of the toaster factory which is responsible for creating
	 * an appropriate {@link Toaster}.
	 * 
	 * @param factory toaster factory
	 */
	public static void setFactory(ToasterFactory factory) {
		synchronized (lock) {
			instance = factory;
		}
	}

	/**
	 * Set settings hints.
	 * 
	 * @param settings settings hints
	 */
	public static void setSettings(ToasterSettings settings) {
		ToasterFactory.settings = settings;
	}

	/**
	 * Get settings hints.
	 * 
	 * @return settings hints
	 */
	public static ToasterSettings getSettings() {
		return ToasterFactory.settings;
	}

	protected ToasterFactory() {
		if (instance != null)
			throw new IllegalStateException("You can only construct one instance of a ToasterFactory.");
		instance = this;
	}

	/**
	 * Create a {@link Toaster} appropriate for this platform.
	 * 
	 * @return toaster.
	 */
	public abstract Toaster toaster();
}
