package org.apache.wicket.markup.html.border;

import org.apache.wicket.behavior.*;
import org.apache.wicket.request.*;
import java.io.*;
import org.apache.wicket.util.resource.locator.*;
import java.util.*;
import org.apache.wicket.util.resource.*;
import org.apache.wicket.markup.*;
import org.apache.wicket.*;
import org.apache.wicket.markup.parser.filter.*;

public class BorderBehavior extends Behavior{
    private static final long serialVersionUID=1L;
    private transient MarkupStream markupStream;
    public void beforeRender(final Component component){
        final MarkupStream stream=this.getMarkupStream(component);
        final Response response=component.getResponse();
        stream.setCurrentIndex(0);
        boolean insideBorderMarkup=false;
        while(stream.hasMore()){
            final MarkupElement elem=stream.get();
            stream.next();
            if(elem instanceof WicketTag){
                final WicketTag wTag=(WicketTag)elem;
                if(!insideBorderMarkup){
                    if(!wTag.isBorderTag()||!wTag.isOpen()){
                        throw new WicketRuntimeException("Unexpected tag encountered in markup of component border "+this.getClass().getName()+". Tag: "+wTag.toString()+", expected tag: <wicket:border>");
                    }
                    insideBorderMarkup=true;
                }
                else{
                    if(wTag.isBodyTag()){
                        break;
                    }
                    throw new WicketRuntimeException("Unexpected tag encountered in markup of component border "+this.getClass().getName()+". Tag: "+wTag.toString()+", expected tag: <wicket:body> or </wicket:body>");
                }
            }
            else{
                if(!insideBorderMarkup){
                    continue;
                }
                response.write(elem.toCharSequence());
            }
        }
        if(!stream.hasMore()){
            throw new WicketRuntimeException("Markup for component border "+this.getClass().getName()+" ended prematurely, was expecting </wicket:border>");
        }
    }
    public void afterRender(final Component component){
        final MarkupStream stream=this.getMarkupStream(component);
        final Response response=component.getResponse();
        while(stream.hasMore()){
            final MarkupElement elem=stream.get();
            stream.next();
            if(elem instanceof WicketTag){
                final WicketTag wTag=(WicketTag)elem;
                if(wTag.isBorderTag()&&wTag.isClose()){
                    break;
                }
                throw new WicketRuntimeException("Unexpected tag encountered in markup of component border "+this.getClass().getName()+". Tag: "+wTag.toString()+", expected tag: </wicket:border>");
            }
            else{
                response.write(elem.toCharSequence());
            }
        }
    }
    private MarkupStream getMarkupStream(final Component component){
        if(this.markupStream==null){
            this.markupStream=this.findMarkupStream(component);
        }
        return this.markupStream;
    }
    private MarkupStream findMarkupStream(final Component owner){
        final MarkupType markupType=this.getMarkupType(owner);
        if(markupType==null){
            return null;
        }
        final IResourceStreamLocator locator=Application.get().getResourceSettings().getResourceStreamLocator();
        final String style=owner.getStyle();
        final String variation=owner.getVariation();
        final Locale locale=owner.getLocale();
        MarkupResourceStream markupResourceStream=null;
        for(Class<?> containerClass=(Class<?>)this.getClass();!containerClass.equals(BorderBehavior.class);containerClass=(Class<?>)containerClass.getSuperclass()){
            final String path=containerClass.getName().replace('.','/');
            final IResourceStream resourceStream=locator.locate(containerClass,path,style,variation,locale,markupType.getExtension(),false);
            if(resourceStream!=null){
                final ContainerInfo ci=new ContainerInfo(containerClass,locale,style,variation,markupType);
                markupResourceStream=new MarkupResourceStream(resourceStream,ci,containerClass);
                break;
            }
        }
        if(markupResourceStream==null){
            throw new WicketRuntimeException("Could not find markup for component border `"+this.getClass().getName()+"`");
        }
        try{
            final IMarkupFragment markup=MarkupFactory.get().newMarkupParser(markupResourceStream).parse();
            return new MarkupStream(markup);
        }
        catch(Exception e){
            throw new WicketRuntimeException("Could not parse markup from markup resource stream: "+markupResourceStream.toString(),e);
        }
        finally{
            try{
                markupResourceStream.close();
            }
            catch(IOException e2){
                throw new WicketRuntimeException("Cannot close markup resource stream: "+markupResourceStream,e2);
            }
        }
    }
    private MarkupType getMarkupType(final Component component){
        MarkupType markupType;
        if(component instanceof MarkupContainer){
            markupType=((MarkupContainer)component).getMarkupType();
        }
        else{
            markupType=component.getParent().getMarkupType();
        }
        return markupType;
    }
    static{
        WicketTagIdentifier.registerWellKnownTagName("border");
        WicketTagIdentifier.registerWellKnownTagName("body");
    }
}
