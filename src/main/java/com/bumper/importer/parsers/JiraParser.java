/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.bumper.importer.parsers;

import com.bumper.importer.factories.JiraStringFactory;
import com.bumper.utils.pojo.Comment;
import com.bumper.utils.pojo.Dataset;
import com.bumper.utils.pojo.Issue;
import com.bumper.utils.pojo.LifecycleEvent;
import com.bumper.utils.pojo.People;
import com.bumper.utils.pojo.Project;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.stream.XMLStreamReader;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

/**
 *
 * @author math
 */
public class JiraParser extends AbstractParser {

    public JiraParser(String baseUrl, Dataset dataset) {
        super(baseUrl, dataset);
    }

    @Override
    protected void populateIssueAtOpenningTagTime(XMLStreamReader reader) {

        if (this.currentIssue == null && reader.getLocalName().compareTo("item") != 0) {
            return;
        }

        switch (reader.getLocalName()) {
            case "item":
                currentIssue = new Issue();
                break;
            case "reporter":
                currentIssue.setReporter(
                        new People(reader.getAttributeValue(0)));
                break;
            case "assignee":
                currentIssue.setAssignee(
                        new People(reader.getAttributeValue(0)));
                break;
            case "comment":
                this.currentComment = new Comment(
                        new People(reader.getAttributeValue(1)),
                        new Date(reader.getAttributeValue(2)));
                break;
            case "type":
                this.currentIssue.setIssueType(
                        JiraStringFactory.getIssueType(new Integer(reader.getAttributeValue(0))));
                break;
            case "status":
                this.currentIssue.setStatus(
                        JiraStringFactory.getStatus(new Integer(reader.getAttributeValue(0))));
                break;
            case "priority":
                this.currentIssue.setSeverity(
                        JiraStringFactory.getSeverity(new Integer(reader.getAttributeValue(0))));
                break;
        }

    }

    @Override
    protected void populateIssueAtClosingTagTime(String localName) {

        if (this.currentIssue == null) {
            return;
        }

        switch (localName) {

            case "title":
                this.currentIssue.setShortDescription(tagContent);
                break;
            case "description":
                this.currentIssue.setLongDescription(tagContent);
                break;
            case "created":
                this.currentIssue.addLifeCycleEvent(
                        new Date(tagContent), this.assignee,
                        "created");
                break;
            case "resolution":
                this.currentIssue.setResolution("fixed");
                break;
            case "version":
                this.currentIssue.setTargetVersion(tagContent);
                break;
            case "resolved":
                this.currentIssue.addLifeCycleEvent(
                        new Date(tagContent), this.assignee,
                        "resolved");
                break;
            case "project":
                this.currentIssue.setProject(
                        new Project(tagContent, dataset));
                break;
            case "reporter":
                currentIssue.getReporter().setPseudo(tagContent);
                break;
            case "assignee":
                currentIssue.getAssignee().setPseudo(tagContent);
                break;
            case "component":
                Project p = new Project(tagContent, dataset,
                        this.currentIssue.getProject());
                break;
            case "comment":
                this.currentComment.setComment(tagContent);
                this.currentIssue.addComment(currentComment);
                break;
            case "key":
                currentIssue.setExteralId(tagContent);
                break;
            case "environment":
                currentIssue.setEnvironment(tagContent);
                break;
            case "item":
                this.wrapUp();
                this.currentIssue.setDataset(dataset);
                this.issues.add(this.currentIssue);
                System.err.println(this.currentIssue);
                System.exit(0);
                break;
        }
    }

    @Override
    protected void wrapUp() {

        try {
            Document doc = Jsoup
                    .connect(
                            this.baseUrl
                            + "browse/"
                            + this.currentIssue.getExteralId()
                            + "?page=com.atlassian.jira.plugin.system.issuetabpanels:all-tabpanel")
                    .timeout(10 * 1000).get();

            for (Element element : doc.getElementsByClass("issue-data-block")) {

                LifecycleEvent lifecycleEvent = new LifecycleEvent();
                lifecycleEvent.setPeople(new People(element.getElementsByClass("user-hover").get(0).text()));

                SimpleDateFormat formatter = new SimpleDateFormat("dd/MMM/yy hh:mm");
                Date parsedDate = formatter.parse(element.getElementsByClass("date").get(0).attr("title"));

                lifecycleEvent.setDate(parsedDate);

                for (Element subElement : element
                        .getElementsByClass("activity-new-val")) {

                    if (subElement.text().trim().contains("Reopened")) {
                        lifecycleEvent.setEventType("reopened");
                        this.currentIssue.addLifeCycleEvent(lifecycleEvent);
                    }
                }
            }

        } catch (IOException e) {
            System.err.println("Error Jsouping: " + this.currentIssue.getExteralId());
        } catch (ParseException ex) {
            Logger.getLogger(JiraParser.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

}
