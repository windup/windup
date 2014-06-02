package org.apache.wicket.markup.parser.filter;

import org.apache.wicket.markup.*;
import java.text.*;
import org.apache.wicket.markup.parser.*;
import java.util.*;

public class OpenCloseTagExpander extends AbstractMarkupFilter{
    private static final List<String> replaceForTags;
    private ComponentTag next;
    public OpenCloseTagExpander(){
        super();
        this.next=null;
    }
    public MarkupElement nextElement() throws ParseException{
        if(this.next!=null){
            final MarkupElement rtn=this.next;
            this.next=null;
            return rtn;
        }
        return super.nextElement();
    }
    protected MarkupElement onComponentTag(final ComponentTag tag) throws ParseException{
        if(tag.isOpenClose()){
            String name=tag.getName();
            if(tag.getNamespace()!=null){
                name=tag.getNamespace()+":"+tag.getName();
            }
            if(this.contains(name)&&this.onFound(tag)){
                (this.next=new ComponentTag(tag.getName(),XmlTag.TagType.CLOSE)).setNamespace(tag.getNamespace());
                this.next.setOpenTag(tag);
                this.next.setModified(true);
            }
        }
        return tag;
    }
    protected boolean onFound(final ComponentTag tag){
        tag.setType(XmlTag.TagType.OPEN);
        tag.setModified(true);
        return true;
    }
    protected boolean contains(final String name){
        return OpenCloseTagExpander.replaceForTags.contains(name.toLowerCase());
    }
    static{
        replaceForTags=Arrays.asList(new String[] { "a","q","sub","sup","abbr","acronym","cite","code","del","dfn","em","ins","kbd","samp","var","label","textarea","tr","td","th","caption","thead","tbody","tfoot","dl","dt","dd","li","ol","ul","h1","h2","h3","h4","h5","h6","i","pre","title","div","span","p","strong","b","e","select","col","article","aside","command","details","summary","figure","figcaption","footer","header","hgroup","mark","meter","nav","progress","ruby","rt","rp","section","audio","video","canvas","datalist","output" });
    }
}
