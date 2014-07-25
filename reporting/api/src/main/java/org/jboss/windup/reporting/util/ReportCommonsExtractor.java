package org.jboss.windup.reporting.util;


import javax.inject.Singleton;
import org.jboss.windup.config.Variables;
import org.jboss.windup.graph.model.WindupVertexFrame;
import org.jboss.windup.reporting.meta.ReportableInfo;
import org.jboss.windup.reporting.meta.ann.Description;
import org.jboss.windup.reporting.meta.ann.Title;
import org.jboss.windup.utils.el.IExprLangEvaluator;
import org.jboss.windup.utils.el.JuelCustomResolverEvaluator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Extracts the report commons meta info from the given Model.
 * 
 * @author Ondrej Zizka, ozizka at redhat.com
 */
@Singleton
public class ReportCommonsExtractor {
    private static final Logger log = LoggerFactory.getLogger( ReportCommonsExtractor.class );
    
    private Variables varStack;


    public ReportCommonsExtractor( Variables varStack ) {
        this.varStack = varStack;
    }
    

    /**
     * Extracts the static metadata from the model class - no EL evaluation.
     */
    public static ReportableInfo extract( Class<? extends WindupVertexFrame> modelClass ){
        ReportableInfo ri = new ReportableInfo();
        return extract( modelClass, ri );
    }
        
    private static ReportableInfo extract( Class<? extends WindupVertexFrame> modelClass, ReportableInfo ri ){

        // Title
        if( ri.getTitle() == null ){
            Title annTitle = modelClass.getAnnotation( Title.class );
            if( annTitle != null )
                ri.setTitle( annTitle.value() );
        }
        
        // Description
        if( ri.getDesc() == null ){
            Description annDesc = modelClass.getAnnotation( Description.class );
            if( annDesc != null )
                ri.setDesc( annDesc.value() );
        }
        
        // Icon
        Description annIcon = modelClass.getAnnotation( Description.class );
        if( ri.getIcon() == null ){
            if( annIcon != null )
                ri.setIcon( annIcon.value() );
        }
        
        if( modelClass.getSuperclass().isAssignableFrom( WindupVertexFrame.class ) )
            extract( (Class<? extends WindupVertexFrame>) modelClass.getSuperclass(), ri );
            
        return ri;
    }


    /**
     * Extracts the metadata from the model class and evaluates them as EL,
     * with a resolver looking in VarStack, and "this" pointing to the frame.
     */
    public ReportableInfo extract( WindupVertexFrame frame )
    {
        ReportableInfo ri = this.extract( frame.getClass() );
        return this.extract( frame, ri );
    }
        
    public ReportableInfo extract( WindupVertexFrame frame, ReportableInfo ri ){
        ReportableInfo ri2 = new ReportableInfo();
        
        // Title
        if( ri.getTitle() != null ){
            ri2.setTitle( evaluateEL(ri.getTitle(), frame) );
        }
        
        // Description
        if( ri.getDesc() != null ){
            ri2.setDesc( evaluateEL(ri.getDesc(), frame) );
        }
        
        // Icon
        if( ri.getIcon() != null ){
            ri.setIcon( evaluateEL( ri.getIcon(), frame ) );
        }
        
        return ri;
    }


    /**
     *  Evaluates EL expressions like "Datasource '${this.name} to ${this.server}".
     */
    private String evaluateEL( String el, WindupVertexFrame frame ) {
        ReportVariablesProvider prov = new ReportVariablesProvider(frame, this.varStack);
        return new JuelCustomResolverEvaluator(prov).evaluateEL( el );
    }

    

    /**
     *  Resolves EL variables, which is the first token in ${...} in EL expressions.
     */
    static class ReportVariablesProvider implements IExprLangEvaluator.IVariablesProvider
    {
        WindupVertexFrame frame;
        Variables varStack;
        // TODO: Some MigrationContext?

        public ReportVariablesProvider( WindupVertexFrame frame, Variables varStack ) {
            this.frame = frame;
            this.varStack = varStack;
        }

        public Object getVariable( String name ) {
            if("this".equals(name))
                return this.frame;
            
            // Not sure if we need the var stack in the context of resolving a model. Probably not.
            //Iterable<WindupVertexFrame> frames = this.varStack.findVariable(name);
            //if( frames != null )
            //    return frames;
            
            return null;
        }

    }

}// class
