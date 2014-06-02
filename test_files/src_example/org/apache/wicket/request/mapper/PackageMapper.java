package org.apache.wicket.request.mapper;

import org.apache.wicket.util.lang.*;
import org.apache.wicket.request.component.*;
import org.apache.wicket.request.*;
import java.lang.reflect.*;
import org.apache.wicket.request.mapper.info.*;
import org.apache.wicket.request.mapper.parameter.*;

public class PackageMapper extends AbstractBookmarkableMapper{
    private final PackageName packageName;
    private final IPageParametersEncoder pageParametersEncoder;
    public PackageMapper(final PackageName packageName){
        this(packageName,(IPageParametersEncoder)new PageParametersEncoder());
    }
    public PackageMapper(final PackageName packageName,final IPageParametersEncoder pageParametersEncoder){
        super();
        Args.notNull((Object)packageName,"packageName");
        Args.notNull((Object)pageParametersEncoder,"pageParametersEncoder");
        this.packageName=packageName;
        this.pageParametersEncoder=pageParametersEncoder;
    }
    protected Url buildUrl(final UrlInfo info){
        final Class<? extends IRequestablePage> pageClass=info.getPageClass();
        final PackageName pageClassPackageName=PackageName.forClass((Class)pageClass);
        if(pageClassPackageName.equals((Object)this.packageName)){
            final Url url=new Url();
            String packageRelativeClassName;
            final String fullyQualifiedClassName=packageRelativeClassName=pageClass.getName();
            final int packageNameLength=this.packageName.getName().length();
            if(packageNameLength>0){
                packageRelativeClassName=fullyQualifiedClassName.substring(packageNameLength+1);
            }
            packageRelativeClassName=this.transformForUrl(packageRelativeClassName);
            url.getSegments().add(packageRelativeClassName);
            this.encodePageComponentInfo(url,info.getPageComponentInfo());
            return this.encodePageParameters(url,info.getPageParameters(),this.pageParametersEncoder);
        }
        return null;
    }
    protected UrlInfo parseRequest(final Request request){
        final Url url=request.getUrl();
        if(url.getSegments().size()>=1){
            final PageComponentInfo info=this.getPageComponentInfo(url);
            String className=(String)url.getSegments().get(0);
            if(!this.isValidClassName(className)){
                return null;
            }
            className=this.transformFromUrl(className);
            final String fullyQualifiedClassName=this.packageName.getName()+'.'+className;
            final Class<? extends IRequestablePage> pageClass=this.getPageClass(fullyQualifiedClassName);
            if(pageClass!=null&&!Modifier.isAbstract(pageClass.getModifiers())&&IRequestablePage.class.isAssignableFrom(pageClass)){
                final PageParameters pageParameters=this.extractPageParameters(request,1,this.pageParametersEncoder);
                return new UrlInfo(info,pageClass,pageParameters);
            }
        }
        return null;
    }
    private boolean isValidClassName(final String className){
        return className!=null&&!className.startsWith(".");
    }
    protected String transformFromUrl(final String classNameAlias){
        return classNameAlias;
    }
    protected String transformForUrl(final String className){
        return className;
    }
    protected boolean pageMustHaveBeenCreatedBookmarkable(){
        return true;
    }
    public int getCompatibilityScore(final Request request){
        return 0;
    }
}
