package org.jboss.windup.config.selectables;

import org.jboss.windup.graph.model.WindupVertexFrame;

/**
 *
 *  @author Ondrej Zizka, ozizka at redhat.com
 */
public interface ICurrentItems {


    /**
     *  Returns the "cursor" for given var name.
     *  The variables typically keep an iterable; the "current payload" concept
     *  holds the reference to the currently iterated vertex.
     */
    @SuppressWarnings( value = "unchecked" )
    <T extends WindupVertexFrame> T getCurrentPayload( Class<T> type, String name );


    /**
     * Sets the "cursor" for given variable to given framed vertex; no validity checks!
     */
    void setCurrentPayload( String name, WindupVertexFrame element );
    
}
