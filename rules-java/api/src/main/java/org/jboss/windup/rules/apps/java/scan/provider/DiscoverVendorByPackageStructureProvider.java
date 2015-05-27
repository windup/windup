package org.jboss.windup.rules.apps.java.scan.provider;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.lang.StringUtils;
import org.jboss.windup.config.AbstractRuleProvider;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.metadata.MetadataBuilder;
import org.jboss.windup.config.operation.iteration.AbstractIterationOperation;
import org.jboss.windup.config.phase.ClassifyFileTypesPhase;
import org.jboss.windup.config.query.Query;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.ArchiveModel;
import org.jboss.windup.graph.service.VendorService;
import org.jboss.windup.util.Logging;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;

/**
 * Look at the package structure of the archive. If the packages all start with known vendor's package structure, then mark as potential vendor.
 * 
 * @author <a href="mailto:bradsdavis@gmail.com">Brad Davis</a>
 */
public class DiscoverVendorByPackageStructureProvider extends AbstractRuleProvider
{
    private static Logger LOG = Logging.get(DiscoverVendorByPackageStructureProvider.class);

    private static final Map<String, String> vendorMap;
    private int maxPackages;
    
    public DiscoverVendorByPackageStructureProvider()
    {
        super(MetadataBuilder.forProvider(DiscoverVendorByPackageStructureProvider.class)
                    .setPhase(ClassifyFileTypesPhase.class)
                    .addExecuteAfter(IndexJavaClassFilesRuleProvider.class));
        
        
        this.maxPackages = 0;
        for(String key : vendorMap.keySet()) {
            //find the number of spaces...
            int temp = StringUtils.countMatches(key, ".") + 1;
            if(temp > maxPackages) {
                this.maxPackages = temp;
            }
        }
    }

    // @formatter:off
    @Override
    public Configuration getConfiguration(GraphContext context)
    {
        final VendorService vendorService = new VendorService(context);
        return ConfigurationBuilder.begin()
                    .addRule()
                    .when(Query.fromType(ArchiveModel.class))
                    .perform(
                        new AbstractIterationOperation<ArchiveModel>() {
                            public void perform(GraphRewrite event, EvaluationContext context, ArchiveModel payload) {
                                
                                LOG.info("Processing Archive: "+payload.getArchiveName());
                                Set<String> packageSet = new HashSet<>();
                                Set<String> possibleVendor = new HashSet<>();
                                
                                File archiveFile = payload.asFile();
                                try(ZipFile zipFile = new ZipFile(archiveFile)) {
                                    Enumeration<? extends ZipEntry> entries = zipFile.entries();
                                    
                                    while(entries.hasMoreElements()) {
                                        ZipEntry entry = entries.nextElement();
                                        
                                        if(!entry.isDirectory()) {
                                            if(StringUtils.endsWith(entry.getName(), ".class")) {
                                                String pkg = findPackage(payload, entry.getName());
                                                packageSet.add(pkg);
                                            }
                                        }
                                    }
                                    
                                    for(String pkg : packageSet) {
                                        String vendor = findVendor(payload, pkg);
                                        if(vendor != null) {
                                            possibleVendor.add(vendor);
                                        }
                                    }
                                }
                                catch(IOException e) {
                                    LOG.warning("Error loading archive: "+payload.getFileName());
                                }
                                
                                if(possibleVendor.size() == 0) {
                                    LOG.info(" -- Archive: "+payload.getArchiveName()+" vendor unknown.");
                                    vendorService.attachVendor(payload, "Unknown");
                                }
                                else if(possibleVendor.size() > 1) {
                                    LOG.warning(" -- Archive: "+payload.getArchiveName()+" has more than one vendor: ");
                                    for(String vendor : possibleVendor) {
                                        //attach the vendor to the archive.
                                        vendorService.attachVendor(payload, vendor);
                                        LOG.warning("   -- "+vendor);
                                    }
                                }
                                else {
                                    vendorService.attachVendor(payload, possibleVendor.iterator().next());
                                    LOG.info(" -- Archive: "+payload.getFileName()+" has vendor: "+possibleVendor.iterator().next());
                                }
                                
                            };
                        }
                    );
    }
    // @formatter:on 

    private String findPackage(final ArchiveModel payload, String entryName) {
        String packageName = StringUtils.removeEnd(entryName, ".class");
        packageName = StringUtils.replace(packageName, "/", ".");
        packageName = StringUtils.substringBeforeLast(packageName, ".");
        
        if(StringUtils.endsWith(payload.getArchiveName(), ".war")) {
            packageName = StringUtils.substringAfterLast(packageName, "WEB-INF.classes.");
        }
        else if(StringUtils.endsWith(payload.getArchiveName(), ".par")) {
            packageName = StringUtils.removeStart(packageName, "classes.");
        }
        
        int position = StringUtils.ordinalIndexOf(packageName, ".", maxPackages);
        if(position > 0) {
            packageName = StringUtils.substring(packageName, 0, position);
        }
        
        return packageName;
    }
    
    private String findVendor(final ArchiveModel payload, String packageName) {
        for (String key : vendorMap.keySet())
        {
            String packageTemp = packageName+".";
            String keyTemp = key +".";
            if (StringUtils.startsWith(packageTemp, keyTemp))
            {
                return vendorMap.get(key);
            }
        }
        
        return null;
    }
    
    static
    {
        vendorMap = new HashMap<String, String>();
        vendorMap.put("bea", "Weblogic");
        vendorMap.put("com.bea", "Weblogic");
        vendorMap.put("com.weblogic", "Weblogic");
        vendorMap.put("weblogic", "Weblogic");
        vendorMap.put("oracle", "Weblogic");
        vendorMap.put("ilog", "IBM");
        vendorMap.put("ibm", "IBM");
        vendorMap.put("com.ibm", "IBM");
        vendorMap.put("websphere", "IBM");
        vendorMap.put("com.iona", "Iona");
        vendorMap.put("com.lombardi", "Lombardi");
        vendorMap.put("com.sybase", "Sybase");
        vendorMap.put("sybase", "Sybase");
        vendorMap.put("com.tangosol", "Tangosol");
        vendorMap.put("com.tibco", "Tibco");
        vendorMap.put("commonj", "Commonj");
        vendorMap.put("java.", "Sun");
        vendorMap.put("com.sun", "Sun");
        vendorMap.put("javax", "Sun");
        vendorMap.put("mx4j", "MX4J");
        vendorMap.put("net.sf.hibernate", "JBoss");
        vendorMap.put("org.jboss", "JBoss");
        vendorMap.put("org.ajax4jsf", "JBoss");
        vendorMap.put("org.hibernate", "JBoss");
        vendorMap.put("org.jgroups", "JBoss");
        vendorMap.put("org.modeshape", "JBoss");
        vendorMap.put("org.drools", "JBoss");
        vendorMap.put("org.jbpm", "JBoss");
        vendorMap.put("org.hornetq", "JBoss");
        vendorMap.put("org.quartz", "Quartz");
        vendorMap.put("com.opensymphony", "Open Symphony");
        vendorMap.put("org.apache", "Apache");
        vendorMap.put("org.mule", "Mule ESB");
        vendorMap.put("org.springframework", "Spring");
        vendorMap.put("org.postgresql", "Postgres");
        vendorMap.put("com.mysql", "MySQL");
        vendorMap.put("org.hsqldb", "HypersonicDB");
        vendorMap.put("microsoft", "Microsoft");
        vendorMap.put("org.xml.sax", "Oagis");
        vendorMap.put("com.thoughtworks", "Thoughtworks");
        vendorMap.put("org.w3c", "W3C");
        vendorMap.put("org.osoa", "OSOA");
        vendorMap.put("org.mvel", "MVEL");
        vendorMap.put("org.codehaus", "Codehaus");
        vendorMap.put("net.sf", "Sourceforge");
        vendorMap.put("net.sourceforge", "Sourceforge");
        vendorMap.put("org.dom4j", "DOM4J");
        vendorMap.put("com.adobe", "Adobe");
        vendorMap.put("coldfusion", "Adobe");
        vendorMap.put("flex.graphics", "Adobe");
        vendorMap.put("flex.management", "Adobe");
        vendorMap.put("flex.messaging", "Adobe");
        vendorMap.put("com.google", "Google");
        vendorMap.put("org.eclipse", "Eclipse Foundation");
        vendorMap.put("org.mozilla", "Mozilla Foundation");
        vendorMap.put("org.bouncycastle", "Bouncy Castle");
        vendorMap.put("com.wso2", "WSO2");
        vendorMap.put("com.sap", "SAP");
        vendorMap.put("com.businessobjects", "SAP");
        vendorMap.put("com.mchange", "Machinery For Change");
        vendorMap.put("com.atlassian", "Atlassian");
        vendorMap.put("atlassian", "Atlassian");
        vendorMap.put("net.sf", "SourceForge");
        vendorMap.put("org.scannotation", "Scannotation");
        vendorMap.put("org.slf4j", "SLF4J");
        vendorMap.put("org.joda", "Joda Time");
        vendorMap.put("freemarker", "Freemarker");
        vendorMap.put("org.milyn", "Milyn");
        vendorMap.put("junit", "JUnit");
        vendorMap.put("org.junit", "JUnit");
        vendorMap.put("org.jdom", "JDOM");
        vendorMap.put("org.jfree", "JFree");
        vendorMap.put("com.google", "Google");
        vendorMap.put("com.yahoo", "Yahoo");
        vendorMap.put("org.antlr", "Antlr");
        vendorMap.put("org.easymock", "EasyMock");
    }
}
