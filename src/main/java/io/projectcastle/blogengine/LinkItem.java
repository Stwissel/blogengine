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
import java.util.List;

public class LinkItem implements Comparable<LinkItem>{

	final public String	name;
	final public String	place;
	final private String sorter;
	final private boolean inverseSort;
	public int		count	= 1;
	public boolean	active	= false;
	

	// Standard full blown link item with specific location
	public LinkItem(String linkItemName, String linkItemUrl, String sorter) {
		this.name = linkItemName;
		this.place = linkItemUrl;
		this.sorter = (sorter == null) ? this.cleanPlace(linkItemName) : sorter;
		this.inverseSort = false;
	}
	
	// Standard full blown link item with specific location
		public LinkItem(String linkItemName, String linkItemUrl, String sorter, boolean reverse) {
			this.name = linkItemName;
			this.place = linkItemUrl;
			this.sorter = (sorter == null) ? this.cleanPlace(linkItemName) : sorter;
			this.inverseSort = reverse;
		}
	
	// Version for the category list
	public LinkItem(String linkItemName, String linkItemBaseUrl) {
		this.name = linkItemName;
		this.place = linkItemBaseUrl+this.cleanPlace(linkItemName);
		this.sorter = this.cleanPlace(linkItemName);
		this.inverseSort = false;
	}

	// Allows to reverse order e.g. for series
	public int compareTo(LinkItem o) {
		return (this.inverseSort) ? o.sorter.compareTo(this.sorter): this.sorter.compareTo(o.sorter);
	}
	
	// ShortCut for simple creation
	public LinkItem(String linkItemName) {
		this.name = linkItemName;
		this.place = this.cleanPlace(linkItemName);
		this.sorter = this.place;
		this.inverseSort = false;
	}

	// A URL and sort friendly representation
	private String cleanPlace(String linkItemName) {
		return linkItemName.trim().toLowerCase().replaceAll("[^a-z0-9\n]+", "");
	}

	// Cleaning up the LinkItem mess in old entries
	public static List<LinkItem> cleanupLinkItems(final List<LinkItem> rawItems) {
		List<LinkItem> result = new ArrayList<>();
		rawItems.forEach(it -> {
			LinkItem oneResult = new LinkItem(it.name);
			result.add(oneResult);
		});
		return result;
	}
}
