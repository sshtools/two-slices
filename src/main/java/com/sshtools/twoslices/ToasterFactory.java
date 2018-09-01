package com.sshtools.twoslices;

import com.sshtools.twoslices.impl.AWTNotifier;
import com.sshtools.twoslices.impl.GrowlToaster;
import com.sshtools.twoslices.impl.NotifyToaster;
import com.sshtools.twoslices.impl.OsXToaster;
import com.sshtools.twoslices.impl.SWTToaster;
import com.sshtools.twoslices.impl.SysOutNotifier;

/**
 * Responsible for creating a {@link Toaster} instance based on the current
 * operating environment.
 * <p>
 * The {@link Toaster} implementation is accessed through {@link #factory()},
 * which will lazily create an instance using the default algorithm, which tries
 * each one until a supported implementation is found. The default order is such
 * that the 'best' is chosen, which in general is the most native looking.
 * <p>
 * You can install you own factory by extending this class and instantiating
 * your custom class before any call to the {@link #factory()} method, either
 * directly or using the convenience {@link Toast} class.
 * <p>
 * Configuration hints can be provided to the toaster implementation via
 * {@link #setConfiguration(ToasterConfiguration)}.
 *
 */
public abstract class ToasterFactory {

	/**
	 * Default {@link ToasterFactory} that will lazily create a {@link Toaster} by
	 * trying all implementations until a supported one is found.
	 */
	public static class DefaultToasterFactory extends ToasterFactory {

		private Toaster instance;
		private Object lock = new Object();

		@Override
		public Toaster toaster() {
			synchronized (lock) {
				if (instance == null) {
					try {
						return instance = new NotifyToaster(configuration);
					} catch (UnsupportedOperationException uoe) {
						try {
							return instance = new GrowlToaster(configuration);
						} catch (UnsupportedOperationException uoe2) {
							try {
								return instance = new OsXToaster(configuration);
							} catch (UnsupportedOperationException uoe3) {
								try {
									return instance = new SWTToaster(configuration);
								} catch (UnsupportedOperationException uoe4) {
									try {
										return instance = new AWTNotifier(configuration);
									} catch (UnsupportedOperationException uoe5) {
										return instance = new SysOutNotifier(configuration);
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
	private static ToasterConfiguration configuration = new ToasterConfiguration();
	private static ToasterFactory instance;

	private static Object lock = new Object();

	/**
	 * Get an instance of the toaster factory which is responsible for creating an
	 * appropriate {@link Toaster}.
	 * 
	 * @return toaster factory
	 */
	public static ToasterFactory factory() {
		synchronized (lock) {
			if (instance == null)
				new DefaultToasterFactory();
			return instance;
		}
	}

	/**
	 * Set configuration hints.
	 * 
	 * @param configuration
	 *            configuration hints
	 */
	public static void setConfiguration(ToasterConfiguration configuration) {
		ToasterFactory.configuration = configuration;
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
