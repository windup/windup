package org.jboss.windup.reporting.meta;

/**
 *  A solution - a short description of the issue found.
 *  The terminology comes from Red Hat Customer Portal.
 *  
 *  @author Ondrej Zizka, ozizka at redhat.com
 */
public class Solution {

    private String text;

    public String getText() {
        return text;
    }

    public void setText( String text ) {
        this.text = text;
    }
    
}// class
