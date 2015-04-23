/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.bumper.importer.factories;

/**
 *
 * @author math
 */
public class JiraStringFactory {

    public static String getStatus(int id) {
        switch (id) {
            case 5:
                return "resolved";
            default:
                return null;
        }
    }

    public static String getSeverity(int id) {
        switch (id) {
            case 1:
                return "trivial";
            case 2:
                return "minor";
            case 3:
                return "major";
            case 4:
                return "critical";
            case 5:
                return "blocker";
            default:
                return null;
        }
    }

    public static String getIssueType(int id) {
        switch (id) {
            case 1:
                return "bug";
            default:
                return null;
        }
    }
}
