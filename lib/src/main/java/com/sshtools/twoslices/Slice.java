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

import java.io.Closeable;
import java.io.IOException;

/**
 * Used as a handle to generated notifications, allowing them to be closed
 * prematurely programmatically when supported by the underlying toaster
 * implementation.
 *
 */
public interface Slice extends Closeable {

	public static Slice defaultSlice() {
		return new Slice() {
			@Override
			public void close() throws IOException {
				throw new UnsupportedOperationException();
			}
		};
	};
}
