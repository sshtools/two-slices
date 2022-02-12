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

import java.util.ServiceLoader;

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
	 * Default {@link ToasterFactory} that will lazily create a {@link Toaster} by
	 * trying all implementations until a supported one is found.
	 */
	public static class ServicesToasterFactory extends ToasterFactory {
		private Toaster instance;
		private Object lock = new Object();

		@Override
		public Toaster toaster() {
			synchronized (lock) {
				if (instance == null) {
					for (ToasterService toaster : ServiceLoader.load(ToasterService.class)) {
						try {
							return instance = toaster.create(settings);
						}
						catch(Exception e) {
						}
					}
					throw new UnsupportedOperationException("No toasters available.");
				}
				return instance;
			}
		}
	}

	private static ToasterSettings settings = new ToasterSettings();
	private static ToasterFactory instance;
	private static Object lock = new Object();

	/**
	 * Get an instance of the toaster factory which is responsible for creating an
	 * appropriate {@link Toaster}.
	 * 
	 * @return toaster factory
	 */
	public static ToasterFactory getFactory() {
		synchronized (lock) {
			if (instance == null)
				instance = new ServicesToasterFactory();
			return instance;
		}
	}

	/**
	 * Set an instance of the toaster factory which is responsible for creating an
	 * appropriate {@link Toaster}.
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
