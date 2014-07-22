package org.jboss.windup.reporting.meta;


import javax.inject.Singleton;
import org.jboss.windup.graph.model.WindupVertexFrame;
import org.jboss.windup.reporting.meta.ann.Description;
import org.jboss.windup.reporting.meta.ann.Title;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 *  @author Ondrej Zizka, ozizka at redhat.com
 */
@Singleton
public class Extractor {
    private static final Logger log = LoggerFactory.getLogger( Extractor.class );

    ReportableInfo extract( WindupVertexFrame frame ){
        ReportableInfo ri = new ReportableInfo();
        //for( Annotation ann : frame.getClass().getAnnotations() )
        
        // Title
        Title annTitle = frame.getClass().getAnnotation( Title.class );
        if( annTitle != null ){
            String titlePattern = annTitle.value();
            String title = evaluateEL(titlePattern, frame);
            ri.setTitle( title );
        }
        
        // Description
        Description annDesc = frame.getClass().getAnnotation( Description.class );
        if( annDesc != null ){
            String descPattern = annDesc.value();
            String desc = evaluateEL(descPattern, frame);
            ri.setTitle( desc );
        }
        
        // Icon
        Description annIcon = frame.getClass().getAnnotation( Description.class );
        if( annIcon != null ){
            String iconPat = annIcon.value();
            String icon = evaluateEL( iconPat, frame );
            ri.setIcon( icon );
        }
        
        return ri;
    }


    private String evaluateEL( String title, WindupVertexFrame frame ) {
        
        return "";
    }

}// class
