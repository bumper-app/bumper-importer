/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.bumper.importer;

import com.bumper.utils.pojo.Comment;
import com.bumper.utils.pojo.Issue;
import com.bumper.utils.pojo.LifecycleEventType;
import com.bumper.utils.pojo.People;
import com.bumper.utils.pojo.Project;
import com.bumper.utils.pojo.Resolution;
import java.util.Date;
import javax.xml.stream.XMLStreamReader;

/**
 *
 * @author math
 */
public class JiraParser extends AbstractParser {

    @Override
    protected void populateIssueAtOpenningTagTime(XMLStreamReader reader) {

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
            case "key":
                currentIssue.setExteralId(reader.getAttributeValue(0));
                break;
            case "comment":
                this.currentComment = new Comment(
                        new People(reader.getAttributeValue(1)),
                        new Date(reader.getAttributeValue(2)));
                break;
        }

    }

    @Override
    protected void populateIssueAtClosingTagTime(String localName) {

        switch (localName) {

            case "title":
                this.currentIssue.setShortDescription(tagContent);
                break;
            case "description":
                this.currentIssue.setShortDescription(tagContent);
                break;
            case "created":
                this.currentIssue.addLifeCycleEvent(
                        new Date(tagContent), this.assignee,
                        LifecycleEventType.CREATED);
                break;
            case "resolution":
                this.currentIssue.setResolution(Resolution.FIXED);
                break;
            case "version":
                this.currentIssue.setTargetVersion(tagContent);
                break;
            case "resolved":
                this.currentIssue.addLifeCycleEvent(
                        new Date(tagContent), this.assignee,
                        LifecycleEventType.RESOLVED);
                this.currentIssue.addLifeCycleEvent(
                        new Date(tagContent), this.assignee,
                        LifecycleEventType.CLOSED);
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
            case "item":
                this.wrapUp();
                this.issues.add(this.currentIssue);
                break;
        }
    }

    @Override
    protected void wrapUp() {

    }

}
