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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.atomic.AtomicReference;

import org.junit.Test;

import com.sshtools.twoslices.ToastBuilder.ToastAction;

/**
 * Unit tests for the inline reply / input action API surface on
 * {@link ToastBuilder}. These are pure and do not display a notification.
 */
public class ToastBuilderInputTest {

	@Test
	public void inputActionStoresPromptAndReplyListener() {
		var got = new AtomicReference<String>();
		var builder = new ToastBuilder()
				.title("Title")
				.content("Body")
				.input("reply", "Reply", "Message", got::set);

		assertEquals(1, builder.actions().size());
		ToastAction a = builder.actions().get(0);
		assertEquals("reply", a.name());
		assertEquals("Reply", a.label());
		assertTrue(a.input());
		assertEquals("Message", a.prompt());

		a.replyListener().reply("hello");
		assertEquals("hello", got.get());
	}

	@Test
	public void ordinaryActionIsNotInput() {
		var builder = new ToastBuilder().action("ok", "OK", () -> {
		});
		ToastAction a = builder.actions().get(0);
		assertFalse(a.input());
		assertNull(a.prompt());
		assertNull(a.replyListener());
	}
}
