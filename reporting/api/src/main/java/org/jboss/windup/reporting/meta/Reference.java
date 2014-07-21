package org.jboss.windup.reporting.meta;

/**
 *  A reference to documentation regarding an issue found.
 *  May link to official documentation, forum post, article, etc.
 * 
 *  @author Ondrej Zizka, ozizka at redhat.com
 */
public class Reference {

    private String title;
    private String url;


    public String getTitle() {
        return title;
    }


    public void setTitle( String title ) {
        this.title = title;
    }


    public String getUrl() {
        return url;
    }


    public void setUrl( String url ) {
        this.url = url;
    }
    
    

}// class
