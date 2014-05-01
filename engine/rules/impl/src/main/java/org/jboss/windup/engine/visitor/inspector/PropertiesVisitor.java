package org.jboss.windup.engine.visitor.inspector;

import java.io.InputStream;
import java.util.Properties;

import javax.inject.Inject;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.jboss.windup.engine.visitor.AbstractGraphVisitor;
import org.jboss.windup.engine.visitor.VisitorPhase;
import org.jboss.windup.graph.WindupContext;
import org.jboss.windup.graph.dao.ArchiveEntryDao;
import org.jboss.windup.graph.dao.PropertiesDao;
import org.jboss.windup.graph.model.meta.PropertiesMetaModel;
import org.jboss.windup.graph.model.resource.ArchiveEntryResourceModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Extracts manifest information to graph. 
 * 
 * @author bradsdavis@gmail.com
 *
 */
public class PropertiesVisitor extends AbstractGraphVisitor {
    private static final Logger LOG = LoggerFactory.getLogger(PropertiesVisitor.class);

    @Inject
    private WindupContext context;
    
    @Inject
    private ArchiveEntryDao archiveEntryDao;
    
    @Inject
    private PropertiesDao propertiesDao;
    
    @Override
    public VisitorPhase getPhase()
    {
        return VisitorPhase.INITIAL_ANALYSIS;
    }
    
    @Override
    public void run() {
        for(ArchiveEntryResourceModel resource : archiveEntryDao.findArchiveEntryWithExtension("properties")) {
            visitArchiveEntry(resource);
        }
        archiveEntryDao.commit();
    }
    
    @Override
    public void visitArchiveEntry(ArchiveEntryResourceModel entry) {
        PropertiesMetaModel properties = propertiesDao.create();
        properties.setResource(entry);
        
        InputStream is = null; 
        try {
            is = entry.asInputStream();
            Properties props = new Properties();
            props.load(is);
            
            for(Object key : props.keySet()) {
                String property = StringUtils.trim(key.toString());
                String propertyValue = StringUtils.trim(props.get(key).toString());
                properties.setProperty(property, propertyValue);
            }
        }
        catch(Exception e) {
            LOG.warn("Exception reading manifest.", e);
        }
        finally {
            IOUtils.closeQuietly(is);
        }
        
        
    }
}
