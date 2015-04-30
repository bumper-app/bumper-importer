/*
 * Copyright (C) 2015 Mathieu Nayrolles
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.bumper.importer.parsers;

import com.bumper.importer.changesets.AbstractChangesetExtractor;
import com.bumper.utils.pojo.Comment;
import com.bumper.utils.pojo.Dataset;
import com.bumper.utils.pojo.Issue;
import com.bumper.utils.pojo.LifecycleEvent;
import com.bumper.utils.pojo.People;
import com.bumper.utils.pojo.Project;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.stream.XMLStreamReader;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

/**
 *
 * @author math
 */
public class BugzillaParser extends AbstractParser {

    private final List<String> revisions;

    private boolean externalDownload = true;
    private boolean diffExtraction = false;
    private int indexBug = 0;

    public BugzillaParser(String baseUrl, Dataset dataset, AbstractChangesetExtractor changesetExtractor) {
        super(baseUrl, dataset, changesetExtractor);
        this.revisions = new ArrayList<>();

    }

    public BugzillaParser(Dataset dataset, String baseUrl, String integrationTestName, AbstractChangesetExtractor changesetExtractor) {
        super(dataset, baseUrl, integrationTestName, changesetExtractor);
        this.revisions = new ArrayList<>();
    }

    public BugzillaParser(Dataset dataset, String baseUrl, String integrationTestName, AbstractChangesetExtractor changesetExtractor,
            boolean externalDownload, boolean diffExtraction) {
        super(dataset, baseUrl, integrationTestName, changesetExtractor);
        this.revisions = new ArrayList<>();
        this.externalDownload = externalDownload;
        this.changesetExtractor = changesetExtractor;
        this.diffExtraction = diffExtraction;
    }

    @Override
    protected void populateIssueAtOpenningTagTime(XMLStreamReader reader) {
        if (this.currentIssue == null && reader.getLocalName().compareTo("bug") != 0) {
            return;
        }

        switch (reader.getLocalName()) {
            case "bug":
                this.currentIssue = new Issue();
                break;
            case "reporter":
                this.currentIssue.setReporter(new People(reader.getAttributeValue(0)));
                break;
            case "assigned_to":
                this.currentIssue.setAssignee(new People(reader.getAttributeValue(0)));
                break;
            case "who":
                this.currentComment.setCommenter(new People(reader.getAttributeValue(0)));
                break;
        }
    }

    @Override
    protected void populateIssueAtClosingTagTime(String localName) {
        if (this.currentIssue == null) {
            return;
        }

        switch (localName) {
            case "bug_id":
                this.currentIssue.setExteralId(tagContent.replace("null", ""));
                break;
            case "creation_ts":
                this.currentIssue.addLifeCycleEvent(new LifecycleEvent(parseDate(tagContent),
                        null, "created"));
                break;
            case "short_desc":
                this.currentIssue.setShortDescription(tagContent);
                break;
            case "product":
                this.currentIssue.setProject(new Project(tagContent, dataset));
                break;
            case "component":
                Project p = new Project(tagContent, dataset);
                p.setParentProject(this.currentIssue.getProject());
                break;
            case "version":
                this.currentIssue.setTargetVersion(tagContent);
                break;
            case "rep_platform":
                this.currentIssue.setEnvironment(tagContent);
                break;
            case "op_sys":
                this.currentIssue.setEnvironment(
                        this.currentIssue.getEnvironment() + "-" + tagContent);
                break;
            case "bug_status":
                this.currentIssue.setStatus(tagContent.toLowerCase());
                break;
            case "resolution":
                this.currentIssue.setResolution(tagContent.toLowerCase());
                break;
            case "bug_severity":
                this.currentIssue.setSeverity(tagContent.toLowerCase());
                break;
            case "reporter":
                this.currentIssue.getReporter().setPseudo(tagContent);
                break;
            case "assigned_to":
                this.currentIssue.getAssignee().setPseudo(tagContent);
                break;
            case "commentid":
                this.currentComment = new Comment();
                break;
            case "who":
                this.currentComment.getCommenter().setPseudo(tagContent);
                break;
            case "bug_when":
                this.currentComment.setDateComment(parseDate(tagContent));
                break;
            case "thetext":
                this.currentComment.setComment(tagContent);
                if (this.currentIssue.getLongDescription() == null) {
                    this.currentIssue.setLongDescription(tagContent);
                } else {

                    if (tagContent.contains(" has been marked as a duplicate of this bug. ***")) {
                        this.currentIssue.addLifeCycleEvent(this.currentComment.getDateComment(),
                                this.currentComment.getCommenter(), "duplicate", tagContent);
                    }

                    if (this.currentComment.getCommenter().getName().compareTo(this.integrationTestName) == 0) {
                        this.extractSHA1(tagContent.replaceAll("User: ", " User: "));
                    }

                    this.currentIssue.addComment(currentComment);
                }

                break;

            case "bug":

                this.currentIssue.setDataset(dataset);
                this.issues.add(currentIssue);
                this.wrapUp();
                revisions.clear();
                break;
        }
    }

    private Date parseDate(String date) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss Z");
        Date parsedDate = null;
        try {
            parsedDate = formatter.parse(date);

        } catch (ParseException ex) {
            Logger.getLogger(BugzillaParser.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
        return parsedDate;
    }

    @Override
    protected void wrapUp() {

        if (externalDownload) {
            try {
                Document doc = Jsoup
                        .connect(
                                this.baseUrl
                                + "show_activity.cgi?id="
                                + this.currentIssue.getExteralId())
                        .timeout(10 * 1000).get();

                for (Element e : doc.getElementsByTag("tr")) {

                    if (e.children().size() > 2
                            && e.child(2).text().compareTo("Status") == 0) {
                        this.currentIssue.addLifeCycleEvent(parseDate(e.child(1).text()),
                                new People(e.child(0).text()),
                                e.child(4).text().toLowerCase());
                    }

                }

            } catch (IOException e) {
                System.err.println("Error Jsouping: " + this.currentIssue.getExteralId());
            }
        }

        if (diffExtraction) {
            for (String revision : revisions) {

                this.changesetExtractor.extractDiffs(revision + " " + this.currentIssue.getExteralId()
                        + " /home/math/Documents/source/bugs/netbeans/main/ "
                        + "/home/math/Documents/source/netbeans_changeset/", revision + "|" + this.currentIssue.getExteralId()
                        + "|" + ++indexBug + "/" + this.issues.size()
                );

            }

        }

    }

    public void extractSHA1(String text) {
        Pattern p = Pattern.compile("\\b[0-9a-f]{11,40}\\b");
        Matcher m = p.matcher(text);

        while (m.find()) {

            String rev = m.group();
            if (!rev.startsWith("20")) {
                revisions.add(rev);
            }

        }

    }

}
