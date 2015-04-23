/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.bumper.importer;

import com.bumper.importer.parsers.AbstractParser;
import com.bumper.importer.parsers.BugzillaParser;
import com.bumper.utils.pojo.Dataset;
import java.io.FileNotFoundException;
import javax.xml.stream.XMLStreamException;

/**
 *
 * @author math
 */
public class Main {

    public static void main(String[] args) throws XMLStreamException, FileNotFoundException {

        AbstractParser parser;
//        parser = new JiraParser("https://issues.apache.org/jira/",
//                new Dataset("Apache"));
//
//        parser.parseFile("/home/math/Documents/Bug_Taxonomy/data/jira/apachejira.xml");

        parser = new BugzillaParser(new Dataset("Netbeans"), "https://netbeans.org/bugzilla/", "Quality Engineering");

        parser.parseFile("/home/math/Documents/Bug_Taxonomy/data/netbeans.xml");
    }

}
