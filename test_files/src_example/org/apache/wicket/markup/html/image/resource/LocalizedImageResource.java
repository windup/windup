package org.apache.wicket.markup.html.image.resource;

import org.apache.wicket.request.mapper.parameter.*;
import java.util.*;
import org.apache.wicket.request.cycle.*;
import org.apache.wicket.markup.*;
import org.apache.wicket.util.lang.*;
import org.apache.wicket.markup.html.border.*;
import org.apache.wicket.request.resource.*;
import org.apache.wicket.*;
import org.apache.wicket.util.string.*;
import org.apache.wicket.util.parse.metapattern.parsers.*;
import org.apache.wicket.util.parse.metapattern.*;

public final class LocalizedImageResource implements IClusterable{
    private static final long serialVersionUID=1L;
    private Boolean resourceKind;
    private final Component component;
    private IResource resource;
    private ResourceReference resourceReference;
    private PageParameters resourceParameters;
    private Locale locale;
    private String style;
    private String variation;
    public LocalizedImageResource(final Component component){
        super();
        this.component=component;
        this.locale=component.getLocale();
        this.style=component.getStyle();
        this.variation=component.getVariation();
    }
    public final void bind(){
        if(this.resourceReference!=null&&this.resourceReference.canBeRegistered()&&Application.exists()){
            Application.get().getResourceReferenceRegistry().registerResourceReference(this.resourceReference);
        }
    }
    public final void onResourceRequested(final PageParameters parameters){
        this.bind();
        final RequestCycle requestCycle=RequestCycle.get();
        final IResource.Attributes attributes=new IResource.Attributes(requestCycle.getRequest(),requestCycle.getResponse(),parameters);
        this.resource.respond(attributes);
    }
    public final void setResource(final IResource resource){
        if(this.resource!=resource){
            this.resourceKind=Boolean.TRUE;
            this.resource=resource;
        }
    }
    public final void setResourceReference(final ResourceReference resourceReference){
        this.setResourceReference(resourceReference,this.resourceParameters);
    }
    public final boolean isStateless(){
        return this.resourceReference!=null;
    }
    public final void setResourceReference(final ResourceReference resourceReference,final PageParameters resourceParameters){
        if(resourceReference!=this.resourceReference){
            this.resourceKind=Boolean.FALSE;
            this.resourceReference=resourceReference;
        }
        this.resourceParameters=resourceParameters;
        this.bind();
    }
    public final void setSrcAttribute(final ComponentTag tag){
        final Locale l=this.component.getLocale();
        final String s=this.component.getStyle();
        final String v=this.component.getVariation();
        if(this.resourceKind==null&&(!Objects.equal((Object)this.locale,(Object)l)||!Objects.equal((Object)this.style,(Object)s)||!Objects.equal((Object)this.variation,(Object)v))){
            this.locale=l;
            this.style=s;
            this.variation=v;
            this.resourceReference=null;
            this.resource=null;
        }
        final Object modelObject=this.component.getDefaultModelObject();
        if(modelObject instanceof ResourceReference){
            this.resourceReference=(ResourceReference)modelObject;
        }
        else if(modelObject instanceof IResource){
            this.resource=(IResource)modelObject;
        }
        if(this.resource==null&&this.resourceReference==null){
            final CharSequence src=(CharSequence)tag.getAttribute("src");
            if(src!=null){
                this.loadStaticImage(src.toString());
            }
            else{
                final CharSequence value=(CharSequence)tag.getAttribute("value");
                if(value!=null){
                    this.newImage(value);
                }
                else{
                    this.loadStaticImage(this.component.getDefaultModelObjectAsString());
                }
            }
        }
        CharSequence url;
        if(this.resourceReference!=null){
            url=RequestCycle.get().urlFor(this.resourceReference,this.resourceParameters);
        }
        else{
            url=this.component.urlFor(IResourceListener.INTERFACE,this.resourceParameters);
        }
        tag.put("src",url);
    }
    private IResourceFactory getResourceFactory(final Application application,final String factoryName){
        final IResourceFactory factory=application.getResourceSettings().getResourceFactory(factoryName);
        if(factory==null){
            throw new WicketRuntimeException("Could not find image resource factory named "+factoryName);
        }
        return factory;
    }
    private void loadStaticImage(final String path){
        MarkupContainer parent=this.component.findParentWithAssociatedMarkup();
        if(parent instanceof Border){
            parent=parent.getParent();
        }
        final Class<?> scope=(Class<?>)parent.getClass();
        this.resourceReference=new PackageResourceReference(scope,path,this.locale,this.style,this.variation);
        this.bind();
    }
    private void newImage(final CharSequence value){
        final ImageValueParser valueParser=new ImageValueParser(value);
        if(valueParser.matches()){
            final String imageReferenceName=valueParser.getImageReferenceName();
            final String specification=Strings.replaceHtmlEscapeNumber(valueParser.getSpecification());
            final String factoryName=valueParser.getFactoryName();
            final Application application=this.component.getApplication();
            if(!Strings.isEmpty((CharSequence)imageReferenceName)){
                if(application.getResourceReferenceRegistry().getResourceReference((Class<?>)Application.class,imageReferenceName,this.locale,this.style,this.variation,true,false)==null){
                    final IResource imageResource=this.getResourceFactory(application,factoryName).newResource(specification,this.locale,this.style,this.variation);
                    final ResourceReference ref=new SimpleStaticResourceReference((Class<?>)Application.class,imageReferenceName,this.locale,this.style,this.variation,imageResource);
                    application.getResourceReferenceRegistry().registerResourceReference(ref);
                }
                this.resourceReference=new PackageResourceReference((Class<?>)Application.class,imageReferenceName,this.locale,this.style,this.variation);
            }
            else{
                this.resource=this.getResourceFactory(application,factoryName).newResource(specification,this.locale,this.style,this.variation);
            }
            return;
        }
        throw new WicketRuntimeException("Could not generate image for value attribute '"+(Object)value+"'.  Was expecting a value attribute of the form \"[resourceFactoryName]:[resourceReferenceName]?:[factorySpecification]\".");
    }
    public final IResource getResource(){
        return this.resource;
    }
    public final ResourceReference getResourceReference(){
        return this.resourceReference;
    }
    private static final class ImageValueParser extends MetaPatternParser{
        private static final Group factoryName;
        private static final Group imageReferenceName;
        private static final Group specification;
        private static final MetaPattern pattern;
        private ImageValueParser(final CharSequence input){
            super(ImageValueParser.pattern,input);
        }
        private String getFactoryName(){
            return ImageValueParser.factoryName.get(this.matcher());
        }
        private String getImageReferenceName(){
            return ImageValueParser.imageReferenceName.get(this.matcher());
        }
        private String getSpecification(){
            return ImageValueParser.specification.get(this.matcher());
        }
        static{
            factoryName=new Group(MetaPattern.VARIABLE_NAME);
            imageReferenceName=new Group(MetaPattern.VARIABLE_NAME);
            specification=new Group(MetaPattern.ANYTHING_NON_EMPTY);
            pattern=new MetaPattern(new MetaPattern[] { ImageValueParser.factoryName,MetaPattern.COLON,new OptionalMetaPattern(new MetaPattern[] { ImageValueParser.imageReferenceName }),MetaPattern.COLON,ImageValueParser.specification });
        }
    }
    static class SimpleStaticResourceReference extends ResourceReference{
        final IResource resource;
        private static final long serialVersionUID=1L;
        public SimpleStaticResourceReference(final Class<?> scope,final String name,final Locale locale,final String style,final String variation,final IResource resource){
            super(scope,name,locale,style,variation);
            this.resource=resource;
        }
        public IResource getResource(){
            return this.resource;
        }
    }
}
