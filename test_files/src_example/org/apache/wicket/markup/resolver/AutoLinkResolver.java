package org.apache.wicket.markup.resolver;

import org.apache.wicket.*;
import org.slf4j.*;
import org.apache.wicket.markup.*;
import org.apache.wicket.request.mapper.parameter.*;
import org.apache.wicket.protocol.http.*;
import java.util.*;
import org.apache.wicket.util.lang.*;
import org.apache.wicket.util.string.*;
import org.apache.wicket.application.*;
import org.apache.wicket.markup.html.link.*;
import org.apache.wicket.markup.html.*;
import org.apache.wicket.request.resource.*;
import org.apache.wicket.request.handler.resource.*;
import org.apache.wicket.request.*;

public final class AutoLinkResolver implements IComponentResolver{
    private static final TagReferenceResolver DEFAULT_ATTRIBUTE_RESOLVER;
    private static final Logger log;
    private static final long serialVersionUID=1L;
    private final Map<String,IAutolinkResolverDelegate> tagNameToAutolinkResolverDelegates;
    private final Map<String,ITagReferenceResolver> tagNameToTagReferenceResolvers;
    public AutoLinkResolver(){
        super();
        this.tagNameToAutolinkResolverDelegates=(Map<String,IAutolinkResolverDelegate>)new HashMap();
        this.tagNameToTagReferenceResolvers=(Map<String,ITagReferenceResolver>)new HashMap();
        final TagReferenceResolver hrefTagReferenceResolver=new TagReferenceResolver("href");
        final TagReferenceResolver srcTagReferenceResolver=new TagReferenceResolver("src");
        this.tagNameToTagReferenceResolvers.put("a",hrefTagReferenceResolver);
        this.tagNameToTagReferenceResolvers.put("link",hrefTagReferenceResolver);
        this.tagNameToTagReferenceResolvers.put("script",srcTagReferenceResolver);
        this.tagNameToTagReferenceResolvers.put("img",srcTagReferenceResolver);
        this.tagNameToTagReferenceResolvers.put("input",srcTagReferenceResolver);
        this.tagNameToTagReferenceResolvers.put("embed",srcTagReferenceResolver);
        this.tagNameToAutolinkResolverDelegates.put("a",new AnchorResolverDelegate());
        this.tagNameToAutolinkResolverDelegates.put("link",new ResourceReferenceResolverDelegate("href"));
        final ResourceReferenceResolverDelegate srcResRefResolver=new ResourceReferenceResolverDelegate("src");
        this.tagNameToAutolinkResolverDelegates.put("script",srcResRefResolver);
        this.tagNameToAutolinkResolverDelegates.put("img",srcResRefResolver);
        this.tagNameToAutolinkResolverDelegates.put("input",srcResRefResolver);
        this.tagNameToAutolinkResolverDelegates.put("embed",srcResRefResolver);
    }
    public final void addTagReferenceResolver(final String tagName,final String attributeName,final IAutolinkResolverDelegate resolver){
        final TagReferenceResolver tagReferenceResolver=new TagReferenceResolver(attributeName);
        this.tagNameToTagReferenceResolvers.put(tagName,tagReferenceResolver);
        this.tagNameToAutolinkResolverDelegates.put(tagName,resolver);
    }
    public final IAutolinkResolverDelegate getAutolinkResolverDelegate(final String tagName){
        return (IAutolinkResolverDelegate)this.tagNameToAutolinkResolverDelegates.get(tagName);
    }
    public final Component resolve(final MarkupContainer container,final MarkupStream markupStream,final ComponentTag tag){
        if(tag.isAutolinkEnabled()){
            final Component link=this.resolveAutomaticLink(container,"_autolink_",tag);
            if(AutoLinkResolver.log.isDebugEnabled()){
                AutoLinkResolver.log.debug("Added autolink "+link);
            }
            return link;
        }
        return null;
    }
    private final Component resolveAutomaticLink(final MarkupContainer container,final String id,final ComponentTag tag){
        final Page page=container.getPage();
        final String autoId=id+Integer.toString(page.getAutoIndex());
        final String tagName=tag.getName();
        if(tag.getId()==null){
            tag.setAutoComponentTag(true);
        }
        ITagReferenceResolver referenceResolver=(ITagReferenceResolver)this.tagNameToTagReferenceResolvers.get(tagName);
        if(referenceResolver==null){
            referenceResolver=AutoLinkResolver.DEFAULT_ATTRIBUTE_RESOLVER;
        }
        final String reference=referenceResolver.getReference(tag);
        final PathInfo pathInfo=new PathInfo(reference);
        final IAutolinkResolverDelegate autolinkResolverDelegate=(IAutolinkResolverDelegate)this.tagNameToAutolinkResolverDelegates.get(tagName);
        Component autoComponent=null;
        if(autolinkResolverDelegate!=null){
            autoComponent=autolinkResolverDelegate.newAutoComponent(container,autoId,pathInfo);
        }
        if(autoComponent==null){
            autoComponent=new AutolinkExternalLink(autoId,pathInfo.reference);
        }
        return autoComponent;
    }
    static{
        DEFAULT_ATTRIBUTE_RESOLVER=new TagReferenceResolver("href");
        log=LoggerFactory.getLogger(AutoLinkResolver.class);
    }
    public abstract static class AbstractAutolinkResolverDelegate implements IAutolinkResolverDelegate{
        protected final Component newPackageResourceReferenceAutoComponent(final MarkupContainer container,final String autoId,final PathInfo pathInfo,final String attribute){
            if(!pathInfo.absolute&&pathInfo.path!=null&&pathInfo.path.length()>0){
                IMarkupFragment markup=container.getAssociatedMarkup();
                if(markup==null){
                    markup=container.getMarkup();
                }
                final MarkupStream markupStream=new MarkupStream(markup);
                Class<? extends Component> clazz=markupStream.getContainerClass();
                if(markupStream.get() instanceof ComponentTag&&markupStream.getTag().getMarkupClass()!=null){
                    clazz=markupStream.getTag().getMarkupClass();
                }
                final ResourceReferenceAutolink autoLink=new ResourceReferenceAutolink(autoId,clazz,pathInfo.reference,attribute,container);
                if(autoLink.resourceReference!=null){
                    return autoLink;
                }
            }
            return null;
        }
    }
    public static final class AutolinkBookmarkablePageLink<T> extends BookmarkablePageLink<T> implements IComponentResolver{
        private static final long serialVersionUID=1L;
        private final String anchor;
        public static boolean autoEnable;
        public AutolinkBookmarkablePageLink(final String id,final Class<C> pageClass,final PageParameters parameters,final String anchor){
            super(id,pageClass,parameters);
            this.anchor=anchor;
            this.setAutoEnable(AutolinkBookmarkablePageLink.autoEnable);
        }
        protected CharSequence getURL(){
            CharSequence url=super.getURL();
            if(this.anchor!=null){
                url=(CharSequence)((Object)url+this.anchor);
            }
            return url;
        }
        public Component resolve(final MarkupContainer container,final MarkupStream markupStream,final ComponentTag tag){
            return this.getParent().get(tag.getId());
        }
        static{
            AutolinkBookmarkablePageLink.autoEnable=true;
        }
    }
    public static final class PathInfo{
        private final boolean absolute;
        private final String anchor;
        private final String extension;
        private final PageParameters pageParameters;
        private final String path;
        private final String reference;
        public PathInfo(final String reference){
            super();
            this.reference=reference;
            final int queryStringPos=reference.indexOf("?");
            String infoPath;
            if(queryStringPos!=-1){
                final String queryString=reference.substring(queryStringPos+1);
                RequestUtils.decodeParameters(queryString,this.pageParameters=new PageParameters());
                infoPath=reference.substring(0,queryStringPos);
            }
            else{
                this.pageParameters=null;
                infoPath=reference;
            }
            this.absolute=(infoPath.startsWith("/")||infoPath.startsWith("\\"));
            String extension=null;
            int pos=infoPath.lastIndexOf(".");
            if(pos!=-1){
                extension=infoPath.substring(pos+1);
                infoPath=infoPath.substring(0,pos);
            }
            String anchor=null;
            if(extension!=null){
                pos=extension.indexOf(35);
                if(pos!=-1){
                    anchor=extension.substring(pos);
                    extension=extension.substring(0,pos);
                }
            }
            if(anchor==null){
                pos=infoPath.indexOf("#");
                if(pos!=-1){
                    anchor=infoPath.substring(pos);
                    infoPath=infoPath.substring(0,pos);
                }
            }
            this.path=infoPath;
            this.extension=extension;
            this.anchor=anchor;
        }
        public final String getAnchor(){
            return this.anchor;
        }
        public final String getExtension(){
            return this.extension;
        }
        public final PageParameters getPageParameters(){
            return this.pageParameters;
        }
        public final String getPath(){
            return this.path;
        }
        public final String getReference(){
            return this.reference;
        }
        public final boolean isAbsolute(){
            return this.absolute;
        }
    }
    private static final class AnchorResolverDelegate extends AbstractAutolinkResolverDelegate{
        private static final String attribute="href";
        private final Set<String> supportedPageExtensions;
        public AnchorResolverDelegate(){
            super();
            (this.supportedPageExtensions=(Set<String>)new HashSet(4)).add("html");
            this.supportedPageExtensions.add("xml");
            this.supportedPageExtensions.add("wml");
            this.supportedPageExtensions.add("svg");
        }
        public Component newAutoComponent(final MarkupContainer container,final String autoId,final PathInfo pathInfo){
            if(pathInfo.extension!=null&&this.supportedPageExtensions.contains(pathInfo.extension)){
                final Page page=container.getPage();
                final IClassResolver defaultClassResolver=page.getApplication().getApplicationSettings().getClassResolver();
                String className=Packages.absolutePath(page.getClass(),pathInfo.path);
                className=Strings.replaceAll((CharSequence)className,(CharSequence)"/",(CharSequence)".").toString();
                if(className.startsWith(".")){
                    className=className.substring(1);
                }
                try{
                    final Class<? extends Page> clazz=(Class<? extends Page>)defaultClassResolver.resolveClass(className);
                    return new AutolinkBookmarkablePageLink<Object>(autoId,clazz,pathInfo.pageParameters,pathInfo.anchor);
                }
                catch(ClassNotFoundException ex){
                    AutoLinkResolver.log.warn("Did not find corresponding java class: "+className);
                    MarkupContainer parentWithContainer=container;
                    if(container.getParent()!=null){
                        parentWithContainer=container.findParentWithAssociatedMarkup();
                    }
                    if(parentWithContainer instanceof Page&&!pathInfo.path.startsWith("/")&&new MarkupStream(page.getMarkup()).isMergedMarkup()){
                        final IMarkupFragment containerMarkup=container.getMarkup();
                        final MarkupStream containerMarkupStream=new MarkupStream(containerMarkup);
                        if(containerMarkupStream.atTag()){
                            final ComponentTag tag=containerMarkupStream.getTag();
                            Class<? extends Page> clazz2=(Class<? extends Page>)tag.getMarkupClass();
                            if(clazz2!=null){
                                className=Packages.absolutePath((Class)clazz2,pathInfo.path);
                                className=Strings.replaceAll((CharSequence)className,(CharSequence)"/",(CharSequence)".").toString();
                                if(className.startsWith(".")){
                                    className=className.substring(1);
                                }
                                try{
                                    clazz2=(Class<? extends Page>)defaultClassResolver.resolveClass(className);
                                    return new AutolinkBookmarkablePageLink<Object>(autoId,clazz2,pathInfo.getPageParameters(),pathInfo.anchor);
                                }
                                catch(ClassNotFoundException ex2){
                                    AutoLinkResolver.log.warn("Did not find corresponding java class: "+className);
                                }
                            }
                        }
                    }
                    return null;
                }
                return this.newPackageResourceReferenceAutoComponent(container,autoId,pathInfo,"href");
            }
            return this.newPackageResourceReferenceAutoComponent(container,autoId,pathInfo,"href");
        }
    }
    private static final class AutolinkExternalLink extends ExternalLink implements IComponentResolver{
        private static final long serialVersionUID=1L;
        public AutolinkExternalLink(final String id,final String href){
            super(id,href);
        }
        public Component resolve(final MarkupContainer container,final MarkupStream markupStream,final ComponentTag tag){
            return this.getParent().get(tag.getId());
        }
    }
    private static final class ResourceReferenceAutolink extends WebMarkupContainer implements IComponentResolver{
        private static final long serialVersionUID=1L;
        private final String attribute;
        private final ResourceReference resourceReference;
        private final MarkupContainer parent;
        public ResourceReferenceAutolink(final String id,final Class<?> clazz,final String href,final String attribute,final MarkupContainer parent){
            super(id);
            this.parent=parent;
            this.attribute=attribute;
            ResourceReference reference=null;
            for(Class<?> cursor=clazz;cursor!=null&&cursor!=Object.class;cursor=(Class<?>)cursor.getSuperclass()){
                if(PackageResource.exists(cursor,href,this.getLocale(),this.getStyle(),this.getVariation())){
                    reference=new PackageResourceReference(cursor,href,this.getLocale(),this.getStyle(),this.getVariation());
                    break;
                }
            }
            this.resourceReference=reference;
        }
        public String getVariation(){
            if(this.parent!=null){
                return this.parent.getVariation();
            }
            return super.getVariation();
        }
        protected final void onComponentTag(final ComponentTag tag){
            super.onComponentTag(tag);
            if(this.resourceReference!=null){
                final ResourceReferenceRequestHandler handler=new ResourceReferenceRequestHandler(this.resourceReference);
                final CharSequence url=this.getRequestCycle().urlFor((IRequestHandler)handler);
                tag.put(this.attribute,url);
            }
        }
        public Component resolve(final MarkupContainer container,final MarkupStream markupStream,final ComponentTag tag){
            return this.getParent().get(tag.getId());
        }
    }
    private static final class ResourceReferenceResolverDelegate extends AbstractAutolinkResolverDelegate{
        private final String attribute;
        public ResourceReferenceResolverDelegate(final String attribute){
            super();
            this.attribute=attribute;
        }
        public Component newAutoComponent(final MarkupContainer container,final String autoId,final PathInfo pathInfo){
            return this.newPackageResourceReferenceAutoComponent(container,autoId,pathInfo,this.attribute);
        }
    }
    private static final class TagReferenceResolver implements ITagReferenceResolver{
        private final String attribute;
        public TagReferenceResolver(final String attribute){
            super();
            this.attribute=attribute;
        }
        public String getReference(final ComponentTag tag){
            return tag.getAttributes().getString(this.attribute);
        }
    }
    private interface ITagReferenceResolver{
        String getReference(ComponentTag p0);
    }
    public interface IAutolinkResolverDelegate{
        Component newAutoComponent(MarkupContainer p0,String p1,PathInfo p2);
    }
}
