package org.apache.wicket.request.handler.resource;

import org.apache.wicket.request.*;
import java.util.*;

public abstract class ResourceLogData implements ILogData{
    private static final long serialVersionUID=1L;
    private final String name;
    private final String locale;
    private final String style;
    private final String variation;
    public ResourceLogData(final String name,final Locale locale,final String style,final String variation){
        super();
        this.name=name;
        this.locale=((locale==null)?null:locale.toString());
        this.style=style;
        this.variation=variation;
    }
    public final String getName(){
        return this.name;
    }
    public final String getLocale(){
        return this.locale;
    }
    public final String getStyle(){
        return this.style;
    }
    public final String getVariation(){
        return this.variation;
    }
    protected void fillToString(final StringBuilder sb){
        sb.append("name=");
        sb.append(this.getName());
        sb.append(",locale=");
        sb.append(this.getLocale());
        sb.append(",style=");
        sb.append(this.getStyle());
        sb.append(",variation=");
        sb.append(this.getVariation());
    }
}
