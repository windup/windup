package org.jboss.windup.reporting.meta;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.ocpsoft.rewrite.config.Rule;


/**
 *  Common reportable information about an item to appear in the report.
 * 
 *  @author Ondrej Zizka, ozizka at redhat.com
 */
public class ReportCommons {

    private String title;
    private String desc;
    private String icon;
    
    private final List<Rule> foundBy = new LinkedList<>();
    private final List<Solution> solutions = new LinkedList<>();
    private final List<Reference> references = new LinkedList<>();
    private final Map<String, ReportCommons> traits = new HashMap();


    public ReportCommons() {
    }

    public ReportCommons( String title ) {
        this.title = title;
    }

   
    /**
     * One-line title of the reportable item.
     */
    public String getTitle() {
        return title;
    }


    /**
     * One-line title of the reportable item.
     */
    public void setTitle( String title ) {
        this.title = title;
    }


    /**
     * One-paragraph description of the reportable item.
     */
    public String getDesc() {
        return desc;
    }


    /**
     * One-paragraph description of the reportable item.
     */
    public void setDesc( String desc ) {
        this.desc = desc;
    }


    /**
     * Path to the icon (on classpath).
     */
    public String getIcon() {
        return icon;
    }


    /**
     * Path to the icon (on classpath).
     */
    public void setIcon( String icon ) {
        this.icon = icon;
    }


    /**
     * A list of rules which discovered this issue.
     */
    public List<Rule> getFoundBy() {
        return foundBy;
    }


    /**
     * A list of rules which discovered this issue.
     */
    public void addFoundBy( Rule foundBy ) {
        this.foundBy.add( foundBy );
    }


    public List<Solution> getSolutions() {
        return solutions;
    }


    public void addSolution( Solution solution ) {
        this.solutions.add( solution );
    }


    public List<Reference> getReferences() {
        return references;
    }


    public void addReference( Reference reference ) {
        this.references.add(reference);
    }

    
    // Traits
    
    /**
     * Returns the traits as an unmodifiable map.
     */
    public Map<String, ReportCommons> getTraits() {
        return Collections.unmodifiableMap( this.traits );
    }
    
    public void putTrait( String propName, ReportCommons rc ) {
        this.traits.put( propName, rc );
    }

    
}// class
