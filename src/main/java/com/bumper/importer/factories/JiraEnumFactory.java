/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.bumper.importer.factories;

import com.bumper.utils.pojo.IssueType;
import com.bumper.utils.pojo.Severity;
import com.bumper.utils.pojo.Status;

/**
 *
 * @author math
 */
public class JiraEnumFactory {

    public static Status getStatus(int id) {
        switch (id) {
            case 5:
                return Status.RESOLVED;
            default:
                return null;
        }
    }

    public static Severity getSeverity(int id) {
        switch (id) {
            case 1:
                return Severity.TRIVIAL;
            case 2:
                return Severity.MINOR;
            case 3:
                return Severity.MAJOR;
            case 4:
                return Severity.CRITICAL;
            case 5:
                return Severity.BLOCKER;
            default:
                return null;
        }
    }

    public static IssueType getIssueType(int id) {
        switch (id) {
            case 1:
                return IssueType.BUG;
            default:
                return null;
        }
    }
}
