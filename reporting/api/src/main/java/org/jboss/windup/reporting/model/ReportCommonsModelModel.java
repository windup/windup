package org.jboss.windup.reporting.model;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.frames.Property;
import java.util.Map;
import org.jboss.windup.config.model.ModelModel;
import org.jboss.windup.reporting.meta.ann.ReportElement;


/**
 * Model for information extracted from the frames models.
 * 
 * @author Ondrej Zizka, ozizka at redhat.com
 */
public interface ReportCommonsModelModel extends ModelModel
{
    
    public static final String TITLE = "title";
    public static final String DESC = "desc";
    public static final String ICON = "icon";
    
    @Property("elementType") void setElement( ReportElement.Type type );
    @Property("elementType") ReportElement.Type getElement();
    
    @Property("cssClass") String getCssClass();
    @Property("cssClass") void setCssClass( String cls );
    
    
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
    
    //@InProperties(label = "traits", direction = Direction.OUT)
    //@XmlJavaTypeAdapter(value = MapPropertiesAdapter.class)
    //Map<String,String> getTraits();
}
