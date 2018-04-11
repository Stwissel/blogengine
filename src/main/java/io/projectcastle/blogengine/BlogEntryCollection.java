/** ========================================================================= *
 * Copyright (C)  2016, 2018 Stephan H. Wissel ( https://wissel.net/ )        *
 *                            All rights reserved.                            *
 *                                                                            *
 *  @author     Stephan H. Wissel (stw) <stephan@wissel.net>                  *
 *                                       @notessensei                         *
 * @version     1.0                                                           *
 * ========================================================================== *
 *                                                                            *
 * Licensed under the  Apache License, Version 2.0  (the "License").  You may *
 * not use this file except in compliance with the License.  You may obtain a *
 * copy of the License at <http://www.apache.org/licenses/LICENSE-2.0>.       *
 *                                                                            *
 * Unless  required  by applicable  law or  agreed  to  in writing,  software *
 * distributed under the License is distributed on an  "AS IS" BASIS, WITHOUT *
 * WARRANTIES OR  CONDITIONS OF ANY KIND, either express or implied.  See the *
 * License for the  specific language  governing permissions  and limitations *
 * under the License.                                                         *
 *                                                                            *
 * ========================================================================== *
 */
package io.projectcastle.blogengine;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

public class BlogEntryCollection extends ArrayList<BlogEntry> {

	private static final long serialVersionUID = 2L;
	private final boolean reverseSortOrder;

	public BlogEntryCollection(final boolean reverseRun) {
		this.reverseSortOrder = reverseRun;
	}

	@SuppressWarnings("unused")
	private BlogEntryCollection() {
		// Hide blank constructor
		this.reverseSortOrder = false;
	}

	@Override
	public boolean add(final BlogEntry e) {
		final boolean result = super.add(e);
		Collections.sort(this);
		if (this.reverseSortOrder) {
			Collections.reverse(this);
		}
		return result;
	}

	@Override
	public boolean addAll(final Collection<? extends BlogEntry> c) {
		final boolean result = super.addAll(c);
		Collections.sort(this);
		if (this.reverseSortOrder) {
			Collections.reverse(this);
		}
		return result;
	}
}
