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

import java.util.Set;

/**
 * Interface to be implemented by all OS specific providers of notification
 * messages. For clients in general, there is no need to directly use methods.
 * Instead use the convenience class {@link Toast}.
 */
public interface Toaster {
	/**
	 * Get the capabilities for this toaster.
	 * 
	 * @return capabilities
	 */
	Set<Capability> capabilities();

	/**
	 * Display a notification message. A handle to the notification message
	 * (allowing programmatic closing etc) will be always be returned, but
	 * the operations it provides may be always be supported.
	 * 
	 * @param builder builder
	 * @return handle to notification message. 
	 * @throws ToasterException if there is a serious unrecoverable error.
	 */
	Slice toast(ToastBuilder builder);
}
