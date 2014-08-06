package org.jboss.windup.reporting.rules;

import org.jboss.windup.graph.model.WindupVertexFrame;
import org.jboss.windup.reporting.meta.ReportCommons;
import org.jboss.windup.reporting.model.ReportCommonsModelModel;
import org.jboss.windup.reporting.util.ReportCommonsExtractor;

/**
 * Fills the metadata found in the model class.
 * TODO: Use WINDUP-147 GraphService.persist()
 * 
 * @author Ondrej Zizka, ozizka at redhat.com
 */
public class ReportsCommonsModelModelUtil {


    public static void fill( ReportCommonsModelModel reportModel, Class<? extends WindupVertexFrame> modelClass ) {
        ReportCommons ri = ReportCommonsExtractor.extract( modelClass );
        reportModel.setIcon(ri.getIcon());
        reportModel.setTitle( ri.getTitle() );
        reportModel.setDescription( ri.getDesc() );
    }
    

}// class
