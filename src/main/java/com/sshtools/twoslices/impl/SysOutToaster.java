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

import com.sshtools.twoslices.AbstractToaster;
import com.sshtools.twoslices.Slice;
import com.sshtools.twoslices.ToastBuilder;
import com.sshtools.twoslices.Toaster;
import com.sshtools.twoslices.ToasterService;
import com.sshtools.twoslices.ToasterSettings;

/**
 * Last resort implementation that outputs to the console.
 */
public class SysOutToaster extends AbstractToaster {
	
	public static class Service implements ToasterService {
		@Override
		public Toaster create(ToasterSettings settings) {
			return new SysOutToaster(settings);
		}
	}

	/**
	 * Constructor
	 * 
	 * @param configuration
	 *            configuration
	 */
	public SysOutToaster(ToasterSettings configuration) {
		super(configuration);
	}

	@Override
	public Slice toast(ToastBuilder builder) {
		System.out.println(String.format("[%1s] %s - %s", textIcon(builder.type()), builder.title(), builder.content()));
		return Slice.defaultSlice();
	}

}
