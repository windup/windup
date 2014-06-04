package org.apache.wicket.markup.html;

import java.util.*;
import org.apache.wicket.util.lang.*;
import org.apache.wicket.util.string.*;
import org.apache.wicket.*;
import org.slf4j.*;

public class PackageResourceGuard implements IPackageResourceGuard{
    private static final Logger log;
    private Set<String> blockedExtensions;
    private Set<String> blockedFiles;
    private boolean allowAccessToWebInfResources;
    public PackageResourceGuard(){
        super();
        this.blockedExtensions=(Set<String>)new HashSet(4);
        this.blockedFiles=(Set<String>)new HashSet(4);
        this.allowAccessToWebInfResources=false;
        this.blockedExtensions.add("properties");
        this.blockedExtensions.add("class");
        this.blockedExtensions.add("java");
        this.blockedFiles.add("applicationContext.xml");
        this.blockedFiles.add("log4j.xml");
    }
    public boolean accept(final Class<?> scope,final String path){
        final String absolutePath=Packages.absolutePath((Class)scope,path);
        return this.acceptAbsolutePath(absolutePath);
    }
    protected boolean acceptAbsolutePath(final String path){
        final int ixExtension=path.lastIndexOf(46);
        final int len=path.length();
        String ext;
        if(ixExtension<=0||ixExtension==len||path.lastIndexOf(47)+1==ixExtension){
            ext=null;
        }
        else{
            ext=path.substring(ixExtension+1).toLowerCase().trim();
        }
        if("html".equals(ext)&&this.getClass().getClassLoader().getResource(path.replaceAll("\\.html",".class"))!=null){
            PackageResourceGuard.log.warn("Access denied to shared (static) resource because it is a Wicket markup file: "+path);
            return false;
        }
        if(!this.acceptExtension(ext)){
            PackageResourceGuard.log.warn("Access denied to shared (static) resource because of the file extension: "+path);
            return false;
        }
        final String filename=Strings.lastPathComponent(path,'/');
        if(!this.acceptFile(filename)){
            PackageResourceGuard.log.warn("Access denied to shared (static) resource because of the file name: "+path);
            return false;
        }
        if(Strings.isEmpty((CharSequence)Application.get().getResourceSettings().getParentFolderPlaceholder())&&path.contains((CharSequence)"..")){
            PackageResourceGuard.log.warn("Access to parent directories via '..' is by default disabled for shared resources: "+path);
            return false;
        }
        if(!this.allowAccessToWebInfResources){
            String absolute=path;
            if(absolute.startsWith("/")){
                absolute=absolute.substring(1);
            }
            if(!absolute.contains((CharSequence)"/")){
                PackageResourceGuard.log.warn("Access to web-inf directory via '..' is by default disabled for shared resources: "+path);
                return false;
            }
        }
        return true;
    }
    protected boolean acceptExtension(final String extension){
        return !this.blockedExtensions.contains(extension);
    }
    protected boolean acceptFile(String file){
        if(file!=null){
            file=file.trim();
        }
        return !this.blockedFiles.contains(file);
    }
    protected final Set<String> getBlockedExtensions(){
        return this.blockedExtensions;
    }
    protected final Set<String> getBlockedFiles(){
        return this.blockedFiles;
    }
    protected final void setBlockedExtensions(final Set<String> blockedExtensions){
        this.blockedExtensions=blockedExtensions;
    }
    protected final void setBlockedFiles(final Set<String> blockedFiles){
        this.blockedFiles=blockedFiles;
    }
    public final boolean isAllowAccessToWebInfResources(){
        return this.allowAccessToWebInfResources;
    }
    public final void setAllowAccessToWebInfResources(final boolean allowAccessToRootResources){
        this.allowAccessToWebInfResources=allowAccessToRootResources;
    }
    static{
        log=LoggerFactory.getLogger(PackageResourceGuard.class);
    }
}
