package org.jboss.windup.boot;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.jboss.forge.furnace.Furnace;
import org.jboss.forge.furnace.addons.AddonRegistry;
import org.jboss.forge.furnace.repositories.AddonRepositoryMode;
import org.jboss.forge.furnace.se.FurnaceFactory;
import org.jboss.windup.configreal.ConfigValidator;
import org.jboss.windup.configreal.CoreConfig;
import org.jboss.windup.configreal.RulesetSpecificProperty;
import org.jboss.windup.configreal.Utils;
import org.jboss.windup.configreal.WindupConfig;
import org.jboss.windup.engine.WindupProcessor;
import org.jboss.windup.util.exception.WindupException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * CLI for Windup - reads the CLI input, parses the config, starts Furnace and invokes the WindupProcessor service.
 * 
 * @author Ondrej Zizka, ozizka at redhat.com
 */
public class WindupApp {
    private static final Logger log = LoggerFactory.getLogger( WindupApp.class );

    public static void main(String[] args)
    {
        // Parse arguments.
        WindupConfig wc = parseArguments( args );
        if( null == wc )
            System.exit(1);
        
        // Validate config.
        List<String> problems = ConfigValidator.validate(wc);
        if( !problems.isEmpty() ){
            for( String problem : problems )
                log.error(problem);
            System.exit(1);
        }
        
        // Run Windup.
        run(wc);
    }


    /**
     *  Parses app's arguments.
     *  @returns  Configuration initialized according to args.
     */
    static WindupConfig parseArguments(String[] args) {
    
        // Global config
        CoreConfig coreConf = new CoreConfig();
        
        // Module-specific options.
        List<RulesetSpecificProperty> rulesetConfigs = new LinkedList<>();
        
        
        // For each argument...
        for (String arg : args) {
            arg = StringUtils.removeStart( arg, "--" );
            
            if( arg.equals("help") ){
                Utils.writeHelp();
                return null;
            }
            if( arg.startsWith("srcServer.dir=") ) {
                coreConf.getSrcServerConfig().setPath(StringUtils.substringAfter(arg, "="));
                continue;
            }

            if( arg.startsWith("destServer.dir=") ) {
                coreConf.getDestServerConfig().setPath(StringUtils.substringAfter(arg, "="));
                continue;
            }


            if( arg.startsWith("app.dir=") || arg.startsWith("app=") ) {
                coreConf.addDeploymentPath( StringUtils.substringAfter(arg, "="));
                continue;
            }

            if( arg.startsWith("valid.skip") ) {
                coreConf.setSkipValidation(true);
                continue;
            }

            if( arg.equals("dry") || arg.equals("dryRun") || arg.equals("dry-run") ) {
                coreConf.setDryRun(true);
                continue;
            }
            
            if( arg.equals("test") || arg.equals("testRun") || arg.equals("test-run") ) {
                coreConf.setTestRun(true);
                continue;
            }
            
            if( arg.startsWith("report.dir=") || arg.startsWith("output.dir=") ) {
                coreConf.setReportDir( StringUtils.substringAfter(arg, "="));
                continue;
            }

            if( arg.startsWith("rulesets.dir=") || arg.startsWith("rules.dir=") ) {
                coreConf.setRulesetsDir(StringUtils.substringAfter(arg, "="));
                continue;
            }

            // User variables available in EL and Groovy in external migrators.
            if (arg.startsWith("userVar.")) {
                
                // --userVar.<property.name>=<value>
                String rest = StringUtils.substringAfter(arg, ".");
                String name = StringUtils.substringBefore(rest, "=");
                String value = StringUtils.substringAfter(rest, "=");
                
                coreConf.getUserVars().put( name, value );
            }
            

            // Module-specific configurations.
            // TODO: Process by calling IMigrator instances' callback.
            if (arg.startsWith("conf.")) {
                
                // --conf.<ruleset>.<property.name>[=<value>]
                String conf = StringUtils.substringAfter(arg, ".");
                String ruleset = StringUtils.substringBefore(conf, ".");
                String propName = StringUtils.substringAfter(conf, ".");
                int pos = propName.indexOf('=');
                String value = null;
                if( pos == -1 ){
                    value = propName.substring(pos+1);
                    propName = propName.substring(0, pos);
                }
                
                rulesetConfigs.add( new RulesetSpecificProperty(ruleset, propName, value));
            }

            
            // Unrecognized.
            System.err.println("Warning: Unknown argument: " + arg);
            Utils.writeHelp();
        }

        WindupConfig configuration = new WindupConfig();
        configuration.setRulesetConfigs(rulesetConfigs);
        configuration.setCoreConfig(coreConf);
        
        return configuration;
        
    }// parseArguments()


    
    /**
     * Runs windup with given configuration.
     */
    private static void run(WindupConfig wc)
    {
        Furnace furnace = startFurnace();
        Path addonsDir = (Path) ObjectUtils.defaultIfNull(wc.getCoreConfig().getRulesetsDir(), Paths.get("rulesets"));
        if( ! addonsDir.toFile().exists() )
            addonsDir = Paths.get(".");
        furnace.addRepository(AddonRepositoryMode.IMMUTABLE, addonsDir.toFile());
        AddonRegistry reg = furnace.getAddonRegistry( furnace.getRepositories().get(0) );
        WindupProcessor wp = reg.getServices(WindupProcessor.class).get();
        
        // Configure - TODO
        
        // Execute
        wp.execute();
    }
    
    
   private static Furnace startFurnace()
    {
        // Create a Furnace instance. NOTE: This must be called only once
        Furnace furnace = FurnaceFactory.getInstance();

        // Add repository containing addons specified in pom.xml
        //furnace.addRepository(AddonRepositoryMode.IMMUTABLE, new File("target/addons"));

        // Start Furnace in another thread
        Future<Furnace> future = furnace.startAsync();
        try {
            // Wait until Furnace is started and return
            return future.get();
        }
        catch( InterruptedException | ExecutionException ex ) {
            throw new WindupException("Failed to start Furnace: " + ex.getMessage(), ex);
        }
    }

}// class
