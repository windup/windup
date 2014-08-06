package org.jboss.windup.reporting.meta.ann;

/**
 * Enum of supported renderers.
 * 
 * @author Ondrej Zizka, ozizka at redhat.com
 */
public interface Renderer {

    enum Type {
        GENERIC, XSLT, FREEMARKER, MARKDOWN
    }
    
}
