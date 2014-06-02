package org.apache.wicket.markup;

import org.apache.wicket.settings.*;
import org.apache.wicket.markup.parser.*;
import org.apache.wicket.*;
import org.apache.wicket.markup.parser.filter.*;
import java.util.*;
import java.io.*;
import org.apache.wicket.util.resource.*;
import java.text.*;
import java.util.regex.*;
import org.slf4j.*;

public abstract class AbstractMarkupParser{
    private static final Logger log;
    public static final Pattern CONDITIONAL_COMMENT_OPENING;
    private final IXmlPullParser xmlParser;
    private IMarkupFilter markupFilterChain;
    private final Markup markup;
    private final IMarkupSettings markupSettings;
    private final List<IMarkupFilter> filters;
    public AbstractMarkupParser(final MarkupResourceStream resource){
        this(new XmlPullParser(),resource);
    }
    public AbstractMarkupParser(final String markup){
        this(new XmlPullParser(),new MarkupResourceStream((IResourceStream)new StringResourceStream((CharSequence)markup)));
    }
    public AbstractMarkupParser(final IXmlPullParser xmlParser,final MarkupResourceStream resource){
        super();
        this.xmlParser=xmlParser;
        this.markupSettings=Application.get().getMarkupSettings();
        this.markup=new Markup(resource);
        this.markupFilterChain=new RootMarkupFilter(xmlParser);
        this.filters=this.initializeMarkupFilters(this.markup);
    }
    public List<IMarkupFilter> getMarkupFilters(){
        return this.filters;
    }
    public final void setWicketNamespace(final String namespace){
        this.markup.getMarkupResourceStream().setWicketNamespace(namespace);
    }
    protected MarkupResourceStream getMarkupResourceStream(){
        return this.markup.getMarkupResourceStream();
    }
    protected abstract List<IMarkupFilter> initializeMarkupFilters(final Markup p0);
    public final Markup parse() throws IOException,ResourceStreamNotFoundException{
        this.markupFilterChain=new RootMarkupFilter(this.xmlParser);
        for(final IMarkupFilter filter : this.getMarkupFilters()){
            filter.setNextFilter(this.markupFilterChain);
            this.markupFilterChain=filter;
        }
        final MarkupResourceStream markupResourceStream=this.markup.getMarkupResourceStream();
        this.xmlParser.parse(markupResourceStream.getResource().getInputStream(),this.markupSettings.getDefaultMarkupEncoding());
        this.parseMarkup();
        markupResourceStream.setEncoding(this.xmlParser.getEncoding());
        markupResourceStream.setDoctype(this.xmlParser.getDoctype());
        if(this.xmlParser.getEncoding()==null){
            final String a="The markup file does not have a XML declaration prolog with 'encoding' attribute";
            final String b=". E.g. <?xml version=\"1.0\" encoding=\"UTF-8\" ?>";
            if(this.markupSettings.getThrowExceptionOnMissingXmlDeclaration()){
                throw new MarkupException(markupResourceStream.getResource(),a+b);
            }
            AbstractMarkupParser.log.debug(a+":"+markupResourceStream.getResource()+". It is more save to use it"+b);
        }
        return this.markup;
    }
    private MarkupElement getNextTag() throws ParseException{
        return this.markupFilterChain.nextElement();
    }
    private void parseMarkup(){
        try{
            int size=this.markup.size();
            MarkupElement elem;
            while(null!=(elem=this.getNextTag())){
                if(elem instanceof HtmlSpecialTag){
                    elem=new ComponentTag(((HtmlSpecialTag)elem).getXmlTag());
                }
                if(elem instanceof ComponentTag){
                    final ComponentTag tag=(ComponentTag)elem;
                    boolean add=tag.getId()!=null;
                    if(!add&&tag.isClose()){
                        add=(tag.getOpenTag()!=null&&tag.getOpenTag().getId()!=null);
                    }
                    if(add||tag.isModified()||this.markup.size()!=size){
                        CharSequence text=this.xmlParser.getInputFromPositionMarker(tag.getPos());
                        if(text.length()>0){
                            text=this.handleRawText(text.toString());
                            this.markup.addMarkupElement(size,new RawMarkup(text));
                        }
                        this.xmlParser.setPositionMarker();
                        if(add){
                            if(!tag.isIgnore()){
                                this.markup.addMarkupElement(tag);
                            }
                        }
                        else if(tag.isModified()){
                            this.markup.addMarkupElement(new RawMarkup(tag.toCharSequence()));
                        }
                        else{
                            this.xmlParser.setPositionMarker(tag.getPos());
                        }
                    }
                    size=this.markup.size();
                }
            }
        }
        catch(ParseException ex){
            final CharSequence text2=this.xmlParser.getInputFromPositionMarker(-1);
            if(text2.length()>0){
                this.markup.addMarkupElement(new RawMarkup(text2));
            }
            this.markup.getMarkupResourceStream().setEncoding(this.xmlParser.getEncoding());
            this.markup.getMarkupResourceStream().setDoctype(this.xmlParser.getDoctype());
            final MarkupStream markupStream=new MarkupStream(this.markup);
            markupStream.setCurrentIndex(this.markup.size()-1);
            throw new MarkupException(markupStream,ex.getMessage(),ex);
        }
        CharSequence text3=this.xmlParser.getInputFromPositionMarker(-1);
        if(text3.length()>0){
            text3=this.handleRawText(text3.toString());
            this.markup.addMarkupElement(new RawMarkup(text3));
        }
        this.postProcess(this.markup);
        this.markup.makeImmutable();
    }
    protected void postProcess(final Markup markup){
        for(IMarkupFilter filter=this.markupFilterChain;filter!=null;filter=filter.getNextFilter()){
            filter.postProcess(markup);
        }
    }
    protected CharSequence handleRawText(String rawMarkup){
        final boolean stripComments=this.markupSettings.getStripComments();
        final boolean compressWhitespace=this.markupSettings.getCompressWhitespace();
        if(stripComments){
            rawMarkup=removeComment(rawMarkup);
        }
        if(compressWhitespace){
            rawMarkup=this.compressWhitespace(rawMarkup);
        }
        return (CharSequence)rawMarkup;
    }
    protected String compressWhitespace(final String rawMarkup){
        final Pattern preBlock=Pattern.compile("<pre>.*?</pre>",40);
        final Matcher m=preBlock.matcher((CharSequence)rawMarkup);
        int lastend=0;
        StringBuilder sb=null;
        while(true){
            final boolean matched=m.find();
            String nonPre=matched?rawMarkup.substring(lastend,m.start()):rawMarkup.substring(lastend);
            nonPre=nonPre.replaceAll("[ \\t]+"," ");
            nonPre=nonPre.replaceAll("( ?[\\r\\n] ?)+","\n");
            if(lastend==0){
                if(!matched){
                    return nonPre;
                }
                sb=new StringBuilder(rawMarkup.length());
            }
            sb.append(nonPre);
            if(!matched){
                return sb.toString();
            }
            sb.append(m.group());
            lastend=m.end();
        }
    }
    private static String removeComment(String rawMarkup){
        for(int pos1=rawMarkup.indexOf("<!--");pos1!=-1;pos1=rawMarkup.indexOf("<!--",pos1)){
            final StringBuilder buf=new StringBuilder(rawMarkup.length());
            final String possibleComment=rawMarkup.substring(pos1);
            final Matcher matcher=AbstractMarkupParser.CONDITIONAL_COMMENT_OPENING.matcher((CharSequence)possibleComment);
            if(matcher.find()){
                pos1+=matcher.end();
            }
            else{
                final int pos2=rawMarkup.indexOf("-->",pos1+4);
                buf.append(rawMarkup.substring(0,pos1));
                if(rawMarkup.length()>=pos2+3){
                    buf.append(rawMarkup.substring(pos2+3));
                }
                rawMarkup=buf.toString();
            }
        }
        return rawMarkup;
    }
    public String toString(){
        return this.markup.toString();
    }
    static{
        log=LoggerFactory.getLogger(AbstractMarkupParser.class);
        CONDITIONAL_COMMENT_OPENING=Pattern.compile("(s?)^[^>]*?<!--\\[if.*?\\]>(-->)?(<!.*?-->)?");
    }
}
