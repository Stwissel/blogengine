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
import java.util.ArrayList;
import java.util.Collection;

import io.projectcastle.blogengine.Config;

public class Cleanup {

    public static void main(String[] args) {
        Cleanup cleanup = new Cleanup();
        cleanup.go();
        System.out.println("Done");
    }

    private final Collection<String> deleteEntries = new ArrayList<>();
    
    private void go() {
        final Config config = Config.get(Config.CONFIG_NAME);
        this.cleanDirectory(config.sourceDirectory + "/" + config.documentDirectory);
        
    }

    private void cleanDirectory(String documentDirectory) {
        File curDir = new File(documentDirectory);
        if (!curDir.exists()) {
            System.err.println("Directory doesn't exist:"+documentDirectory);
            return;
        }
        
        if (curDir.isDirectory()) {
            for (final String curFile : curDir.list()) {
                this.cleanDirectory(curDir.getPath() + "/" + curFile);
            }
        } else {
            this.deleteEntries.forEach(e -> {
                if (curDir.getName().endsWith(e)) {
                    System.out.print(curDir.getName());
                    curDir.delete();
                    System.out.println(" - deleted");
                }
            });
        }
        
    }

    
    public Cleanup() {
        deleteEntries.add(".html");
        deleteEntries.add(".json");
        deleteEntries.add(".more");
    }
    
}
