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

/**
 * Interface defining the callback invoked when the user submits text into an
 * inline reply / input field on a notification. Only toasters that have
 * {@link Capability#INPUT} support this; on others an input action behaves as
 * (or is ignored like) an ordinary action.
 */
@FunctionalInterface
public interface ToastReplyListener {
	/**
	 * Called when the user submits an inline reply.
	 *
	 * @param text the text the user entered
	 */
	void reply(String text);
}
