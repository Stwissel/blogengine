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

import java.io.File;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Contains the list of Blogentries that get summarized can be Month, Year,
 * Category
 * 
 * @author stw
 */
public class RenderInstructions {
	public String                              TemplateName;
	public String                              outFileName;
	public String                              pageTitle;
	public String                              pageLink;
	public String                              key;
	public String                              type;
	public LinkItem                            previousItem;
	public LinkItem                            nextItem;
	public TreeSet<BlogEntry>                  members    = null;
	public TreeMap<String, RenderInstructions> categories = null;
	public boolean reverse = false;

    public void add(BlogEntry be) {
        if (this.members == null) {
            this.members = new TreeSet<BlogEntry>();
        }
        this.members.add(be);
    }

    public void addToCategory(BlogEntry be, String categoryName, String categoryValue) {
        RenderInstructions ri;
        if (this.categories == null) {
            this.categories = new TreeMap<String, RenderInstructions>();
        }
        if (this.categories.containsKey(categoryValue)) {
            ri = this.categories.get(categoryValue);
        } else {
            ri = new RenderInstructions();
            ri.outFileName = this.outFileName;
            ri.TemplateName = this.TemplateName;
            ri.pageTitle = categoryName;
            ri.pageLink = categoryValue;
            ri.key = categoryValue;
            this.categories.put(categoryValue, ri);
        }
        ri.add(be);
    }

    /**
     * We check if there is a special template with the
     * keyname in it
     * 
     * @param theKey
     * @return a template name
     */
    public String getFinalTemplateName(String dirLocation, String theKey) {
        int dotPosition = this.TemplateName.lastIndexOf(".");
        StringBuilder b = new StringBuilder();

        b.append(this.TemplateName.substring(0, dotPosition));
        b.append("-");
        b.append(theKey);
        b.append(this.TemplateName.substring(dotPosition));
        String result = b.toString();
        File specialTemplate = new File(dirLocation + result);

        if (specialTemplate.exists()) {
            System.out.println(result);
        }

        return (specialTemplate.exists() ? result : this.TemplateName);

    }
}
