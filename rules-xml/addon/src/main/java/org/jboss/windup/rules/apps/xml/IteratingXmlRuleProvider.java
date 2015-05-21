package org.jboss.windup.rules.apps.xml;

import org.jboss.windup.config.AbstractRuleProvider;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.RuleProvider;
import org.jboss.windup.config.metadata.RuleProviderMetadata;
import org.jboss.windup.config.ruleprovider.IteratingRuleProvider;
import org.jboss.windup.graph.model.WindupVertexFrame;
import org.jboss.windup.rules.apps.xml.model.XmlFileModel;
import org.ocpsoft.rewrite.context.EvaluationContext;
import org.w3c.dom.Document;

import com.esotericsoftware.minlog.Log;

/**
 * This provides a simplified way to extend {@link AbstractRuleProvider} for cases where the rule simply needs to
 * provide some query, and wants to execute a function over each valid xml row.
 * 
 * @author <a href="mailto:bradsdavis@gmail.com">Brad Davis</a>
 */
public abstract class IteratingXmlRuleProvider<PAYLOADTYPE extends XmlFileModel> extends IteratingRuleProvider<PAYLOADTYPE>
{

    public IteratingXmlRuleProvider()
    {
        super();
    }

    public IteratingXmlRuleProvider(RuleProviderMetadata metadata)
    {
        super(metadata);
    }

    public IteratingXmlRuleProvider(Class<? extends RuleProvider> implementationType, String id)
    {
        super(implementationType, id);
    }

    /**
     * Perform this function for each {@link WindupVertexFrame} returned by the "when" clause.
     */
    public void perform(GraphRewrite event, EvaluationContext context, PAYLOADTYPE payload) {
        Document doc = payload.asDocument();
        if(doc == null) {
            Log.warn("Document is null.");
            return;
        }
        
        perform(event, context, payload, doc);
    }
    
    public abstract void perform(GraphRewrite event, EvaluationContext context, PAYLOADTYPE payload, Document doc);
}
