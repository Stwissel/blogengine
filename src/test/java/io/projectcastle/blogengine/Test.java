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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;

import io.projectcastle.blogengine.EntriesWithFiles;

public class Test {

	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		Test t = new Test();
		t.test1();

	}

	public void test1() throws IOException {
		String dataName = "/home/stw/Documents/Projects/wisselblog/src/attachments/attachments.json";
		String templateDir = "/home/stw/Documents/Projects/wisselblog/src/layouts/";
		String template = "downloads.mustache";
		String destination = "/home/stw/www/blog/downloads/index.html";

		FileOutputStream out = new FileOutputStream(new File(destination));
		FileInputStream in = new FileInputStream(new File(dataName));
		Writer pw = new PrintWriter(out);
		// Writer pw = new OutputStreamWriter(System.out);

		MustacheFactory mf = new DefaultMustacheFactory(new File(templateDir));
		Mustache mustache = mf.compile(template);
		EntriesWithFiles ef = EntriesWithFiles.loadDataFromJson(in);
		mustache.execute(pw, ef);
		pw.flush();

		pw.close();
		out.close();
		in.close();

		System.out.println("Done");
	}

}
