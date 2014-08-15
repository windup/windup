package org.jboss.windup.configreal;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 *
 *  @author Ondrej Zizka, ozizka at redhat.com
 */
public class CoreConfig {
    
    private ServerConfig srcServerConfig = new ServerConfig();
    private ServerConfig destServerConfig = new ServerConfig();

    private final Set<Path> appPaths = new HashSet();
    

    private boolean skipValidation = false;
    
    private boolean dryRun = false;

    private boolean isTestRun = false;
    
    // This is rather for test purposes. If null, all are used.
    private final List<String> onlyRulesets = new LinkedList();
    
    private Path reportDir = Paths.get("WindupReport");
    
    private Path rulesetsDir = Paths.get("rulesets");

    
    // User vars - accessible in EL's and Groovy scripts.
    private Map<String, String> userVars = new HashMap();

    
    
    //<editor-fold defaultstate="collapsed" desc="get/set">
    
    public ServerConfig getSrcServerConfig()
    {
        return srcServerConfig;
    }
    
    
    public void setSrcServerConfig(ServerConfig serverConfig)
    {
        this.srcServerConfig = serverConfig;
    }
    
    
    public ServerConfig getDestServerConfig()
    {
        return destServerConfig;
    }
    
    
    public void setDestServerConfig(ServerConfig destServerConfig)
    {
        this.destServerConfig = destServerConfig;
    }
    
    public Set<Path> getDeploymentsPaths() { return appPaths; }
    public void addDeploymentPath(String deplPath) { this.appPaths.add( Paths.get(deplPath) ); }
    
    public boolean isSkipValidation() { return skipValidation; }
    public void setSkipValidation(boolean skipValidation) { this.skipValidation = skipValidation; }
    
    public boolean isDryRun() { return dryRun; }
    public void setDryRun( boolean dryRun ) { this.dryRun = dryRun; }
    
    public boolean isTestRun() { return isTestRun; }
    public void setTestRun( boolean isTestRun ) { this.isTestRun = isTestRun; }
    
    public Path getReportDir() { return reportDir; }
    public void setReportDir( String reportDir ) { this.reportDir = Paths.get(reportDir); }
    
    public boolean isIsTestRun() { return isTestRun; }
    public void setIsTestRun(boolean isTestRun) { this.isTestRun = isTestRun; }
    
    public Path getRulesetsDir() { return rulesetsDir; }
    public void setRulesetsDir(String rulesetsDir) { this.rulesetsDir = Paths.get(rulesetsDir); }
    
    public List<String> getOnlyMigrators() { return onlyRulesets; }
    public List<String> addOnlyMigrator( String name ) { onlyRulesets.add(name); return onlyRulesets; }
    
    public Map<String, String> getUserVars() { return userVars; }
    public String getUserVar( String name) { return userVars.get(name); }
    public void setUserVar( String name, String val ) { this.userVars.put( name, val ); }
    
    //</editor-fold>

}// class
