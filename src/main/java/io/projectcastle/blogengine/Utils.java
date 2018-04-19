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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class Utils {

    public final static String DATE_COMPARE_FORMAT   = "yyyy-DDD-HH-mm-ss-S";
    public final static String YAML_DATE_FORMAT      = "yyyy-MM-dd'T'HH:mm:ss'Z'";
    public final static String YAML_SHORTDATE_FORMAT = "yyyy-MM-dd";

    public static String date2ComparableString(final Date inDate) {
        final SimpleDateFormat sdf = new SimpleDateFormat(Utils.DATE_COMPARE_FORMAT, Locale.US);
        return sdf.format(inDate);
    }

    public static Date extractDateFromYaml(final Object dateCandidate) {
        Date result = null;
        if (dateCandidate == null) {
            result = new Date();
        } else if (dateCandidate instanceof Date) {
            result = (Date) dateCandidate;
        } else {
            final SimpleDateFormat sdf = new SimpleDateFormat(Utils.YAML_DATE_FORMAT, Locale.US);
            try {
                result = sdf.parse(String.valueOf(dateCandidate));
            } catch (final ParseException e) {
                final SimpleDateFormat sdf2 = new SimpleDateFormat(Utils.YAML_SHORTDATE_FORMAT, Locale.US);
                try {
                    result = sdf2.parse(String.valueOf(dateCandidate).substring(0, 10));
                } catch (final ParseException e2) {
                    System.err.println("Can't parse the date:" + String.valueOf(dateCandidate));
                    result = new Date();
                }

            }
        }

        return result;

    }

    /**
     * Extract the text content of a HTML file or fragment 
     * @param htmlString the source String
     * @param maxLength the maximum length
     * @return the plain text without markup
     * 
     */
    public static String getTextFromHTML(String htmlString, int maxLength) {
        StringBuilder result = new StringBuilder();
        Document htmlDoc = Jsoup.parse(htmlString);
        if (htmlDoc.body().hasText()) {
        result.append(htmlDoc.body().text());
        } else {
            result.append(Config.get().rssDescription);
        }
        return result.substring(0, Math.min(maxLength, result.length()));
    }
}
