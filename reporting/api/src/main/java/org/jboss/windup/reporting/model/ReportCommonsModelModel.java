package org.jboss.windup.reporting.model;

import com.tinkerpop.frames.Property;
import org.jboss.windup.config.model.ModelModel;


/**
 *
 *  @author Ondrej Zizka, ozizka at redhat.com
 */
public interface ReportCommonsModelModel extends ModelModel
{
    
    public static final String TITLE = "title";
    public static final String DESC = "desc";
    public static final String ICON = "icon";
    
    
    @Property(TITLE)
    public void setTitle(String title);

    @Property(TITLE)
    public String getTitle();
    
    
    @Property(DESC)
    public void setDescription(String title);

    @Property(DESC)
    public String getDescription();

    
    @Property(ICON)
    public void setIcon(String title);

    @Property(ICON)
    public String getIcon();
}
