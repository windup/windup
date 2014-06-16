package org.jboss.windup.rules.apps.java.basic;

import org.jboss.windup.graph.renderer.Label;
import com.tinkerpop.frames.Property;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;
import org.jboss.windup.graph.model.WindupVertexFrame;


@TypeValue("Regex")
public interface RegexModel extends WindupVertexFrame
{
    public static final String FOR_LANG = "forLang";

    @Property("regex")
    public String getRegex();

    @Property("regex")
    public void setRegex(String regex);

    
    @Property(FOR_LANG)
    public String getForLang();

    @Property(FOR_LANG)
    public void setForLang(String regex);


    @Label
    @Property("desc")
    public String getDescription();

    @Property("desc")
    public void setDescription(String desc);

    
    //@Adjacency(label = "fromRule", direction = Direction.OUT)
    //public RuleModel getFromRule();
    
    //@Adjacency(label = "fromRule", direction = Direction.OUT)
    //public void setFromRule();
}
