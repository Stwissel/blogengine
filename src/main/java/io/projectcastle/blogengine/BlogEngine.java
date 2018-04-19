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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.joda.time.Duration;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import com.google.common.io.Files;

import io.projectcastle.blogengine.EntriesWithFiles.FileEntry;

/**
 * @author stw
 */
public class BlogEngine {

    private final static String ALL_CATEGORY_NAME = "allCategories";
    private final static String ALL_ENTRY_NAME    = "allEntries";
    private final static String BLOG_EXTENSION    = ".blog";

    /**
     * @param args
     * @throws NotesException
     * @throws IOException
     */
    public static void main(final String[] args) throws IOException {
        final Date start = new Date();
        // ALL Parameters are in the config object which reads/writes
        // configuration from JSON
        // If we have any command line arguments we save back the config
        final BlogEngine blogEngine = new BlogEngine(Config.get(Config.CONFIG_NAME,(args.length > 0)));

        System.out.println("\n\n *************** Loading Blog from disk ********************\n\n");
        blogEngine.loadBlogFromDisk();
        System.out.println("\n\n ***************** Rendering to disk ***********************\n\n");
        blogEngine.renderBlog();
        final Date end = new Date();
        System.out.println("\n\n ************************** Done! **************************\n\n");
        final Duration d = new Duration(start.getTime(), end.getTime());
        System.out.println("Duration: " + String.valueOf(d.getStandardSeconds()) + " seconds");

    }

    private final TreeMap<String, LinkItem>                  allCategories     = new TreeMap<String, LinkItem>();
    private final TreeMap<String, LinkItem>                  allDateCategories = new TreeMap<String, LinkItem>();
    private final TreeMap<String, TreeMap<String, LinkItem>> allSeries         = new TreeMap<String, TreeMap<String, LinkItem>>();
    private final TreeSet<BlogEntry>                         theBlog           = new TreeSet<BlogEntry>();
    private final Map<String, BlogEntry>                     blogById          = new HashMap<>();
    private final Config                                     config;
    private EntriesWithFiles                                 fileEntries       = new EntriesWithFiles();
    private EntriesWithFiles                                 imgEntries        = new EntriesWithFiles();
    private final TreeMap<String, RenderInstructions>        overviewPages     = new TreeMap<String, RenderInstructions>();
    // for rendering and lookup of old/new URLs
    private final TreeMap<String, String> mapperOldNewURLs = new TreeMap<String, String>();

    public BlogEngine(final Config config) {
        this.config = config;
        // have 2 render instructions for the all.html and the
        // categories/index.html
        final RenderInstructions riAll = new RenderInstructions();
        riAll.type = "index";
        riAll.key = "AllDocuments";
        riAll.pageTitle = "All entries";
        riAll.outFileName = config.allIndexFileName;
        riAll.TemplateName = config.ALL_INDEX_TEMPLATE;
        riAll.pageLink = "AllDocuments";
        riAll.members = this.theBlog;
        riAll.reverse = true;

        final RenderInstructions riCat = new RenderInstructions();
        riCat.type = "index";
        riCat.key = BlogEngine.ALL_CATEGORY_NAME;
        riCat.pageTitle = "All Categories";
        riCat.outFileName = "categories/" + config.indexFileName;
        riCat.TemplateName = config.ALL_CATEGORY_TEMPLATE;
        riCat.pageLink = BlogEngine.ALL_CATEGORY_NAME;
        riCat.reverse = true;

        this.overviewPages.put(BlogEngine.ALL_ENTRY_NAME, riAll);
        this.overviewPages.put(BlogEngine.ALL_CATEGORY_NAME, riCat);

    }

    /**
     * @return the config
     */
    public Config getConfig() {
        return this.config;
    }

    /**
     * @return the theBlog
     */
    public TreeSet<BlogEntry> getTheBlog() {
        return this.theBlog;
    }

    /**
     * Laedt alle Blog entries von JSON Files auf Disk
     *
     * @param sourceFileOrDirName
     * @throws IOException
     */
    public Set<BlogEntry> loadBlogFromDisk() throws IOException {
        final File srcDir = new File(this.config.sourceDirectory);
        if (!srcDir.exists()) {
            System.err.print(this.config.sourceDirectory + " doesn't exist!");
            return null;
        } else if (!srcDir.isDirectory()) {
            System.err.print(this.config.sourceDirectory + " is not a directory!");
            return null;
        }

        final String path = srcDir.getPath();
        this.loadBlogEntriesFromDisk(path + this.getConfig().documentDirectory);
        System.out.println("\n\nBlog loaded from disk");
        System.out.println("\n\nLoading comments...");
        this.loadCommentsFromDisk(path + this.getConfig().commentDirectory);
        System.out.println("\nComments loaded from disk");
        this.loadFileDefinitionsFromDisk(path);
        System.out.println("\n\nFile definitions loaded from disk");
        return this.getTheBlog();

    }

    private void loadCommentsFromDisk(String sourceFileOrDirName) {
        final File srcDir = new File(sourceFileOrDirName);
        if (!srcDir.exists()) {
            System.err.print(sourceFileOrDirName + " doesn't exist");
            return;
        }

        if (srcDir.isDirectory()) {
            // Recursive call to get files in directory structure
            System.out.println("Comments from " + srcDir.getAbsolutePath());
            for (final String curFile : srcDir.list()) {
                this.loadCommentsFromDisk(srcDir.getPath() + "/" + curFile);
            }

        } else if (srcDir.getName().endsWith(".comment") || srcDir.getName().endsWith(".json")) {
            BlogComment bc = BlogComment.loadFromJson(srcDir);
            if (bc != null && bc.isValid()) {
                String parent = bc.getParentId();
                if (this.blogById.containsKey(parent)) {
                    this.blogById.get(parent).addComment(bc);
                } else {
                    System.err.println("Can't find parent:" + parent);
                }
            }
        }
    }

    /**
     * Adds values of a blog entries to the global arrays
     *
     * @param be
     */
    private void addBlogContext(final BlogEntry be) {
        if (be != null) {
            LinkItem catItem = null;
            final LinkItem dateItem = new LinkItem(be.getDateYear(), this.config.webBlogLocation + be.getDateYear(),
                    Utils.date2ComparableString(be.getPublishDate()));
            // Store it in the big blog list for retrieval
            this.theBlog.add(be);
            // and lookup
            this.blogById.put(be.getUNID(), be);

            // Add the links to the mapping
            // this.config.plinkPrefix will be handled by the HTTP rule...
            // String key = this.config.plinkPrefix +
            // be.getOldURL().toLowerCase();
            final String key = String.valueOf(be.getOldURL()).toLowerCase();
            final String value = be.getEntryUrl();
            if (!"null".equals(key)) {
                this.mapperOldNewURLs.put(key, value);
            }

            // Populate the category list
            for (final String catName : be.getCategory()) {
                final LinkItem li = new LinkItem(catName);
                final String catKey = li.place;
                if (this.allCategories.containsKey(catKey)) {
                    this.allCategories.get(catKey).count += 1;
                } else {
                    catItem = new LinkItem(li.name, this.config.webBlogLocation + this.config.categoriesLocation);
                    this.allCategories.put(catKey, catItem);
                }

                this.overviewPages.get("allCategories").addToCategory(be, li.name, li.place);

            }
            // Date with month and year or only year?
            // Month - year
            // this.allDateCategories.put(be.getDateURL(), dateItem);
            // Year only
            if (this.allDateCategories.containsKey(be.getDateYear())) {
                this.allDateCategories.get(be.getDateYear()).count += 1;
            } else {
                this.allDateCategories.put(be.getDateYear(), dateItem);
            }
            // Add to the lists for category, month, year
            this.addToOverviewPage("year", null, be.getDateYear(), be);
            for (final String catName : be.getCategory()) {
                final LinkItem li = new LinkItem(catName);
                this.addToOverviewPage("category", li.name, li.place, be);
            }
            this.addToOverviewPage("yearmonth", null, be.getDateYear() + "/" + be.getDateMonthNumber(), be);

            // Add to a series collection if there
            if (be.getSeries() != null) {
                final String series = be.getSeries();
                final TreeMap<String, LinkItem> c = this.allSeries.containsKey(series) ? this.allSeries.get(series)
                        : new TreeMap<String, LinkItem>();
                // We sort categories reverse
                catItem = new LinkItem(be.getTitle(), this.config.webBlogLocation + be.getEntryUrl(),
                        Utils.date2ComparableString(be.getPublishDate()), true);
                c.put(be.getEntryUrl(), catItem);

                this.allSeries.put(series, c);
            }

        }
    }

    private void addToOverviewPage(final String type, final String title, final String key, final BlogEntry blogEntry) {
        RenderInstructions ri;
        String templateName;
        String outfileName;
        String pageTitle;
        String pageLink;

        if (blogEntry == null) {
            return;
        }

        if (type.equals("year")) {
            templateName = this.config.YEAR_TEMPLATE;
            outfileName = key + "/" + this.config.indexFileName;
            pageTitle = "Year " + key;
            pageLink = key;

        } else if (type.equals("category")) {
            templateName = this.config.CATEGORY_TEMPLATE;
            outfileName = this.config.categoriesLocation + key + ".html";
            pageTitle = title;
            pageLink = key;
        } else if (type.equals("yearmonth")) {
            templateName = this.config.MONTH_TEMPLATE;
            outfileName = key + "/" + this.config.indexFileName;
            pageTitle = "By Date: " + blogEntry.getPublishDateStringShort();
            pageLink = key;
        } else {
            // We dont know the type
            return;
        }

        if (this.overviewPages.containsKey(key)) {
            ri = this.overviewPages.get(key);
        } else {
            ri = new RenderInstructions();
            ri.type = type;
            ri.key = key;
            ri.pageTitle = pageTitle;
            ri.outFileName = outfileName;
            ri.TemplateName = templateName;
            ri.pageLink = pageLink;
            this.overviewPages.put(key, ri);
        }

        // In year we want to subcategorize with month
        if (type.equals("year")) {
            ri.addToCategory(blogEntry, blogEntry.getDateMonth(), blogEntry.getDateMonthNumber());
        } else {
            ri.add(blogEntry);
        }
    }

    private String cleanNginxMapperString(final String inString, final boolean isKey) {
        final StringBuilder result = new StringBuilder();

        if (!inString.startsWith(this.config.webBlogLocation)) {
            if (isKey) {
                result.append(this.config.plinkPrefix);
            } else {
                result.append(this.config.webBlogLocation);
            }
        }
        for (int i = 0; i < inString.length(); i++) {
            final char x = inString.charAt(i);
            if (x == ' ') {
                result.append("%20");
            } /* more here */else {
                result.append(x);
            }
        }

        return result.toString();
    }

    private void retrieveBlogFilesFromDisk(final String sourceFileOrDirName, final Collection<File> blogFileList) {
        final File fileCandidate = new File(sourceFileOrDirName);
        if (!fileCandidate.exists()) {
            System.err.print(sourceFileOrDirName + " doesn't exist");
            return;
        }

        if (fileCandidate.isDirectory()) {
            System.out.println(fileCandidate.getAbsolutePath());
            // Recursive call to get files in directory structure
            for (final String curFile : fileCandidate.list()) {
                this.retrieveBlogFilesFromDisk(fileCandidate.getPath() + File.separator + curFile, blogFileList);
            }

        } else if (fileCandidate.getName().endsWith(BLOG_EXTENSION)) {
            blogFileList.add(fileCandidate);

        }
    }

    /**
     * Loads Blog entries recursively from disk
     *
     * @param sourceDirName
     * @throws IOException
     */
    private void loadBlogEntriesFromDisk(final String sourceDirName) {
        final File srcDir = new File(sourceDirName);
        if (!srcDir.exists()) {
            System.err.print(sourceDirName + " doesn't exist");
            return;
        }

        Collection<File> blogFileList = new ArrayList<>();
        this.retrieveBlogFilesFromDisk(sourceDirName, blogFileList);

        blogFileList.forEach(blogfile -> {
            BlogEntry be = null;
            try {
                final FileInputStream in = new FileInputStream(blogfile);
                be = BlogEntry.loadDataFromBlog(in);
                in.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            if ((be != null) && (be.getTitle() != null) && be.getStatus().equalsIgnoreCase("Published")) {
                this.addBlogContext(be);
            }
        });
    }

    private EntriesWithFiles loadFileDefFromDisk(final String sourceFileName) {

        EntriesWithFiles result = null;
        final File source = new File(sourceFileName);

        try {
            final FileInputStream in = new FileInputStream(source);
            result = EntriesWithFiles.loadDataFromJson(in);
            in.close();
        } catch (final IOException e) {
            e.printStackTrace();
        }

        return result;

    }

    private void loadFileDefinitionsFromDisk(final String sourceDirectory) {

        this.imgEntries = this.loadFileDefFromDisk(sourceDirectory + this.config.imageDirectory + "images.json");
        this.fileEntries = this
                .loadFileDefFromDisk(sourceDirectory + this.config.attachmentDirectory + "attachments.json");
        this.updateMapper(this.mapperOldNewURLs, this.imgEntries);
        this.updateMapper(this.mapperOldNewURLs, this.fileEntries);
    }

    private void render404() {

        final String template = this.config.ERROR_TEMPLATE;
        final String finalDestination = this.config.destinationDirectory + this.config.errorFileName;

        final BlogIndex bi = new BlogIndex();
        bi.allCategories = this.allCategories.values();
        bi.allDateCategories = this.allDateCategories.values();
        bi.topArticles = new BlogEntryCollection(true);
        bi.topArticles.addAll(this.theBlog);

        this.renderToDisk(template, finalDestination, bi);
        System.out.println("Rendered 404");

    }

    private void renderAttachments() {

        final String template = this.config.ATTACHMENT_TEMPLATE;
        final String finalDestination = this.config.destinationDirectory + this.config.downloadDirectory
                + this.config.indexFileName;
        this.renderToDisk(template, finalDestination, this.fileEntries);

        System.out.println("Rendered Attachments");

    }

    /**
     * Writes all Blog entries out to disk. Since they are ordered by date in
     * the TreeMap that happens in sequence. Special challenge: We need to have
     * the next and previous entries to successfully render them completely.
     *
     * @throws IOException
     */
    private void renderBlog() throws IOException {
        final String template = this.config.ENTRY_TEMPLATE;
        final BlogIndex seriesIndex = new BlogIndex();
        final MustacheFactory mf = new DefaultMustacheFactory(new File(this.config.templateDirectory));
        final Mustache mustache = mf.compile(template);

        this.prepareBlogEntriesWithPrevNextSeries(seriesIndex);

        this.theBlog.forEach(renderEntry -> {
            this.renderOneEntry(renderEntry, mustache);
        });

        System.out.println("\nEntries completed, now categories & dates\n");

        // Categories & Date Categories !!
        this.renderOverViewPages();
        this.renderAttachments();
        this.renderIndex();
        this.renderIndexRSS();
        this.render404();
        this.renderSeries(seriesIndex);
        this.renderImprint();
        this.renderURLMapper();
        this.renderNGinxURLMapper();

        System.out.println("...Done...");
    }

    private void prepareBlogEntriesWithPrevNextSeries(BlogIndex seriesIndex) {
        final String baseDir = this.config.webBlogLocation;
        seriesIndex.topArticles = new BlogEntryCollection(true);
        final Set<String> completedSeries = new HashSet<String>();
        // The previously rendered entry
        BlogEntry renderEntry = null;

        // Prepare Blog entries with previous and next entries as well as Series
        for (final BlogEntry be : this.theBlog) {
            be.cleanupComments();
            be.setAllCategories(this.allCategories.values());
            be.setAllDateCategories(this.allDateCategories.descendingMap().values());

            if ((be.getSeries() != null) && be.getStatus().equals("Published")) {
                final String series = be.getSeries();
                if (this.allSeries.containsKey(series)) {
                    final List<LinkItem> l = new ArrayList<LinkItem>();
                    l.addAll(this.allSeries.get(series).values());
                    be.setSeriesMember(l);
                    if (!completedSeries.contains(series)) {
                        // First entry of series - to be captures
                        seriesIndex.topArticles.add(be);
                        completedSeries.add(series);
                    }
                }
            }
            // We capture the previous link if we have one - only possible
            // for the second entry onwards
            // renderEntry contains the previous Blogentry which is the
            // needs its nextLink populated by be and be needs its previousLink
            // populated by renderentry
            if (renderEntry != null) {
                renderEntry.setNextItem(be.getLinkItem(baseDir));
                be.setPreviousItem(renderEntry.getLinkItem(baseDir));
            }

            renderEntry = be;
        }
    }

    private void renderImprint() throws IOException {

        final String template = this.config.IMPRINT_TEMPLATE;
        final String finalDestination = this.config.destinationDirectory + this.config.imprintFileName;

        final BlogIndex bi = new BlogIndex();
        bi.allCategories = this.allCategories.values();
        bi.allDateCategories = this.allDateCategories.values();
        bi.topArticles = new BlogEntryCollection(true);
        final int max = 5;
        int i = 0;
        final Iterator<BlogEntry> it = this.theBlog.descendingSet().iterator();

        while (it.hasNext() && (i < max)) {
            final BlogEntry cur = it.next();

            bi.topArticles.add(cur);
            i++;
        }

        this.renderToDisk(template, finalDestination, bi);
        System.out.println("Rendered Imprint");

    }

    private void renderIndex() {

        final String template = this.config.INDEX_TEMPLATE;
        final String finalDestination = this.config.destinationDirectory + this.config.indexFileName;

        final BlogIndex bi = new BlogIndex();
        bi.allCategories = this.allCategories.values();
        bi.allDateCategories = this.allDateCategories.values();
        bi.topArticles = new BlogEntryCollection(true);
        final int max = this.config.entriesOnFrontPage;
        int i = 0;
        final Iterator<BlogEntry> it = this.theBlog.descendingIterator();

        while (it.hasNext() && (i < max)) {
            final BlogEntry cur = it.next();
            if (cur.getStatus().equals("Published")) {
                bi.topArticles.add(cur);
                i++;
            }
        }
        this.renderToDisk(template, finalDestination, bi);

        System.out.println("Rendered Index");
    }

    private void renderIndexRSS() {
        final String finalDestination = this.config.destinationDirectory + this.config.indexRSSName;
        final String finalDestination2 = this.config.destinationDirectory + this.config.indexRSSName2;
        final BlogOutput out = new BlogOutput(finalDestination);
        final BlogIndex bi = new BlogIndex();
        bi.allCategories = this.allCategories.values();
        bi.allDateCategories = this.allDateCategories.values();
        bi.topArticles = new BlogEntryCollection(true);
        final int max = this.config.entriesInRSS;
        int i = 0;
        final Iterator<BlogEntry> it = this.theBlog.descendingIterator();

        while (it.hasNext() && (i < max)) {
            final BlogEntry cur = it.next();
                bi.topArticles.add(cur);
                i++;
        }

        final RSSFeedWriter rss = new RSSFeedWriter(this.getConfig(), bi);
        try {
            rss.write(out);
            out.flush();
            out.close();
            Files.copy(new File(finalDestination), new File(finalDestination2));
            System.out.println("\nRSS updated");

        } catch (final Exception e) {
            System.out.println("\nstories.rss rendering failed: " + e.getMessage());
        }

    }

    /**
     * Creates the map for blog redirections
     *
     * @param destination
     * @throws FileNotFoundException
     */
    private void renderNGinxURLMapper() throws FileNotFoundException {
        final File outFile = new File(this.config.destinationDirectory + this.config.urlmapNGinx);
        if (outFile.exists()) {
            outFile.delete();
        }

        final ArrayList<String> keysWritten = new ArrayList<String>();

        final PrintWriter pw = new PrintWriter(outFile);

        for (final Map.Entry<String, String> e : this.mapperOldNewURLs.entrySet()) {
            final String key = e.getKey().toLowerCase();
            final String value = e.getValue();

            final String realKey = this.cleanNginxMapperString(key, true);
            final String realValue = this.cleanNginxMapperString(value, false);

            if (!keysWritten.contains(realKey)) {
                pw.write(realKey);
                pw.write(" ");
                pw.write(realValue);
                pw.write(";\n");
                keysWritten.add(realKey);
            }

        }
        pw.flush();
        pw.close();
        System.out.println("URL mapping written to file " + outFile.getPath());

    }

    private void renderOneEntry(final BlogEntry be, final Mustache mustache) {

        //TODO: remove dependency on this.allCategories and this.allSeries and this.allDateCategories
        
        final String location = this.config.destinationDirectory + be.getEntryUrl();

        // Set the current context
        for (final String catName : be.getCategory()) {
            final LinkItem cat = new LinkItem(catName);
            final String c = cat.place;
            this.allCategories.get(c).active = true;
        }
        this.allDateCategories.get(be.getDateYear()).active = true;

        if (be.getSeries() != null) {
            final String series = be.getSeries();
            if (this.allSeries.containsKey(series)) {
                this.allSeries.get(series).get(be.getEntryUrl()).active = true;
            }
        }

        // Prepare to write out
        this.renderToDisk(mustache, location, be);

        // Cleanup
        for (final String catName : be.getCategory()) {
            final LinkItem cat = new LinkItem(catName);
            final String c = cat.place;
            this.allCategories.get(c).active = false;
        }
        this.allDateCategories.get(be.getDateYear()).active = false;

        if (be.getSeries() != null) {
            final String series = be.getSeries();
            if (this.allSeries.containsKey(series)) {
                this.allSeries.get(series).get(be.getEntryUrl()).active = false;
            }
        }
    }

    private void renderOverViewPage(final RenderInstructions ri) {
        final String template = ri.getFinalTemplateName(this.config.templateDirectory, ri.key);
        final String finalDestination = this.config.destinationDirectory + ri.outFileName;
        boolean goodToGo = false;
        // Set of categories
        if (this.allCategories.containsKey(ri.key)) {
            this.allCategories.get(ri.key).active = true;
        }
        if (this.allDateCategories.containsKey(ri.key)) {
            this.allDateCategories.get(ri.key).active = true;
        }

        final BlogIndex bi = new BlogIndex();
        bi.allCategories = this.allCategories.values();
        bi.allDateCategories = this.allDateCategories.values();
        bi.pageTitle = ri.pageTitle;
        bi.pageLink = ri.pageLink;
        bi.nextItem = ri.nextItem;
        bi.previousItem = ri.previousItem;

        // The renderinstructions might have entries or categorized entries
        // We check for null before we render

        if (ri.members != null) {
            bi.topArticles = new BlogEntryCollection(!ri.reverse);
            // Little confusion on sorting order
            final Iterator<BlogEntry> it = ri.members.iterator();
            // We need to copy from the render instruction to get the
            // sequence reversed
            while (it.hasNext()) {
                final BlogEntry cur = it.next();
                if (cur.getStatus().equals("Published")) {
                    bi.topArticles.add(cur);
                }
            }
            goodToGo = true;
        } else if (ri.categories != null) {
            bi.categorizedEntries = new ArrayList<BlogIndex>();
            final Iterator<String> it = (ri.reverse) ? ri.categories.keySet().iterator()
                    : ri.categories.descendingKeySet().iterator();
            // We need to copy from the render instruction to get the
            // sequence reversed
            while (it.hasNext()) {
                final RenderInstructions curRi = ri.categories.get(it.next());
                final BlogIndex subBi = new BlogIndex();
                subBi.allCategories = this.allCategories.values();
                subBi.allDateCategories = this.allDateCategories.values();
                subBi.pageTitle = curRi.pageTitle;
                subBi.pageLink = curRi.pageLink;
                subBi.topArticles = new BlogEntryCollection(true);
                bi.categorizedEntries.add(subBi);
                final Iterator<BlogEntry> subIt = curRi.members.descendingSet().iterator();
                while (subIt.hasNext()) {
                    final BlogEntry cur = subIt.next();
                    if (cur.getStatus().equals("Published")) {
                        subBi.topArticles.add(cur);
                    }
                }
            }
            goodToGo = true;
        }

        if (goodToGo) {
            this.renderToDisk(template, finalDestination, bi);
        }

        // Reset of categories
        if (this.allCategories.containsKey(ri.key)) {
            this.allCategories.get(ri.key).active = false;
        }
        if (this.allDateCategories.containsKey(ri.key)) {
            this.allDateCategories.get(ri.key).active = false;
        }

    }

    /**
     * Renders all overview pages: year, month, categories
     */
    private void renderOverViewPages() {

        // Special challenge: every overview page needs
        // to have a previous and a next entry
        final String baseDir = this.config.webBlogLocation;
        RenderInstructions currentRI = null;
        LinkItem nextEntry = null;
        LinkItem previousEntry = null;

        for (final RenderInstructions ri : this.overviewPages.values()) {
            // We render one offset in the loop to be able to fetch the entry
            nextEntry = new LinkItem(ri.pageTitle, baseDir + ri.outFileName, null);
            if (currentRI != null) {
                previousEntry = new LinkItem(currentRI.pageTitle, baseDir + currentRI.outFileName, null);
                currentRI.nextItem = nextEntry;
                ri.previousItem = previousEntry;
                this.renderOverViewPage(currentRI);
            }
            currentRI = ri;
        }
        // The last entry wasn't rendered inside the loop
        this.renderOverViewPage(currentRI);
    }

    private void renderSeries(final BlogIndex bi) throws IOException {

        final String template = this.config.SERIES_TEMPLATE;
        final String finalDestination = this.config.destinationDirectory + this.config.seriesFileName;

        bi.allCategories = this.allCategories.values();
        bi.allDateCategories = this.allDateCategories.values();

        this.renderToDisk(template, finalDestination, bi);
        System.out.println("Rendered series");
    }

    /**
     * Renders one object to disk based on a template and a destination only
     * saves it to disk if it actually had changed
     * 
     * @param template
     *            Name of the template to use
     * @param finalDestination
     *            file location
     * @param payload
     *            object to render
     */
    private void renderToDisk(final String template, final String finalDestination, final Object payload) {
        final MustacheFactory mf = new DefaultMustacheFactory(new File(this.config.templateDirectory));
        final Mustache mustache = mf.compile(template);
        this.renderToDisk(mustache, finalDestination, payload);
    }

    private void renderToDisk(final Mustache mustache, final String finalDestination, final Object payload) {
        final BlogOutput out = new BlogOutput(finalDestination, true);
        final Writer pw = new PrintWriter(out);
        mustache.execute(pw, payload);
        try {
            pw.flush();
            pw.close();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Creates the map for blog redirections
     *
     * @param destination
     * @throws FileNotFoundException
     */
    private void renderURLMapper() throws FileNotFoundException {
        final File outFile = new File(this.config.destinationDirectory + this.config.urlmapFile);
        if (outFile.exists()) {
            outFile.delete();
        }

        final PrintWriter pw = new PrintWriter(outFile);
        pw.write("# Mapping of legacy blog URL into the new format\n");

        for (final Map.Entry<String, String> e : this.mapperOldNewURLs.entrySet()) {
            pw.write(e.getKey().toLowerCase());
            pw.write(" ");
            pw.write(e.getValue());
            pw.write("\n");
        }
        pw.flush();
        pw.close();
        System.out.println("URL mapping written to file " + outFile.getPath());

    }

    private void updateMapper(final Map<String, String> mapper, final EntriesWithFiles outerList) {
        for (final FileEntry oneEntry : outerList.getAttachmentList()) {
            final String oldUrlBeginning = oneEntry.url;
            for (final FileEntry subEntry : oneEntry.subEntries) {
                final String old = (oldUrlBeginning + subEntry.subject).toLowerCase();
                mapper.put(old, subEntry.url);
            }
        }
    }

}
