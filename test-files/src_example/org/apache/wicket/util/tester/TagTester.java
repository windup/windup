package org.apache.wicket.util.tester;

import org.apache.wicket.markup.parser.*;
import org.apache.wicket.util.value.*;
import org.apache.wicket.util.string.*;
import org.apache.wicket.*;
import java.util.*;

public class TagTester{
    private final XmlTag openTag;
    private final XmlTag closeTag;
    private final XmlPullParser parser;
    private TagTester(final XmlPullParser parser,final XmlTag openTag,final XmlTag closeTag){
        super();
        this.parser=parser;
        this.openTag=openTag;
        this.closeTag=closeTag;
    }
    public String getName(){
        return this.openTag.getName();
    }
    public boolean hasAttribute(final String attribute){
        boolean hasAttribute=false;
        if(this.getAttribute(attribute)!=null){
            hasAttribute=true;
        }
        return hasAttribute;
    }
    public String getAttribute(final String attribute){
        String value=null;
        final IValueMap attributeMap=this.openTag.getAttributes();
        if(attributeMap!=null){
            for(final String attr : attributeMap.keySet()){
                if(attr.equalsIgnoreCase(attribute)){
                    value=attributeMap.getString(attr);
                }
            }
        }
        return value;
    }
    public boolean getAttributeContains(final String attribute,final String partialValue){
        boolean contains=false;
        if(partialValue!=null){
            final String value=this.getAttribute(attribute);
            if(value!=null&&value.contains((CharSequence)partialValue)){
                contains=true;
            }
        }
        return contains;
    }
    public boolean getAttributeIs(final String attribute,final String expected){
        boolean is=false;
        final String val=this.getAttribute(attribute);
        if((val==null&&expected==null)||(expected!=null&&expected.equals(val))){
            is=true;
        }
        return is;
    }
    public boolean getAttributeEndsWith(final String attribute,final String expected){
        boolean endsWith=false;
        if(expected!=null){
            final String val=this.getAttribute(attribute);
            if(val!=null&&val.endsWith(expected)){
                endsWith=true;
            }
        }
        return endsWith;
    }
    public boolean hasChildTag(final String tagName){
        boolean hasChild=false;
        if(Strings.isEmpty((CharSequence)tagName)){
            throw new IllegalArgumentException("You need to provide a not empty/not null argument.");
        }
        if(this.openTag.isOpen()){
            try{
                final int startPos=this.openTag.getPos()+this.openTag.getLength();
                final int endPos=this.closeTag.getPos();
                final String markup=this.parser.getInput(startPos,endPos).toString();
                if(!Strings.isEmpty((CharSequence)markup)){
                    final XmlPullParser p=new XmlPullParser();
                    p.parse((CharSequence)markup);
                    XmlTag tag=null;
                    while((tag=p.nextTag())!=null){
                        if(tagName.equalsIgnoreCase(tag.getName())){
                            hasChild=true;
                            break;
                        }
                    }
                }
            }
            catch(Exception e){
                throw new WicketRuntimeException(e);
            }
        }
        return hasChild;
    }
    public TagTester getChild(final String attribute,final String value){
        TagTester childTag=null;
        if(this.openTag.isOpen()){
            final String markup=this.getMarkup();
            if(!Strings.isEmpty((CharSequence)markup)){
                childTag=createTagByAttribute(markup,attribute,value);
            }
        }
        return childTag;
    }
    public String getMarkup(){
        final int openPos=this.openTag.getPos();
        final int closePos=this.closeTag.getPos()+this.closeTag.getLength();
        return this.parser.getInput(openPos,closePos).toString();
    }
    public String getValue(){
        final int openPos=this.openTag.getPos()+this.openTag.getLength();
        final int closePos=this.closeTag.getPos();
        return this.parser.getInput(openPos,closePos).toString();
    }
    public static TagTester createTagByAttribute(final String markup,final String attribute,final String value){
        TagTester tester=null;
        if(!Strings.isEmpty((CharSequence)markup)&&!Strings.isEmpty((CharSequence)attribute)&&!Strings.isEmpty((CharSequence)value)){
            try{
                final XmlPullParser parser=new XmlPullParser();
                parser.parse((CharSequence)markup);
                XmlTag elm=null;
                XmlTag openTag=null;
                XmlTag closeTag=null;
                int level=0;
                while((elm=parser.nextTag())!=null&&closeTag==null){
                    final XmlTag xmlTag=elm;
                    if(openTag==null){
                        final IValueMap attributeMap=xmlTag.getAttributes();
                        for(final Map.Entry<String,Object> entry : attributeMap.entrySet()){
                            final String attr=(String)entry.getKey();
                            if(attr.equals(attribute)&&value.equals(entry.getValue())){
                                if(xmlTag.isOpen()){
                                    openTag=xmlTag;
                                }
                                else{
                                    if(!xmlTag.isOpenClose()){
                                        continue;
                                    }
                                    openTag=xmlTag;
                                    closeTag=xmlTag;
                                }
                            }
                        }
                    }
                    else{
                        if(xmlTag.isOpen()&&xmlTag.getName().equals(openTag.getName())){
                            ++level;
                        }
                        if(!xmlTag.isClose()||!xmlTag.getName().equals(openTag.getName())){
                            continue;
                        }
                        if(level==0){
                            closeTag=xmlTag;
                            closeTag.setOpenTag(openTag);
                        }
                        else{
                            --level;
                        }
                    }
                }
                if(openTag!=null&&closeTag!=null){
                    tester=new TagTester(parser,openTag,closeTag);
                }
            }
            catch(Exception e){
                throw new WicketRuntimeException(e);
            }
        }
        return tester;
    }
    public static TagTester createTagsByAttribute(final String markup,final String attribute,final String value){
        final List<TagTester> tester=createTagsByAttribute(markup,attribute,value,true);
        if(tester==null||tester.size()==0){
            return null;
        }
        return (TagTester)tester.get(0);
    }
    public static List<TagTester> createTagsByAttribute(final String markup,final String attribute,final String value,final boolean stopAfterFirst){
        final List<TagTester> testers=(List<TagTester>)new ArrayList();
        if(!Strings.isEmpty((CharSequence)markup)&&!Strings.isEmpty((CharSequence)attribute)&&!Strings.isEmpty((CharSequence)value)){
            try{
                final XmlPullParser parser=new XmlPullParser();
                parser.parse((CharSequence)markup);
                XmlTag elm=null;
                XmlTag openTag=null;
                XmlTag closeTag=null;
                int level=0;
                while((elm=parser.nextTag())!=null){
                    final XmlTag xmlTag=elm;
                    if(openTag==null){
                        final IValueMap attributeMap=xmlTag.getAttributes();
                        for(final Map.Entry<String,Object> entry : attributeMap.entrySet()){
                            if(((String)entry.getKey()).equals(attribute)&&value.equals(entry.getValue())){
                                if(xmlTag.isOpen()){
                                    openTag=xmlTag;
                                }
                                else{
                                    if(!xmlTag.isOpenClose()){
                                        continue;
                                    }
                                    openTag=xmlTag;
                                    closeTag=xmlTag;
                                }
                            }
                        }
                    }
                    else{
                        if(xmlTag.isOpen()&&xmlTag.getName().equals(openTag.getName())){
                            ++level;
                        }
                        if(xmlTag.isClose()&&xmlTag.getName().equals(openTag.getName())){
                            if(level==0){
                                closeTag=xmlTag;
                                closeTag.setOpenTag(openTag);
                            }
                            else{
                                --level;
                            }
                        }
                    }
                    if(openTag!=null&&closeTag!=null&&level==0){
                        final TagTester tester=new TagTester(parser,openTag,closeTag);
                        testers.add(tester);
                        openTag=null;
                        closeTag=null;
                    }
                    if(stopAfterFirst&&closeTag!=null){
                        break;
                    }
                }
            }
            catch(Exception e){
                throw new WicketRuntimeException(e);
            }
        }
        return testers;
    }
}
