package org.jboss.windup.engine.visitor.reporter.html.renderer;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.apache.commons.io.FileUtils;
import org.jboss.windup.engine.visitor.AbstractGraphVisitor;
import org.jboss.windup.engine.visitor.VisitorPhase;
import org.jboss.windup.engine.visitor.reporter.html.model.ApplicationReport;
import org.jboss.windup.engine.visitor.reporter.html.model.ArchiveReport;
import org.jboss.windup.engine.visitor.reporter.html.model.ArchiveReport.ResourceReportRow;
import org.jboss.windup.engine.visitor.reporter.html.model.ApplicationContext;
import org.jboss.windup.engine.visitor.reporter.html.model.Level;
import org.jboss.windup.engine.visitor.reporter.html.model.SimpleName;
import org.jboss.windup.engine.visitor.reporter.html.model.Tag;
import org.jboss.windup.graph.GraphUtil;
import org.jboss.windup.graph.WindupContext;
import org.jboss.windup.graph.dao.ApplicationReferenceDao;
import org.jboss.windup.graph.dao.ArchiveDao;
import org.jboss.windup.graph.dao.EJBConfigurationDao;
import org.jboss.windup.graph.dao.JarManifestDao;
import org.jboss.windup.graph.dao.JavaClassDao;
import org.jboss.windup.graph.dao.MavenFacetDao;
import org.jboss.windup.graph.dao.PropertiesDao;
import org.jboss.windup.graph.dao.WebConfigurationDao;
import org.jboss.windup.graph.dao.XmlResourceDao;
import org.jboss.windup.graph.model.meta.ApplicationReference;
import org.jboss.windup.graph.model.meta.JarManifest;
import org.jboss.windup.graph.model.meta.PropertiesMeta;
import org.jboss.windup.graph.model.meta.xml.EjbConfigurationFacet;
import org.jboss.windup.graph.model.meta.xml.MavenFacet;
import org.jboss.windup.graph.model.meta.xml.WebConfigurationFacet;
import org.jboss.windup.graph.model.resource.ArchiveEntryResource;
import org.jboss.windup.graph.model.resource.ArchiveResource;
import org.jboss.windup.graph.model.resource.JavaClass;
import org.jboss.windup.graph.model.resource.XmlResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import freemarker.template.Configuration;
import freemarker.template.Template;

public class ApplicationReportRenderer extends AbstractGraphVisitor {
    private static final Logger LOG = LoggerFactory.getLogger(ApplicationReportRenderer.class);
    
    @Inject
    private WindupContext context;
    
    @Inject
    private ApplicationReferenceDao appRefDao;

    @Inject
    private JavaClassDao javaDao;
    
    @Inject
    private XmlResourceDao xmlDao;
    
    @Inject
    private JarManifestDao manifestDao;
    
    @Inject
    private ArchiveDao archiveDao;
    
    @Inject
    private NamingUtility namingUtility;
    
    @Inject
    private WebConfigurationDao webConfigurationDao;
    
    @Inject
    private EJBConfigurationDao ejbConfigurationDao;
    
    @Inject
    private MavenFacetDao mavenDao;
    
    @Inject
    private PropertiesDao propertiesDao;
    
    @Inject
    private GraphUtil graphUtil;
    
    private Configuration cfg;
    private File runDirectory;
    private File reportReference;

    @Override
    public VisitorPhase getPhase()
    {
        return VisitorPhase.REPORTING;
    }
    
    @PostConstruct
    private void postConstruct() throws IOException {
        cfg = new Configuration();
        cfg.setTemplateUpdateDelay(500);
        cfg.setClassForTemplateLoading(this.getClass(), "/");
        
        runDirectory = context.getRunDirectory();
        File archiveReportDirectory = new File(runDirectory, "applications");
        File archiveDirectory = new File(archiveReportDirectory, "application");
        FileUtils.forceMkdir(archiveDirectory);
        reportReference = new File(archiveDirectory, "index.html");
    }
    
    
    @Override
    public void run() {
        try {
            ApplicationReport applicationReport = new ApplicationReport();
            for(ApplicationReference app : appRefDao.getAll()) {
                ArchiveResource reference = app.getArchive();
                applicationReport.setApplicationName(reference.getArchiveName());
                recurseArchive(applicationReport, app.getArchive());
            }

            Template template = cfg.getTemplate("/reports/templates/application.ftl");
            
            java.util.Map<String, Object> objects = new HashMap<String, Object>();
            objects.put("application", applicationReport);
            
            template.process(objects, new FileWriter(reportReference));
            LOG.info("Wrote report: "+reportReference.getAbsolutePath());
            
        } catch (Exception e) {
            throw new RuntimeException("Exception writing report.", e);
        }
    }
    
    protected void recurseArchive(ApplicationReport report, ArchiveResource resource) {
        ArchiveReport archiveReport = new ArchiveReport();
        
        String name = null;
        if(resource.getParentResource() instanceof ArchiveEntryResource) {
            ArchiveEntryResource parentEntry = graphUtil.castToType(resource.getParentResource().asVertex(), ArchiveEntryResource.class);
            name = namingUtility.buildFullPath(parentEntry);
        }
        else {
            name = resource.getArchiveName();
        }
        
        archiveReport.setApplicationPath(name);
        
        for(ArchiveEntryResource entry : resource.getChildrenArchiveEntries()) {
            //check to see about facets.
            processEntry(archiveReport, entry);
        }
        
        for(ArchiveResource childResource : resource.getChildrenArchive()) {
            recurseArchive(report, childResource);
        }
        
        report.getArchives().add(archiveReport);
    }
    
    protected void processEntry(ArchiveReport report, ArchiveEntryResource entry) {
        ResourceReportRow reportRow = new ResourceReportRow();
        
        //see if the resource is a java class...
        if(javaDao.isJavaClass(entry)) {
            JavaClass clz = javaDao.getJavaClassFromResource(entry);
            if(!clz.isCustomerPackage()) {
                return;
            }
            reportRow.setResourceName(namingUtility.getReportJavaResource(runDirectory, reportReference, clz));
            reportRow.getTechnologyTags().add(new Tag("Java", Level.SUCCESS));
            
            report.getResources().add(reportRow);
            return;
        }

        //check if it is a XML resource...
        if(xmlDao.isXmlResource(entry)) 
        {
            XmlResource resource = xmlDao.getXmlFromResource(entry);
            reportRow.setResourceName(namingUtility.getReportXmlResource(runDirectory, reportReference, resource));
            reportRow.getTechnologyTags().add(new Tag("XML", Level.SUCCESS));
                    
            //check the XML for some tags...
            if(ejbConfigurationDao.isEJBConfiguration(resource)) {
                EjbConfigurationFacet ejbConfiguration = ejbConfigurationDao.getEjbConfigurationFromResource(resource);
                reportRow.getTechnologyTags().add(new Tag("EJB "+ejbConfiguration.getSpecificationVersion()+" Configuration", Level.SUCCESS));
            }
            
            if(webConfigurationDao.isWebConfiguration(resource)) {
                WebConfigurationFacet webConfiguration = webConfigurationDao.getWebConfigurationFromResource(resource);
                reportRow.getTechnologyTags().add(new Tag("Web "+webConfiguration.getSpecificationVersion()+" Configuration", Level.SUCCESS));
            }
            
            if(mavenDao.isMavenConfiguration(resource)) {
                MavenFacet mavenConfiguration = mavenDao.getMavenConfigurationFromResource(resource);
                reportRow.getTechnologyTags().add(new Tag("Maven "+mavenConfiguration.getSpecificationVersion()+" Configuration", Level.SUCCESS));
            }
                
            report.getResources().add(reportRow);
            return;
        }
        
        if(propertiesDao.isPropertiesResource(entry)) {
            PropertiesMeta meta = propertiesDao.getPropertiesFromResource(entry);
            reportRow.setResourceName(namingUtility.getReportPropertiesResource(runDirectory, reportReference, meta));
            reportRow.getTechnologyTags().add(new Tag("Properties", Level.SUCCESS));
            report.getResources().add(reportRow);
            return;
        }
        
        //check if it is a manifest...
        if(manifestDao.isManifestResource(entry))
        {
            JarManifest resource = manifestDao.getManifestFromResource(entry);
            reportRow.setResourceName(namingUtility.getReportManifestResource(runDirectory, reportReference, resource));
            reportRow.getTechnologyTags().add(new Tag("Manifest", Level.SUCCESS));
                    
            report.getResources().add(reportRow);
            return;
        }
        
        if(archiveDao.isArchiveResource(entry)) {
            //skip.
            return;
        }
        
        
        reportRow.setResourceName(new SimpleName(entry.getArchiveEntry()));
        reportRow.getIssueTags().add(new Tag("Unknown Type", Level.WARNING));
            
        report.getResources().add(reportRow);
        return;
        
    }
    
}
