package org.apache.wicket.settings.def;

import org.apache.wicket.settings.*;
import org.apache.wicket.markup.*;
import org.apache.wicket.util.lang.*;

public class MarkupSettings implements IMarkupSettings{
    private boolean automaticLinking;
    private boolean compressWhitespace;
    private String defaultAfterDisabledLink;
    private String defaultBeforeDisabledLink;
    private String defaultMarkupEncoding;
    private MarkupFactory markupFactory;
    private boolean throwExceptionOnMissingXmlDeclaration;
    private boolean stripComments;
    private boolean stripWicketTags;
    public MarkupSettings(){
        super();
        this.automaticLinking=false;
        this.compressWhitespace=false;
        this.defaultAfterDisabledLink="</em>";
        this.defaultBeforeDisabledLink="<em>";
        this.throwExceptionOnMissingXmlDeclaration=false;
        this.stripComments=false;
        this.stripWicketTags=false;
    }
    public boolean getAutomaticLinking(){
        return this.automaticLinking;
    }
    public boolean getCompressWhitespace(){
        return this.compressWhitespace;
    }
    public String getDefaultAfterDisabledLink(){
        return this.defaultAfterDisabledLink;
    }
    public String getDefaultBeforeDisabledLink(){
        return this.defaultBeforeDisabledLink;
    }
    public String getDefaultMarkupEncoding(){
        return this.defaultMarkupEncoding;
    }
    public MarkupFactory getMarkupFactory(){
        if(this.markupFactory==null){
            this.markupFactory=new MarkupFactory();
        }
        return this.markupFactory;
    }
    public boolean getStripComments(){
        return this.stripComments;
    }
    public boolean getStripWicketTags(){
        return this.stripWicketTags;
    }
    public boolean getThrowExceptionOnMissingXmlDeclaration(){
        return this.throwExceptionOnMissingXmlDeclaration;
    }
    public void setAutomaticLinking(final boolean automaticLinking){
        this.automaticLinking=automaticLinking;
    }
    public void setCompressWhitespace(final boolean compressWhitespace){
        this.compressWhitespace=compressWhitespace;
    }
    public void setDefaultAfterDisabledLink(final String defaultAfterDisabledLink){
        this.defaultAfterDisabledLink=defaultAfterDisabledLink;
    }
    public void setDefaultBeforeDisabledLink(final String defaultBeforeDisabledLink){
        this.defaultBeforeDisabledLink=defaultBeforeDisabledLink;
    }
    public void setDefaultMarkupEncoding(final String encoding){
        this.defaultMarkupEncoding=encoding;
    }
    public void setMarkupFactory(final MarkupFactory factory){
        Args.notNull((Object)factory,"markup factory");
        this.markupFactory=factory;
    }
    public void setStripComments(final boolean stripComments){
        this.stripComments=stripComments;
    }
    public void setStripWicketTags(final boolean stripWicketTags){
        this.stripWicketTags=stripWicketTags;
    }
    public void setThrowExceptionOnMissingXmlDeclaration(final boolean throwException){
        this.throwExceptionOnMissingXmlDeclaration=throwException;
    }
}
