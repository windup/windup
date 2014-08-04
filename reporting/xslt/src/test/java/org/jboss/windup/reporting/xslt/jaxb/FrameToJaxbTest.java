package org.jboss.windup.reporting.xslt.jaxb;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.inject.Inject;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.forge.arquillian.AddonDependency;
import org.jboss.forge.arquillian.Dependencies;
import org.jboss.forge.arquillian.archive.ForgeArchive;
import org.jboss.forge.furnace.repositories.AddonDependencyEntry;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.log.jul.config.Logging;
import org.jboss.windup.reporting.xslt.util.XmlUtils;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 *
 * @author Ondrej Zizka, ozizka at redhat.com
 */
@RunWith(Arquillian.class)
public class FrameToJaxbTest
{
    @Deployment
    @Dependencies({
        @AddonDependency(name = "org.jboss.forge.furnace.container:cdi"),
        @AddonDependency(name = "org.jboss.windup.config:windup-config"),
        @AddonDependency(name = "org.jboss.windup.graph:windup-graph"),
        @AddonDependency(name = "org.jboss.windup.reporting:windup-reporting"),
        @AddonDependency(name = "org.jboss.windup.reporting:windup-reporting-xslt"),
    })
    public static ForgeArchive getDeployment()
    {
        ForgeArchive archive = ShrinkWrap.create(ForgeArchive.class)
            .addBeansXML()
            .addClass( XmlUtils.class )
            .addPackage("org.jboss.windup.reporting.xslt.jaxb")
            .addPackage("org.jboss.windup.reporting.xslt.util")
            //.addAsResource(new File("src/test/resources/reports"))
            .addAsAddonDependencies(
                AddonDependencyEntry.create("org.jboss.forge.furnace.container:cdi"),
                AddonDependencyEntry.create("org.jboss.windup.config:windup-config"),
                AddonDependencyEntry.create("org.jboss.windup.graph:windup-graph"),
                AddonDependencyEntry.create("org.jboss.windup.reporting:windup-reporting"),
                AddonDependencyEntry.create("org.jboss.windup.reporting:windup-reporting-xslt")
            );
        return archive;
    }

    @Inject
    private GraphContext context;
    
    @BeforeClass
    public static void init(){
        //Logging.init();
        //System.setProperty("java.util.logging.manager", "org.jboss.logmanager.LogManager");
        //org.jboss.logmanager.Logger.getLogger("org.jboss.forge").setLevel(Level.WARNING);
        //org.jboss.logmanager.Logger.getLogger("org.jboss.weld").setLevel(Level.WARNING);
    }

    @Test
    public void testFrameToJaxb() throws Exception
    {
        GraphRewrite event = new GraphRewrite(this.context);
        
        TestJaxbModel model = this.context.getFramed().addVertex(null, TestJaxbModel.class);
        model.setFoo("Hello");

        TestJaxbAdjacentModel model2 = this.context.getFramed().addVertex(null, TestJaxbAdjacentModel.class);
        model2.setBar("there");
        model.setAdjacent( model2 );
        
        // Marshall
        Map<String, Object> props = new HashMap();
        props.put( Marshaller.JAXB_FORMATTED_OUTPUT, true );
        props.put( Marshaller.JAXB_ENCODING, "UTF-8");
        Class cls = TestJaxbModel.class;
        JAXBContext jaxbCtx = org.eclipse.persistence.jaxb.JAXBContextFactory.createContext(new Class[]{cls}, props);
        Marshaller mar = jaxbCtx.createMarshaller();
        //JDK way: Marshaller mar = JAXBContext.newInstance(cls).createMarshaller();
        
        final StringWriter sw = new StringWriter();
        mar.marshal( model, sw );
        Logging.of(FrameToJaxbTest.class).info("\n" + sw.toString());
    }

}

