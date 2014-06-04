package org.apache.wicket.markup.html.tree;

import org.apache.wicket.util.lang.*;

public final class LinkType extends EnumeratedType{
    public static final LinkType AJAX;
    public static final LinkType AJAX_FALLBACK;
    public static final LinkType REGULAR;
    private static final long serialVersionUID=1L;
    public LinkType(final String name){
        super(name);
    }
    static{
        AJAX=new LinkType("AJAX");
        AJAX_FALLBACK=new LinkType("AJAX_FALLBACK");
        REGULAR=new LinkType("REGULAR");
    }
}
