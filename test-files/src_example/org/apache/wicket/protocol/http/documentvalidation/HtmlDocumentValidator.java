package org.apache.wicket.protocol.http.documentvalidation;

import org.apache.wicket.util.collections.*;
import java.util.*;
import org.slf4j.*;

public class HtmlDocumentValidator{
    private static final Logger log;
    private final List<DocumentElement> elements;
    private boolean skipComments;
    private Tag workingTag;
    public HtmlDocumentValidator(){
        super();
        this.elements=(List<DocumentElement>)new ArrayList();
        this.skipComments=true;
    }
    public void addRootElement(final DocumentElement e){
        this.elements.add(e);
    }
    public boolean isDocumentValid(final String document){
        final HtmlDocumentParser parser=new HtmlDocumentParser(document);
        Iterator<DocumentElement> expectedElements=(Iterator<DocumentElement>)this.elements.iterator();
        final ArrayListStack<Iterator<DocumentElement>> iteratorStack=(ArrayListStack<Iterator<DocumentElement>>)new ArrayListStack();
        final ArrayListStack<String> tagNameStack=(ArrayListStack<String>)new ArrayListStack();
        boolean end=false;
        boolean valid=true;
        while(!end){
            final int token=parser.getNextToken();
            switch(token){
                case -1:{
                    return false;
                }
                case 0:{
                    end=true;
                    continue;
                }
                case 1:{
                    valid=this.validateComment(expectedElements,parser);
                    if(!valid){
                        end=true;
                        continue;
                    }
                    continue;
                }
                case 2:{
                    valid=this.validateTag(expectedElements,parser);
                    if(!valid){
                        end=true;
                        continue;
                    }
                    expectedElements=this.saveOpenTagState(iteratorStack,expectedElements,tagNameStack);
                    continue;
                }
                case 3:{
                    valid=this.validateTag(expectedElements,parser);
                    if(valid){
                        valid=this.checkOpenCloseTag();
                    }
                    if(!valid){
                        end=true;
                        continue;
                    }
                    continue;
                }
                case 4:{
                    expectedElements=this.validateCloseTag(tagNameStack,parser,expectedElements,iteratorStack);
                    if(expectedElements==null){
                        valid=false;
                        end=true;
                        continue;
                    }
                    continue;
                }
                case 5:{
                    valid=this.validateText(expectedElements,parser);
                    if(!valid){
                        end=true;
                        continue;
                    }
                    continue;
                }
            }
        }
        return valid;
    }
    public void setSkipComments(final boolean skipComments){
        this.skipComments=skipComments;
    }
    private boolean checkOpenCloseTag(){
        boolean valid=true;
        if(!this.workingTag.getExpectedChildren().isEmpty()){
            HtmlDocumentValidator.log.error("Found tag <"+this.workingTag.getTag()+"/> was expected to have "+this.workingTag.getExpectedChildren().size()+" child elements");
            valid=false;
        }
        return valid;
    }
    private boolean isNonClosedTag(String tag){
        tag=this.workingTag.getTag().toLowerCase();
        return tag.equals("area")||tag.equals("base")||tag.equals("basefont")||tag.equals("bgsound")||tag.equals("br")||tag.equals("col")||tag.equals("frame")||tag.equals("hr")||tag.equals("img")||tag.equals("input")||tag.equals("isindex")||tag.equals("keygen")||tag.equals("link")||tag.equals("meta")||tag.equals("param")||tag.equals("spacer")||tag.equals("wbr");
    }
    private Iterator<DocumentElement> saveOpenTagState(final ArrayListStack<Iterator<DocumentElement>> iteratorStack,Iterator<DocumentElement> expectedElements,final ArrayListStack<String> tagNameStack){
        if(!this.isNonClosedTag(this.workingTag.getTag())){
            iteratorStack.push((Object)expectedElements);
            expectedElements=(Iterator<DocumentElement>)this.workingTag.getExpectedChildren().iterator();
            tagNameStack.push((Object)this.workingTag.getTag());
        }
        return expectedElements;
    }
    private Iterator<DocumentElement> validateCloseTag(final ArrayListStack<String> tagNameStack,final HtmlDocumentParser parser,Iterator<DocumentElement> expectedElements,final ArrayListStack<Iterator<DocumentElement>> iteratorStack){
        if(tagNameStack.isEmpty()){
            HtmlDocumentValidator.log.error("Found closing tag </"+parser.getTag()+"> when there are no "+"tags currently open");
            expectedElements=null;
        }
        else{
            final String expectedTag=(String)tagNameStack.pop();
            if(!expectedTag.equals(parser.getTag())){
                HtmlDocumentValidator.log.error("Found closing tag </"+parser.getTag()+"> when we expecting "+"the closing tag </"+expectedTag+"> instead");
                expectedElements=null;
            }
            else if(expectedElements.hasNext()){
                final DocumentElement e=(DocumentElement)expectedElements.next();
                HtmlDocumentValidator.log.error("Found closing tag </"+parser.getTag()+"> but we were "+"expecting to find another child element: "+e.toString());
                expectedElements=null;
            }
            else if(iteratorStack.isEmpty()){
                HtmlDocumentValidator.log.error("Unexpected parsing error");
                expectedElements=null;
            }
            else{
                expectedElements=(Iterator<DocumentElement>)iteratorStack.pop();
            }
        }
        return expectedElements;
    }
    private boolean validateComment(final Iterator<DocumentElement> expectedElements,final HtmlDocumentParser parser){
        boolean valid=true;
        if(!this.skipComments){
            if(expectedElements.hasNext()){
                final DocumentElement e=(DocumentElement)expectedElements.next();
                if(e instanceof Comment){
                    if(!((Comment)e).getText().equals(parser.getComment())){
                        HtmlDocumentValidator.log.error("Found comment '"+parser.getComment()+"' does not match "+"expected comment '"+((Comment)e).getText()+"'");
                        valid=false;
                    }
                }
                else{
                    HtmlDocumentValidator.log.error("Found comment '"+parser.getComment()+"' was not expected. "+"We were expecting: "+e.toString());
                    valid=false;
                }
            }
            else{
                HtmlDocumentValidator.log.error("Found comment '"+parser.getComment()+"' was not expected. "+"We were not expecting any more elements within the current tag");
                valid=false;
            }
        }
        return valid;
    }
    private boolean validateTag(final Iterator<DocumentElement> expectedElements,final HtmlDocumentParser parser){
        boolean valid=true;
        if(expectedElements.hasNext()){
            final DocumentElement e=(DocumentElement)expectedElements.next();
            if(e instanceof Tag){
                this.workingTag=(Tag)e;
                if(!this.workingTag.getTag().equals(parser.getTag())){
                    HtmlDocumentValidator.log.error("Found tag <"+parser.getTag()+"> does not match "+"expected tag <"+this.workingTag.getTag()+">");
                    valid=false;
                }
                else{
                    final Map<String,String> actualAttributes=parser.getAttributes();
                    final Map<String,String> expectedAttributes=this.workingTag.getExpectedAttributes();
                    for(final Map.Entry<String,String> entry : expectedAttributes.entrySet()){
                        final String name=(String)entry.getKey();
                        final String pattern=(String)entry.getValue();
                        if(!actualAttributes.containsKey(name)){
                            HtmlDocumentValidator.log.error("Tag <"+this.workingTag.getTag()+"> was expected to have a '"+name+"' attribute "+"but this was not present");
                            valid=false;
                        }
                        final String value=(String)actualAttributes.get(name);
                        if(value==null){
                            HtmlDocumentValidator.log.error("Attribute "+name+" was expected but not found");
                            valid=false;
                        }
                        else{
                            if(value.matches(pattern)){
                                continue;
                            }
                            HtmlDocumentValidator.log.error("The value '"+value+"' of attribute '"+name+"' of tag <"+this.workingTag.getTag()+"> was expected to match the pattern '"+pattern+"' but it does not");
                            valid=false;
                        }
                    }
                    for(final String name2 : this.workingTag.getIllegalAttributes()){
                        if(actualAttributes.containsKey(name2)){
                            HtmlDocumentValidator.log.error("Tag <"+this.workingTag.getTag()+"> should not have an attributed named '"+name2+"'");
                            valid=false;
                        }
                    }
                }
            }
            else{
                HtmlDocumentValidator.log.error("Found tag <"+parser.getTag()+"> was not expected. "+"We were expecting: "+e.toString());
                valid=false;
            }
        }
        else{
            HtmlDocumentValidator.log.error("Found tag <"+parser.getTag()+"> was not expected. "+"We were not expecting any more elements within the current tag");
            valid=false;
        }
        return valid;
    }
    private boolean validateText(final Iterator<DocumentElement> expectedElements,final HtmlDocumentParser parser){
        boolean valid=true;
        if(expectedElements.hasNext()){
            final DocumentElement e=(DocumentElement)expectedElements.next();
            if(e instanceof TextContent){
                if(!parser.getText().matches(((TextContent)e).getValue())){
                    HtmlDocumentValidator.log.error("Found text '"+parser.getText()+"' does not match "+"expected text '"+((TextContent)e).getValue()+"'");
                    valid=false;
                }
            }
            else{
                HtmlDocumentValidator.log.error("Found text '"+parser.getText()+"' was not expected. "+"We were expecting: "+e.toString());
                valid=false;
            }
        }
        else{
            HtmlDocumentValidator.log.error("Found text '"+parser.getText()+"' was not expected. "+"We were not expecting any more elements within the current tag");
            valid=false;
        }
        return valid;
    }
    static{
        log=LoggerFactory.getLogger(HtmlDocumentValidator.class);
    }
}
