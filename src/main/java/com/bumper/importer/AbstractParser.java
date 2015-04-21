/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.bumper.importer;

import com.bumper.utils.pojo.Comment;
import com.bumper.utils.pojo.Dataset;
import com.bumper.utils.pojo.Issue;
import com.bumper.utils.pojo.People;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

/**
 *
 * @author math
 */
public abstract class AbstractParser {

    protected Issue currentIssue;
    protected List<Issue> issues = new ArrayList<>();
    protected String tagContent;
    protected People assignee;
    protected Dataset dataset;
    protected Comment currentComment;
    protected String baseUrl;

    public AbstractParser(String baseUrl, Dataset dataset) {
        this.baseUrl = baseUrl;
        this.dataset = dataset;
    }

    public void parseFile(String filePath) throws XMLStreamException, FileNotFoundException {

        XMLInputFactory factory = XMLInputFactory.newInstance();

        XMLStreamReader reader = factory
                .createXMLStreamReader(new FileReader(filePath));

        while (reader.hasNext()) {

            int event = reader.next();

            switch (event) {

                case XMLStreamConstants.START_ELEMENT:
                    populateIssueAtOpenningTagTime(reader);
                    break;

                case XMLStreamConstants.CHARACTERS:
                    tagContent += reader.getText().trim();
                    break;

                case XMLStreamConstants.END_ELEMENT:
                    populateIssueAtClosingTagTime(reader.getLocalName());
                    tagContent = "";
                    break;

            }
        }
    }

    protected abstract void populateIssueAtOpenningTagTime(XMLStreamReader reader);

    protected abstract void populateIssueAtClosingTagTime(String localName);

    protected abstract void wrapUp();

}
