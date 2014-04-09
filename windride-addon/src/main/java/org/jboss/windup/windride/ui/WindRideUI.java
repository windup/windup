package org.jboss.windup.windride.ui;


import javax.inject.Inject;
import org.jboss.forge.addon.resource.DirectoryResource;
import org.jboss.forge.addon.ui.command.UICommand;
import org.jboss.forge.addon.ui.context.UIBuilder;
import org.jboss.forge.addon.ui.context.UIContext;
import org.jboss.forge.addon.ui.context.UIExecutionContext;
import org.jboss.forge.addon.ui.context.UIValidationContext;
import org.jboss.forge.addon.ui.input.UIInput;
import org.jboss.forge.addon.ui.metadata.UICommandMetadata;
import org.jboss.forge.addon.ui.metadata.WithAttributes;
import org.jboss.forge.addon.ui.result.Result;
import org.jboss.forge.addon.ui.result.Results;
import org.jboss.forge.addon.ui.util.Categories;
import org.jboss.forge.addon.ui.util.Metadata;
import org.jboss.loom.conf.Configuration;
import org.jboss.loom.conf.ConfigurationValidator;
import org.jboss.windup.windride.impl.WindRideService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 *  @author Ondrej Zizka, ozizka at redhat.com
 */
public class WindRideUI implements UICommand {
    private static final Logger log = LoggerFactory.getLogger( WindRideUI.class );


    @Inject private WindRideService windride;

    // Source server - e.g. directory of EAP 5.
    @Inject  @WithAttributes(label = "Source server directory", required = true)
    private UIInput<DirectoryResource> srcServer;

    // Target server, e.g. AS 7. Used to start the server through Arquillian.
    @Inject  @WithAttributes(label = "Target server directory", required = true)
    private UIInput<DirectoryResource> destServer;
    private Configuration conf;


    // Give Forge the command metadata.
    @Override public UICommandMetadata getMetadata( UIContext uic ) {
        return Metadata.forCommand(getClass()).name("Run Windup 1.x")
              .description("Run Windup 1.x Migration Analyzer")
              .category(Categories.create("Platform", "Migration"));
    }


    public boolean isEnabled( UIContext uic ) {
        return true;
    }


    @Override public void initializeUI(final UIBuilder builder) throws Exception {
         builder.add(srcServer).add(destServer);
    }


    public void validate( UIValidationContext uivc ) {
        Configuration conf = new Configuration();
        
        conf.getGlobal().getSourceServerConf().setDir( srcServer.getValue().getContents() );
        conf.getGlobal().getSourceServerConf().setProfileName("all");
        
        conf.getGlobal().getTargetServerConf().setDir( srcServer.getValue().getContents() ); // target/as7copy
        conf.getGlobal().getTargetServerConf().setConfigPath("standalone/configuration/standalone.xml");
        
        ConfigurationValidator.validate( conf );
        this.conf = conf;
    }


    public Result execute( UIExecutionContext uiec ) {
        try {
            windride.doMigration( conf );
            return Results.success();
        } catch( Exception e ) {
            log.error( "Error executing WindRide", e );
            return Results.fail( "Error executing WindRide", e );
        }
    }
    

}// class
