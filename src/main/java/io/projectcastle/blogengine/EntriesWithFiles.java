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

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeMap;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class EntriesWithFiles {

    public TreeMap<String, FileEntry> entries = new TreeMap<String, FileEntry>();
    private final boolean             isFile  = true;

    public static EntriesWithFiles loadDataFromJson(InputStream in) {
        EntriesWithFiles result = null;
        Gson gson = new GsonBuilder().create();
        result = (EntriesWithFiles) gson.fromJson(new InputStreamReader(in), EntriesWithFiles.class);
        return result;
    }

    public Collection<FileEntry> getAttachmentList() {
        return this.entries.values();
    }

    /**
     * Save the object to a JSON file for reuse
     */
    public void saveDatatoJson(OutputStream out) {
        GsonBuilder gb = new GsonBuilder();
        gb.setPrettyPrinting();
        gb.disableHtmlEscaping();
        Gson gson = gb.create();
        PrintWriter writer = new PrintWriter(out);
        gson.toJson(this, writer);
        writer.flush();
        writer.close();
    }

    public FileEntry add(FileEntry e) {
        this.entries.put(e.url, e);
        return e;
    }

    public FileEntry add(String subject, String url, String description, Date created) {
        FileEntry e = new FileEntry(subject, url, description, created);
        return this.add(e);
    }

    static class FileEntry {
        String          url;
        String          subject;
        String          description;
        Date            created;
        List<FileEntry> subEntries = null;

        public FileEntry(String subject, String url, String description, Date created) {
            this.subject = subject;
            this.created = created;
            this.url = url;
            this.description = description;
        }

        public FileEntry add(String subject, String url, String description, Date created) {
            FileEntry e = new FileEntry(subject, url, description, created);
            return this.add(e);
        }

        public FileEntry add(FileEntry e) {
            if (this.subEntries == null) {
                this.subEntries = new LinkedList<EntriesWithFiles.FileEntry>();
            }
            this.subEntries.add(e);
            return e;
        }

    }

    public boolean isFile() {
        return this.isFile;
    }
}
