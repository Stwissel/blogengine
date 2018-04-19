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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Document.OutputSettings;
import org.jsoup.nodes.Document.OutputSettings.Syntax;
import org.jsoup.nodes.Entities.EscapeMode;

import com.google.common.base.Charsets;
import com.google.common.io.Closeables;
import com.google.common.io.Files;

/**
 * Specialized Output Stream for Blog handling
 * It cleans a HTML Stream that was written to it to make it pretty
 * and only saves when the content of an eventual existing file
 * has changed bytes compared to the content written to this Stream
 * 
 * @author swissel
 *
 */
public class BlogOutput extends OutputStream {

    private static final int            OUT_SIZE = 102400;
    private final ByteArrayOutputStream out;
    private final String                outputFileName;
    private final boolean cleanupHTML;

    public BlogOutput(final String fileName) {
        this.outputFileName = fileName;
        this.cleanupHTML = false;
        this.out = new ByteArrayOutputStream(BlogOutput.OUT_SIZE);
    }
    
    public BlogOutput(final String location, final boolean cleanupHTML) {
        this.outputFileName = location;
        this.cleanupHTML = cleanupHTML;
        this.out = new ByteArrayOutputStream(BlogOutput.OUT_SIZE);
    }

    @Override
    public void close() throws IOException {
        this.out.close();
        this.save();
    }

    @Override
    public void flush() throws IOException {
        this.out.flush();
    }

    @Override
    public void write(final byte[] b) throws IOException {
        this.out.write(b);
    }

    @Override
    public void write(final byte[] b, final int off, final int len) throws IOException {
        this.out.write(b, off, len);
    }

    @Override
    public void write(final int b) throws IOException {
        this.out.write(b);

    }
    
    private byte[] getBytesToSave() {
        if (this.cleanupHTML) {
            // Make really pretty HTML
            Document htmlDoc = Jsoup.parse(new String(this.out.toByteArray(),Charsets.UTF_8));
            OutputSettings outputSettings = new OutputSettings();
            outputSettings.indentAmount(4);
            outputSettings.escapeMode(EscapeMode.extended);
            outputSettings.prettyPrint(true);
            outputSettings.syntax(Syntax.xml);
            htmlDoc.outputSettings(outputSettings);
            return htmlDoc.outerHtml().getBytes();
        } else {
            return this.out.toByteArray();
        }        
    }
    
    private boolean isSaveRequired(final byte[] payload) {
        final File targetFile = new File(this.outputFileName);
        if (targetFile.isDirectory()) {
            System.err.println("Directory encountered!" + this.outputFileName);
            return false;
        }
        boolean saveThis = true;
        if (targetFile.exists()) {
            byte[] existingByte;
            try {
                existingByte = Files.asByteSource(targetFile).read();
                saveThis = !Arrays.equals(existingByte, payload);
            } catch (final IOException e) {
                saveThis = true;
            }

            if (saveThis) {
                targetFile.delete();
            }
        }
        return saveThis;
    }

    /**
     * Saves the output stream if it has been modified
     * 
     * @return true if it has been saved - false if not
     */
    private boolean save() {
        final byte[] saveCandidate = this.getBytesToSave();
        if(this.isSaveRequired(saveCandidate)) {
            final File targetFile = new File(this.outputFileName);
            try {
                // Ensure the directory structure exists
                Files.createParentDirs(targetFile);
                FileOutputStream finalOut = new FileOutputStream(targetFile);
                finalOut.write(saveCandidate);
                finalOut.flush();
                Closeables.close(finalOut, true);
                return true;
            } catch (final FileNotFoundException e) {
                e.printStackTrace();
            } catch (final IOException e) {
                e.printStackTrace();
            }
            System.out.println("\n+" + targetFile);
        }
        
        return false;
    }

}
